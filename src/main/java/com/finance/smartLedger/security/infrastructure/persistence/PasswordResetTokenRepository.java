package com.finance.smartLedger.security.infrastructure.persistence;

import com.finance.smartLedger.security.domain.PasswordResetToken;
import com.finance.smartLedger.security.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

  Optional<PasswordResetToken> findByTokenHash(String tokenHash);

  void deleteByUser(User user);
}
