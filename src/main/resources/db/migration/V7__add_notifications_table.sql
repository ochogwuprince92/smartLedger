-- Add notifications table for notification module
-- This migration creates the notifications table for managing system notifications

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
    metadata TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL
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

-- Create trigger for automatic updated_at timestamp
CREATE TRIGGER update_notifications_modified_column
    BEFORE UPDATE ON notifications
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- Add notification permissions
INSERT INTO permissions (id, code, name, description, resource, action, created_at, created_by, updated_at, updated_by)
VALUES 
  (gen_random_uuid(), 'NOTIFICATION:CREATE', 'Create Notification', 'Create new notifications', 'notification', 'create', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'NOTIFICATION:READ', 'Read Notification', 'Read notification information', 'notification', 'read', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'NOTIFICATION:UPDATE', 'Update Notification', 'Update notification information', 'notification', 'update', NOW(), 'SYSTEM', NOW(), 'SYSTEM'),
  (gen_random_uuid(), 'NOTIFICATION:DELETE', 'Delete Notification', 'Delete notifications', 'notification', 'delete', NOW(), 'SYSTEM', NOW(), 'SYSTEM')
ON CONFLICT (code) DO NOTHING;
