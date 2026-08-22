-- Change metadata and recommendations columns from JSONB to TEXT to fix Hibernate type mismatch
ALTER TABLE ai_insights ALTER COLUMN metadata TYPE TEXT;
ALTER TABLE ai_insights ALTER COLUMN recommendations TYPE TEXT;
