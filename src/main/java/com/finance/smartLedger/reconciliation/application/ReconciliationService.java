package com.finance.smartLedger.reconciliation.application;

import com.finance.smartLedger.ledger.application.AccountService;
import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.reconciliation.domain.MatchStatus;
import com.finance.smartLedger.reconciliation.domain.Reconciliation;
import com.finance.smartLedger.reconciliation.domain.ReconciliationItem;
import com.finance.smartLedger.reconciliation.domain.ReconciliationStatus;
import com.finance.smartLedger.reconciliation.infrastructure.persistence.ReconciliationItemRepository;
import com.finance.smartLedger.reconciliation.infrastructure.persistence.ReconciliationRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReconciliationService {

  private final ReconciliationRepository reconciliationRepository;
  private final ReconciliationItemRepository reconciliationItemRepository;
  private final AccountService accountService;

  @Transactional
  public Reconciliation createReconciliation(
      String reconciliationNumber,
      LocalDateTime reconciliationDate,
      String sourceSystem,
      String sourceReference,
      BigDecimal totalExpectedAmount,
      String description,
      String createdBy) {

    if (reconciliationRepository.existsByReconciliationNumber(reconciliationNumber)) {
      throw new IllegalArgumentException(
          "Reconciliation with number " + reconciliationNumber + " already exists");
    }

    Reconciliation reconciliation =
        new Reconciliation(
            reconciliationNumber,
            reconciliationDate,
            sourceSystem,
            sourceReference,
            totalExpectedAmount,
            description,
            createdBy);

    return reconciliationRepository.save(reconciliation);
  }

  @Transactional
  public ReconciliationItem addItem(
      UUID reconciliationId,
      String itemReference,
      String itemType,
      BigDecimal expectedAmount,
      String description,
      String createdBy) {

    Reconciliation reconciliation =
        reconciliationRepository
            .findById(reconciliationId)
            .orElseThrow(() -> new IllegalArgumentException("Reconciliation not found"));

    if (reconciliation.getStatus() != ReconciliationStatus.PENDING
        && reconciliation.getStatus() != ReconciliationStatus.IN_PROGRESS) {
      throw new IllegalStateException(
          "Cannot add items to reconciliation in " + reconciliation.getStatus() + " status");
    }

    ReconciliationItem item =
        new ReconciliationItem(
            reconciliationId, itemReference, itemType, expectedAmount, description, createdBy);

    item = reconciliationItemRepository.save(item);
    reconciliation.addItem(item);
    reconciliationRepository.save(reconciliation);

    return item;
  }

  @Transactional
  public Reconciliation startReconciliation(UUID reconciliationId, String updatedBy) {
    Reconciliation reconciliation =
        reconciliationRepository
            .findById(reconciliationId)
            .orElseThrow(() -> new IllegalArgumentException("Reconciliation not found"));

    reconciliation.startReconciliation(updatedBy);
    return reconciliationRepository.save(reconciliation);
  }

  @Transactional
  public ReconciliationItem matchItem(
      UUID itemId, UUID transactionId, BigDecimal actualAmount, String updatedBy) {
    ReconciliationItem item =
        reconciliationItemRepository
            .findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Reconciliation item not found"));

    item.match(transactionId, actualAmount, updatedBy);
    reconciliationItemRepository.save(item);

    Reconciliation reconciliation =
        reconciliationRepository
            .findById(item.getReconciliationId())
            .orElseThrow(() -> new IllegalArgumentException("Reconciliation not found"));

    reconciliation.calculateVariance();
    reconciliationRepository.save(reconciliation);

    return item;
  }

  @Transactional
  public ReconciliationItem moveToSuspense(UUID itemId, UUID suspenseAccountId, String updatedBy) {
    ReconciliationItem item =
        reconciliationItemRepository
            .findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Reconciliation item not found"));

    Account suspenseAccount =
        accountService
            .findById(suspenseAccountId)
            .orElseThrow(() -> new IllegalArgumentException("Suspense account not found"));

    item.moveToSuspense(suspenseAccountId, updatedBy);
    reconciliationItemRepository.save(item);

    Reconciliation reconciliation =
        reconciliationRepository
            .findById(item.getReconciliationId())
            .orElseThrow(() -> new IllegalArgumentException("Reconciliation not found"));

    if (reconciliation.getSuspenseAccountId() == null) {
      reconciliation.setSuspenseAccountId(suspenseAccountId);
    }

    reconciliation.markPartiallyMatched(updatedBy);
    reconciliationRepository.save(reconciliation);

    return item;
  }

  @Transactional
  public Reconciliation completeReconciliation(UUID reconciliationId, String updatedBy) {
    Reconciliation reconciliation =
        reconciliationRepository
            .findById(reconciliationId)
            .orElseThrow(() -> new IllegalArgumentException("Reconciliation not found"));

    reconciliation.calculateVariance();

    if (reconciliation.isBalanced()) {
      reconciliation.completeReconciliation(updatedBy);
    } else {
      reconciliation.markPartiallyMatched(updatedBy);
    }

    return reconciliationRepository.save(reconciliation);
  }

  @Transactional
  public Reconciliation failReconciliation(UUID reconciliationId, String updatedBy) {
    Reconciliation reconciliation =
        reconciliationRepository
            .findById(reconciliationId)
            .orElseThrow(() -> new IllegalArgumentException("Reconciliation not found"));

    reconciliation.failReconciliation(updatedBy);
    return reconciliationRepository.save(reconciliation);
  }

  public Optional<Reconciliation> findById(UUID id) {
    return reconciliationRepository.findById(id);
  }

  public Optional<Reconciliation> findByReconciliationNumber(String reconciliationNumber) {
    return reconciliationRepository.findByReconciliationNumber(reconciliationNumber);
  }

  public List<Reconciliation> findByStatus(ReconciliationStatus status) {
    return reconciliationRepository.findByStatus(status);
  }

  public List<Reconciliation> findBySourceSystem(String sourceSystem) {
    return reconciliationRepository.findBySourceSystem(sourceSystem);
  }

  public List<Reconciliation> findByReconciliationDateBetween(
      LocalDateTime startDate, LocalDateTime endDate) {
    return reconciliationRepository.findByReconciliationDateBetween(startDate, endDate);
  }

  public List<ReconciliationItem> findItemsByReconciliationId(UUID reconciliationId) {
    return reconciliationItemRepository.findByReconciliationId(reconciliationId);
  }

  public List<ReconciliationItem> findUnmatchedItems(UUID reconciliationId) {
    return reconciliationItemRepository.findByReconciliationIdAndMatchStatus(
        reconciliationId, MatchStatus.UNMATCHED);
  }

  @Transactional
  public void deleteReconciliation(UUID id) {
    Reconciliation reconciliation =
        reconciliationRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reconciliation not found"));

    if (reconciliation.getStatus() == ReconciliationStatus.COMPLETED) {
      throw new IllegalStateException("Cannot delete a completed reconciliation");
    }

    reconciliationItemRepository.deleteByReconciliationId(id);
    reconciliationRepository.deleteById(id);
  }
}
