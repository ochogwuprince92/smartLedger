# Sequence Diagram - Webhook Handling

## Webhook Processing Flow

```mermaid
sequenceDiagram
    participant Gateway as Payment Gateway
    participant WebhookController as Webhook Controller
    participant Security as Security Service
    participant Command as Command Handler
    participant AppService as Webhook Application Service
    participant PaymentAggregate as Payment Aggregate
    participant Repo as Payment Repository
    participant EventPublisher as Event Publisher
    participant Audit as Audit Logger
    
    Gateway->>WebhookController: POST /api/webhooks/payment
    WebhookController->>Security: verifyHMAC(payload, signature)
    
    alt HMAC Valid
        Security->>Security: verifyReplayProtection(eventId)
        
        alt Not Replayed
            Security-->>WebhookController: Valid
            WebhookController->>WebhookController: Validate Payload
            WebhookController->>Command: ProcessWebhookCommand
            Command->>AppService: processWebhook(command)
            
            AppService->>Repo: findByGatewayTransactionId()
            Repo-->>AppService: Optional<Payment>
            
            alt Payment Found
                AppService->>PaymentAggregate: updateFromWebhook(webhookData)
                
                alt Webhook: Payment Succeeded
                    PaymentAggregate->>PaymentAggregate: markCompleted()
                    AppService->>Repo: save(payment)
                    AppService->>EventPublisher: publish(PaymentCompletedEvent)
                    AppService->>Audit: logWebhookProcessed()
                    AppService-->>Command: WebhookResponse (PROCESSED)
                else Webhook: Payment Failed
                    PaymentAggregate->>PaymentAggregate: markFailed()
                    AppService->>Repo: save(payment)
                    AppService->>EventPublisher: publish(PaymentFailedEvent)
                    AppService->>Audit: logWebhookProcessed()
                    AppService-->>Command: WebhookResponse (PROCESSED)
                end
            else Payment Not Found
                AppService->>AppService: createSuspenseEntry(webhookData)
                AppService->>EventPublisher: publish(OrphanedWebhookEvent)
                AppService->>Audit: logOrphanedWebhook()
                AppService-->>Command: WebhookResponse (SUSPENSE)
            end
            
            Command-->>WebhookController: WebhookResponse
            WebhookController-->>Gateway: 200 OK
        else Replayed Request
            Security-->>WebhookController: Rejected
            WebhookController-->>Gateway: 409 Conflict
        end
    else HMAC Invalid
        Security-->>WebhookController: Invalid
        WebhookController->>Audit: logSecurityViolation()
        WebhookController-->>Gateway: 401 Unauthorized
    end
```

## Webhook Processing Steps

1. **HMAC Verification**: Security service verifies webhook signature
2. **Replay Protection**: Check if webhook was already processed
3. **Payload Validation**: Validate webhook payload structure
4. **Payment Lookup**: Find payment by gateway transaction ID
5. **Payment Update**: Update payment aggregate based on webhook status
6. **Event Publishing**: Publish domain events for notifications
7. **Audit Logging**: Log all webhook processing for compliance
8. **Suspense Handling**: Create suspense entry for orphaned webhooks

## Key Design Decisions
- HMAC verification ensures webhook authenticity
- Replay protection prevents duplicate processing
- Orphaned webhooks go to suspense account for manual review
- Audit trail maintained for all webhook events
