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
