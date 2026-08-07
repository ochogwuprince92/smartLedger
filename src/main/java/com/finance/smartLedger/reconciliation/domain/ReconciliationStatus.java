package com.finance.smartLedger.reconciliation.domain;

public enum ReconciliationStatus {
  PENDING,
  IN_PROGRESS,
  COMPLETED,
  FAILED,
  PARTIALLY_MATCHED,
  PENDING_APPROVAL
}
