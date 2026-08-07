-- Add receipts table for receipt module
-- This migration creates the receipts table for managing payment receipts

CREATE TABLE IF NOT EXISTS receipts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    receipt_number VARCHAR(50) UNIQUE NOT NULL,
    payment_id UUID NOT NULL,
    receipt_date TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    payer_name VARCHAR(100),
    payer_email VARCHAR(100),
    payer_phone VARCHAR(20),
    description VARCHAR(500),
    payment_method VARCHAR(30),
    payment_reference VARCHAR(100),
    sent_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    generated_file_path VARCHAR(500),
    metadata TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL
);

-- Create indexes for common queries
CREATE INDEX idx_receipts_receipt_number ON receipts(receipt_number);
CREATE INDEX idx_receipts_payment_id ON receipts(payment_id);
CREATE INDEX idx_receipts_status ON receipts(status);
CREATE INDEX idx_receipts_payer_email ON receipts(payer_email);
CREATE INDEX idx_receipts_receipt_date ON receipts(receipt_date);
CREATE INDEX idx_receipts_created_at ON receipts(created_at);

-- Create trigger for automatic updated_at timestamp
CREATE TRIGGER update_receipts_modified_column
    BEFORE UPDATE ON receipts
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- Add receipt permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'RECEIPT:CREATE', 'Create Receipt', 'Create new receipts', 'receipt', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'RECEIPT:READ', 'Read Receipt', 'Read receipt information', 'receipt', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'RECEIPT:UPDATE', 'Update Receipt', 'Update receipt information', 'receipt', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'RECEIPT:DELETE', 'Delete Receipt', 'Delete receipts', 'receipt', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;
