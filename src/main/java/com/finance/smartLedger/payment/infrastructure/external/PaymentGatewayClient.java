package com.finance.smartLedger.payment.infrastructure.external;

import com.finance.smartLedger.payment.application.dto.PaymentVerifyResponse;
import java.math.BigDecimal;
import java.util.Map;

public interface PaymentGatewayClient {

  PaystackInitiationResult initiatePayment(
      BigDecimal amount,
      String currency,
      String description,
      String customerEmail,
      String customerName,
      Map<String, String> metadata,
      String callbackUrl);

  PaymentVerifyResponse verifyPayment(String reference);

  String retrievePaymentStatus(String paymentId);

  boolean refundPayment(String paymentId, BigDecimal amount);

  boolean cancelPayment(String paymentId);
}
