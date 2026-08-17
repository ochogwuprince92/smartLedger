-- Add reports table for financial reporting

CREATE TABLE IF NOT EXISTS reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_number VARCHAR(50) NOT NULL UNIQUE,
    report_date TIMESTAMP WITH TIME ZONE NOT NULL,
    report_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    period_start_date TIMESTAMP WITH TIME ZONE,
    period_end_date TIMESTAMP WITH TIME ZONE,
    currency_code VARCHAR(3) NOT NULL,
    generated_at TIMESTAMP WITH TIME ZONE,
    file_path VARCHAR(500),
    error_message VARCHAR(1000),
    description VARCHAR(500),
    report_data TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL
);

-- Create indexes
CREATE INDEX idx_reports_report_number ON reports(report_number);
CREATE INDEX idx_reports_report_date ON reports(report_date);
CREATE INDEX idx_reports_report_type ON reports(report_type);
CREATE INDEX idx_reports_status ON reports(status);
CREATE INDEX idx_reports_period ON reports(period_start_date, period_end_date);
CREATE INDEX idx_reports_deleted_at ON reports(deleted_at);

-- Create trigger for automatic updated_at timestamp
CREATE TRIGGER update_reports_modified_column
    BEFORE UPDATE ON reports
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();