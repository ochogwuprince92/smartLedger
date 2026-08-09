package com.finance.smartLedger.ai.application;

import com.finance.smartLedger.ai.domain.AIInsight;
import com.finance.smartLedger.ai.domain.AIInsightType;
import com.finance.smartLedger.ai.domain.InsightStatus;
import com.finance.smartLedger.ai.infrastructure.persistence.AIInsightRepository;
import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.ledger.domain.Transaction;
import com.finance.smartLedger.payment.domain.Payment;
import com.finance.smartLedger.payment.domain.PaymentMethod;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnomalyDetectionService {

  private final AIInsightRepository aiInsightRepository;

  @Value("${ai.anomaly-detection.duplicate-threshold-seconds:300}")
  private long duplicateThresholdSeconds;

  @Value("${ai.anomaly-detection.outlier-std-dev-threshold:3.0}")
  private double outlierStdDevThreshold;

  @Value("${ai.anomaly-detection.risk-score-weights.amount:0.4}")
  private double riskWeightAmount;

  @Value("${ai.anomaly-detection.risk-score-weights.frequency:0.3}")
  private double riskWeightFrequency;

  @Value("${ai.anomaly-detection.risk-score-weights.timing:0.2}")
  private double riskWeightTiming;

  @Value("${ai.anomaly-detection.risk-score-weights.pattern:0.1}")
  private double riskWeightPattern;

  /** Detect duplicate payments based on amount, recipient, and time proximity */
  @Transactional
  public List<AIInsight> detectDuplicatePayments(List<Payment> payments) {
    List<AIInsight> anomalies = new ArrayList<>();

    for (int i = 0; i < payments.size(); i++) {
      for (int j = i + 1; j < payments.size(); j++) {
        Payment payment1 = payments.get(i);
        Payment payment2 = payments.get(j);

        if (isDuplicatePayment(payment1, payment2)) {
          AIInsight insight = createDuplicatePaymentInsight(payment1, payment2, "system");
          aiInsightRepository.save(insight);
          anomalies.add(insight);
          log.info("Duplicate payment detected: {} and {}", payment1.getId(), payment2.getId());
        }
      }
    }

    return anomalies;
  }

  /** Detect negative balances in accounts */
  @Transactional
  public List<AIInsight> detectNegativeBalances(List<Account> accounts) {
    List<AIInsight> anomalies = new ArrayList<>();

    for (Account account : accounts) {
      if (account.getBalance().getCurrentBalance().getAmount().compareTo(BigDecimal.ZERO) < 0) {
        AIInsight insight = createNegativeBalanceInsight(account, "system");
        aiInsightRepository.save(insight);
        anomalies.add(insight);
        log.warn("Negative balance detected for account: {}", account.getAccountNumber());
      }
    }

    return anomalies;
  }

  /** Detect outlier transactions using statistical analysis (Z-score) */
  @Transactional
  public List<AIInsight> detectOutlierTransactions(List<Transaction> transactions) {
    List<AIInsight> anomalies = new ArrayList<>();

    if (transactions.size() < 3) {
      return anomalies; // Need at least 3 transactions for statistical analysis
    }

    // Calculate mean and standard deviation
    double mean = calculateMean(transactions);
    double stdDev = calculateStandardDeviation(transactions, mean);

    for (Transaction transaction : transactions) {
      double amount = transaction.getAmount().getAmount().doubleValue();
      double zScore = Math.abs((amount - mean) / stdDev);

      if (zScore > outlierStdDevThreshold) {
        AIInsight insight = createOutlierInsight(transaction, zScore, mean, stdDev, "system");
        aiInsightRepository.save(insight);
        anomalies.add(insight);
        log.info("Outlier transaction detected: {} with Z-score: {}", transaction.getId(), zScore);
      }
    }

    return anomalies;
  }

  /** Calculate risk score for a payment based on multiple factors */
  public double calculateRiskScore(Payment payment, List<Payment> recentPayments) {
    double amountRisk = calculateAmountRisk(payment, recentPayments);
    double frequencyRisk = calculateFrequencyRisk(payment, recentPayments);
    double timingRisk = calculateTimingRisk(payment);
    double patternRisk = calculatePatternRisk(payment);

    return (amountRisk * riskWeightAmount)
        + (frequencyRisk * riskWeightFrequency)
        + (timingRisk * riskWeightTiming)
        + (patternRisk * riskWeightPattern);
  }

  /** Detect high-risk payments based on risk score threshold */
  @Transactional
  public List<AIInsight> detectHighRiskPayments(List<Payment> payments, double riskThreshold) {
    List<AIInsight> anomalies = new ArrayList<>();

    for (Payment payment : payments) {
      double riskScore = calculateRiskScore(payment, payments);

      if (riskScore > riskThreshold) {
        AIInsight insight = createHighRiskPaymentInsight(payment, riskScore, "system");
        aiInsightRepository.save(insight);
        anomalies.add(insight);
        log.warn("High-risk payment detected: {} with risk score: {}", payment.getId(), riskScore);
      }
    }

    return anomalies;
  }

  /** Run comprehensive anomaly detection */
  @Transactional
  public Map<String, List<AIInsight>> runComprehensiveDetection(
      List<Payment> payments, List<Account> accounts, List<Transaction> transactions) {
    Map<String, List<AIInsight>> results = new HashMap<>();

    results.put("duplicates", detectDuplicatePayments(payments));
    results.put("negative_balances", detectNegativeBalances(accounts));
    results.put("outliers", detectOutlierTransactions(transactions));
    results.put("high_risk", detectHighRiskPayments(payments, 0.7));

    return results;
  }

  // Helper methods

  private boolean isDuplicatePayment(Payment payment1, Payment payment2) {
    // Check if amounts are the same
    if (!payment1.getAmount().equals(payment2.getAmount())) {
      return false;
    }

    // Check if payments are within time threshold
    Duration timeDiff = Duration.between(payment1.getPaymentDate(), payment2.getPaymentDate());
    if (Math.abs(timeDiff.getSeconds()) > duplicateThresholdSeconds) {
      return false;
    }

    // Check if payment methods are similar
    if (!payment1.getPaymentMethod().equals(payment2.getPaymentMethod())) {
      return false;
    }

    return true;
  }

  private AIInsight createDuplicatePaymentInsight(
      Payment payment1, Payment payment2, String createdBy) {
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("payment1_id", payment1.getId());
    metadata.put("payment2_id", payment2.getId());
    metadata.put("amount", payment1.getAmount());
    metadata.put(
        "time_diff_seconds",
        Duration.between(payment1.getPaymentDate(), payment2.getPaymentDate()).getSeconds());

    AIInsight insight =
        AIInsight.builder()
            .insightType(AIInsightType.ANOMALY_DETECTION)
            .summary("Potential Duplicate Payment Detected")
            .rootCause(
                String.format(
                    "Two payments with amount %s were made within %d seconds. Payment IDs: %s, %s",
                    payment1.getAmount(),
                    Duration.between(payment1.getPaymentDate(), payment2.getPaymentDate())
                        .getSeconds(),
                    payment1.getId(),
                    payment2.getId()))
            .recommendations(
                "Review both payments and confirm if they are legitimate duplicates. If confirmed, refund one payment.")
            .status(InsightStatus.PENDING)
            .requestedAt(LocalDateTime.now())
            .build();
    insight.setCreatedBy(createdBy);
    return insight;
  }

  private AIInsight createNegativeBalanceInsight(Account account, String createdBy) {
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("account_number", account.getAccountNumber());
    metadata.put("account_type", account.getAccountType());
    metadata.put("current_balance", account.getBalance().getCurrentBalance().getAmount());

    AIInsight insight =
        AIInsight.builder()
            .insightType(AIInsightType.ANOMALY_DETECTION)
            .summary("Negative Balance Detected")
            .rootCause(
                String.format(
                    "Account %s (Type: %s) has a negative balance of %s",
                    account.getAccountNumber(),
                    account.getAccountType(),
                    account.getBalance().getCurrentBalance().getAmount()))
            .recommendations(
                "Review the account transactions and investigate the cause of the negative balance. Consider adjusting the account or reconciling transactions.")
            .status(InsightStatus.PENDING)
            .requestedAt(LocalDateTime.now())
            .build();
    insight.setCreatedBy(createdBy);
    return insight;
  }

  private AIInsight createOutlierInsight(
      Transaction transaction, double zScore, double mean, double stdDev, String createdBy) {
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("transaction_id", transaction.getId());
    metadata.put("amount", transaction.getAmount().getAmount());
    metadata.put("z_score", zScore);
    metadata.put("mean", mean);
    metadata.put("std_dev", stdDev);

    AIInsight insight =
        AIInsight.builder()
            .insightType(AIInsightType.ANOMALY_DETECTION)
            .summary("Outlier Transaction Detected")
            .rootCause(
                String.format(
                    "Transaction %s with amount %s is an outlier (Z-score: %.2f, Mean: %.2f, Std Dev: %.2f)",
                    transaction.getId(), transaction.getAmount().getAmount(), zScore, mean, stdDev))
            .recommendations(
                "Review this transaction for legitimacy. Verify if the amount is correct and authorized.")
            .status(InsightStatus.PENDING)
            .requestedAt(LocalDateTime.now())
            .build();
    insight.setCreatedBy(createdBy);
    return insight;
  }

  private AIInsight createHighRiskPaymentInsight(
      Payment payment, double riskScore, String createdBy) {
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("payment_id", payment.getId());
    metadata.put("amount", payment.getAmount());
    metadata.put("risk_score", riskScore);
    metadata.put("payment_method", payment.getPaymentMethod());

    AIInsight insight =
        AIInsight.builder()
            .insightType(AIInsightType.ANOMALY_DETECTION)
            .summary("High-Risk Payment Detected")
            .rootCause(
                String.format(
                    "Payment %s with amount %s has a high risk score of %.2f",
                    payment.getId(), payment.getAmount(), riskScore))
            .recommendations(
                "Conduct additional verification for this payment. Review payment history and consider manual approval.")
            .status(InsightStatus.PENDING)
            .requestedAt(LocalDateTime.now())
            .build();
    insight.setCreatedBy(createdBy);
    return insight;
  }

  private double calculateMean(List<Transaction> transactions) {
    double sum = 0.0;
    for (Transaction transaction : transactions) {
      sum += transaction.getAmount().getAmount().doubleValue();
    }
    return sum / transactions.size();
  }

  private double calculateStandardDeviation(List<Transaction> transactions, double mean) {
    double sumSquaredDiff = 0.0;
    for (Transaction transaction : transactions) {
      double diff = transaction.getAmount().getAmount().doubleValue() - mean;
      sumSquaredDiff += diff * diff;
    }
    return Math.sqrt(sumSquaredDiff / transactions.size());
  }

  private double calculateAmountRisk(Payment payment, List<Payment> recentPayments) {
    double avgAmount =
        recentPayments.stream().mapToDouble(p -> p.getAmount().doubleValue()).average().orElse(0.0);

    double amount = payment.getAmount().doubleValue();
    double ratio = avgAmount > 0 ? amount / avgAmount : 1.0;

    // High risk if amount is significantly higher than average
    if (ratio > 3.0) return 1.0;
    if (ratio > 2.0) return 0.8;
    if (ratio > 1.5) return 0.6;
    if (ratio > 1.0) return 0.4;
    return 0.2;
  }

  private double calculateFrequencyRisk(Payment payment, List<Payment> recentPayments) {
    // Simplified - count payments from same payer
    long count =
        recentPayments.stream()
            .filter(
                p -> p.getPayerName() != null && p.getPayerName().equals(payment.getPayerName()))
            .count();

    // High risk if too many payments from same payer recently
    if (count > 10) return 1.0;
    if (count > 5) return 0.7;
    if (count > 3) return 0.5;
    return 0.2;
  }

  private double calculateTimingRisk(Payment payment) {
    // High risk if payment is made outside business hours or on weekends
    LocalDateTime paymentTime = payment.getPaymentDate();
    int hour = paymentTime.getHour();
    int dayOfWeek = paymentTime.getDayOfWeek().getValue();

    if (dayOfWeek >= 6) return 0.8; // Weekend
    if (hour < 6 || hour > 22) return 0.6; // Outside business hours
    return 0.2;
  }

  private double calculatePatternRisk(Payment payment) {
    // Simplified pattern analysis - in real implementation, use ML models
    // Check for unusual payment patterns
    PaymentMethod paymentMethod = payment.getPaymentMethod();

    // Higher risk for certain payment methods
    if (paymentMethod == PaymentMethod.BANK_TRANSFER) return 0.6;
    if (paymentMethod == PaymentMethod.CREDIT_CARD) return 0.3;
    if (paymentMethod == PaymentMethod.DEBIT_CARD) return 0.2;

    return 0.2;
  }
}
