package com.finance.smartLedger.web.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

  @GetMapping({"/", "/dashboard"})
  public String dashboard() {
    return "dashboard";
  }

  @GetMapping("/fees")
  public String fees() {
    return "fees";
  }

  @GetMapping("/payments")
  public String payments() {
    return "payments";
  }

  @GetMapping("/ledger")
  public String ledger() {
    return "ledger";
  }

  @GetMapping("/reconciliation")
  public String reconciliation() {
    return "reconciliation";
  }

  @GetMapping("/reports")
  public String reports() {
    return "reports";
  }

  @GetMapping("/ai-insights")
  public String aiInsights() {
    return "ai-insights";
  }
}
