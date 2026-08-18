-- Add idempotency_key column to payments table for idempotent payment capture
-- This prevents duplicate processing of the same payment request

-- Add idempotency_key column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'payments' AND column_name = 'idempotency_key'
    ) THEN
        ALTER TABLE payments ADD COLUMN idempotency_key VARCHAR(100);
    END IF;
END $$;

-- Add unique constraint on idempotency_key if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'uk_payments_idempotency_key'
    ) THEN
        ALTER TABLE payments ADD CONSTRAINT uk_payments_idempotency_key UNIQUE (idempotency_key);
    END IF;
END $$;

-- Add index for faster lookups if it doesn't exist
CREATE INDEX IF NOT EXISTS idx_payments_idempotency_key ON payments(idempotency_key);
