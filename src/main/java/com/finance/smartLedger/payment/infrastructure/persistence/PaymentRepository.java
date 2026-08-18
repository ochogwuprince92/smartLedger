package com.finance.smartLedger.payment.infrastructure.persistence;

import com.finance.smartLedger.payment.domain.Payment;
import com.finance.smartLedger.payment.domain.PaymentMethod;
import com.finance.smartLedger.payment.domain.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository
    extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {

  @Query("SELECT p FROM Payment p WHERE p.paymentNumber = :paymentNumber")
  Optional<Payment> findByPaymentNumber(@Param("paymentNumber") String paymentNumber);

  @Query("SELECT p FROM Payment p WHERE p.idempotencyKey = :idempotencyKey")
  Optional<Payment> findByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

  @Query(
      "SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Payment p WHERE p.paymentNumber = :paymentNumber")
  boolean existsByPaymentNumber(@Param("paymentNumber") String paymentNumber);

  @Query(
      "SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Payment p WHERE p.idempotencyKey = :idempotencyKey")
  boolean existsByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

  @Query("SELECT p FROM Payment p WHERE p.gatewayTransactionId = :gatewayTransactionId")
  Optional<Payment> findByGatewayTransactionId(
      @Param("gatewayTransactionId") String gatewayTransactionId);

  @Query("SELECT p FROM Payment p WHERE p.status = :status")
  List<Payment> findByStatus(@Param("status") PaymentStatus status);

  @Query("SELECT p FROM Payment p WHERE p.paymentMethod = :paymentMethod")
  List<Payment> findByPaymentMethod(@Param("paymentMethod") PaymentMethod paymentMethod);

  @Query("SELECT p FROM Payment p WHERE p.currencyCode = :currencyCode")
  List<Payment> findByCurrencyCode(@Param("currencyCode") String currencyCode);

  @Query("SELECT p FROM Payment p WHERE p.payerEmail = :payerEmail")
  List<Payment> findByPayerEmail(@Param("payerEmail") String payerEmail);

  @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
  List<Payment> findByPaymentDateBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query("SELECT p FROM Payment p WHERE p.status = :status ORDER BY p.paymentDate DESC")
  List<Payment> findByStatusOrderByDateDesc(@Param("status") PaymentStatus status);

  @Query("SELECT p FROM Payment p WHERE p.gatewayReference = :gatewayReference")
  Optional<Payment> findByGatewayReference(@Param("gatewayReference") String gatewayReference);
}
