-- Add missing reconciliation permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'RECONCILIATION:CREATE', 'Create Reconciliation', 'Create new reconciliations', 'reconciliation', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'RECONCILIATION:UPDATE', 'Update Reconciliation', 'Update reconciliation records', 'reconciliation', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'RECONCILIATION:DELETE', 'Delete Reconciliation', 'Delete reconciliations', 'reconciliation', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- Grant all reconciliation permissions to ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r 
CROSS JOIN permissions p 
WHERE r.code = 'ADMIN' 
AND p.code IN (
  'RECONCILIATION:CREATE', 
  'RECONCILIATION:UPDATE', 
  'RECONCILIATION:DELETE',
  'RECONCILIATION:EXECUTE',
  'RECONCILIATION:READ'
)
ON CONFLICT (role_id, permission_id) DO NOTHING;
