-- Create AI Insights table for storing AI-generated financial insights and anomalies
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
