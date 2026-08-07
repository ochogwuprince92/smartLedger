-- Add transactions table for ledger module
-- This migration adds the transactions table for recording financial transactions

CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency_code VARCHAR(3) DEFAULT 'USD' NOT NULL,
    debit_account_id UUID NOT NULL,
    credit_account_id UUID NOT NULL,
    reference_number VARCHAR(100),
    transaction_date TIMESTAMP WITH TIME ZONE NOT NULL,
    is_posted BOOLEAN DEFAULT false NOT NULL,
    posted_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_transactions_debit_account FOREIGN KEY (debit_account_id) REFERENCES accounts(id) ON DELETE RESTRICT,
    CONSTRAINT fk_transactions_credit_account FOREIGN KEY (credit_account_id) REFERENCES accounts(id) ON DELETE RESTRICT
);

CREATE INDEX idx_transaction_type ON transactions(transaction_type);
CREATE INDEX idx_transaction_date ON transactions(transaction_date);
CREATE INDEX idx_reference_number ON transactions(reference_number);
CREATE INDEX idx_debit_account_id ON transactions(debit_account_id);
CREATE INDEX idx_credit_account_id ON transactions(credit_account_id);

-- Add trigger for automatic timestamp updates
CREATE TRIGGER update_transactions_modtime
    BEFORE UPDATE ON transactions
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();
