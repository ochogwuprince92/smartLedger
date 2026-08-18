package com.finance.smartLedger.journal.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Journal entry type")
public enum JournalEntryTypeDto {
  MANUAL,
  AUTOMATIC,
  RECURRING,
  ADJUSTING,
  CLOSING;

  @JsonCreator
  public static JournalEntryTypeDto fromString(String value) {
    return JournalEntryTypeDto.valueOf(value.toUpperCase());
  }

  public com.finance.smartLedger.journal.domain.JournalEntryType toDomain() {
    return com.finance.smartLedger.journal.domain.JournalEntryType.valueOf(name());
  }
}
