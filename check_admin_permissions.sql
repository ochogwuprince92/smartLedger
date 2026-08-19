-- Check what permissions the admin user actually has
-- Run this in your production database

-- 1. Get admin user ID
SELECT id, username, enabled FROM users WHERE username = 'admin';

-- 2. Get admin's roles
SELECT u.username, r.code as role_code, r.name as role_name
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'admin';

-- 3. Get all permissions assigned to admin's roles
SELECT r.code as role_code, p.code as permission_code, p.name as permission_name
FROM roles r
JOIN role_permissions rp ON r.id = rp.role_id
JOIN permissions p ON rp.permission_id = p.id
WHERE r.code = 'ADMIN'
ORDER BY p.code;

-- 4. Get total count of permissions in database
SELECT COUNT(*) as total_permissions FROM permissions;

-- 5. Get total count of permissions assigned to ADMIN role
SELECT COUNT(*) as admin_permissions 
FROM role_permissions 
WHERE role_id = (SELECT id FROM roles WHERE code = 'ADMIN');

-- 6. Check if USER:CREATE permission exists and is assigned to ADMIN
SELECT 
  (SELECT COUNT(*) FROM permissions WHERE code = 'USER:CREATE') as permission_exists,
  (SELECT COUNT(*) FROM role_permissions rp 
   JOIN roles r ON rp.role_id = r.id 
   JOIN permissions p ON rp.permission_id = p.id 
   WHERE r.code = 'ADMIN' AND p.code = 'USER:CREATE') as assigned_to_admin;
