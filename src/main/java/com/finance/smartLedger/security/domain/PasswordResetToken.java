package com.finance.smartLedger.security.domain;

import com.finance.smartLedger.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "password_reset_tokens", indexes = {@Index(columnList = "token_hash")})
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"user"})
public class PasswordResetToken extends AuditableEntity {

  @Column(name = "token_hash", nullable = false, unique = true, length = 255)
  private String tokenHash;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Column(name = "used", nullable = false)
  private Boolean used = false;

  public PasswordResetToken(String tokenHash, User user, LocalDateTime expiresAt) {
    this.tokenHash = tokenHash;
    this.user = user;
    this.expiresAt = expiresAt;
    this.used = false;
    setCreatedBy("system");
    setUpdatedBy("system");
  }

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiresAt);
  }

  public boolean isValid() {
    return !used && !isExpired();
  }
}
