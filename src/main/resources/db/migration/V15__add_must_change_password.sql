-- Add must_change_password column if it doesn't exist (for backwards compatibility)
DO $$
BEGIN
    -- Check if column exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'must_change_password'
    ) THEN
        -- Add column as nullable first
        ALTER TABLE users ADD COLUMN must_change_password BOOLEAN DEFAULT FALSE;
        
        -- Update existing rows to set default value (in case any NULLs exist)
        UPDATE users SET must_change_password = FALSE WHERE must_change_password IS NULL;
        
        -- Now add the NOT NULL constraint
        ALTER TABLE users ALTER COLUMN must_change_password SET NOT NULL;
        
        -- Set default value for future inserts
        ALTER TABLE users ALTER COLUMN must_change_password SET DEFAULT FALSE;
        
        RAISE NOTICE 'Added must_change_password column to users table';
    ELSE
        -- Column exists, check if it has NOT NULL constraint
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'users' 
            AND column_name = 'must_change_password' 
            AND is_nullable = 'NO'
        ) THEN
            -- Add NOT NULL constraint
            UPDATE users SET must_change_password = FALSE WHERE must_change_password IS NULL;
            ALTER TABLE users ALTER COLUMN must_change_password SET NOT NULL;
            ALTER TABLE users ALTER COLUMN must_change_password SET DEFAULT FALSE;
            RAISE NOTICE 'Added NOT NULL constraint to must_change_password column';
        END IF;
    END IF;
END $$;

-- Add USER:RESET_PASSWORD permission (idempotent)
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
SELECT
  uuid_generate_v4(),
  'USER:RESET_PASSWORD',
  'Reset User Password',
  'Reset user password (admin recovery)',
  'user',
  'reset_password',
  CURRENT_TIMESTAMP,
  'system',
  CURRENT_TIMESTAMP,
  'system'
WHERE NOT EXISTS (
  SELECT 1 FROM permissions WHERE code = 'USER:RESET_PASSWORD'
);

-- Grant USER:RESET_PASSWORD to ADMIN role (idempotent)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ADMIN'
AND p.code = 'USER:RESET_PASSWORD'
AND NOT EXISTS (
  SELECT 1 FROM role_permissions rp
  JOIN roles r2 ON rp.role_id = r2.id
  JOIN permissions p2 ON rp.permission_id = p2.id
  WHERE r2.code = 'ADMIN' AND p2.code = 'USER:RESET_PASSWORD'
);
