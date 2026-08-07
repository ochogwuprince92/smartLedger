package com.finance.smartLedger.ledger.domain.valueobject;

import jakarta.persistence.*;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
public class AccountCode {

  @Column(name = "account_code")
  private String value;

  private AccountCode(String value) {
    String upperValue = value.toUpperCase();
    validate(upperValue);
    this.value = upperValue;
  }

  private static void validate(String value) {
    if (value == null) {
      throw new NullPointerException("Account code cannot be null");
    }
    if (value.trim().isEmpty()) {
      throw new IllegalArgumentException("Account code cannot be empty");
    }
    if (!value.matches("^[A-Z]{2,4}\\d{2,6}$")) {
      throw new IllegalArgumentException(
          "Account code must be 2-4 letters followed by 2-6 digits (e.g., 'GL001', 'SACC123')");
    }
  }

  public static AccountCode of(String value) {
    return new AccountCode(value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AccountCode that = (AccountCode) o;
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
