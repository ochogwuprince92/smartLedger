package com.finance.smartLedger.security.application.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminPasswordResetResponse {
  private UUID userId;
  private String temporaryPassword;
}
