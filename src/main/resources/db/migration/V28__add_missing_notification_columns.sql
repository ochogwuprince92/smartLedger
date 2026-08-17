-- Add missing columns to notifications table for soft delete and retry support
-- This migration adds the deleted_at and last_retry_at columns that were omitted in V7

ALTER TABLE notifications ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS last_retry_at TIMESTAMP WITH TIME ZONE;

-- Create indexes for the new columns for efficient queries
CREATE INDEX IF NOT EXISTS idx_notifications_deleted_at ON notifications(deleted_at);
CREATE INDEX IF NOT EXISTS idx_notifications_last_retry_at ON notifications(last_retry_at);