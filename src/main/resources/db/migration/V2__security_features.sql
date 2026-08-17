-- Security features migration
-- This migration adds service credentials for API key authentication and related permissions

-- Create service_credentials table for API key authentication
CREATE TABLE IF NOT EXISTS service_credentials (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    hashed_api_key VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

-- Create service_credential_permissions table for storing granted permissions
CREATE TABLE IF NOT EXISTS service_credential_permissions (
    credential_id UUID NOT NULL,
    permission_code VARCHAR(255) NOT NULL,
    PRIMARY KEY (credential_id, permission_code),
    FOREIGN KEY (credential_id) REFERENCES service_credentials(id) ON DELETE CASCADE
);

-- Create index on name for faster lookups
CREATE INDEX IF NOT EXISTS idx_service_credentials_name ON service_credentials(name);
CREATE INDEX IF NOT EXISTS idx_service_credentials_enabled ON service_credentials(enabled);

-- Create index on deleted_at for soft deletes
CREATE INDEX IF NOT EXISTS idx_service_credentials_deleted_at ON service_credentials(deleted_at);

-- Add SERVICE_CREDENTIAL:MANAGE permission
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
SELECT
  gen_random_uuid(),
  'SERVICE_CREDENTIAL:MANAGE',
  'Manage Service Credentials',
  'Ability to create, list, and disable service credentials for API key authentication',
  'SERVICE_CREDENTIAL',
  'MANAGE',
  NOW(),
  'SYSTEM',
  NOW(),
  'SYSTEM'
WHERE NOT EXISTS (
  SELECT 1 FROM permissions WHERE code = 'SERVICE_CREDENTIAL:MANAGE'
);

-- Assign SERVICE_CREDENTIAL:MANAGE to ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ADMIN'
AND p.code = 'SERVICE_CREDENTIAL:MANAGE'
AND NOT EXISTS (
  SELECT 1 FROM role_permissions rp
  JOIN roles r2 ON rp.role_id = r2.id
  JOIN permissions p2 ON rp.permission_id = p2.id
  WHERE r2.code = 'ADMIN' AND p2.code = 'SERVICE_CREDENTIAL:MANAGE'
);

-- Add missing reconciliation permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'RECONCILIATION:CREATE', 'Create Reconciliation', 'Create new reconciliations', 'reconciliation', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'RECONCILIATION:UPDATE', 'Update Reconciliation', 'Update reconciliation records', 'reconciliation', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'RECONCILIATION:DELETE', 'Delete Reconciliation', 'Delete reconciliations', 'reconciliation', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- Grant all reconciliation permissions to ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r 
CROSS JOIN permissions p 
WHERE r.code = 'ADMIN' 
AND p.code IN (
  'RECONCILIATION:CREATE', 
  'RECONCILIATION:UPDATE', 
  'RECONCILIATION:DELETE',
  'RECONCILIATION:EXECUTE',
  'RECONCILIATION:READ'
)
ON CONFLICT (role_id, permission_id) DO NOTHING;
