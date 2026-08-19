-- Add ALL base permissions from V1 that are missing
-- This migration ensures the foundational permissions exist in case V1 didn't run properly

-- ============================================
-- USER PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'USER:CREATE', 'Create User', 'Create new users', 'user', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'USER:READ', 'Read User', 'Read user information', 'user', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- ROLE PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'ROLE:CREATE', 'Create Role', 'Create new roles', 'role', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'ROLE:READ', 'Read Role', 'Read role information', 'role', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- PERMISSION PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'PERMISSION:CREATE', 'Create Permission', 'Create new permissions', 'permission', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'PERMISSION:READ', 'Read Permission', 'Read permission information', 'permission', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- PAYMENT PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'PAYMENT:CREATE', 'Create Payment', 'Create new payments', 'payment', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'PAYMENT:READ', 'Read Payment', 'Read payment information', 'payment', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'PAYMENT:UPDATE', 'Update Payment', 'Update payment information', 'payment', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- LEDGER PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'LEDGER:READ', 'Read Ledger', 'Read ledger information', 'ledger', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'LEDGER:UPDATE', 'Update Ledger', 'Update ledger information', 'ledger', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- JOURNAL PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'JOURNAL:CREATE', 'Create Journal Entry', 'Create journal entries', 'journal', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'JOURNAL:READ', 'Read Journal Entry', 'Read journal entries', 'journal', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'JOURNAL:POST', 'Post Journal Entry', 'Post journal entries', 'journal', 'post', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- RECONCILIATION PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'RECONCILIATION:EXECUTE', 'Execute Reconciliation', 'Execute reconciliation processes', 'reconciliation', 'execute', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'RECONCILIATION:READ', 'Read Reconciliation', 'Read reconciliation information', 'reconciliation', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- REPORT PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'REPORT:GENERATE', 'Generate Report', 'Generate reports', 'report', 'generate', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'REPORT:READ', 'Read Report', 'Read reports', 'report', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- RECEIPT PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'RECEIPT:CREATE', 'Create Receipt', 'Create new receipts', 'receipt', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'RECEIPT:READ', 'Read Receipt', 'Read receipt information', 'receipt', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- NOTIFICATION PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'NOTIFICATION:CREATE', 'Create Notification', 'Create new notifications', 'notification', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'NOTIFICATION:READ', 'Read Notification', 'Read notification information', 'notification', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- AUDIT PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'AUDIT:CREATE', 'Create Audit Log', 'Create new audit logs', 'audit', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'AUDIT:READ', 'Read Audit Log', 'Read audit log information', 'audit', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- FEE PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'FEE:CREATE', 'Create Fee', 'Create fee schedules and invoices', 'fee', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'FEE:READ', 'Read Fee', 'Read fee schedules and invoices', 'fee', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- AI PERMISSIONS
-- ============================================
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'AI:READ', 'Read AI Insights', 'Read AI-powered insights and analytics', 'ai', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- GRANT ALL PERMISSIONS TO ADMIN ROLE
-- ============================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r 
CROSS JOIN permissions p 
WHERE r.code = 'ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

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
