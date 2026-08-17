-- Add SERVICE_CREDENTIAL:MANAGE permission
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

-- Assign SERVICE_CREDENTIAL:MANAGE to ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ADMIN'
AND p.code = 'SERVICE_CREDENTIAL:MANAGE'
AND NOT EXISTS (
  SELECT 1 FROM role_permissions rp
  JOIN roles r2 ON rp.role_id = r2.id
  JOIN permissions p2 ON rp.permission_id = p2.id
  WHERE r2.code = 'ADMIN' AND p2.code = 'SERVICE_CREDENTIAL:MANAGE'
);
