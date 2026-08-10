-- Add source_payment_id to fee_payments table for idempotency
-- This allows linking FeePayment back to the original Payment for deduplication

ALTER TABLE fee_payments 
ADD COLUMN source_payment_id UUID;

-- Create index for source_payment_id queries
CREATE INDEX idx_fee_payments_source_payment_id ON fee_payments(source_payment_id);
