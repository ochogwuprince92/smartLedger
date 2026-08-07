package com.finance.smartLedger.ledger.infrastructure.event;

import com.finance.smartLedger.ledger.domain.event.AccountCreated;
import com.finance.smartLedger.ledger.domain.event.BalanceUpdated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccountEventHandler {

  @Async
  @EventListener
  public void handleAccountCreated(AccountCreated event) {
    log.info(
        "Handling AccountCreated event for account: {} by: {}",
        event.getAccountNumber(),
        event.getCreatedBy());
    // Additional logic can be added here, such as:
    // - Sending notifications
    // - Updating caches
    // - Triggering workflows
  }

  @Async
  @EventListener
  public void handleBalanceUpdated(BalanceUpdated event) {
    log.info(
        "Handling BalanceUpdated event for account: {}, change: {} by: {}",
        event.getAccountNumber(),
        event.getChangeAmount(),
        event.getUpdatedBy());
    // Additional logic can be added here, such as:
    // - Sending notifications for significant balance changes
    // - Updating caches
    // - Triggering reconciliation
  }
}
