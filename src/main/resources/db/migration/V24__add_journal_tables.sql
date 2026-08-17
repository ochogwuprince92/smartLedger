-- Add journal_entries and journal_line_items tables for double-entry accounting

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
