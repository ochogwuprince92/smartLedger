-- Reset admin user email and password
-- This migration updates the admin user's email to admin.smartledger@gmail.com 
-- and resets the password to admin@me
-- BCrypt hash for 'admin@me' (cost factor 10)

UPDATE users 
SET 
    email = 'admin.smartledger@gmail.com',
    password = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
    must_change_password = true,
    enabled = true,
    account_non_locked = true,
    credentials_non_expired = true,
    account_non_expired = true,
    updated_at = NOW(),
    updated_by = 'MIGRATION'
WHERE username = 'admin';