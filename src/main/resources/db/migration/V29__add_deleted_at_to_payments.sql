-- Add deleted_at column to payments table for soft delete support
-- This migration adds the deleted_at column to match the BaseEntity field

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'payments' AND column_name = 'deleted_at'
    ) THEN
        ALTER TABLE payments ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;
    END IF;
END $$;

-- Create index for soft delete queries (idempotent)
CREATE INDEX IF NOT EXISTS idx_payments_deleted_at ON payments(deleted_at);
