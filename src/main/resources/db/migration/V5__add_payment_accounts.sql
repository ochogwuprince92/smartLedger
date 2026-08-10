-- Add payment-related accounts to the ledger
-- This migration creates cash accounts for Paystack payment methods and accounts receivable

-- Paystack Cash Account
INSERT INTO accounts (id, account_number, account_code, account_name, account_type, current_balance, current_balance_currency, debit_balance, debit_balance_currency, credit_balance, credit_balance_currency, balance_last_updated, description, is_active, created_at, created_by, updated_at, updated_by)
VALUES (
    gen_random_uuid(),
    '1001',
    'CASH_PS',
    'Paystack Cash Account',
    'ASSET',
    0.00,
    'USD',
    0.00,
    'USD',
    0.00,
    'USD',
    CURRENT_TIMESTAMP,
    'Cash account for Paystack payments',
    true,
    CURRENT_TIMESTAMP,
    'SYSTEM',
    CURRENT_TIMESTAMP,
    'SYSTEM'
) ON CONFLICT (account_code) DO NOTHING;

-- Bank Transfer Cash Account
INSERT INTO accounts (id, account_number, account_code, account_name, account_type, current_balance, current_balance_currency, debit_balance, debit_balance_currency, credit_balance, credit_balance_currency, balance_last_updated, description, is_active, created_at, created_by, updated_at, updated_by)
VALUES (
    gen_random_uuid(),
    '1002',
    'CASH_BT',
    'Bank Transfer Cash Account',
    'ASSET',
    0.00,
    'USD',
    0.00,
    'USD',
    0.00,
    'USD',
    CURRENT_TIMESTAMP,
    'Cash account for bank transfer payments',
    true,
    CURRENT_TIMESTAMP,
    'SYSTEM',
    CURRENT_TIMESTAMP,
    'SYSTEM'
) ON CONFLICT (account_code) DO NOTHING;

-- USSD Cash Account
INSERT INTO accounts (id, account_number, account_code, account_name, account_type, current_balance, current_balance_currency, debit_balance, debit_balance_currency, credit_balance, credit_balance_currency, balance_last_updated, description, is_active, created_at, created_by, updated_at, updated_by)
VALUES (
    gen_random_uuid(),
    '1003',
    'CASH_USSD',
    'USSD Cash Account',
    'ASSET',
    0.00,
    'USD',
    0.00,
    'USD',
    0.00,
    'USD',
    CURRENT_TIMESTAMP,
    'Cash account for USSD payments',
    true,
    CURRENT_TIMESTAMP,
    'SYSTEM',
    CURRENT_TIMESTAMP,
    'SYSTEM'
) ON CONFLICT (account_code) DO NOTHING;

-- Card Cash Account
INSERT INTO accounts (id, account_number, account_code, account_name, account_type, current_balance, current_balance_currency, debit_balance, debit_balance_currency, credit_balance, credit_balance_currency, balance_last_updated, description, is_active, created_at, created_by, updated_at, updated_by)
VALUES (
    gen_random_uuid(),
    '1004',
    'CASH_CARD',
    'Card Cash Account',
    'ASSET',
    0.00,
    'USD',
    0.00,
    'USD',
    0.00,
    'USD',
    CURRENT_TIMESTAMP,
    'Cash account for card payments',
    true,
    CURRENT_TIMESTAMP,
    'SYSTEM',
    CURRENT_TIMESTAMP,
    'SYSTEM'
) ON CONFLICT (account_code) DO NOTHING;

-- QR Code Cash Account
INSERT INTO accounts (id, account_number, account_code, account_name, account_type, current_balance, current_balance_currency, debit_balance, debit_balance_currency, credit_balance, credit_balance_currency, balance_last_updated, description, is_active, created_at, created_by, updated_at, updated_by)
VALUES (
    gen_random_uuid(),
    '1005',
    'CASH_QR',
    'QR Code Cash Account',
    'ASSET',
    0.00,
    'USD',
    0.00,
    'USD',
    0.00,
    'USD',
    CURRENT_TIMESTAMP,
    'Cash account for QR code payments',
    true,
    CURRENT_TIMESTAMP,
    'SYSTEM',
    CURRENT_TIMESTAMP,
    'SYSTEM'
) ON CONFLICT (account_code) DO NOTHING;

-- Accounts Receivable Account
INSERT INTO accounts (id, account_number, account_code, account_name, account_type, current_balance, current_balance_currency, debit_balance, debit_balance_currency, credit_balance, credit_balance_currency, balance_last_updated, description, is_active, created_at, created_by, updated_at, updated_by)
VALUES (
    gen_random_uuid(),
    '1200',
    'AR01',
    'Accounts Receivable',
    'ASSET',
    0.00,
    'USD',
    0.00,
    'USD',
    0.00,
    'USD',
    CURRENT_TIMESTAMP,
    'Accounts receivable for general payments',
    true,
    CURRENT_TIMESTAMP,
    'SYSTEM',
    CURRENT_TIMESTAMP,
    'SYSTEM'
) ON CONFLICT (account_code) DO NOTHING;
