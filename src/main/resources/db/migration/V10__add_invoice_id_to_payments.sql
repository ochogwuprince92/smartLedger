-- Add invoice_id to payments table for fee payment integration
-- This allows linking gateway payments to fee invoices

ALTER TABLE payments 
ADD COLUMN invoice_id UUID;

-- Create index for invoice_id queries
CREATE INDEX idx_payments_invoice_id ON payments(invoice_id);
