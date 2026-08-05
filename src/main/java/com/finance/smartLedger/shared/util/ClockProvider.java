package com.finance.smartLedger.shared.util;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

@Component
public class ClockProvider {

  private Clock clock = Clock.systemDefaultZone();

  public ClockProvider() {}

  public Clock getClock() {
    return clock;
  }

  public void setClock(Clock clock) {
    this.clock = clock;
  }

  public LocalDateTime now() {
    return LocalDateTime.now(clock);
  }

  public void resetToDefault() {
    this.clock = Clock.systemDefaultZone();
  }

  public void setToFixed(LocalDateTime fixedDateTime) {
    this.clock =
        Clock.fixed(
            fixedDateTime.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
  }

  public long millis() {
    return clock.millis();
  }
}
