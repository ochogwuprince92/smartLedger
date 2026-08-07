package com.finance.smartLedger.ai.infrastructure.persistence;

import com.finance.smartLedger.ai.domain.AIInsight;
import com.finance.smartLedger.ai.domain.InsightStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AIInsightRepository extends JpaRepository<AIInsight, UUID> {

  List<AIInsight> findByInsightType(String insightType);

  List<AIInsight> findByStatus(InsightStatus status);

  List<AIInsight> findBySeverity(String severity);

  List<AIInsight> findByDataSource(String dataSource);

  List<AIInsight> findByReferenceDateBetween(LocalDate startDate, LocalDate endDate);

  List<AIInsight> findByStatusAndIsReviewedFalse(InsightStatus status);

  List<AIInsight> findByIsActionableTrueAndIsResolvedFalse();
}
