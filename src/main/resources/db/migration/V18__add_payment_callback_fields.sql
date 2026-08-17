-- Add authorization_url and callback_url fields to payments table
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'payments' AND column_name = 'authorization_url'
    ) THEN
        ALTER TABLE payments ADD COLUMN authorization_url VARCHAR(500);
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'payments' AND column_name = 'callback_url'
    ) THEN
        ALTER TABLE payments ADD COLUMN callback_url VARCHAR(500);
    END IF;
END $$;

-- Add indexes for the new columns
CREATE INDEX IF NOT EXISTS idx_payments_authorization_url ON payments(authorization_url) WHERE authorization_url IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_payments_callback_url ON payments(callback_url) WHERE callback_url IS NOT NULL;
