package com.finance.smartLedger.ledger.domain.valueobject;

import com.finance.smartLedger.shared.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountBalanceTest {

    @Test
    void creditRevenueAccount_ShouldIncreaseCurrentBalance() {
        // Given - REVENUE is a credit-normal account (isDebitNormal = false)
        AccountBalance balance = AccountBalance.zero("USD");
        Money creditAmount = Money.of(new BigDecimal("100.00"), "USD");

        // When
        balance.credit(creditAmount, false);

        // Then - For credit-normal accounts, crediting should INCREASE current balance
        assertEquals(new BigDecimal("100.00"), balance.getCurrentBalance().getAmount());
    }

    @Test
    void debitRevenueAccount_ShouldDecreaseCurrentBalance() {
        // Given - REVENUE is a credit-normal account (isDebitNormal = false)
        AccountBalance balance = AccountBalance.zero("USD");
        balance.credit(Money.of(new BigDecimal("100.00"), "USD"), false);
        Money debitAmount = Money.of(new BigDecimal("50.00"), "USD");

        // When
        balance.debit(debitAmount, false);

        // Then - For credit-normal accounts, debiting should DECREASE current balance
        assertEquals(new BigDecimal("50.00"), balance.getCurrentBalance().getAmount());
    }

    @Test
    void creditLiabilityAccount_ShouldIncreaseCurrentBalance() {
        // Given - LIABILITY is a credit-normal account (isDebitNormal = false)
        AccountBalance balance = AccountBalance.zero("USD");
        Money creditAmount = Money.of(new BigDecimal("100.00"), "USD");

        // When
        balance.credit(creditAmount, false);

        // Then - For credit-normal accounts, crediting should INCREASE current balance
        assertEquals(new BigDecimal("100.00"), balance.getCurrentBalance().getAmount());
    }

    @Test
    void creditAssetAccount_ShouldDecreaseCurrentBalance_NoRegression() {
        // Given - ASSET is a debit-normal account (isDebitNormal = true)
        AccountBalance balance = AccountBalance.zero("USD");
        balance.debit(Money.of(new BigDecimal("100.00"), "USD"), true);
        Money creditAmount = Money.of(new BigDecimal("50.00"), "USD");

        // When
        balance.credit(creditAmount, true);

        // Then - For debit-normal accounts, crediting should DECREASE current balance
        assertEquals(new BigDecimal("50.00"), balance.getCurrentBalance().getAmount());
    }

    @Test
    void debitAssetAccount_ShouldIncreaseCurrentBalance_NoRegression() {
        // Given - ASSET is a debit-normal account (isDebitNormal = true)
        AccountBalance balance = AccountBalance.zero("USD");
        Money debitAmount = Money.of(new BigDecimal("100.00"), "USD");

        // When
        balance.debit(debitAmount, true);

        // Then - For debit-normal accounts, debiting should INCREASE current balance
        assertEquals(new BigDecimal("100.00"), balance.getCurrentBalance().getAmount());
    }

    @Test
    void debitBalanceAccumulatesRegardlessOfAccountType() {
        // Given
        AccountBalance balance = AccountBalance.zero("USD");
        Money debitAmount = Money.of(new BigDecimal("100.00"), "USD");

        // When
        balance.debit(debitAmount, true);
        balance.debit(debitAmount, true);

        // Then - debitBalance should accumulate regardless of account type
        assertEquals(new BigDecimal("200.00"), balance.getDebitBalance().getAmount());
    }

    @Test
    void creditBalanceAccumulatesRegardlessOfAccountType() {
        // Given
        AccountBalance balance = AccountBalance.zero("USD");
        Money creditAmount = Money.of(new BigDecimal("100.00"), "USD");

        // When
        balance.credit(creditAmount, true);
        balance.credit(creditAmount, true);

        // Then - creditBalance should accumulate regardless of account type
        assertEquals(new BigDecimal("200.00"), balance.getCreditBalance().getAmount());
    }
}
