-- Add audit_logs table for audit module
-- This migration creates the audit_logs table for tracking system changes

CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID,
    action VARCHAR(30) NOT NULL,
    description VARCHAR(500),
    old_value TEXT,
    new_value TEXT,
    changed_fields TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    request_id VARCHAR(100),
    session_id VARCHAR(100),
    metadata TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL
);

-- Create indexes for common queries
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_created_by ON audit_logs(created_by);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_logs_ip_address ON audit_logs(ip_address);
CREATE INDEX idx_audit_logs_session_id ON audit_logs(session_id);
CREATE INDEX idx_audit_logs_request_id ON audit_logs(request_id);

-- Create trigger for automatic updated_at timestamp
CREATE TRIGGER update_audit_logs_modified_column
    BEFORE UPDATE ON audit_logs
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- Add audit permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'AUDIT:CREATE', 'Create Audit Log', 'Create new audit logs', 'audit', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'AUDIT:READ', 'Read Audit Log', 'Read audit log information', 'audit', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'AUDIT:UPDATE', 'Update Audit Log', 'Update audit log information', 'audit', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'AUDIT:DELETE', 'Delete Audit Log', 'Delete audit logs', 'audit', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;
