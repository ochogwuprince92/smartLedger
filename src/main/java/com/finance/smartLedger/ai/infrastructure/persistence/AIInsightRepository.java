package com.finance.smartLedger.ai.infrastructure.persistence;

import com.finance.smartLedger.ai.domain.AIInsight;
import com.finance.smartLedger.ai.domain.AIInsightType;
import com.finance.smartLedger.ai.domain.InsightStatus;
import com.finance.smartLedger.ai.domain.RiskLevel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AIInsightRepository extends JpaRepository<AIInsight, UUID> {

  Optional<AIInsight> findByRequestId(String requestId);

  List<AIInsight> findByReconciliationId(UUID reconciliationId);

  List<AIInsight> findByStatus(InsightStatus status);

  List<AIInsight> findByInsightType(AIInsightType insightType);

  List<AIInsight> findByRiskLevel(RiskLevel riskLevel);

  @Query("SELECT i FROM AIInsight i WHERE i.status = :status AND i.retryCount < i.maxRetries")
  List<AIInsight> findByStatusAndRetryCountLessThanMaxRetries(InsightStatus status);
}
