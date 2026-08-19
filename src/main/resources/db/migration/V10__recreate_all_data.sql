-- Recreate ALL roles, admin user, and role permissions
-- This migration fixes the issue where V1 was baselined but never executed,
-- leaving the database without any roles, admin user, or role permissions

-- ============================================
-- CREATE ROLES
-- ============================================
INSERT INTO roles (id, code, name, description, level, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'ADMIN', 'Administrator', 'Full system access', 0, NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'ACCOUNTANT', 'Accountant', 'Financial operations access', 0, NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'AUDITOR', 'Auditor', 'Read-only access to financial data', 0, NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'USER', 'User', 'Basic user access', 0, NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- CREATE ADMIN USER
-- ============================================
INSERT INTO users (
  id,
  username,
  email,
  password,
  first_name,
  enabled,
  account_non_expired,
  account_non_locked,
  credentials_non_expired,
  must_change_password,
  failed_login_attempts,
  created_at,
  created_by,
  updated_at,
  updated_by
)
VALUES (
  gen_random_uuid(),
  'admin',
  'admin.smartledger@gmail.com',
  '$2a$10$zWCJ7LtWBhMlYRQIaopu..QDoaBA9DrW8UXaGKv7/URV3MFdDzhGu',
  'Administrator',
  true,
  true,
  true,
  true,
  false,
  0,
  NOW(),
  'SYSTEM',
  NOW(),
  'SYSTEM'
) ON CONFLICT (username) DO NOTHING;

-- ============================================
-- ASSIGN ADMIN ROLE TO ADMIN USER
-- ============================================
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u 
CROSS JOIN roles r 
WHERE u.username = 'admin' 
AND r.code = 'ADMIN'
AND NOT EXISTS (
  SELECT 1 FROM user_roles ur
  WHERE ur.user_id = u.id AND ur.role_id = r.id
);

-- ============================================
-- GRANT ALL PERMISSIONS TO ADMIN
-- ============================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r 
CROSS JOIN permissions p 
WHERE r.code = 'ADMIN'
AND NOT EXISTS (
  SELECT 1 FROM role_permissions rp
  WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
