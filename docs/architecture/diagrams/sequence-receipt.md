# Sequence Diagram - Receipt Generation

## Receipt Generation Flow

```mermaid
sequenceDiagram
    participant Client as Client
    participant REST as REST Controller
    participant Command as Command Handler
    participant AppService as Receipt Application Service
    participant ReceiptAggregate as Receipt Aggregate
    participant PaymentRepo as Payment Repository
    participant TemplateService as Template Service
    participant PDFGenerator as PDF Generator
    participant StorageService as Storage Service
    participant Notification as Notification Service
    participant Audit as Audit Logger
    
    Client->>REST: POST /api/receipts/generate
    REST->>REST: Validate Request
    REST->>Command: GenerateReceiptCommand
    Command->>AppService: generateReceipt(command)
    
    AppService->>PaymentRepo: findById(paymentId)
    PaymentRepo-->>AppService: Payment
    
    alt Payment Found and Completed
        AppService->>ReceiptAggregate: createReceipt(payment)
        ReceiptAggregate->>ReceiptAggregate: populateDetails()
        ReceiptAggregate->>ReceiptAggregate: calculateTotals()
        
        AppService->>TemplateService: loadTemplate(receiptType)
        TemplateService-->>AppService: Thymeleaf Template
        
        AppService->>ReceiptAggregate: renderWithTemplate(template)
        ReceiptAggregate-->>AppService: HTML Content
        
        AppService->>PDFGenerator: generatePDF(htmlContent)
        PDFGenerator-->>AppService: PDF Bytes
        
        AppService->>StorageService: storePDF(pdfBytes, fileName)
        StorageService-->>AppService: StorageUrl
        
        AppService->>ReceiptAggregate: setStorageUrl(storageUrl)
        AppService->>ReceiptRepo: save(receipt)
        
        alt Email Delivery Requested
            AppService->>Notification: sendEmailWithReceipt(customerEmail, storageUrl)
            Notification-->>AppService: EmailSent
        end
        
        AppService->>Audit: logReceiptGeneration()
        AppService-->>Command: ReceiptResponse (SUCCESS)
    else Payment Not Found
        AppService->>Audit: logPaymentNotFound()
        AppService-->>Command: ErrorResponse (NOT_FOUND)
    else Payment Not Completed
        AppService->>Audit: logPaymentNotCompleted()
        AppService-->>Command: ErrorResponse (INVALID_STATE)
    end
    
    Command-->>REST: ReceiptResponse
    REST-->>Client: 200 OK with ReceiptResponse
```

## Receipt Generation Steps

1. **Request Validation**: REST controller validates receipt generation request
2. **Payment Lookup**: Fetch payment by ID
3. **Receipt Creation**: Receipt aggregate creates receipt with payment details
4. **Template Loading**: Load appropriate Thymeleaf template
5. **HTML Rendering**: Render receipt with template
6. **PDF Generation**: Convert HTML to PDF
7. **Storage**: Store PDF in cloud storage
8. **Email Delivery**: Send receipt via email if requested
9. **Audit Logging**: Log receipt generation for compliance

## Key Design Decisions
- Receipt aggregate encapsulates receipt logic
- Thymeleaf templates for flexible receipt design
- PDF generation for professional receipts
- Cloud storage for persistent receipt storage
- Email delivery for customer convenience
- Audit trail for compliance
