-- Advanced features migration
-- This migration adds journal entries, reconciliation, suspense accounts, and reports tables

-- ============================================
-- JOURNAL ENTRIES AND LINE ITEMS
-- ============================================

CREATE TABLE IF NOT EXISTS journal_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entry_number VARCHAR(50) NOT NULL UNIQUE,
    entry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    entry_type VARCHAR(20) NOT NULL,
    reference_number VARCHAR(100),
    description VARCHAR(500) NOT NULL,
    posted BOOLEAN NOT NULL DEFAULT FALSE,
    posted_date TIMESTAMP WITH TIME ZONE,
    posted_by VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS journal_line_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    journal_entry_id UUID NOT NULL,
    account_id UUID NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    debit_credit VARCHAR(10) NOT NULL,
    amount_amount DECIMAL(19, 4) NOT NULL,
    amount_currency_code VARCHAR(3) NOT NULL,
    description VARCHAR(255),
    sequence_number INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    CONSTRAINT fk_journal_line_items_journal_entry FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_journal_entries_entry_number ON journal_entries(entry_number);
CREATE INDEX idx_journal_entries_entry_date ON journal_entries(entry_date);
CREATE INDEX idx_journal_entries_entry_type ON journal_entries(entry_type);
CREATE INDEX idx_journal_entries_posted ON journal_entries(posted);
CREATE INDEX idx_journal_line_items_journal_entry_id ON journal_line_items(journal_entry_id);
CREATE INDEX idx_journal_line_items_account_id ON journal_line_items(account_id);
CREATE INDEX idx_journal_line_items_account_number ON journal_line_items(account_number);

-- Create trigger for automatic updated_at timestamp
CREATE TRIGGER update_journal_entries_modified_column
    BEFORE UPDATE ON journal_entries
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_journal_line_items_modified_column
    BEFORE UPDATE ON journal_line_items
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- Add journal permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'JOURNAL:CREATE', 'Create Journal Entry', 'Create new journal entries', 'journal', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'JOURNAL:READ', 'Read Journal Entry', 'Read journal entry information', 'journal', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'JOURNAL:UPDATE', 'Update Journal Entry', 'Update journal entry information', 'journal', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'JOURNAL:DELETE', 'Delete Journal Entry', 'Delete journal entries', 'journal', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'JOURNAL:POST', 'Post Journal Entry', 'Post journal entries', 'journal', 'post', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- SUSPENSE ACCOUNTS
-- ============================================

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

-- ============================================
-- RECONCILIATION TABLES
-- ============================================

CREATE TABLE IF NOT EXISTS reconciliations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reconciliation_number VARCHAR(50) NOT NULL UNIQUE,
    reconciliation_date TIMESTAMP WITH TIME ZONE NOT NULL,
    source_system VARCHAR(50) NOT NULL,
    source_reference VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    total_expected_amount DECIMAL(19, 4),
    total_actual_amount DECIMAL(19, 4),
    variance_amount DECIMAL(19, 4),
    suspense_account_id UUID,
    description VARCHAR(500),
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS reconciliation_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reconciliation_id UUID NOT NULL,
    item_reference VARCHAR(100) NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    expected_amount DECIMAL(19, 4) NOT NULL,
    actual_amount DECIMAL(19, 4),
    variance_amount DECIMAL(19, 4),
    match_status VARCHAR(20),
    matched_transaction_id UUID,
    matched_at TIMESTAMP WITH TIME ZONE,
    description VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    CONSTRAINT fk_reconciliation_items_reconciliation FOREIGN KEY (reconciliation_id) REFERENCES reconciliations(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_reconciliations_reconciliation_number ON reconciliations(reconciliation_number);
CREATE INDEX idx_reconciliations_reconciliation_date ON reconciliations(reconciliation_date);
CREATE INDEX idx_reconciliations_status ON reconciliations(status);
CREATE INDEX idx_reconciliations_source_system ON reconciliations(source_system);
CREATE INDEX idx_reconciliation_items_reconciliation_id ON reconciliation_items(reconciliation_id);
CREATE INDEX idx_reconciliation_items_item_reference ON reconciliation_items(item_reference);
CREATE INDEX idx_reconciliation_items_match_status ON reconciliation_items(match_status);
CREATE INDEX idx_reconciliation_items_matched_transaction_id ON reconciliation_items(matched_transaction_id);

-- Create trigger for automatic updated_at timestamp
CREATE TRIGGER update_reconciliations_modified_column
    BEFORE UPDATE ON reconciliations
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_reconciliation_items_modified_column
    BEFORE UPDATE ON reconciliation_items
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- ============================================
-- REPORTS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_number VARCHAR(50) NOT NULL UNIQUE,
    report_date TIMESTAMP WITH TIME ZONE NOT NULL,
    report_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    period_start_date TIMESTAMP WITH TIME ZONE,
    period_end_date TIMESTAMP WITH TIME ZONE,
    currency_code VARCHAR(3) NOT NULL,
    generated_at TIMESTAMP WITH TIME ZONE,
    file_path VARCHAR(500),
    error_message VARCHAR(1000),
    description VARCHAR(500),
    report_data TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL
);

-- Create indexes
CREATE INDEX idx_reports_report_number ON reports(report_number);
CREATE INDEX idx_reports_report_date ON reports(report_date);
CREATE INDEX idx_reports_report_type ON reports(report_type);
CREATE INDEX idx_reports_status ON reports(status);
CREATE INDEX idx_reports_period ON reports(period_start_date, period_end_date);
CREATE INDEX idx_reports_deleted_at ON reports(deleted_at);

-- Create trigger for automatic updated_at timestamp
CREATE TRIGGER update_reports_modified_column
    BEFORE UPDATE ON reports
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();
