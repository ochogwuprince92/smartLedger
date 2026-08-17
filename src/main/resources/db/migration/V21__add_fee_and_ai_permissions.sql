-- Add FEE permissions and AI:READ permission
-- This migration adds missing FEE permissions used by FeeController and AI:READ permission for AI insights page

-- Add FEE permissions (matching FeeController @PreAuthorize annotations)
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'FEE:CREATE', 'Create Fee', 'Create fee schedules and invoices', 'fee', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'FEE:READ', 'Read Fee', 'Read fee schedules and invoices', 'fee', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'FEE:UPDATE', 'Update Fee', 'Update fee schedules and invoices', 'fee', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'FEE:DELETE', 'Delete Fee', 'Delete fee schedules and invoices', 'fee', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- Add AI:READ permission for AI insights page
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'AI:READ', 'Read AI Insights', 'Read AI-powered insights and analytics', 'ai', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- Grant FEE permissions to ADMIN (all permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r 
CROSS JOIN permissions p 
WHERE r.code = 'ADMIN'
AND p.code IN ('FEE:CREATE', 'FEE:READ', 'FEE:UPDATE', 'FEE:DELETE', 'AI:READ')
AND NOT EXISTS (
  SELECT 1 FROM role_permissions rp
  JOIN roles r2 ON rp.role_id = r2.id
  JOIN permissions p2 ON rp.permission_id = p2.id
  WHERE r2.code = 'ADMIN' AND p2.code IN ('FEE:CREATE', 'FEE:READ', 'FEE:UPDATE', 'FEE:DELETE', 'AI:READ')
);

-- Grant FEE permissions to ACCOUNTANT (financial operations)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r 
CROSS JOIN permissions p 
WHERE r.code = 'ACCOUNTANT'
AND p.code IN ('FEE:CREATE', 'FEE:READ', 'FEE:UPDATE')
AND NOT EXISTS (
  SELECT 1 FROM role_permissions rp
  JOIN roles r2 ON rp.role_id = r2.id
  JOIN permissions p2 ON rp.permission_id = p2.id
  WHERE r2.code = 'ACCOUNTANT' AND p2.code IN ('FEE:CREATE', 'FEE:READ', 'FEE:UPDATE')
);

-- Grant FEE:READ and AI:READ to ACCOUNTANT
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r 
CROSS JOIN permissions p 
WHERE r.code = 'ACCOUNTANT'
AND p.code IN ('FEE:READ', 'AI:READ')
AND NOT EXISTS (
  SELECT 1 FROM role_permissions rp
  JOIN roles r2 ON rp.role_id = r2.id
  JOIN permissions p2 ON rp.permission_id = p2.id
  WHERE r2.code = 'ACCOUNTANT' AND p2.code IN ('FEE:READ', 'AI:READ')
);

-- Grant FEE:READ and AI:READ to AUDITOR (read-only)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r 
CROSS JOIN permissions p 
WHERE r.code = 'AUDITOR'
AND p.code IN ('FEE:READ', 'AI:READ')
AND NOT EXISTS (
  SELECT 1 FROM role_permissions rp
  JOIN roles r2 ON rp.role_id = r2.id
  JOIN permissions p2 ON rp.permission_id = p2.id
  WHERE r2.code = 'AUDITOR' AND p2.code IN ('FEE:READ', 'AI:READ')
);

-- Grant FEE:READ and AI:READ to USER (basic access)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r 
CROSS JOIN permissions p 
WHERE r.code = 'USER'
AND p.code IN ('FEE:READ', 'AI:READ')
AND NOT EXISTS (
  SELECT 1 FROM role_permissions rp
  JOIN roles r2 ON rp.role_id = r2.id
  JOIN permissions p2 ON rp.permission_id = p2.id
  WHERE r2.code = 'USER' AND p2.code IN ('FEE:READ', 'AI:READ')
);
