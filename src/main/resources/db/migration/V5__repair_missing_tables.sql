-- Repair migration for missing tables in production database
-- This migration checks for and creates tables that should exist but are missing
-- This is safe to run as it only creates tables if they don't exist

-- Create accounts table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'accounts') THEN
        CREATE TABLE accounts (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            account_number VARCHAR(20) NOT NULL UNIQUE,
            account_code VARCHAR(10) NOT NULL UNIQUE,
            account_name VARCHAR(100) NOT NULL,
            account_type VARCHAR(20) NOT NULL,
            current_balance DECIMAL(19, 2) NOT NULL,
            current_balance_currency VARCHAR(3) NOT NULL,
            debit_balance DECIMAL(19, 2) NOT NULL,
            debit_balance_currency VARCHAR(3) NOT NULL,
            credit_balance DECIMAL(19, 2) NOT NULL,
            credit_balance_currency VARCHAR(3) NOT NULL,
            balance_last_updated TIMESTAMP,
            description TEXT,
            is_active BOOLEAN NOT NULL DEFAULT true,
            parent_account_id UUID REFERENCES accounts(id),
            created_at TIMESTAMP NOT NULL,
            created_by VARCHAR(100) NOT NULL,
            updated_at TIMESTAMP NOT NULL,
            updated_by VARCHAR(100) NOT NULL,
            deleted_at TIMESTAMP
        );
        
        CREATE INDEX idx_accounts_account_type ON accounts(account_type);
        CREATE INDEX idx_accounts_is_active ON accounts(is_active);
        CREATE INDEX idx_accounts_parent_account ON accounts(parent_account_id);
        
        RAISE NOTICE 'Created accounts table';
    ELSE
        RAISE NOTICE 'accounts table already exists';
    END IF;
END $$;

-- Create account_categories table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'account_categories') THEN
        CREATE TABLE account_categories (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            code VARCHAR(10) UNIQUE NOT NULL,
            name VARCHAR(100) NOT NULL,
            description TEXT,
            normal_balance VARCHAR(10) NOT NULL CHECK (normal_balance IN ('DEBIT', 'CREDIT')),
            parent_category_id UUID REFERENCES account_categories(id),
            display_order INTEGER DEFAULT 0,
            is_active BOOLEAN DEFAULT true NOT NULL,
            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
            updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
            created_by VARCHAR(255) NOT NULL,
            updated_by VARCHAR(255) NOT NULL,
            deleted_at TIMESTAMP WITH TIME ZONE
        );
        
        CREATE INDEX idx_account_categories_code ON account_categories(code);
        CREATE INDEX idx_account_categories_parent ON account_categories(parent_category_id);
        CREATE INDEX idx_account_categories_is_active ON account_categories(is_active);
        
        RAISE NOTICE 'Created account_categories table';
    ELSE
        RAISE NOTICE 'account_categories table already exists';
    END IF;
END $$;

-- Create account_subcategories table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'account_subcategories') THEN
        CREATE TABLE account_subcategories (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            category_id UUID NOT NULL REFERENCES account_categories(id) ON DELETE CASCADE,
            code VARCHAR(20) UNIQUE NOT NULL,
            name VARCHAR(100) NOT NULL,
            description TEXT,
            normal_balance VARCHAR(10) NOT NULL CHECK (normal_balance IN ('DEBIT', 'CREDIT')),
            display_order INTEGER DEFAULT 0,
            is_active BOOLEAN DEFAULT true NOT NULL,
            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
            updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
            created_by VARCHAR(255) NOT NULL,
            updated_by VARCHAR(255) NOT NULL,
            deleted_at TIMESTAMP WITH TIME ZONE
        );
        
        CREATE INDEX idx_account_subcategories_category ON account_subcategories(category_id);
        CREATE INDEX idx_account_subcategories_code ON account_subcategories(code);
        CREATE INDEX idx_account_subcategories_is_active ON account_subcategories(is_active);
        
        RAISE NOTICE 'Created account_subcategories table';
    ELSE
        RAISE NOTICE 'account_subcategories table already exists';
    END IF;
END $$;

-- Create ledger_accounts table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'ledger_accounts') THEN
        CREATE TABLE ledger_accounts (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            subcategory_id UUID REFERENCES account_subcategories(id) ON DELETE SET NULL,
            account_number VARCHAR(20) UNIQUE NOT NULL,
            account_code VARCHAR(10) UNIQUE NOT NULL,
            account_name VARCHAR(100) NOT NULL,
            description TEXT,
            account_type VARCHAR(20) NOT NULL CHECK (account_type IN ('ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE')),
            normal_balance VARCHAR(10) NOT NULL CHECK (normal_balance IN ('DEBIT', 'CREDIT')),
            currency VARCHAR(3) DEFAULT 'USD' NOT NULL,
            opening_balance DECIMAL(19, 2) DEFAULT 0 NOT NULL,
            current_balance DECIMAL(19, 2) DEFAULT 0 NOT NULL,
            parent_account_id UUID REFERENCES ledger_accounts(id) ON DELETE SET NULL,
            account_level INTEGER DEFAULT 0 NOT NULL CHECK (account_level >= 0),
            is_active BOOLEAN DEFAULT true NOT NULL,
            is_control_account BOOLEAN DEFAULT false NOT NULL,
            is_reconcilable BOOLEAN DEFAULT true NOT NULL,
            tax_code VARCHAR(20),
            cost_center VARCHAR(50),
            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
            updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
            created_by VARCHAR(255) NOT NULL,
            updated_by VARCHAR(255) NOT NULL,
            deleted_at TIMESTAMP WITH TIME ZONE
        );
        
        CREATE INDEX idx_ledger_accounts_subcategory ON ledger_accounts(subcategory_id);
        CREATE INDEX idx_ledger_accounts_account_number ON ledger_accounts(account_number);
        CREATE INDEX idx_ledger_accounts_account_code ON ledger_accounts(account_code);
        CREATE INDEX idx_ledger_accounts_account_type ON ledger_accounts(account_type);
        CREATE INDEX idx_ledger_accounts_parent ON ledger_accounts(parent_account_id);
        CREATE INDEX idx_ledger_accounts_is_active ON ledger_accounts(is_active);
        CREATE INDEX idx_ledger_accounts_level ON ledger_accounts(account_level);
        
        RAISE NOTICE 'Created ledger_accounts table';
    ELSE
        RAISE NOTICE 'ledger_accounts table already exists';
    END IF;
END $$;

-- Create account_relationships table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'account_relationships') THEN
        CREATE TABLE account_relationships (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            parent_account_id UUID NOT NULL REFERENCES ledger_accounts(id) ON DELETE CASCADE,
            child_account_id UUID NOT NULL REFERENCES ledger_accounts(id) ON DELETE CASCADE,
            relationship_type VARCHAR(20) NOT NULL CHECK (relationship_type IN ('HIERARCHY', 'CONSOLIDATION', 'ALLOCATION')),
            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
            created_by VARCHAR(255) NOT NULL,
            UNIQUE(parent_account_id, child_account_id, relationship_type)
        );
        
        CREATE INDEX idx_account_relationships_parent ON account_relationships(parent_account_id);
        CREATE INDEX idx_account_relationships_child ON account_relationships(child_account_id);
        
        RAISE NOTICE 'Created account_relationships table';
    ELSE
        RAISE NOTICE 'account_relationships table already exists';
    END IF;
END $$;

-- Create account_balances table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'account_balances') THEN
        CREATE TABLE account_balances (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            account_id UUID NOT NULL REFERENCES ledger_accounts(id) ON DELETE CASCADE,
            balance_date DATE NOT NULL,
            opening_balance DECIMAL(19, 2) DEFAULT 0 NOT NULL,
            debit_amount DECIMAL(19, 2) DEFAULT 0 NOT NULL,
            credit_amount DECIMAL(19, 2) DEFAULT 0 NOT NULL,
            closing_balance DECIMAL(19, 2) DEFAULT 0 NOT NULL,
            currency VARCHAR(3) DEFAULT 'USD' NOT NULL,
            fiscal_year INTEGER NOT NULL,
            fiscal_period INTEGER NOT NULL CHECK (fiscal_period BETWEEN 1 AND 12),
            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
            created_by VARCHAR(255) NOT NULL,
            UNIQUE(account_id, balance_date, fiscal_year, fiscal_period)
        );
        
        CREATE INDEX idx_account_balances_account ON account_balances(account_id);
        CREATE INDEX idx_account_balances_date ON account_balances(balance_date);
        CREATE INDEX idx_account_balances_fiscal ON account_balances(fiscal_year, fiscal_period);
        
        RAISE NOTICE 'Created account_balances table';
    ELSE
        RAISE NOTICE 'account_balances table already exists';
    END IF;
END $$;

-- Create triggers for accounts table if they don't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.triggers WHERE trigger_name = 'update_accounts_modtime') THEN
        CREATE TRIGGER update_accounts_modtime
            BEFORE UPDATE ON accounts
            FOR EACH ROW
            EXECUTE FUNCTION update_modified_column();
        RAISE NOTICE 'Created update_accounts_modtime trigger';
    END IF;
END $$;

-- Create audit trigger for accounts if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.triggers WHERE trigger_name = 'audit_accounts') THEN
        CREATE TRIGGER audit_accounts
            AFTER INSERT OR UPDATE OR DELETE ON accounts
            FOR EACH ROW
            EXECUTE FUNCTION audit_trigger_function();
        RAISE NOTICE 'Created audit_accounts trigger';
    END IF;
END $$;
