-- Fix admin email to match documentation
UPDATE users
SET email = 'admin.smartledger@gmail.com',
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM'
WHERE username = 'admin';