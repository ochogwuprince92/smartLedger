-- Create admin user
INSERT INTO users (
  id, 
  username, 
  email, 
  password, 
  first_name, 
  last_name, 
  enabled, 
  account_non_expired, 
  account_non_locked, 
  credentials_non_expired, 
  failed_login_attempts, 
  created_at, 
  created_by, 
  updated_at, 
  updated_by
)
VALUES (
  gen_random_uuid(), 
  'admin', 
  'admin@smartledger.com', 
  'Ogwaaismywife@gmail.com',
  'System', 
  'Administrator', 
  true, 
  true, 
  true, 
  true, 
  0, 
  NOW(), 
  'SYSTEM', 
  NOW(), 
  'SYSTEM'
);

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u 
CROSS JOIN roles r 
WHERE u.username = 'admin' 
AND r.code = 'ADMIN';
