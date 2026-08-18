package com.finance.smartLedger.ledger.infrastructure.persistence;

import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.ledger.domain.AccountType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository
    extends JpaRepository<Account, UUID>, JpaSpecificationExecutor<Account> {

  @Query("SELECT a FROM Account a WHERE a.accountNumber.value = :accountNumber")
  Optional<Account> findByAccountNumber_Value(@Param("accountNumber") String accountNumber);

  @Query("SELECT a FROM Account a WHERE a.accountCode.value = :accountCode")
  Optional<Account> findByAccountCode_Value(@Param("accountCode") String accountCode);

  @Query("SELECT a FROM Account a WHERE a.accountCode.value = :accountCode")
  Optional<Account> findByCode(@Param("accountCode") String accountCode);

  @Query(
      "SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Account a WHERE a.accountNumber.value = :accountNumber")
  boolean existsByAccountNumber_Value(@Param("accountNumber") String accountNumber);

  @Query(
      "SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Account a WHERE a.accountCode.value = :accountCode")
  boolean existsByAccountCode_Value(@Param("accountCode") String accountCode);

  @Query("SELECT a FROM Account a WHERE a.accountType = :accountType")
  java.util.List<Account> findByAccountType(@Param("accountType") AccountType accountType);

  @Query("SELECT a FROM Account a WHERE a.isActive = true")
  java.util.List<Account> findByIsActiveTrue();

  @Query("SELECT a FROM Account a WHERE a.parentAccountId = :parentAccountId")
  java.util.List<Account> findByParentAccountId(@Param("parentAccountId") UUID parentAccountId);
}
