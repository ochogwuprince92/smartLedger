package com.finance.smartLedger.payment.infrastructure.external;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentGatewayClient {

  String initiatePayment(
      BigDecimal amount,
      String currency,
      String description,
      String customerEmail,
      String customerName,
      Map<String, String> metadata);

  String retrievePaymentStatus(String paymentId);

  boolean refundPayment(String paymentId, BigDecimal amount);

  boolean cancelPayment(String paymentId);
}
