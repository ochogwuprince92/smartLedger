package com.finance.smartLedger.shared.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class DomainEvent {

  private UUID eventId;
  private LocalDateTime occurredOn;
  private String eventType;

  protected DomainEvent(String eventType) {
    this.eventId = UUID.randomUUID();
    this.occurredOn = LocalDateTime.now();
    this.eventType = eventType;
  }
}
