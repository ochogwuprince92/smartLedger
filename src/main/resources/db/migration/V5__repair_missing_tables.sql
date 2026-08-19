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

-- Create transactions table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'transactions') THEN
        CREATE TABLE transactions (
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
        
        CREATE TRIGGER update_transactions_modtime
            BEFORE UPDATE ON transactions
            FOR EACH ROW
            EXECUTE FUNCTION update_modified_column();
            
        RAISE NOTICE 'Created transactions table';
    ELSE
        RAISE NOTICE 'transactions table already exists';
    END IF;
END $$;

-- Create payments table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'payments') THEN
        CREATE TABLE payments (
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
            idempotency_key VARCHAR(100),
            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
            updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
            created_by VARCHAR(255) NOT NULL,
            updated_by VARCHAR(255) NOT NULL,
            deleted_at TIMESTAMP WITH TIME ZONE
        );
        
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
        CREATE INDEX IF NOT EXISTS idx_payments_idempotency_key ON payments(idempotency_key);
        
        CREATE TRIGGER update_payments_modified_column
            BEFORE UPDATE ON payments
            FOR EACH ROW
            EXECUTE FUNCTION update_modified_column();
            
        RAISE NOTICE 'Created payments table';
    ELSE
        RAISE NOTICE 'payments table already exists';
    END IF;
END $$;

-- Add idempotency_key constraint if table exists but constraint doesn't
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'payments') AND
       NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'uk_payments_idempotency_key') THEN
        ALTER TABLE payments ADD CONSTRAINT uk_payments_idempotency_key UNIQUE (idempotency_key);
        RAISE NOTICE 'Added uk_payments_idempotency_key constraint';
    END IF;
END $$;

-- Create receipts table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'receipts') THEN
        CREATE TABLE receipts (
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
        
        CREATE INDEX idx_receipts_receipt_number ON receipts(receipt_number);
        CREATE INDEX idx_receipts_payment_id ON receipts(payment_id);
        CREATE INDEX idx_receipts_status ON receipts(status);
        CREATE INDEX idx_receipts_payer_email ON receipts(payer_email);
        CREATE INDEX idx_receipts_receipt_date ON receipts(receipt_date);
        CREATE INDEX idx_receipts_created_at ON receipts(created_at);
        
        CREATE TRIGGER update_receipts_modified_column
            BEFORE UPDATE ON receipts
            FOR EACH ROW
            EXECUTE FUNCTION update_modified_column();
            
        RAISE NOTICE 'Created receipts table';
    ELSE
        RAISE NOTICE 'receipts table already exists';
    END IF;
END $$;

-- Create notifications table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'notifications') THEN
        CREATE TABLE notifications (
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
        
        CREATE TRIGGER update_notifications_modified_column
            BEFORE UPDATE ON notifications
            FOR EACH ROW
            EXECUTE FUNCTION update_modified_column();
            
        RAISE NOTICE 'Created notifications table';
    ELSE
        RAISE NOTICE 'notifications table already exists';
    END IF;
END $$;

-- Create audit_logs table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'audit_logs') THEN
        CREATE TABLE audit_logs (
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
        
        CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
        CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
        CREATE INDEX idx_audit_logs_action ON audit_logs(action);
        CREATE INDEX idx_audit_logs_created_by ON audit_logs(created_by);
        CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
        CREATE INDEX idx_audit_logs_ip_address ON audit_logs(ip_address);
        CREATE INDEX idx_audit_logs_session_id ON audit_logs(session_id);
        CREATE INDEX idx_audit_logs_request_id ON audit_logs(request_id);
        
        CREATE TRIGGER update_audit_logs_modified_column
            BEFORE UPDATE ON audit_logs
            FOR EACH ROW
            EXECUTE FUNCTION update_modified_column();
            
        RAISE NOTICE 'Created audit_logs table';
    ELSE
        RAISE NOTICE 'audit_logs table already exists';
    END IF;
END $$;

-- Create ai_insights table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'ai_insights') THEN
        CREATE TABLE ai_insights (
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
            
        RAISE NOTICE 'Created ai_insights table';
    ELSE
        RAISE NOTICE 'ai_insights table already exists';
    END IF;
END $$;

-- Create password_reset_tokens table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'password_reset_tokens') THEN
        CREATE TABLE password_reset_tokens (
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
        
        CREATE TRIGGER update_password_reset_tokens_modtime
            BEFORE UPDATE ON password_reset_tokens
            FOR EACH ROW
            EXECUTE FUNCTION update_modified_column();
            
        RAISE NOTICE 'Created password_reset_tokens table';
    ELSE
        RAISE NOTICE 'password_reset_tokens table already exists';
    END IF;
END $$;
