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
    must_change_password BOOLEAN DEFAULT FALSE NOT NULL,
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
-- FEES MODULE TABLES
-- ============================================

-- Fee Schedules table
CREATE TABLE IF NOT EXISTS fee_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) UNIQUE NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    academic_term VARCHAR(50),
    class_grade VARCHAR(50),
    effective_from DATE NOT NULL,
    effective_to DATE,
    total_amount DECIMAL(19, 2) DEFAULT 0 NOT NULL,
    total_amount_currency VARCHAR(3) DEFAULT 'USD' NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED')),
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_schedule_academic_year ON fee_schedules(academic_year);
CREATE INDEX idx_schedule_class_grade ON fee_schedules(class_grade);
CREATE INDEX idx_schedule_status ON fee_schedules(status);

-- Fee Schedule Items table
CREATE TABLE IF NOT EXISTS fee_schedule_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fee_schedule_id UUID NOT NULL REFERENCES fee_schedules(id) ON DELETE CASCADE,
    fee_type VARCHAR(50) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency_code VARCHAR(3) DEFAULT 'USD' NOT NULL,
    mandatory BOOLEAN DEFAULT true NOT NULL,
    description TEXT,
    display_order INTEGER DEFAULT 0,
    tax_rate DECIMAL(5, 2) DEFAULT 0,
    discount_percentage DECIMAL(5, 2) DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_schedule_items_schedule ON fee_schedule_items(fee_schedule_id);

-- Fee Invoices table
CREATE TABLE IF NOT EXISTS fee_invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    student_id UUID NOT NULL,
    academic_year VARCHAR(20),
    academic_term VARCHAR(50),
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    subtotal DECIMAL(19, 2) DEFAULT 0 NOT NULL,
    subtotal_currency_code VARCHAR(3) DEFAULT 'USD' NOT NULL,
    tax_amount DECIMAL(19, 2) DEFAULT 0 NOT NULL,
    tax_amount_currency_code VARCHAR(3) DEFAULT 'USD' NOT NULL,
    discount_amount DECIMAL(19, 2) DEFAULT 0 NOT NULL,
    discount_amount_currency_code VARCHAR(3) DEFAULT 'USD' NOT NULL,
    total_amount DECIMAL(19, 2) DEFAULT 0 NOT NULL,
    total_amount_currency_code VARCHAR(3) DEFAULT 'USD' NOT NULL,
    paid_amount DECIMAL(19, 2) DEFAULT 0 NOT NULL,
    paid_amount_currency_code VARCHAR(3) DEFAULT 'USD' NOT NULL,
    balance_amount DECIMAL(19, 2) DEFAULT 0 NOT NULL,
    balance_amount_currency_code VARCHAR(3) DEFAULT 'USD' NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'ISSUED', 'PARTIALLY_PAID', 'PAID', 'OVERDUE', 'CANCELLED', 'WRITTEN_OFF')),
    notes TEXT,
    generated_by VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_invoice_student_id ON fee_invoices(student_id);
CREATE INDEX idx_invoice_term ON fee_invoices(academic_term);
CREATE INDEX idx_invoice_status ON fee_invoices(status);
CREATE INDEX idx_invoice_due_date ON fee_invoices(due_date);

-- Fee Invoice Line Items table
CREATE TABLE IF NOT EXISTS fee_invoice_line_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id UUID NOT NULL REFERENCES fee_invoices(id) ON DELETE CASCADE,
    fee_type VARCHAR(50) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency_code VARCHAR(3) DEFAULT 'USD' NOT NULL,
    description TEXT,
    quantity INTEGER DEFAULT 1 NOT NULL,
    line_total DECIMAL(19, 2) NOT NULL,
    line_total_currency_code VARCHAR(3) DEFAULT 'USD' NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_line_items_invoice ON fee_invoice_line_items(invoice_id);

-- Fee Payments table
CREATE TABLE IF NOT EXISTS fee_payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    invoice_id UUID REFERENCES fee_invoices(id) ON DELETE SET NULL,
    source_payment_id UUID,
    fee_type VARCHAR(50) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency_code VARCHAR(3) DEFAULT 'USD' NOT NULL,
    payment_date TIMESTAMP WITH TIME ZONE NOT NULL,
    payment_method VARCHAR(50),
    reference_number VARCHAR(100),
    description TEXT,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELLED')),
    processed_by VARCHAR(255),
    receipt_number VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_payment_student_id ON fee_payments(student_id);
CREATE INDEX idx_payment_invoice_id ON fee_payments(invoice_id);
CREATE INDEX idx_payment_date ON fee_payments(payment_date);
CREATE INDEX idx_payment_status ON fee_payments(status);
CREATE INDEX idx_fee_payments_source_payment_id ON fee_payments(source_payment_id);

-- Add triggers for automatic timestamp updates on fees tables
CREATE TRIGGER update_fee_schedules_modtime
    BEFORE UPDATE ON fee_schedules
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_fee_schedule_items_modtime
    BEFORE UPDATE ON fee_schedule_items
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_fee_invoices_modtime
    BEFORE UPDATE ON fee_invoices
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_fee_invoice_line_items_modtime
    BEFORE UPDATE ON fee_invoice_line_items
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_fee_payments_modtime
    BEFORE UPDATE ON fee_payments
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- ============================================
-- TRANSACTIONS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency_code VARCHAR(3) DEFAULT 'USD' NOT NULL,
    debit_account_id UUID NOT NULL,
    credit_account_id UUID NOT NULL,
    reference_number VARCHAR(100),
    transaction_date TIMESTAMP WITH TIME ZONE NOT NULL,
    is_posted BOOLEAN DEFAULT false NOT NULL,
    posted_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_transactions_debit_account FOREIGN KEY (debit_account_id) REFERENCES accounts(id) ON DELETE RESTRICT,
    CONSTRAINT fk_transactions_credit_account FOREIGN KEY (credit_account_id) REFERENCES accounts(id) ON DELETE RESTRICT
);

CREATE INDEX idx_transaction_type ON transactions(transaction_type);
CREATE INDEX idx_transaction_date ON transactions(transaction_date);
CREATE INDEX idx_reference_number ON transactions(reference_number);
CREATE INDEX idx_debit_account_id ON transactions(debit_account_id);
CREATE INDEX idx_credit_account_id ON transactions(credit_account_id);

-- Add trigger for automatic timestamp updates
CREATE TRIGGER update_transactions_modtime
    BEFORE UPDATE ON transactions
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- ============================================
-- PAYMENTS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_number VARCHAR(50) UNIQUE NOT NULL,
    payment_date TIMESTAMP WITH TIME ZONE NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    payer_name VARCHAR(100),
    payer_email VARCHAR(100),
    payer_phone VARCHAR(20),
    description VARCHAR(500),
    gateway_transaction_id VARCHAR(100),
    gateway_reference VARCHAR(100),
    gateway_response_code VARCHAR(50),
    gateway_response_message VARCHAR(500),
    processed_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    failed_at TIMESTAMP WITH TIME ZONE,
    refunded_at TIMESTAMP WITH TIME ZONE,
    metadata TEXT,
    invoice_id UUID,
    authorization_url VARCHAR(500),
    callback_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Create indexes for common queries
CREATE INDEX idx_payments_payment_number ON payments(payment_number);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_payment_method ON payments(payment_method);
CREATE INDEX idx_payments_currency_code ON payments(currency_code);
CREATE INDEX idx_payments_payer_email ON payments(payer_email);
CREATE INDEX idx_payments_payment_date ON payments(payment_date);
CREATE INDEX idx_payments_gateway_transaction_id ON payments(gateway_transaction_id);
CREATE INDEX idx_payments_gateway_reference ON payments(gateway_reference);
CREATE INDEX idx_payments_created_at ON payments(created_at);
CREATE INDEX idx_payments_invoice_id ON payments(invoice_id);
CREATE INDEX idx_payments_authorization_url ON payments(authorization_url) WHERE authorization_url IS NOT NULL;
CREATE INDEX idx_payments_callback_url ON payments(callback_url) WHERE callback_url IS NOT NULL;
CREATE INDEX idx_payments_deleted_at ON payments(deleted_at);

-- Create trigger for automatic updated_at timestamp
CREATE TRIGGER update_payments_modified_column
    BEFORE UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- ============================================
-- PAYMENT ACCOUNTS
-- ============================================

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

-- ============================================
-- RECEIPTS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS receipts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    receipt_number VARCHAR(50) UNIQUE NOT NULL,
    payment_id UUID NOT NULL,
    receipt_date TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    payer_name VARCHAR(100),
    payer_email VARCHAR(100),
    payer_phone VARCHAR(20),
    description VARCHAR(500),
    payment_method VARCHAR(30),
    payment_reference VARCHAR(100),
    sent_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    generated_file_path VARCHAR(500),
    metadata TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL
);

-- Create indexes for common queries
CREATE INDEX idx_receipts_receipt_number ON receipts(receipt_number);
CREATE INDEX idx_receipts_payment_id ON receipts(payment_id);
CREATE INDEX idx_receipts_status ON receipts(status);
CREATE INDEX idx_receipts_payer_email ON receipts(payer_email);
CREATE INDEX idx_receipts_receipt_date ON receipts(receipt_date);
CREATE INDEX idx_receipts_created_at ON receipts(created_at);

-- Create trigger for automatic updated_at timestamp
CREATE TRIGGER update_receipts_modified_column
    BEFORE UPDATE ON receipts
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- ============================================
-- NOTIFICATIONS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_email VARCHAR(100),
    recipient_phone VARCHAR(20),
    notification_type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    subject VARCHAR(200),
    message TEXT NOT NULL,
    related_entity_type VARCHAR(50),
    related_entity_id UUID,
    sent_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    failed_at TIMESTAMP WITH TIME ZONE,
    error_message VARCHAR(500),
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    scheduled_at TIMESTAMP WITH TIME ZONE,
    last_retry_at TIMESTAMP WITH TIME ZONE,
    metadata TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Create indexes for common queries
CREATE INDEX idx_notifications_recipient_email ON notifications(recipient_email);
CREATE INDEX idx_notifications_recipient_phone ON notifications(recipient_phone);
CREATE INDEX idx_notifications_notification_type ON notifications(notification_type);
CREATE INDEX idx_notifications_channel ON notifications(channel);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_related_entity ON notifications(related_entity_type, related_entity_id);
CREATE INDEX idx_notifications_scheduled_at ON notifications(scheduled_at);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_deleted_at ON notifications(deleted_at);
CREATE INDEX idx_notifications_last_retry_at ON notifications(last_retry_at);

-- Create trigger for automatic updated_at timestamp
CREATE TRIGGER update_notifications_modified_column
    BEFORE UPDATE ON notifications
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- ============================================
-- AUDIT LOGS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID,
    action VARCHAR(30) NOT NULL,
    description VARCHAR(500),
    old_value TEXT,
    new_value TEXT,
    changed_fields TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    request_id VARCHAR(100),
    session_id VARCHAR(100),
    metadata TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL
);

-- Create indexes for common queries
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_created_by ON audit_logs(created_by);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_logs_ip_address ON audit_logs(ip_address);
CREATE INDEX idx_audit_logs_session_id ON audit_logs(session_id);
CREATE INDEX idx_audit_logs_request_id ON audit_logs(request_id);

-- Create trigger for automatic updated_at timestamp
CREATE TRIGGER update_audit_logs_modified_column
    BEFORE UPDATE ON audit_logs
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- ============================================
-- AI INSIGHTS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS ai_insights (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id VARCHAR(255) UNIQUE NOT NULL,
    reconciliation_id UUID,
    insight_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    risk_level VARCHAR(20),
    summary TEXT,
    root_cause TEXT,
    recommendations JSONB,
    metadata JSONB,
    anomaly_count INTEGER,
    requested_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    failure_reason TEXT,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_ai_insights_request_id ON ai_insights(request_id);
CREATE INDEX idx_ai_insights_reconciliation_id ON ai_insights(reconciliation_id);
CREATE INDEX idx_ai_insights_type ON ai_insights(insight_type);
CREATE INDEX idx_ai_insights_status ON ai_insights(status);
CREATE INDEX idx_ai_insights_risk_level ON ai_insights(risk_level);
CREATE INDEX idx_ai_insights_deleted_at ON ai_insights(deleted_at);

CREATE TRIGGER update_ai_insights_modtime
    BEFORE UPDATE ON ai_insights
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

DROP TRIGGER IF EXISTS audit_ai_insights ON ai_insights;
CREATE TRIGGER audit_ai_insights
    AFTER INSERT OR UPDATE OR DELETE ON ai_insights
    FOR EACH ROW
    EXECUTE FUNCTION audit_trigger_function();

-- ============================================
-- PASSWORD RESET TOKENS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    user_id UUID NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255) DEFAULT 'system' NOT NULL,
    updated_by VARCHAR(255) DEFAULT 'system' NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_tokens_token_hash ON password_reset_tokens(token_hash);
CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_tokens_expires_at ON password_reset_tokens(expires_at);

-- Create trigger for automatic updated_at
DROP TRIGGER IF EXISTS update_password_reset_tokens_modtime ON password_reset_tokens;
CREATE TRIGGER update_password_reset_tokens_modtime
    BEFORE UPDATE ON password_reset_tokens
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- ============================================
-- INITIAL DATA - PERMISSIONS
-- ============================================

-- User permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'USER:CREATE', 'Create User', 'Create new users', 'user', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'USER:READ', 'Read User', 'Read user information', 'user', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'USER:UPDATE', 'Update User', 'Update user information', 'user', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'USER:DELETE', 'Delete User', 'Delete users', 'user', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'USER:ASSIGN_ROLE', 'Assign Role to User', 'Assign roles to users', 'user', 'assign_role', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'USER:ASSIGN_PERMISSION', 'Assign Permission to User', 'Assign permissions to users', 'user', 'assign_permission', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'USER:RESET_PASSWORD', 'Reset User Password', 'Reset user password (admin recovery)', 'user', 'reset_password', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- Role permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'ROLE:CREATE', 'Create Role', 'Create new roles', 'role', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'ROLE:READ', 'Read Role', 'Read role information', 'role', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'ROLE:UPDATE', 'Update Role', 'Update role information', 'role', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'ROLE:DELETE', 'Delete Role', 'Delete roles', 'role', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'ROLE:ASSIGN_PERMISSION', 'Assign Permission to Role', 'Assign permissions to roles', 'role', 'assign_permission', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- Permission permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'PERMISSION:CREATE', 'Create Permission', 'Create new permissions', 'permission', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'PERMISSION:READ', 'Read Permission', 'Read permission information', 'permission', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'PERMISSION:UPDATE', 'Update Permission', 'Update permission information', 'permission', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'PERMISSION:DELETE', 'Delete Permission', 'Delete permissions', 'permission', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- Payment permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'PAYMENT:CREATE', 'Create Payment', 'Create new payments', 'payment', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'PAYMENT:READ', 'Read Payment', 'Read payment information', 'payment', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'PAYMENT:UPDATE', 'Update Payment', 'Update payment information', 'payment', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'PAYMENT:DELETE', 'Delete Payment', 'Delete payments', 'payment', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- Ledger permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'LEDGER:READ', 'Read Ledger', 'Read ledger information', 'ledger', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'LEDGER:UPDATE', 'Update Ledger', 'Update ledger information', 'ledger', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- Journal permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'JOURNAL:CREATE', 'Create Journal Entry', 'Create journal entries', 'journal', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'JOURNAL:READ', 'Read Journal Entry', 'Read journal entries', 'journal', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- Reconciliation permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'RECONCILIATION:EXECUTE', 'Execute Reconciliation', 'Execute reconciliation processes', 'reconciliation', 'execute', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'RECONCILIATION:READ', 'Read Reconciliation', 'Read reconciliation information', 'reconciliation', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- Report permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'REPORT:GENERATE', 'Generate Report', 'Generate reports', 'report', 'generate', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'REPORT:READ', 'Read Report', 'Read reports', 'report', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- Receipt permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'RECEIPT:CREATE', 'Create Receipt', 'Create new receipts', 'receipt', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'RECEIPT:READ', 'Read Receipt', 'Read receipt information', 'receipt', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'RECEIPT:UPDATE', 'Update Receipt', 'Update receipt information', 'receipt', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'RECEIPT:DELETE', 'Delete Receipt', 'Delete receipts', 'receipt', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- Notification permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'NOTIFICATION:CREATE', 'Create Notification', 'Create new notifications', 'notification', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'NOTIFICATION:READ', 'Read Notification', 'Read notification information', 'notification', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'NOTIFICATION:UPDATE', 'Update Notification', 'Update notification information', 'notification', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'NOTIFICATION:DELETE', 'Delete Notification', 'Delete notifications', 'notification', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- Audit permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'AUDIT:CREATE', 'Create Audit Log', 'Create new audit logs', 'audit', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'AUDIT:READ', 'Read Audit Log', 'Read audit log information', 'audit', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'AUDIT:UPDATE', 'Update Audit Log', 'Update audit log information', 'audit', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'AUDIT:DELETE', 'Delete Audit Log', 'Delete audit logs', 'audit', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- FEE permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'FEE:CREATE', 'Create Fee', 'Create fee schedules and invoices', 'fee', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'FEE:READ', 'Read Fee', 'Read fee schedules and invoices', 'fee', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'FEE:UPDATE', 'Update Fee', 'Update fee schedules and invoices', 'fee', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'FEE:DELETE', 'Delete Fee', 'Delete fee schedules and invoices', 'fee', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- AI permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'AI:READ', 'Read AI Insights', 'Read AI-powered insights and analytics', 'ai', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- INITIAL DATA - ROLES
-- ============================================

-- Create roles
INSERT INTO roles (id, code, name, description, level, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'ADMIN', 'Administrator', 'Full system access', 0, NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'ACCOUNTANT', 'Accountant', 'Financial operations access', 0, NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'AUDITOR', 'Auditor', 'Read-only access to financial data', 0, NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'USER', 'User', 'Basic user access', 0, NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- Grant all permissions to ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r 
CROSS JOIN permissions p 
WHERE r.code = 'ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

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
  'REPORT:GENERATE', 'REPORT:READ',
  'FEE:CREATE', 'FEE:READ', 'FEE:UPDATE',
  'AI:READ',
  'RECEIPT:CREATE', 'RECEIPT:READ'
)
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Grant read permissions to AUDITOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r 
CROSS JOIN permissions p 
WHERE r.code = 'AUDITOR' 
AND p.code IN (
  'PAYMENT:READ', 'LEDGER:READ', 'JOURNAL:READ',
  'RECONCILIATION:READ', 'REPORT:READ',
  'FEE:READ', 'AI:READ'
)
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Grant basic permissions to USER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r 
CROSS JOIN permissions p 
WHERE r.code = 'USER' 
AND p.code IN ('USER:READ', 'PAYMENT:READ', 'REPORT:READ', 'FEE:READ', 'AI:READ')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ============================================
-- INITIAL DATA - ADMIN USER
-- ============================================

-- Create admin user with correct password and email
INSERT INTO users (
  id,
  username,
  email,
  password,
  first_name,
  enabled,
  account_non_expired,
  account_non_locked,
  credentials_non_expired,
  must_change_password,
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
  '$2a$10$zWCJ7LtWBhMlYRQIaopu..QDoaBA9DrW8UXaGKv7/URV3MFdDzhGu',
  'Administrator',
  true,
  true,
  true,
  true,
  false,
  0,
  NOW(),
  'SYSTEM',
  NOW(),
  'SYSTEM'
) ON CONFLICT (username) DO NOTHING;

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u 
CROSS JOIN roles r 
WHERE u.username = 'admin' 
AND r.code = 'ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- ============================================
-- INITIAL DATA - ACCOUNT CATEGORIES
-- ============================================

-- Account Categories
INSERT INTO account_categories (id, code, name, description, normal_balance, display_order, is_active, created_at, updated_at, created_by, updated_by)
VALUES 
  (gen_random_uuid(), 'ASSET', 'Assets', 'Resources owned by the company', 'DEBIT', 1, true, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
  (gen_random_uuid(), 'LIABILITY', 'Liabilities', 'Obligations of the company', 'CREDIT', 2, true, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
  (gen_random_uuid(), 'EQUITY', 'Equity', 'Owner''s equity in the company', 'CREDIT', 3, true, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
  (gen_random_uuid(), 'REVENUE', 'Revenue', 'Income from business operations', 'CREDIT', 4, true, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
  (gen_random_uuid(), 'EXPENSE', 'Expense', 'Costs of doing business', 'DEBIT', 5, true, NOW(), NOW(), 'SYSTEM', 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

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
FROM account_categories ac WHERE ac.code = 'ASSET'
ON CONFLICT (code) DO NOTHING;
