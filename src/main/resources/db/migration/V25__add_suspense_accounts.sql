-- Add suspense_accounts table for holding unallocated funds

CREATE TABLE IF NOT EXISTS suspense_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_code VARCHAR(20) NOT NULL UNIQUE,
    account_name VARCHAR(100) NOT NULL,
    description TEXT,
    current_balance DECIMAL(19, 4) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_reconciled_at TIMESTAMP WITH TIME ZONE,
    requires_review BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL
);

-- Create indexes
CREATE INDEX idx_suspense_accounts_account_code ON suspense_accounts(account_code);
CREATE INDEX idx_suspense_accounts_status ON suspense_accounts(status);
CREATE INDEX idx_suspense_accounts_requires_review ON suspense_accounts(requires_review);

-- Create trigger for automatic updated_at timestamp
CREATE TRIGGER update_suspense_accounts_modified_column
    BEFORE UPDATE ON suspense_accounts
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- Add suspense account permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'SUSPENSE:CREATE', 'Create Suspense Account', 'Create new suspense accounts', 'suspense', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'SUSPENSE:READ', 'Read Suspense Account', 'Read suspense account information', 'suspense', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'SUSPENSE:UPDATE', 'Update Suspense Account', 'Update suspense account information', 'suspense', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'SUSPENSE:DELETE', 'Delete Suspense Account', 'Delete suspense accounts', 'suspense', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'SUSPENSE:RECONCILE', 'Reconcile Suspense Account', 'Reconcile suspense accounts', 'suspense', 'reconcile', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;
