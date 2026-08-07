package com.finance.smartLedger.ledger.domain.valueobject;

import jakarta.persistence.*;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
public class AccountNumber {

  @Column(name = "account_number")
  private String value;

  private AccountNumber(String value) {
    validate(value);
    this.value = value;
  }

  private static void validate(String value) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException("Account number cannot be null or empty");
    }
    if (!value.matches("^\\d{8,20}$")) {
      throw new IllegalArgumentException("Account number must be 8-20 digits");
    }
  }

  public static AccountNumber of(String value) {
    return new AccountNumber(value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AccountNumber that = (AccountNumber) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
