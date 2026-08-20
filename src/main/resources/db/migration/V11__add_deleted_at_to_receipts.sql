-- ============================================
-- ADD SOFT DELETE COLUMN TO RECEIPTS
-- ============================================
-- The Receipt entity extends BaseEntity, which maps deletedAt, but the
-- receipts table was created without it, breaking Hibernate schema validation.

ALTER TABLE receipts ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX IF NOT EXISTS idx_receipts_deleted_at ON receipts(deleted_at);
