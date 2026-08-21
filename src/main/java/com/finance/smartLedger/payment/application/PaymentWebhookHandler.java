package com.finance.smartLedger.payment.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.smartLedger.payment.domain.Payment;
import com.finance.smartLedger.payment.infrastructure.security.WebhookSignatureValidator;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookHandler {

  private final PaymentService paymentService;
  private final ObjectMapper objectMapper;
  private final WebhookSignatureValidator signatureValidator;

  public Payment handleWebhook(String gatewayType, String payload, String signature) {
    try {
      // Validate webhook signature for security
      if (!validateSignature(gatewayType, payload, signature)) {
        log.error("Invalid webhook signature for gateway: {}", gatewayType);
        throw new SecurityException("Invalid webhook signature");
      }

      JsonNode jsonNode = objectMapper.readTree(payload);
      String eventType = extractEventType(jsonNode, gatewayType);
      String gatewayTransactionId = extractTransactionId(jsonNode, gatewayType);
      String gatewayReference = extractReference(jsonNode, gatewayType);

      // Look up payment by gatewayReference (not gatewayTransactionId which is null before completion)
      Optional<Payment> existingPayment =
          paymentService.findByGatewayReference(gatewayReference);

      if (existingPayment.isPresent()) {
        return updatePaymentFromWebhook(
            existingPayment.get(), eventType, jsonNode, gatewayType, gatewayReference, gatewayTransactionId);
      } else {
        log.warn("Payment not found for gateway reference: {}", gatewayReference);
        return null;
      }
    } catch (Exception e) {
      log.error("Error processing webhook", e);
      throw new RuntimeException("Failed to process webhook", e);
    }
  }

  private boolean validateSignature(String gatewayType, String payload, String signature) {
    if (signature == null || signature.isEmpty()) {
      log.warn("No signature provided for webhook");
      return false;
    }

    return signatureValidator.validatePaystackSignature(payload, signature);
  }

  private Payment updatePaymentFromWebhook(
      Payment payment,
      String eventType,
      JsonNode jsonNode,
      String gatewayType,
      String gatewayReference,
      String gatewayTransactionId) {
    return switch (eventType.toLowerCase()) {
      case "charge.success", "payment.success" -> {
        // First process the payment if it's still pending
        if (payment.getStatus().name().equals("PENDING")) {
          paymentService.processPayment(payment.getId(), "webhook");
        }
        // Then complete it with the actual gateway transaction ID from webhook
        yield paymentService.completePayment(
            payment.getId(),
            gatewayTransactionId,
            gatewayReference,
            extractResponseCode(jsonNode, gatewayType),
            extractResponseMessage(jsonNode, gatewayType),
            "webhook");
      }
      case "charge.failed", "payment.failed" ->
          paymentService.failPayment(
              payment.getId(),
              extractResponseCode(jsonNode, gatewayType),
              extractResponseMessage(jsonNode, gatewayType),
              "webhook");
      case "refund.processed" -> paymentService.refundPayment(payment.getId(), "webhook");
      default -> {
        log.warn("Unhandled webhook event type: {}", eventType);
        yield payment;
      }
    };
  }

  private String extractEventType(JsonNode jsonNode, String gatewayType) {
    return jsonNode.has("event") ? jsonNode.get("event").asText() : "unknown";
  }

  private String extractTransactionId(JsonNode jsonNode, String gatewayType) {
    return jsonNode.path("data").path("reference").asText();
  }

  private String extractReference(JsonNode jsonNode, String gatewayType) {
    return jsonNode.path("data").path("reference").asText();
  }

  private String extractResponseCode(JsonNode jsonNode, String gatewayType) {
    return jsonNode.path("data").path("status").asText();
  }

  private String extractResponseMessage(JsonNode jsonNode, String gatewayType) {
    return jsonNode.path("data").path("message").asText();
  }
}
