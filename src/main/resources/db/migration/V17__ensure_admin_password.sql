-- Ensure admin password is set correctly to "admin"
-- This migration is idempotent and will always set the correct password
-- BCrypt hash for "admin" generated with Spring Security BCryptPasswordEncoder

UPDATE users 
SET password = '$2a$10$zWCJ7LtWBhMlYRQIaopu..QDoaBA9DrW8UXaGKv7/URV3MFdDzhGu',
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM'
WHERE username = 'admin';
