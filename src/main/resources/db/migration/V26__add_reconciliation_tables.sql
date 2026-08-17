-- Add reconciliations and reconciliation_items tables for reconciliation process

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
