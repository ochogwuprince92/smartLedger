package com.finance.smartLedger.configuration;

import com.finance.smartLedger.shared.util.ClockProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.scheduled.enabled", havingValue = "true", matchIfMissing = true)
public class HealthCheckScheduler {

  private static final Logger log = LoggerFactory.getLogger(HealthCheckScheduler.class);

  private final ClockProvider clockProvider;

  @Value("${app.scheduled.health-check:0 */15 * * * ?}")
  private String healthCheckCron;

  public HealthCheckScheduler(ClockProvider clockProvider) {
    this.clockProvider = clockProvider;
  }

  @Scheduled(cron = "${app.scheduled.health-check:0 */15 * * * ?}")
  public void performHealthCheck() {
    log.info("Health check performed at: {}", clockProvider.now());
  }
}
