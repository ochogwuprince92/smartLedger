-- Create roles
INSERT INTO roles (id, code, name, description, level, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'ADMIN', 'Administrator', 'Full system access', 0, NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'ACCOUNTANT', 'Accountant', 'Financial operations access', 0, NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'AUDITOR', 'Auditor', 'Read-only access to financial data', 0, NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'USER', 'User', 'Basic user access', 0, NOW(), 'SYSTEM', NOW(), 'SYSTEM');

-- Grant all permissions to ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r 
CROSS JOIN permissions p 
WHERE r.code = 'ADMIN';

-- Grant financial permissions to ACCOUNTANT
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r 
CROSS JOIN permissions p 
WHERE r.code = 'ACCOUNTANT' 
AND p.code IN (
  'PAYMENT:CREATE', 'PAYMENT:READ', 'PAYMENT:UPDATE',
  'LEDGER:READ', 'LEDGER:UPDATE',
  'JOURNAL:CREATE', 'JOURNAL:READ',
  'RECONCILIATION:EXECUTE', 'RECONCILIATION:READ',
  'REPORT:GENERATE', 'REPORT:READ'
);

-- Grant read permissions to AUDITOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r 
CROSS JOIN permissions p 
WHERE r.code = 'AUDITOR' 
AND p.code IN (
  'PAYMENT:READ', 'LEDGER:READ', 'JOURNAL:READ',
  'RECONCILIATION:READ', 'REPORT:READ'
);

-- Grant basic permissions to USER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r 
CROSS JOIN permissions p 
WHERE r.code = 'USER' 
AND p.code IN ('USER:READ', 'PAYMENT:READ', 'REPORT:READ');
