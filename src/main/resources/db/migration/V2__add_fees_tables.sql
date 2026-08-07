-- Add fees module tables
-- This migration adds the fee management system tables

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
