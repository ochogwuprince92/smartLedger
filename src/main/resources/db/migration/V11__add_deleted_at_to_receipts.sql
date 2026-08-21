-- ============================================
-- ADD SOFT DELETE COLUMN TO RECEIPTS
-- ============================================
-- The Receipt entity extends BaseEntity, which maps deletedAt, but the
-- receipts table was created without it, breaking Hibernate schema validation.

-- Create receipts table with deleted_at if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'receipts') THEN
        CREATE TABLE receipts (
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
            updated_by VARCHAR(255) NOT NULL,
            deleted_at TIMESTAMP WITH TIME ZONE
        );
        
        CREATE INDEX idx_receipts_receipt_number ON receipts(receipt_number);
        CREATE INDEX idx_receipts_payment_id ON receipts(payment_id);
        CREATE INDEX idx_receipts_status ON receipts(status);
        CREATE INDEX idx_receipts_payer_email ON receipts(payer_email);
        CREATE INDEX idx_receipts_receipt_date ON receipts(receipt_date);
        CREATE INDEX idx_receipts_created_at ON receipts(created_at);
        CREATE INDEX idx_receipts_deleted_at ON receipts(deleted_at);
        
        CREATE TRIGGER update_receipts_modified_column
            BEFORE UPDATE ON receipts
            FOR EACH ROW
            EXECUTE FUNCTION update_modified_column();
            
        RAISE NOTICE 'Created receipts table with deleted_at column';
    ELSE
        -- Table exists, just add the column if missing
        ALTER TABLE receipts ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITH TIME ZONE;
        CREATE INDEX IF NOT EXISTS idx_receipts_deleted_at ON receipts(deleted_at);
        RAISE NOTICE 'Added deleted_at column to existing receipts table';
    END IF;
END $$;
