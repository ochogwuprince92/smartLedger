-- Add missing permissions to match controller requirements
-- This migration ensures all permissions required by controllers are available in the database

-- ============================================
-- LEDGER PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'LEDGER:CREATE', 'Create Ledger Account', 'Create new ledger accounts', 'ledger', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'LEDGER:DELETE', 'Delete Ledger Account', 'Delete ledger accounts', 'ledger', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- JOURNAL PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'JOURNAL:DELETE', 'Delete Journal Entry', 'Delete journal entries', 'journal', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- PAYMENT PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'PAYMENT:DELETE', 'Delete Payment', 'Delete payments', 'payment', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- FEE PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'FEE:UPDATE', 'Update Fee', 'Update fee schedules and invoices', 'fee', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'FEE:DELETE', 'Delete Fee', 'Delete fee schedules and invoices', 'fee', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- RECEIPT PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'RECEIPT:UPDATE', 'Update Receipt', 'Update receipt status', 'receipt', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'RECEIPT:DELETE', 'Delete Receipt', 'Delete receipts', 'receipt', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- USER PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'USER:UPDATE', 'Update User', 'Update user information', 'user', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'USER:DELETE', 'Delete User', 'Delete users', 'user', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'USER:RESET_PASSWORD', 'Reset User Password', 'Reset user passwords (admin function)', 'user', 'reset_password', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'USER:ASSIGN_ROLE', 'Assign Role to User', 'Assign roles to users', 'user', 'assign_role', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'USER:ASSIGN_PERMISSION', 'Assign Permission to User', 'Assign permissions to users', 'user', 'assign_permission', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- ROLE PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'ROLE:UPDATE', 'Update Role', 'Update role information', 'role', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'ROLE:DELETE', 'Delete Role', 'Delete roles', 'role', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'ROLE:ASSIGN_PERMISSION', 'Assign Permission to Role', 'Assign permissions to roles', 'role', 'assign_permission', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'ROLE:MANAGE_HIERARCHY', 'Manage Role Hierarchy', 'Manage parent-child role relationships', 'role', 'manage_hierarchy', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- PERMISSION PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'PERMISSION:DELETE', 'Delete Permission', 'Delete permissions', 'permission', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- AI INSIGHT PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'AI_INSIGHT:READ', 'Read AI Insights', 'Read AI-powered insights and analytics', 'ai_insight', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'AI_INSIGHT:RETRY', 'Retry AI Insights', 'Retry failed AI insights', 'ai_insight', 'retry', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- SERVICE CREDENTIAL PERMISSIONS
-- ============================================
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

-- ============================================
-- ASSIGN ALL NEW PERMISSIONS TO ADMIN ROLE
-- ============================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r 
CROSS JOIN permissions p 
WHERE r.code = 'ADMIN'
AND p.code IN (
  'LEDGER:CREATE',
  'LEDGER:DELETE',
  'JOURNAL:DELETE',
  'PAYMENT:DELETE',
  'FEE:UPDATE',
  'FEE:DELETE',
  'RECEIPT:UPDATE',
  'RECEIPT:DELETE',
  'USER:UPDATE',
  'USER:DELETE',
  'USER:RESET_PASSWORD',
  'USER:ASSIGN_ROLE',
  'USER:ASSIGN_PERMISSION',
  'ROLE:UPDATE',
  'ROLE:DELETE',
  'ROLE:ASSIGN_PERMISSION',
  'ROLE:MANAGE_HIERARCHY',
  'PERMISSION:DELETE',
  'AI_INSIGHT:READ',
  'AI_INSIGHT:RETRY',
  'SERVICE_CREDENTIAL:MANAGE'
)
AND NOT EXISTS (
  SELECT 1 FROM role_permissions rp
  WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- ============================================
-- ENSURE ADMIN USER HAS ADMIN ROLE
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
