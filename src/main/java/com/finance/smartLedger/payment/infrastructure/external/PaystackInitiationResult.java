package com.finance.smartLedger.payment.infrastructure.external;

public record PaystackInitiationResult(String reference, String authorizationUrl, String accessCode) {}
