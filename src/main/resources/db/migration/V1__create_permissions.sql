-- User permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'USER:CREATE', 'Create User', 'Create new users', 'user', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'USER:READ', 'Read User', 'Read user information', 'user', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'USER:UPDATE', 'Update User', 'Update user information', 'user', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'USER:DELETE', 'Delete User', 'Delete users', 'user', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'USER:ASSIGN_ROLE', 'Assign Role to User', 'Assign roles to users', 'user', 'assign_role', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'USER:ASSIGN_PERMISSION', 'Assign Permission to User', 'Assign permissions to users', 'user', 'assign_permission', NOW(), 'SYSTEM', NOW(), 'SYSTEM');

-- Role permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'ROLE:CREATE', 'Create Role', 'Create new roles', 'role', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'ROLE:READ', 'Read Role', 'Read role information', 'role', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'ROLE:UPDATE', 'Update Role', 'Update role information', 'role', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'ROLE:DELETE', 'Delete Role', 'Delete roles', 'role', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'ROLE:ASSIGN_PERMISSION', 'Assign Permission to Role', 'Assign permissions to roles', 'role', 'assign_permission', NOW(), 'SYSTEM', NOW(), 'SYSTEM');

-- Permission permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'PERMISSION:CREATE', 'Create Permission', 'Create new permissions', 'permission', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'PERMISSION:READ', 'Read Permission', 'Read permission information', 'permission', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'PERMISSION:UPDATE', 'Update Permission', 'Update permission information', 'permission', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'PERMISSION:DELETE', 'Delete Permission', 'Delete permissions', 'permission', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM');

-- Payment permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'PAYMENT:CREATE', 'Create Payment', 'Create new payments', 'payment', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'PAYMENT:READ', 'Read Payment', 'Read payment information', 'payment', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'PAYMENT:UPDATE', 'Update Payment', 'Update payment information', 'payment', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'PAYMENT:DELETE', 'Delete Payment', 'Delete payments', 'payment', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM');

-- Ledger permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'LEDGER:READ', 'Read Ledger', 'Read ledger information', 'ledger', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'LEDGER:UPDATE', 'Update Ledger', 'Update ledger information', 'ledger', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM');

-- Journal permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'JOURNAL:CREATE', 'Create Journal Entry', 'Create journal entries', 'journal', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'JOURNAL:READ', 'Read Journal Entry', 'Read journal entries', 'journal', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM');

-- Reconciliation permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'RECONCILIATION:EXECUTE', 'Execute Reconciliation', 'Execute reconciliation processes', 'reconciliation', 'execute', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'RECONCILIATION:READ', 'Read Reconciliation', 'Read reconciliation information', 'reconciliation', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM');

-- Report permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'REPORT:GENERATE', 'Generate Report', 'Generate reports', 'report', 'generate', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'REPORT:READ', 'Read Report', 'Read reports', 'report', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM');
