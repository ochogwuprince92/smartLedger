-- Add invoice_id to payments table for fee payment integration
-- This allows linking gateway payments to fee invoices

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'payments' 
        AND column_name = 'invoice_id'
    ) THEN
        ALTER TABLE payments ADD COLUMN invoice_id UUID;
    END IF;
END $$;

-- Create index for invoice_id queries
CREATE INDEX IF NOT EXISTS idx_payments_invoice_id ON payments(invoice_id);
