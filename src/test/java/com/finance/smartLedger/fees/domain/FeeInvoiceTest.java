package com.finance.smartLedger.fees.domain;

import com.finance.smartLedger.shared.exception.BusinessException;
import com.finance.smartLedger.shared.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FeeInvoiceTest {

    @Test
    void addPayment_ExceedingOutstandingBalance_ShouldThrowException() {
        // Given - Create an invoice with $100 total
        UUID studentId = UUID.randomUUID();
        FeeInvoice invoice = new FeeInvoice(studentId, "INV-001", LocalDate.now().plusDays(30));
        invoice.addLineItem(FeeType.TUITION_FEE, Money.of(new BigDecimal("100.00"), "USD"), "Tuition Fee");
        invoice.markAsIssued();

        // When - Try to add a payment of $150 (exceeds the $100 outstanding balance)
        FeePayment overpayment = new FeePayment(
            studentId,
            invoice.getId(),
            FeeType.TUITION_FEE,
            Money.of(new BigDecimal("150.00"), "USD"),
            "CASH",
            "REF-001"
        );
        overpayment.markAsCompleted("REC-001", "SYSTEM");
        invoice.getPayments().add(overpayment);

        // Then - Should throw BusinessException before state mutation
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            invoice.recalculateTotals();
        });

        // Verify the exception is for overpayment
        assertTrue(exception.getMessage().contains("exceeds invoice outstanding balance") ||
                   exception.getMessage().contains("exceeds"));

        // Verify invoice state remains unchanged (not marked as PAID with negative balance)
        assertNotEquals(FeeInvoice.InvoiceStatus.PAID, invoice.getStatus());
        assertTrue(invoice.getBalanceAmount().getAmount().compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void addPayment_ExactOutstandingBalance_ShouldMarkAsPaid() {
        // Given - Create an invoice with $100 total
        UUID studentId = UUID.randomUUID();
        FeeInvoice invoice = new FeeInvoice(studentId, "INV-002", LocalDate.now().plusDays(30));
        invoice.addLineItem(FeeType.TUITION_FEE, Money.of(new BigDecimal("100.00"), "USD"), "Tuition Fee");
        invoice.markAsIssued();

        // When - Add a payment of exactly $100 (matches outstanding balance)
        FeePayment exactPayment = new FeePayment(
            studentId,
            invoice.getId(),
            FeeType.TUITION_FEE,
            Money.of(new BigDecimal("100.00"), "USD"),
            "CASH",
            "REF-002"
        );
        exactPayment.markAsCompleted("REC-002", "SYSTEM");
        invoice.getPayments().add(exactPayment);
        invoice.recalculateTotals();

        // Then - Should be marked as PAID with zero balance
        assertEquals(FeeInvoice.InvoiceStatus.PAID, invoice.getStatus());
        assertTrue(invoice.getBalanceAmount().isZero());
    }

    @Test
    void addPayment_LessThanOutstandingBalance_ShouldMarkAsPartiallyPaid() {
        // Given - Create an invoice with $100 total
        UUID studentId = UUID.randomUUID();
        FeeInvoice invoice = new FeeInvoice(studentId, "INV-003", LocalDate.now().plusDays(30));
        invoice.addLineItem(FeeType.TUITION_FEE, Money.of(new BigDecimal("100.00"), "USD"), "Tuition Fee");
        invoice.markAsIssued();

        // When - Add a payment of $50 (less than outstanding balance)
        FeePayment partialPayment = new FeePayment(
            studentId,
            invoice.getId(),
            FeeType.TUITION_FEE,
            Money.of(new BigDecimal("50.00"), "USD"),
            "CASH",
            "REF-003"
        );
        partialPayment.markAsCompleted("REC-003", "SYSTEM");
        invoice.getPayments().add(partialPayment);
        invoice.recalculateTotals();

        // Then - Should be marked as PARTIALLY_PAID with $50 remaining balance
        assertEquals(FeeInvoice.InvoiceStatus.PARTIALLY_PAID, invoice.getStatus());
        assertEquals(new BigDecimal("50.00"), invoice.getBalanceAmount().getAmount());
    }
}
