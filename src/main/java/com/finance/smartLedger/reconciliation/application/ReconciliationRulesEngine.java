package com.finance.smartLedger.reconciliation.application;

import com.finance.smartLedger.ledger.domain.Transaction;
import com.finance.smartLedger.reconciliation.domain.MatchStatus;
import com.finance.smartLedger.reconciliation.domain.Reconciliation;
import com.finance.smartLedger.reconciliation.domain.ReconciliationItem;
import com.finance.smartLedger.reconciliation.infrastructure.persistence.ReconciliationItemRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReconciliationRulesEngine {

  private final ReconciliationService reconciliationService;
  private final ReconciliationItemRepository reconciliationItemRepository;

  @Value("${reconciliation.auto-match.amount-tolerance:0.01}")
  private BigDecimal amountTolerance;

  @Value("${reconciliation.auto-match.date-tolerance-days:3}")
  private int dateToleranceDays;

  @Value("${reconciliation.auto-match.enabled:true}")
  private boolean autoMatchEnabled;

  /** Automatically match reconciliation items based on configurable rules */
  @Transactional
  public Reconciliation autoMatchItems(
      UUID reconciliationId, List<Transaction> availableTransactions) {
    if (!autoMatchEnabled) {
      log.info("Auto-matching is disabled, skipping automatic reconciliation");
      return reconciliationService.findById(reconciliationId).orElseThrow();
    }

    Reconciliation reconciliation =
        reconciliationService
            .findById(reconciliationId)
            .orElseThrow(() -> new IllegalArgumentException("Reconciliation not found"));

    List<ReconciliationItem> unmatchedItems =
        reconciliationService.findUnmatchedItems(reconciliationId);

    log.info(
        "Starting auto-matching for reconciliation {} with {} unmatched items",
        reconciliationId,
        unmatchedItems.size());

    // Create a mutable copy of available transactions to remove matched ones
    List<Transaction> remainingTransactions = new ArrayList<>(availableTransactions);

    int matchedCount = 0;
    for (ReconciliationItem item : unmatchedItems) {
      Optional<Transaction> bestMatch = findBestMatch(item, remainingTransactions);

      if (bestMatch.isPresent()) {
        Transaction transaction = bestMatch.get();
        reconciliationService.matchItem(
            item.getId(), transaction.getId(), transaction.getAmount().getAmount(), "system");
        matchedCount++;
        
        // Remove the matched transaction from the pool so it can't be matched again
        remainingTransactions.remove(transaction);
        
        log.info(
            "Auto-matched item {} to transaction {} with amount {}",
            item.getId(),
            transaction.getId(),
            transaction.getAmount().getAmount());
      }
    }

    log.info("Auto-matching completed: {} items matched", matchedCount);

    // Update reconciliation status
    reconciliation.calculateVariance();
    if (reconciliation.isBalanced()) {
      reconciliationService.completeReconciliation(reconciliationId, "system");
    } else if (matchedCount > 0) {
      reconciliation.markPartiallyMatched("system");
    }

    return reconciliationService.findById(reconciliationId).orElseThrow();
  }

  /** Find the best matching transaction for a reconciliation item */
  private Optional<Transaction> findBestMatch(
      ReconciliationItem item, List<Transaction> transactions) {
    List<Transaction> candidates = new ArrayList<>();

    for (Transaction transaction : transactions) {
      if (isPotentialMatch(item, transaction)) {
        double matchScore = calculateMatchScore(item, transaction);
        candidates.add(transaction);
      }
    }

    if (candidates.isEmpty()) {
      return Optional.empty();
    }

    // Return the transaction with the highest match score
    return candidates.stream().max(Comparator.comparingDouble(t -> calculateMatchScore(item, t)));
  }

  /** Check if a transaction is a potential match for a reconciliation item */
  private boolean isPotentialMatch(ReconciliationItem item, Transaction transaction) {
    // Check amount match (within tolerance)
    if (!isAmountMatch(item.getExpectedAmount(), transaction.getAmount().getAmount())) {
      return false;
    }

    // Check date proximity
    if (!isDateMatch(item.getCreatedAt(), transaction.getTransactionDate())) {
      return false;
    }

    // Check reference match (if available)
    if (item.getItemReference() != null
        && transaction.getReferenceNumber() != null
        && !item.getItemReference().equals(transaction.getReferenceNumber())) {
      return false;
    }

    return true;
  }

  /** Calculate match score for a transaction (0.0 to 1.0) */
  private double calculateMatchScore(ReconciliationItem item, Transaction transaction) {
    double score = 0.0;

    // Amount match score (40% weight)
    double amountScore =
        calculateAmountMatchScore(item.getExpectedAmount(), transaction.getAmount().getAmount());
    score += amountScore * 0.4;

    // Date match score (30% weight)
    double dateScore =
        calculateDateMatchScore(item.getCreatedAt(), transaction.getTransactionDate());
    score += dateScore * 0.3;

    // Reference match score (20% weight)
    double referenceScore =
        calculateReferenceMatchScore(item.getItemReference(), transaction.getReferenceNumber());
    score += referenceScore * 0.2;

    // Account match score (10% weight)
    double accountScore = calculateAccountMatchScore(item, transaction);
    score += accountScore * 0.1;

    return Math.min(1.0, score);
  }

  /** Check if amounts match within tolerance */
  private boolean isAmountMatch(BigDecimal expected, BigDecimal actual) {
    BigDecimal difference = expected.subtract(actual).abs();
    return difference.compareTo(amountTolerance) <= 0;
  }

  /** Check if dates are within tolerance */
  private boolean isDateMatch(LocalDateTime itemDate, LocalDateTime transactionDate) {
    if (itemDate == null || transactionDate == null) {
      return true; // Can't determine, assume match
    }
    long daysDifference = Math.abs(java.time.Duration.between(itemDate, transactionDate).toDays());
    return daysDifference <= dateToleranceDays;
  }

  /** Calculate amount match score */
  private double calculateAmountMatchScore(BigDecimal expected, BigDecimal actual) {
    if (expected.compareTo(BigDecimal.ZERO) == 0) {
      // If expected amount is zero, handle as special case
      if (actual.compareTo(BigDecimal.ZERO) == 0) {
        return 1.0; // Both zero - perfect match
      } else {
        return 0.0; // Expected zero but actual non-zero - no match
      }
    }
    
    if (expected.compareTo(actual) == 0) {
      return 1.0;
    }
    BigDecimal difference = expected.subtract(actual).abs();
    BigDecimal ratio = difference.divide(expected, 4, RoundingMode.HALF_UP);
    return Math.max(0.0, 1.0 - ratio.doubleValue() * 10);
  }

  /** Calculate date match score */
  private double calculateDateMatchScore(LocalDateTime itemDate, LocalDateTime transactionDate) {
    if (itemDate == null || transactionDate == null) {
      return 0.5; // Neutral score when can't determine
    }
    long daysDifference = Math.abs(java.time.Duration.between(itemDate, transactionDate).toDays());
    if (daysDifference == 0) {
      return 1.0;
    }
    return Math.max(0.0, 1.0 - (double) daysDifference / dateToleranceDays);
  }

  /** Calculate reference match score */
  private double calculateReferenceMatchScore(String itemRef, String transactionRef) {
    if (itemRef == null || transactionRef == null) {
      return 0.5; // Neutral score when can't determine
    }
    return itemRef.equals(transactionRef) ? 1.0 : 0.0;
  }

  /** Calculate account match score */
  private double calculateAccountMatchScore(ReconciliationItem item, Transaction transaction) {
    // Simplified - in real implementation, would check account relationships
    return 0.5;
  }

  /** Detect variances in reconciliation items */
  @Transactional
  public Map<String, Object> detectVariances(UUID reconciliationId) {
    Reconciliation reconciliation =
        reconciliationService
            .findById(reconciliationId)
            .orElseThrow(() -> new IllegalArgumentException("Reconciliation not found"));

    List<ReconciliationItem> items =
        reconciliationService.findItemsByReconciliationId(reconciliationId);

    Map<String, Object> varianceReport = new HashMap<>();
    List<Map<String, Object>> itemVariances = new ArrayList<>();

    BigDecimal totalVariance = BigDecimal.ZERO;
    int varianceCount = 0;

    for (ReconciliationItem item : items) {
      if (item.getMatchStatus() == MatchStatus.MATCHED) {
        BigDecimal variance = item.getExpectedAmount().subtract(item.getActualAmount());
        if (variance.compareTo(BigDecimal.ZERO) != 0) {
          Map<String, Object> itemVariance = new HashMap<>();
          itemVariance.put("item_id", item.getId());
          itemVariance.put("item_reference", item.getItemReference());
          itemVariance.put("expected_amount", item.getExpectedAmount());
          itemVariance.put("actual_amount", item.getActualAmount());
          itemVariance.put("variance", variance);
          
          // Handle zero expected amount case
          if (item.getExpectedAmount().compareTo(BigDecimal.ZERO) == 0) {
            itemVariance.put("variance_percentage", BigDecimal.ZERO);
          } else {
            itemVariance.put(
                "variance_percentage",
                variance
                    .divide(item.getExpectedAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")));
          }

          itemVariances.add(itemVariance);
          totalVariance = totalVariance.add(variance.abs());
          varianceCount++;
        }
      }
    }

    varianceReport.put("reconciliation_id", reconciliationId);
    varianceReport.put("reconciliation_number", reconciliation.getReconciliationNumber());
    varianceReport.put("total_variance", totalVariance);
    varianceReport.put("variance_count", varianceCount);
    varianceReport.put("item_variances", itemVariances);
    varianceReport.put("is_balanced", reconciliation.isBalanced());

    return varianceReport;
  }

  /** Apply reconciliation rules to identify items requiring manual review */
  @Transactional
  public List<ReconciliationItem> identifyItemsForManualReview(UUID reconciliationId) {
    List<ReconciliationItem> items =
        reconciliationService.findItemsByReconciliationId(reconciliationId);
    List<ReconciliationItem> reviewItems = new ArrayList<>();

    for (ReconciliationItem item : items) {
      if (requiresManualReview(item)) {
        reviewItems.add(item);
      }
    }

    return reviewItems;
  }

  /** Check if an item requires manual review */
  private boolean requiresManualReview(ReconciliationItem item) {
    // Unmatched items always require review
    if (item.getMatchStatus() == MatchStatus.UNMATCHED) {
      return true;
    }

    // Items in suspense require review
    if (item.getMatchStatus() == MatchStatus.SUSPENSE) {
      return true;
    }

    // Items with significant variance require review
    if (item.getMatchStatus() == MatchStatus.MATCHED) {
      BigDecimal variance = item.getExpectedAmount().subtract(item.getActualAmount()).abs();
      
      // Handle zero expected amount case
      if (item.getExpectedAmount().compareTo(BigDecimal.ZERO) == 0) {
        // If expected is zero but actual is non-zero, requires manual review
        if (item.getActualAmount().compareTo(BigDecimal.ZERO) != 0) {
          return true;
        }
        // Both zero - no variance, no review needed
        return false;
      }
      
      BigDecimal variancePercentage =
          variance.divide(item.getExpectedAmount(), 4, RoundingMode.HALF_UP);
      if (variancePercentage.compareTo(new BigDecimal("0.05")) > 0) { // > 5% variance
        return true;
      }
    }

    return false;
  }
}
