package com.finance.smartLedger.shared.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Money Value Object Tests")
class MoneyTest {

  @Test
  @DisplayName("Should create Money with valid amount and currency")
  void shouldCreateMoneyWithValidAmountAndCurrency() {
    Money money = Money.of(new BigDecimal("100.50"), "USD");

    assertEquals(new BigDecimal("100.50"), money.getAmount());
    assertEquals("USD", money.getCurrencyCode());
  }

  @Test
  @DisplayName("Should create Money with double amount")
  void shouldCreateMoneyWithDoubleAmount() {
    Money money = Money.of(100.50, "USD");

    assertEquals(new BigDecimal("100.5"), money.getAmount());
    assertEquals("USD", money.getCurrencyCode());
  }

  @Test
  @DisplayName("Should create zero Money")
  void shouldCreateZeroMoney() {
    Money money = Money.zero("USD");

    assertEquals(BigDecimal.ZERO, money.getAmount());
    assertEquals("USD", money.getCurrencyCode());
    assertTrue(money.isZero());
  }

  @Test
  @DisplayName("Should throw exception when amount is null")
  void shouldThrowExceptionWhenAmountIsNull() {
    assertThrows(IllegalArgumentException.class, () -> Money.of(null, "USD"));
  }

  @Test
  @DisplayName("Should throw exception when amount is negative")
  void shouldThrowExceptionWhenAmountIsNegative() {
    assertThrows(IllegalArgumentException.class, () -> Money.of(new BigDecimal("-100"), "USD"));
  }

  @Test
  @DisplayName("Should throw exception when currency code is null")
  void shouldThrowExceptionWhenCurrencyCodeIsNull() {
    assertThrows(IllegalArgumentException.class, () -> Money.of(new BigDecimal("100"), null));
  }

  @Test
  @DisplayName("Should throw exception when currency code is empty")
  void shouldThrowExceptionWhenCurrencyCodeIsEmpty() {
    assertThrows(IllegalArgumentException.class, () -> Money.of(new BigDecimal("100"), ""));
  }

  @Test
  @DisplayName("Should accept any currency code")
  void shouldAcceptAnyCurrencyCode() {
    Money money = Money.of(new BigDecimal("100"), "XXX");

    assertEquals("XXX", money.getCurrencyCode());
  }

  @Test
  @DisplayName("Should add money with same currency")
  void shouldAddMoneyWithSameCurrency() {
    Money money1 = Money.of(new BigDecimal("100.00"), "USD");
    Money money2 = Money.of(new BigDecimal("50.00"), "USD");

    Money result = money1.add(money2);

    assertEquals(new BigDecimal("150.00"), result.getAmount());
    assertEquals("USD", result.getCurrencyCode());
  }

  @Test
  @DisplayName("Should throw exception when adding money with different currencies")
  void shouldThrowExceptionWhenAddingMoneyWithDifferentCurrencies() {
    Money money1 = Money.of(new BigDecimal("100.00"), "USD");
    Money money2 = Money.of(new BigDecimal("50.00"), "EUR");

    assertThrows(IllegalArgumentException.class, () -> money1.add(money2));
  }

  @Test
  @DisplayName("Should subtract money with same currency")
  void shouldSubtractMoneyWithSameCurrency() {
    Money money1 = Money.of(new BigDecimal("100.00"), "USD");
    Money money2 = Money.of(new BigDecimal("50.00"), "USD");

    Money result = money1.subtract(money2);

    assertEquals(new BigDecimal("50.00"), result.getAmount());
    assertEquals("USD", result.getCurrencyCode());
  }

  @Test
  @DisplayName("Should throw exception when subtracting money with different currencies")
  void shouldThrowExceptionWhenSubtractingMoneyWithDifferentCurrencies() {
    Money money1 = Money.of(new BigDecimal("100.00"), "USD");
    Money money2 = Money.of(new BigDecimal("50.00"), "EUR");

    assertThrows(IllegalArgumentException.class, () -> money1.subtract(money2));
  }

  @Test
  @DisplayName("Should multiply money")
  void shouldMultiplyMoney() {
    Money money = Money.of(new BigDecimal("100.00"), "USD");

    Money result = money.multiply(new BigDecimal("2"));

    assertEquals(new BigDecimal("200.00"), result.getAmount());
    assertEquals("USD", result.getCurrencyCode());
  }

  @Test
  @DisplayName("Should divide money with rounding")
  void shouldDivideMoneyWithRounding() {
    Money money = Money.of(new BigDecimal("100.00"), "USD");

    Money result = money.divide(new BigDecimal("3"));

    assertEquals(new BigDecimal("33.33"), result.getAmount());
    assertEquals("USD", result.getCurrencyCode());
  }

  @Test
  @DisplayName("Should compare money with same currency")
  void shouldCompareMoneyWithSameCurrency() {
    Money money1 = Money.of(new BigDecimal("100.00"), "USD");
    Money money2 = Money.of(new BigDecimal("50.00"), "USD");
    Money money3 = Money.of(new BigDecimal("150.00"), "USD");

    assertTrue(money1.isGreaterThan(money2));
    assertTrue(money1.isLessThan(money3));
    assertFalse(money1.isGreaterThan(money3));
    assertFalse(money1.isLessThan(money2));
  }

  @Test
  @DisplayName("Should throw exception when comparing money with different currencies")
  void shouldThrowExceptionWhenComparingMoneyWithDifferentCurrencies() {
    Money money1 = Money.of(new BigDecimal("100.00"), "USD");
    Money money2 = Money.of(new BigDecimal("50.00"), "EUR");

    assertThrows(IllegalArgumentException.class, () -> money1.isGreaterThan(money2));
    assertThrows(IllegalArgumentException.class, () -> money1.isLessThan(money2));
  }

  @Test
  @DisplayName("Should check if money is zero")
  void shouldCheckIfMoneyIsZero() {
    Money zeroMoney = Money.zero("USD");
    Money positiveMoney = Money.of(new BigDecimal("100.00"), "USD");

    assertTrue(zeroMoney.isZero());
    assertFalse(positiveMoney.isZero());
  }

  @Test
  @DisplayName("Should check if money is positive")
  void shouldCheckIfMoneyIsPositive() {
    Money zeroMoney = Money.zero("USD");
    Money positiveMoney = Money.of(new BigDecimal("100.00"), "USD");

    assertFalse(zeroMoney.isPositive());
    assertTrue(positiveMoney.isPositive());
  }

  @Test
  @DisplayName("Should check if money is negative")
  void shouldCheckIfMoneyIsNegative() {
    Money zeroMoney = Money.zero("USD");
    Money positiveMoney = Money.of(new BigDecimal("100.00"), "USD");

    assertFalse(zeroMoney.isNegative());
    assertFalse(positiveMoney.isNegative());
  }

  @Test
  @DisplayName("Should convert money to string")
  void shouldConvertMoneyToString() {
    Money money = Money.of(new BigDecimal("100.50"), "USD");

    assertEquals("100.50 USD", money.toString());
  }

  @Test
  @DisplayName("Should handle different valid currency codes")
  void shouldHandleDifferentValidCurrencyCodes() {
    Money usd = Money.of(new BigDecimal("100"), "USD");
    Money eur = Money.of(new BigDecimal("100"), "EUR");
    Money gbp = Money.of(new BigDecimal("100"), "GBP");
    Money jpy = Money.of(new BigDecimal("100"), "JPY");

    assertEquals("USD", usd.getCurrencyCode());
    assertEquals("EUR", eur.getCurrencyCode());
    assertEquals("GBP", gbp.getCurrencyCode());
    assertEquals("JPY", jpy.getCurrencyCode());
  }

  @Test
  @DisplayName("Should handle decimal precision correctly")
  void shouldHandleDecimalPrecisionCorrectly() {
    Money money1 = Money.of(new BigDecimal("100.123456"), "USD");
    Money money2 = Money.of(new BigDecimal("0.876544"), "USD");

    Money sum = money1.add(money2);

    assertEquals(new BigDecimal("101.000000"), sum.getAmount());
  }
}
