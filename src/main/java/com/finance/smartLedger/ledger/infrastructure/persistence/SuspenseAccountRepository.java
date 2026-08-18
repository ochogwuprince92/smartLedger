package com.finance.smartLedger.ledger.infrastructure.persistence;

import com.finance.smartLedger.ledger.domain.SuspenseAccount;
import com.finance.smartLedger.ledger.domain.SuspenseAccount.SuspenseAccountStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuspenseAccountRepository extends JpaRepository<SuspenseAccount, UUID> {

  boolean existsByAccountCode(String accountCode);

  Optional<SuspenseAccount> findByAccountCode(String accountCode);

  List<SuspenseAccount> findByStatus(SuspenseAccountStatus status);

  List<SuspenseAccount> findByRequiresReviewTrue();

  List<SuspenseAccount> findByStatusAndCurrentBalanceGreaterThan(
      SuspenseAccountStatus status, BigDecimal amount);
}
