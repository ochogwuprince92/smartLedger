package com.finance.smartLedger.shared.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ClockProvider Tests")
class ClockProviderTest {

  private ClockProvider clockProvider = new ClockProvider();

  @AfterEach
  void tearDown() {
    clockProvider.resetToDefault();
  }

  @Test
  @DisplayName("Should get default system clock")
  void shouldGetDefaultSystemClock() {
    Clock clock = clockProvider.getClock();

    assertNotNull(clock);
    assertEquals(Clock.systemDefaultZone().getZone(), clock.getZone());
  }

  @Test
  @DisplayName("Should get current time")
  void shouldGetCurrentTime() {
    LocalDateTime now = clockProvider.now();

    assertNotNull(now);
    assertTrue(now.isBefore(LocalDateTime.now().plusSeconds(1)));
    assertTrue(now.isAfter(LocalDateTime.now().minusSeconds(1)));
  }

  @Test
  @DisplayName("Should set custom clock")
  void shouldSetCustomClock() {
    Clock customClock = Clock.systemUTC();

    clockProvider.setClock(customClock);

    assertEquals(customClock, clockProvider.getClock());
  }

  @Test
  @DisplayName("Should reset to default clock")
  void shouldResetToDefaultClock() {
    Clock customClock = Clock.systemUTC();
    clockProvider.setClock(customClock);

    clockProvider.resetToDefault();

    assertEquals(Clock.systemDefaultZone().getZone(), clockProvider.getClock().getZone());
  }

  @Test
  @DisplayName("Should set fixed time")
  void shouldSetFixedTime() {
    LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

    clockProvider.setToFixed(fixedTime);

    LocalDateTime now = clockProvider.now();
    assertEquals(fixedTime, now);
  }

  @Test
  @DisplayName("Should return same time when clock is fixed")
  void shouldReturnSameTimeWhenClockIsFixed() {
    LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
    clockProvider.setToFixed(fixedTime);

    LocalDateTime time1 = clockProvider.now();
    LocalDateTime time2 = clockProvider.now();

    assertEquals(time1, time2);
    assertEquals(fixedTime, time1);
  }

  @Test
  @DisplayName("Should get current millis")
  void shouldGetCurrentMillis() {
    long millis1 = clockProvider.millis();
    long millis2 = System.currentTimeMillis();

    assertTrue(millis1 > 0);
    assertTrue(Math.abs(millis1 - millis2) < 1000);
  }

  @Test
  @DisplayName("Should get fixed millis when clock is fixed")
  void shouldGetFixedMillisWhenClockIsFixed() {
    LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
    clockProvider.setToFixed(fixedTime);

    long millis1 = clockProvider.millis();
    long millis2 = clockProvider.millis();

    assertEquals(millis1, millis2);
  }

  @Test
  @DisplayName("Should handle multiple clock changes")
  void shouldHandleMultipleClockChanges() {
    Clock clock1 = Clock.systemUTC();
    Clock clock2 = Clock.systemDefaultZone();

    clockProvider.setClock(clock1);
    assertEquals(clock1, clockProvider.getClock());

    clockProvider.setClock(clock2);
    assertEquals(clock2, clockProvider.getClock());
  }

  @Test
  @DisplayName("Should handle null clock reset")
  void shouldHandleNullClockReset() {
    clockProvider.setClock(null);

    clockProvider.resetToDefault();

    assertNotNull(clockProvider.getClock());
  }

  @Test
  @DisplayName("Should maintain zone when setting fixed time")
  void shouldMaintainZoneWhenSettingFixedTime() {
    LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
    clockProvider.setToFixed(fixedTime);

    assertEquals(ZoneId.systemDefault(), clockProvider.getClock().getZone());
  }

  @Test
  @DisplayName("Should return different times after reset")
  void shouldReturnDifferentTimesAfterReset() {
    LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
    clockProvider.setToFixed(fixedTime);

    LocalDateTime fixedNow = clockProvider.now();
    clockProvider.resetToDefault();
    LocalDateTime systemNow = clockProvider.now();

    assertNotEquals(fixedNow, systemNow);
  }
}
