-- Add source_payment_id to fee_payments table for idempotency
-- This allows linking FeePayment back to the original Payment for deduplication

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'fee_payments' 
        AND column_name = 'source_payment_id'
    ) THEN
        ALTER TABLE fee_payments ADD COLUMN source_payment_id UUID;
    END IF;
END $$;

-- Create index for source_payment_id queries
CREATE INDEX IF NOT EXISTS idx_fee_payments_source_payment_id ON fee_payments(source_payment_id);
