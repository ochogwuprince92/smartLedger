-- Add metadata column to ai_insights table for storing structured insight data
-- Follows the JSONB pattern already used for the recommendations column in V9

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'ai_insights' 
        AND column_name = 'metadata'
    ) THEN
        ALTER TABLE ai_insights ADD COLUMN metadata JSONB;
    END IF;
END $$;

-- Create index for metadata queries (optional, for future use)
-- CREATE INDEX IF NOT EXISTS idx_ai_insights_metadata ON ai_insights USING GIN (metadata);
