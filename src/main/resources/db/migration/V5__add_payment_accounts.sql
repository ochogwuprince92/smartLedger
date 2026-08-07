-- Add payment-related accounts to the ledger
-- This migration creates cash accounts for different payment methods and accounts receivable

-- Credit Card Cash Account
INSERT INTO accounts (id, account_number, account_code, account_name, account_type, description, is_active, created_at, created_by, updated_at, updated_by)
VALUES (
    gen_random_uuid(),
    '1001',
    'CASH_CC',
    'Credit Card Cash Account',
    'ASSET',
    'Cash account for credit card payments',
    true,
    CURRENT_TIMESTAMP,
    'SYSTEM',
    CURRENT_TIMESTAMP,
    'SYSTEM'
) ON CONFLICT (account_code) DO NOTHING;

-- Debit Card Cash Account
INSERT INTO accounts (id, account_number, account_code, account_name, account_type, description, is_active, created_at, created_by, updated_at, updated_by)
VALUES (
    gen_random_uuid(),
    '1002',
    'CASH_DC',
    'Debit Card Cash Account',
    'ASSET',
    'Cash account for debit card payments',
    true,
    CURRENT_TIMESTAMP,
    'SYSTEM',
    CURRENT_TIMESTAMP,
    'SYSTEM'
) ON CONFLICT (account_code) DO NOTHING;

-- Bank Transfer Cash Account
INSERT INTO accounts (id, account_number, account_code, account_name, account_type, description, is_active, created_at, created_by, updated_at, updated_by)
VALUES (
    gen_random_uuid(),
    '1003',
    'CASH_BT',
    'Bank Transfer Cash Account',
    'ASSET',
    'Cash account for bank transfer payments',
    true,
    CURRENT_TIMESTAMP,
    'SYSTEM',
    CURRENT_TIMESTAMP,
    'SYSTEM'
) ON CONFLICT (account_code) DO NOTHING;

-- Mobile Money Cash Account
INSERT INTO accounts (id, account_number, account_code, account_name, account_type, description, is_active, created_at, created_by, updated_at, updated_by)
VALUES (
    gen_random_uuid(),
    '1004',
    'CASH_MM',
    'Mobile Money Cash Account',
    'ASSET',
    'Cash account for mobile money payments',
    true,
    CURRENT_TIMESTAMP,
    'SYSTEM',
    CURRENT_TIMESTAMP,
    'SYSTEM'
) ON CONFLICT (account_code) DO NOTHING;

-- Cryptocurrency Cash Account
INSERT INTO accounts (id, account_number, account_code, account_name, account_type, description, is_active, created_at, created_by, updated_at, updated_by)
VALUES (
    gen_random_uuid(),
    '1005',
    'CASH_CR',
    'Cryptocurrency Cash Account',
    'ASSET',
    'Cash account for cryptocurrency payments',
    true,
    CURRENT_TIMESTAMP,
    'SYSTEM',
    CURRENT_TIMESTAMP,
    'SYSTEM'
) ON CONFLICT (account_code) DO NOTHING;

-- Check Cash Account
INSERT INTO accounts (id, account_number, account_code, account_name, account_type, description, is_active, created_at, created_by, updated_at, updated_by)
VALUES (
    gen_random_uuid(),
    '1006',
    'CASH_CH',
    'Check Cash Account',
    'ASSET',
    'Cash account for check payments',
    true,
    CURRENT_TIMESTAMP,
    'SYSTEM',
    CURRENT_TIMESTAMP,
    'SYSTEM'
) ON CONFLICT (account_code) DO NOTHING;

-- Physical Cash Account
INSERT INTO accounts (id, account_number, account_code, account_name, account_type, description, is_active, created_at, created_by, updated_at, updated_by)
VALUES (
    gen_random_uuid(),
    '1007',
    'CASH_CP',
    'Physical Cash Account',
    'ASSET',
    'Cash account for physical cash payments',
    true,
    CURRENT_TIMESTAMP,
    'SYSTEM',
    CURRENT_TIMESTAMP,
    'SYSTEM'
) ON CONFLICT (account_code) DO NOTHING;

-- Accounts Receivable Account
INSERT INTO accounts (id, account_number, account_code, account_name, account_type, description, is_active, created_at, created_by, updated_at, updated_by)
VALUES (
    gen_random_uuid(),
    '1200',
    'AR01',
    'Accounts Receivable',
    'ASSET',
    'Accounts receivable for general payments',
    true,
    CURRENT_TIMESTAMP,
    'SYSTEM',
    CURRENT_TIMESTAMP,
    'SYSTEM'
) ON CONFLICT (account_code) DO NOTHING;
