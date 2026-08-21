# Webhook Payment Test Flow - Swagger UI

## Overview
This test flow validates the automatic payment processing through webhooks, ensuring that after completing payment on Paystack, the system automatically:
- Processes payment (PENDING → PROCESSING)
- Completes payment (PROCESSING → COMPLETED)
- Creates journal entries
- Generates receipt
- Sends notification

## Prerequisites
- Valid authorization token
- Paystack test mode enabled
- ngrok tunnel running (for testing) or production webhook URL configured
- Paystack secret key configured in application properties

## Test Flow

### Step 1: Initiate Gateway Payment
**Endpoint:** `POST /api/payment/initiate-gateway-payment`

**Headers:**
- `Authorization: Bearer YOUR_TOKEN`
- `Idempotency-Key: unique-key-1724236380`

**Request Body:**
```json
{
  "paymentNumber": "PAY-2024-TEST-001",
  "paymentDate": "2026-08-21T10:00:00",
  "paymentMethod": "PAYSTACK",
  "amount": 2500.00,
  "currencyCode": "NGN",
  "payerName": "Test User",
  "payerEmail": "test@example.com",
  "payerPhone": "+1234567890",
  "description": "Test payment for webhook flow",
  "callbackUrl": "https://your-ngrok-url.ngrok-free.dev/api/v1/webhooks/paystack"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Payment initiated successfully.Redirect to authorizationUrl to complete payment.",
  "data": {
    "id": "payment-uuid",
    "paymentNumber": "PAY-2024-TEST-001",
    "status": "PENDING",
    "authorizationUrl": "https://checkout.paystack.com/...",
    "gatewayReference": "T..."
  }
}
```

**Save these values for next steps:**
- `payment.id` (UUID)
- `gatewayReference` (Paystack reference)

---

### Step 2: Complete Payment on Paystack
**Action:** Open the `authorizationUrl` from Step 1 response in a browser.

**Steps:**
1. Navigate to the Paystack checkout URL
2. Enter test card details (Paystack test mode):
   - Card Number: `4084084084084081`
   - Expiry: Any future date
   - CVV: Any 3 digits
   - PIN: Any 4 digits
3. Submit payment
4. Wait for success page

**Expected Result:** Payment shows successful on Paystack interface

---

### Step 3: Verify Payment Status (Automatic Webhook Processing)
**Endpoint:** `GET /api/payment/payments/{id}`

**Headers:**
- `Authorization: Bearer YOUR_TOKEN`

**URL Parameter:** Use the `payment.id` from Step 1

**Expected Response (after webhook processes):**
```json
{
  "success": true,
  "data": {
    "id": "payment-uuid",
    "paymentNumber": "PAY-2024-TEST-001",
    "status": "COMPLETED",
    "gatewayTransactionId": "...",
    "gatewayReference": "T...",
    "gatewayResponseCode": "00",
    "gatewayResponseMessage": "Payment successful",
    "processedAt": "2026-08-21T10:05:00",
    "completedAt": "2026-08-21T10:05:01"
  }
}
```

**If status is still PENDING after 30 seconds:**
- Webhook may have failed (ngrok issues, signature validation)
- Proceed to manual completion test flow below

---

### Step 4: Verify Journal Entries Created
**Endpoint:** `GET /api/journal/entries`

**Headers:**
- `Authorization: Bearer YOUR_TOKEN`

**Query Parameters:**
- `referenceNumber`: `PAY-2024-TEST-001`

**Expected Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "transaction-uuid",
      "type": "PAYMENT",
      "description": "Payment received: PAY-2024-TEST-001 from Test User via PAYSTACK",
      "amount": {
        "amount": 2500.00,
        "currencyCode": "NGN"
      },
      "debitAccount": {
        "accountCode": "CASH01",
        "accountName": "Paystack Cash Account"
      },
      "creditAccount": {
        "accountCode": "AR001",
        "accountName": "Accounts Receivable"
      },
      "referenceNumber": "PAY-2024-TEST-001"
    }
  ]
}
```

---

### Step 5: Verify Receipt Generated
**Endpoint:** `GET /api/receipts/payment/{paymentId}`

**Headers:**
- `Authorization: Bearer YOUR_TOKEN`

**URL Parameter:** Use the `payment.id` from Step 1

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "id": "receipt-uuid",
    "paymentId": "payment-uuid",
    "receiptNumber": "REC-2024-...",
    "amount": 2500.00,
    "currencyCode": "NGN",
    "generatedAt": "2026-08-21T10:05:02",
    "status": "GENERATED"
  }
}
```

---

### Step 6: Verify Account Balances Updated
**Endpoint:** `GET /api/ledger/balances`

**Headers:**
- `Authorization: Bearer YOUR_TOKEN`

**Expected Response:**
- Paystack Cash Account (`CASH01`): Balance increased by 2500.00 NGN
- Accounts Receivable (`AR001`): Balance decreased by 2500.00 NGN

---

## Manual Completion Test Flow (If Webhook Fails)

### Step 1: Process Payment
**Endpoint:** `POST /api/payment/payments/{id}/process`

**Headers:**
- `Authorization: Bearer YOUR_TOKEN`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "updatedBy": "admin"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Payment processing started",
  "data": {
    "status": "PROCESSING",
    "processedAt": "2026-08-21T10:05:00"
  }
}
```

---

### Step 2: Complete Payment
**Endpoint:** `POST /api/payment/payments/{id}/complete`

**Headers:**
- `Authorization: Bearer YOUR_TOKEN`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "gatewayTransactionId": "txn_test_12345",
  "gatewayReference": "T...",
  "gatewayResponseCode": "00",
  "gatewayResponseMessage": "Payment successful",
  "updatedBy": "admin"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Payment completed successfully",
  "data": {
    "status": "COMPLETED",
    "completedAt": "2026-08-21T10:05:01"
  }
}
```

---

## Verification Checklist

After completing the test flow, verify:

- [ ] Payment status changed from PENDING → PROCESSING → COMPLETED
- [ ] Journal entry created with correct debit/credit accounts
- [ ] Receipt generated successfully
- [ ] Account balances updated correctly
- [ ] Payment notification sent (check logs or email)
- [ ] No errors in application logs
- [ ] Webhook signature validation passed (check logs)

---

## Troubleshooting

### Webhook Not Triggering
- Check ngrok tunnel is running
- Verify callback URL is accessible from external network
- Check Paystack webhook logs in Paystack dashboard
- Verify signature validation is not failing

### Payment Stuck in PENDING
- Check application logs for webhook processing errors
- Verify Paystack secret key is correctly configured
- Check network connectivity between Paystack and your endpoint

### Account Code Validation Error
- Ensure accounts `CASH01` and `AR001` exist in ledger
- Verify account codes follow format: 2-4 letters + 2-6 digits

### Journal Entry Not Created
- Check payment status is COMPLETED
- Verify account codes exist in system
- Check application logs for accounting service errors

---

## Notes
- Use unique idempotency keys for each test
- Clean up test data after testing
- Monitor application logs during webhook processing
- Test with different payment amounts and scenarios
- Consider adding webhook retry logic for production robustness
