-- Add payments table for payment module
-- This migration creates the payments table for processing payments

CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_number VARCHAR(50) UNIQUE NOT NULL,
    payment_date TIMESTAMP WITH TIME ZONE NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    payer_name VARCHAR(100),
    payer_email VARCHAR(100),
    payer_phone VARCHAR(20),
    description VARCHAR(500),
    gateway_transaction_id VARCHAR(100),
    gateway_reference VARCHAR(100),
    gateway_response_code VARCHAR(50),
    gateway_response_message VARCHAR(500),
    processed_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    failed_at TIMESTAMP WITH TIME ZONE,
    refunded_at TIMESTAMP WITH TIME ZONE,
    metadata TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL
);

-- Create indexes for common queries
CREATE INDEX idx_payments_payment_number ON payments(payment_number);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_payment_method ON payments(payment_method);
CREATE INDEX idx_payments_currency_code ON payments(currency_code);
CREATE INDEX idx_payments_payer_email ON payments(payer_email);
CREATE INDEX idx_payments_payment_date ON payments(payment_date);
CREATE INDEX idx_payments_gateway_transaction_id ON payments(gateway_transaction_id);
CREATE INDEX idx_payments_gateway_reference ON payments(gateway_reference);
CREATE INDEX idx_payments_created_at ON payments(created_at);

-- Create trigger for automatic updated_at timestamp
CREATE TRIGGER update_payments_modified_column
    BEFORE UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();
