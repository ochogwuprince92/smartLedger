-- Initial schema setup for smartLedger
-- This migration creates the complete database structure in a single file

-- Create UUID extension if not exists
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create audit function for automatic timestamp updates
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- ============================================
-- SHARED TABLES (Users, Roles, Permissions)
-- ============================================

-- Users table for authentication and authorization
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone VARCHAR(20),
    enabled BOOLEAN DEFAULT TRUE NOT NULL,
    account_non_expired BOOLEAN DEFAULT TRUE NOT NULL,
    account_non_locked BOOLEAN DEFAULT TRUE NOT NULL,
    credentials_non_expired BOOLEAN DEFAULT TRUE NOT NULL,
    last_login_at TIMESTAMP WITH TIME ZONE,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    level INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_roles_code ON roles(code);

-- Permissions table
CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    resource VARCHAR(50),
    action VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_permissions_code ON permissions(code);
CREATE INDEX idx_permissions_resource ON permissions(resource, action);

-- User-Role junction table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- User-Permission junction table
CREATE TABLE IF NOT EXISTS user_permissions (
    user_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (user_id, permission_id),
    CONSTRAINT fk_user_permissions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Role-Permission junction table
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Role hierarchy junction table
CREATE TABLE IF NOT EXISTS role_hierarchy (
    parent_role_id UUID NOT NULL,
    child_role_id UUID NOT NULL,
    PRIMARY KEY (parent_role_id, child_role_id),
    CONSTRAINT fk_role_hierarchy_parent FOREIGN KEY (parent_role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_hierarchy_child FOREIGN KEY (child_role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Create triggers for automatic updated_at
CREATE TRIGGER update_users_modtime
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_roles_modtime
    BEFORE UPDATE ON roles
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_permissions_modtime
    BEFORE UPDATE ON permissions
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- ============================================
-- AUDIT TABLES
-- ============================================

CREATE TABLE IF NOT EXISTS audit_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_type VARCHAR(255) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_value JSONB,
    new_value JSONB,
    changed_by VARCHAR(255),
    changed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT
);

CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_changed_at ON audit_log(changed_at);
CREATE INDEX idx_audit_log_changed_by ON audit_log(changed_by);

-- ============================================
-- ACCOUNTS TABLE
-- ============================================

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

-- ============================================
-- CHART OF ACCOUNTS STRUCTURE
-- ============================================

-- Account Categories (Asset, Liability, Equity, Revenue, Expense)
CREATE TABLE IF NOT EXISTS account_categories (
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

-- Account Subcategories (more granular classification)
CREATE TABLE IF NOT EXISTS account_subcategories (
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

-- Ledger Accounts (the actual accounts in the chart of accounts)
CREATE TABLE IF NOT EXISTS ledger_accounts (
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

-- Account relationships (for hierarchical structure)
CREATE TABLE IF NOT EXISTS account_relationships (
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

-- Account balances (historical balance tracking)
CREATE TABLE IF NOT EXISTS account_balances (
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

-- Triggers for automatic timestamp updates
CREATE TRIGGER update_account_categories_modtime
    BEFORE UPDATE ON account_categories
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_account_subcategories_modtime
    BEFORE UPDATE ON account_subcategories
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_ledger_accounts_modtime
    BEFORE UPDATE ON ledger_accounts
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- Add constraint to ensure account numbers are properly formatted
ALTER TABLE ledger_accounts
ADD CONSTRAINT chk_account_number_format 
CHECK (account_number ~ '^[0-9]{4,20}$');

-- Add constraint to ensure account codes are properly formatted
ALTER TABLE ledger_accounts
ADD CONSTRAINT chk_account_code_format 
CHECK (account_code ~ '^[0-9]{1,10}$');

-- Add constraint to ensure currency codes are valid ISO 4217
ALTER TABLE ledger_accounts
ADD CONSTRAINT chk_currency_code 
CHECK (currency ~ '^[A-Z]{3}$');

-- Add constraint to ensure account level matches hierarchy
ALTER TABLE ledger_accounts
ADD CONSTRAINT chk_account_level_hierarchy
CHECK (
    (parent_account_id IS NULL AND account_level = 0) OR
    (parent_account_id IS NOT NULL AND account_level > 0)
);

-- ============================================
-- AUDIT TRIGGERS
-- ============================================

-- Enhanced audit function that captures detailed change information
CREATE OR REPLACE FUNCTION audit_trigger_function()
RETURNS TRIGGER AS $$
DECLARE
    old_data JSONB;
    new_data JSONB;
    operation VARCHAR(10);
BEGIN
    IF TG_OP = 'INSERT' THEN
        operation := 'INSERT';
        new_data := to_jsonb(NEW);
        old_data := NULL;
    ELSIF TG_OP = 'UPDATE' THEN
        operation := 'UPDATE';
        new_data := to_jsonb(NEW);
        old_data := to_jsonb(OLD);
    ELSIF TG_OP = 'DELETE' THEN
        operation := 'DELETE';
        new_data := NULL;
        old_data := to_jsonb(OLD);
    ELSE
        RETURN NEW;
    END IF;

    INSERT INTO audit_log (
        entity_type,
        entity_id,
        action,
        old_value,
        new_value,
        changed_by,
        changed_at,
        ip_address,
        user_agent
    ) VALUES (
        TG_TABLE_NAME,
        COALESCE(NEW.id, OLD.id),
        operation,
        old_data,
        new_data,
        COALESCE(NEW.updated_by, NEW.created_by, OLD.updated_by, OLD.created_by, 'SYSTEM'),
        CURRENT_TIMESTAMP,
        inet_client_addr()::TEXT,
        current_setting('application_name', true)
    );

    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Create audit triggers for all tables
DROP TRIGGER IF EXISTS audit_account_categories ON account_categories;
CREATE TRIGGER audit_account_categories
    AFTER INSERT OR UPDATE OR DELETE ON account_categories
    FOR EACH ROW
    EXECUTE FUNCTION audit_trigger_function();

DROP TRIGGER IF EXISTS audit_account_subcategories ON account_subcategories;
CREATE TRIGGER audit_account_subcategories
    AFTER INSERT OR UPDATE OR DELETE ON account_subcategories
    FOR EACH ROW
    EXECUTE FUNCTION audit_trigger_function();

DROP TRIGGER IF EXISTS audit_ledger_accounts ON ledger_accounts;
CREATE TRIGGER audit_ledger_accounts
    AFTER INSERT OR UPDATE OR DELETE ON ledger_accounts
    FOR EACH ROW
    EXECUTE FUNCTION audit_trigger_function();

DROP TRIGGER IF EXISTS audit_account_relationships ON account_relationships;
CREATE TRIGGER audit_account_relationships
    AFTER INSERT OR UPDATE OR DELETE ON account_relationships
    FOR EACH ROW
    EXECUTE FUNCTION audit_trigger_function();

DROP TRIGGER IF EXISTS audit_account_balances ON account_balances;
CREATE TRIGGER audit_account_balances
    AFTER INSERT OR UPDATE OR DELETE ON account_balances
    FOR EACH ROW
    EXECUTE FUNCTION audit_trigger_function();

DROP TRIGGER IF EXISTS audit_users ON users;
CREATE TRIGGER audit_users
    AFTER INSERT OR UPDATE OR DELETE ON users
    FOR EACH ROW
    EXECUTE FUNCTION audit_trigger_function();

DROP TRIGGER IF EXISTS audit_roles ON roles;
CREATE TRIGGER audit_roles
    AFTER INSERT OR UPDATE OR DELETE ON roles
    FOR EACH ROW
    EXECUTE FUNCTION audit_trigger_function();

DROP TRIGGER IF EXISTS audit_permissions ON permissions;
CREATE TRIGGER audit_permissions
    AFTER INSERT OR UPDATE OR DELETE ON permissions
    FOR EACH ROW
    EXECUTE FUNCTION audit_trigger_function();

DROP TRIGGER IF EXISTS audit_accounts ON accounts;
CREATE TRIGGER audit_accounts
    AFTER INSERT OR UPDATE OR DELETE ON accounts
    FOR EACH ROW
    EXECUTE FUNCTION audit_trigger_function();

-- Balance change tracking trigger for ledger_accounts
CREATE OR REPLACE FUNCTION balance_change_trigger()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' AND 
       (NEW.current_balance IS DISTINCT FROM OLD.current_balance OR
        NEW.opening_balance IS DISTINCT FROM OLD.opening_balance) THEN
        INSERT INTO audit_log (
            entity_type,
            entity_id,
            action,
            old_value,
            new_value,
            changed_by,
            changed_at
        ) VALUES (
            'ledger_accounts_balance',
            NEW.id,
            'BALANCE_CHANGE',
            jsonb_build_object(
                'old_current_balance', OLD.current_balance,
                'old_opening_balance', OLD.opening_balance,
                'change_date', CURRENT_TIMESTAMP
            ),
            jsonb_build_object(
                'new_current_balance', NEW.current_balance,
                'new_opening_balance', NEW.opening_balance,
                'change_date', CURRENT_TIMESTAMP
            ),
            NEW.updated_by,
            CURRENT_TIMESTAMP
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS balance_change_ledger_accounts ON ledger_accounts;
CREATE TRIGGER balance_change_ledger_accounts
    BEFORE UPDATE ON ledger_accounts
    FOR EACH ROW
    EXECUTE FUNCTION balance_change_trigger();

-- Account hierarchy validation trigger
CREATE OR REPLACE FUNCTION validate_account_hierarchy()
RETURNS TRIGGER AS $$
DECLARE
    has_cycle BOOLEAN;
BEGIN
    IF TG_OP = 'INSERT' OR (TG_OP = 'UPDATE' AND NEW.parent_account_id IS DISTINCT FROM OLD.parent_account_id) THEN
        IF NEW.parent_account_id IS NOT NULL THEN
            WITH RECURSIVE account_tree AS (
                SELECT id, parent_account_id FROM ledger_accounts WHERE id = NEW.parent_account_id
                UNION ALL
                SELECT la.id, la.parent_account_id 
                FROM ledger_accounts la
                INNER JOIN account_tree at ON la.id = at.parent_account_id
            )
            SELECT EXISTS(SELECT 1 FROM account_tree WHERE id = NEW.id) INTO has_cycle;
            
            IF has_cycle THEN
                RAISE EXCEPTION 'Circular reference detected in account hierarchy for account %', NEW.account_number;
            END IF;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS validate_hierarchy_ledger_accounts ON ledger_accounts;
CREATE TRIGGER validate_hierarchy_ledger_accounts
    BEFORE INSERT OR UPDATE ON ledger_accounts
    FOR EACH ROW
    EXECUTE FUNCTION validate_account_hierarchy();

-- Account relationship validation trigger
CREATE OR REPLACE FUNCTION validate_account_relationships()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.parent_account_id = NEW.child_account_id THEN
        RAISE EXCEPTION 'An account cannot be related to itself';
    END IF;
    
    IF TG_OP = 'INSERT' THEN
        IF EXISTS (
            SELECT 1 FROM account_relationships 
            WHERE parent_account_id = NEW.parent_account_id 
            AND child_account_id = NEW.child_account_id
            AND relationship_type = NEW.relationship_type
        ) THEN
            RAISE EXCEPTION 'Duplicate relationship already exists';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS validate_relationships_account_relationships ON account_relationships;
CREATE TRIGGER validate_relationships_account_relationships
    BEFORE INSERT OR UPDATE ON account_relationships
    FOR EACH ROW
    EXECUTE FUNCTION validate_account_relationships();

-- Account balance validation trigger
CREATE OR REPLACE FUNCTION validate_account_balance()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
        IF ABS(NEW.closing_balance - (NEW.opening_balance + NEW.debit_amount - NEW.credit_amount)) > 0.01 THEN
            RAISE EXCEPTION 'Closing balance must equal opening balance + debit amount - credit amount for account % on date %', 
                          NEW.account_id, NEW.balance_date;
        END IF;
        
        IF NEW.opening_balance < 0 OR NEW.debit_amount < 0 OR NEW.credit_amount < 0 THEN
            RAISE EXCEPTION 'Balance amounts cannot be negative for account % on date %', 
                          NEW.account_id, NEW.balance_date;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS validate_balance_account_balances ON account_balances;
CREATE TRIGGER validate_balance_account_balances
    BEFORE INSERT OR UPDATE ON account_balances
    FOR EACH ROW
    EXECUTE FUNCTION validate_account_balance();

-- Account status change trigger
CREATE OR REPLACE FUNCTION account_status_change_trigger()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' AND NEW.is_active IS DISTINCT FROM OLD.is_active THEN
        INSERT INTO audit_log (
            entity_type,
            entity_id,
            action,
            old_value,
            new_value,
            changed_by,
            changed_at
        ) VALUES (
            'ledger_accounts_status',
            NEW.id,
            'STATUS_CHANGE',
            jsonb_build_object('old_is_active', OLD.is_active),
            jsonb_build_object('new_is_active', NEW.is_active),
            NEW.updated_by,
            CURRENT_TIMESTAMP
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS status_change_ledger_accounts ON ledger_accounts;
CREATE TRIGGER status_change_ledger_accounts
    AFTER UPDATE ON ledger_accounts
    FOR EACH ROW
    EXECUTE FUNCTION account_status_change_trigger();

-- ============================================
-- INITIAL DATA
-- ============================================

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
  'admin.smartledger@gmail.com',
  '$2b$10$QXXLP77UqerY.DYP5g1Hb.QABl5VVgfl7FT66SCSdzw6Wqma5HUdq',
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

-- Account Categories
INSERT INTO account_categories (id, code, name, description, normal_balance, display_order, is_active, created_at, updated_at, created_by, updated_by)
VALUES 
  (gen_random_uuid(), 'ASSET', 'Assets', 'Resources owned by the company', 'DEBIT', 1, true, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
  (gen_random_uuid(), 'LIABILITY', 'Liabilities', 'Obligations of the company', 'CREDIT', 2, true, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
  (gen_random_uuid(), 'EQUITY', 'Equity', 'Owner''s equity in the company', 'CREDIT', 3, true, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
  (gen_random_uuid(), 'REVENUE', 'Revenue', 'Income from business operations', 'CREDIT', 4, true, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
  (gen_random_uuid(), 'EXPENSE', 'Expense', 'Costs of doing business', 'DEBIT', 5, true, NOW(), NOW(), 'SYSTEM', 'SYSTEM');

-- Asset Subcategories
INSERT INTO account_subcategories (id, category_id, code, name, description, normal_balance, display_order, is_active, created_at, updated_at, created_by, updated_by)
SELECT 
    gen_random_uuid(),
    ac.id,
    'CASH',
    'Cash and Cash Equivalents',
    'Cash on hand and in bank accounts',
    'DEBIT',
    1,
    true,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM'
FROM account_categories ac WHERE ac.code = 'ASSET';

INSERT INTO account_subcategories (id, category_id, code, name, description, normal_balance, display_order, is_active, created_at, updated_at, created_by, updated_by)
SELECT 
    gen_random_uuid(),
    ac.id,
    'AR',
    'Accounts Receivable',
    'Money owed by customers',
    'DEBIT',
    2,
    true,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM'
FROM account_categories ac WHERE ac.code = 'ASSET';

INSERT INTO account_subcategories (id, category_id, code, name, description, normal_balance, display_order, is_active, created_at, updated_at, created_by, updated_by)
SELECT 
    gen_random_uuid(),
    ac.id,
    'INV',
    'Inventory',
    'Goods held for sale',
    'DEBIT',
    3,
    true,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM'
FROM account_categories ac WHERE ac.code = 'ASSET';

INSERT INTO account_subcategories (id, category_id, code, name, description, normal_balance, display_order, is_active, created_at, updated_at, created_by, updated_by)
SELECT 
    gen_random_uuid(),
    ac.id,
    'FIX',
    'Fixed Assets',
    'Long-term tangible assets',
    'DEBIT',
    4,
    true,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM'
FROM account_categories ac WHERE ac.code = 'ASSET';

-- Liability Subcategories
INSERT INTO account_subcategories (id, category_id, code, name, description, normal_balance, display_order, is_active, created_at, updated_at, created_by, updated_by)
SELECT 
    gen_random_uuid(),
    ac.id,
    'AP',
    'Accounts Payable',
    'Money owed to suppliers',
    'CREDIT',
    1,
    true,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM'
FROM account_categories ac WHERE ac.code = 'LIABILITY';

INSERT INTO account_subcategories (id, category_id, code, name, description, normal_balance, display_order, is_active, created_at, updated_at, created_by, updated_by)
SELECT 
    gen_random_uuid(),
    ac.id,
    'TAX',
    'Tax Liabilities',
    'Taxes owed to government',
    'CREDIT',
    2,
    true,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM'
FROM account_categories ac WHERE ac.code = 'LIABILITY';

INSERT INTO account_subcategories (id, category_id, code, name, description, normal_balance, display_order, is_active, created_at, updated_at, created_by, updated_by)
SELECT 
    gen_random_uuid(),
    ac.id,
    'LOAN',
    'Loans and Borrowings',
    'Long-term debt obligations',
    'CREDIT',
    3,
    true,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM'
FROM account_categories ac WHERE ac.code = 'LIABILITY';

-- Equity Subcategories
INSERT INTO account_subcategories (id, category_id, code, name, description, normal_balance, display_order, is_active, created_at, updated_at, created_by, updated_by)
SELECT 
    gen_random_uuid(),
    ac.id,
    'CAP',
    'Capital',
    'Owner''s capital investment',
    'CREDIT',
    1,
    true,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM'
FROM account_categories ac WHERE ac.code = 'EQUITY';

INSERT INTO account_subcategories (id, category_id, code, name, description, normal_balance, display_order, is_active, created_at, updated_at, created_by, updated_by)
SELECT 
    gen_random_uuid(),
    ac.id,
    'RET',
    'Retained Earnings',
    'Accumulated profits',
    'CREDIT',
    2,
    true,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM'
FROM account_categories ac WHERE ac.code = 'EQUITY';

-- Revenue Subcategories
INSERT INTO account_subcategories (id, category_id, code, name, description, normal_balance, display_order, is_active, created_at, updated_at, created_by, updated_by)
SELECT 
    gen_random_uuid(),
    ac.id,
    'SALES',
    'Sales Revenue',
    'Income from sales of goods',
    'CREDIT',
    1,
    true,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM'
FROM account_categories ac WHERE ac.code = 'REVENUE';

INSERT INTO account_subcategories (id, category_id, code, name, description, normal_balance, display_order, is_active, created_at, updated_at, created_by, updated_by)
SELECT 
    gen_random_uuid(),
    ac.id,
    'SERV',
    'Service Revenue',
    'Income from services',
    'CREDIT',
    2,
    true,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM'
FROM account_categories ac WHERE ac.code = 'REVENUE';

-- Expense Subcategories
INSERT INTO account_subcategories (id, category_id, code, name, description, normal_balance, display_order, is_active, created_at, updated_at, created_by, updated_by)
SELECT 
    gen_random_uuid(),
    ac.id,
    'COGS',
    'Cost of Goods Sold',
    'Direct costs of producing goods',
    'DEBIT',
    1,
    true,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM'
FROM account_categories ac WHERE ac.code = 'EXPENSE';

INSERT INTO account_subcategories (id, category_id, code, name, description, normal_balance, display_order, is_active, created_at, updated_at, created_by, updated_by)
SELECT 
    gen_random_uuid(),
    ac.id,
    'OPX',
    'Operating Expenses',
    'Day-to-day business expenses',
    'DEBIT',
    2,
    true,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM'
FROM account_categories ac WHERE ac.code = 'EXPENSE';

INSERT INTO account_subcategories (id, category_id, code, name, description, normal_balance, display_order, is_active, created_at, updated_at, created_by, updated_by)
SELECT 
    gen_random_uuid(),
    ac.id,
    'PAY',
    'Payroll Expenses',
    'Employee-related expenses',
    'DEBIT',
    3,
    true,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM'
FROM account_categories ac WHERE ac.code = 'EXPENSE';

-- Sample Ledger Accounts
INSERT INTO ledger_accounts (id, subcategory_id, account_number, account_code, account_name, description, account_type, normal_balance, currency, opening_balance, current_balance, account_level, is_active, is_control_account, is_reconcilable, created_at, updated_at, created_by, updated_by)
SELECT 
    gen_random_uuid(),
    sub.id,
    '1000',
    '1000',
    'Cash',
    'Cash on hand and in bank accounts',
    'ASSET',
    'DEBIT',
    'USD',
    0.00,
    0.00,
    0,
    true,
    true,
    true,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM'
FROM account_subcategories sub WHERE sub.code = 'CASH';

INSERT INTO ledger_accounts (id, subcategory_id, account_number, account_code, account_name, description, account_type, normal_balance, currency, opening_balance, current_balance, account_level, is_active, is_control_account, is_reconcilable, created_at, updated_at, created_by, updated_by)
SELECT 
    gen_random_uuid(),
    sub.id,
    '1100',
    '1100',
    'Accounts Receivable',
    'Money owed by customers',
    'ASSET',
    'DEBIT',
    'USD',
    0.00,
    0.00,
    0,
    true,
    true,
    true,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM'
FROM account_subcategories sub WHERE sub.code = 'AR';

INSERT INTO ledger_accounts (id, subcategory_id, account_number, account_code, account_name, description, account_type, normal_balance, currency, opening_balance, current_balance, account_level, is_active, is_control_account, is_reconcilable, created_at, updated_at, created_by, updated_by)
SELECT 
    gen_random_uuid(),
    sub.id,
    '2000',
    '2000',
    'Accounts Payable',
    'Money owed to suppliers',
    'LIABILITY',
    'CREDIT',
    'USD',
    0.00,
    0.00,
    0,
    true,
    true,
    true,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM'
FROM account_subcategories sub WHERE sub.code = 'AP';

INSERT INTO ledger_accounts (id, subcategory_id, account_number, account_code, account_name, description, account_type, normal_balance, currency, opening_balance, current_balance, account_level, is_active, is_control_account, is_reconcilable, created_at, updated_at, created_by, updated_by)
SELECT 
    gen_random_uuid(),
    sub.id,
    '3000',
    '3000',
    'Capital',
    'Owner''s capital investment',
    'EQUITY',
    'CREDIT',
    'USD',
    0.00,
    0.00,
    0,
    true,
    false,
    false,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM'
FROM account_subcategories sub WHERE sub.code = 'CAP';

INSERT INTO ledger_accounts (id, subcategory_id, account_number, account_code, account_name, description, account_type, normal_balance, currency, opening_balance, current_balance, account_level, is_active, is_control_account, is_reconcilable, created_at, updated_at, created_by, updated_by)
SELECT 
    gen_random_uuid(),
    sub.id,
    '4000',
    '4000',
    'Sales Revenue',
    'Income from sales of goods',
    'REVENUE',
    'CREDIT',
    'USD',
    0.00,
    0.00,
    0,
    true,
    true,
    false,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM'
FROM account_subcategories sub WHERE sub.code = 'SALES';

INSERT INTO ledger_accounts (id, subcategory_id, account_number, account_code, account_name, description, account_type, normal_balance, currency, opening_balance, current_balance, account_level, is_active, is_control_account, is_reconcilable, created_at, updated_at, created_by, updated_by)
SELECT 
    gen_random_uuid(),
    sub.id,
    '5000',
    '5000',
    'Cost of Goods Sold',
    'Direct costs of producing goods',
    'EXPENSE',
    'DEBIT',
    'USD',
    0.00,
    0.00,
    0,
    true,
    false,
    false,
    NOW(),
    NOW(),
    'SYSTEM',
    'SYSTEM'
FROM account_subcategories sub WHERE sub.code = 'COGS';
