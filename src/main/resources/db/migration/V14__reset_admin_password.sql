-- Reset admin password to known valid BCrypt hash
-- This migration ensures the admin password is set to a valid BCrypt hash
-- Password: admin123
UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
WHERE username = 'admin';
