-- Add metadata column to ai_insights table for storing structured insight data
-- Follows the JSONB pattern already used for the recommendations column in V9

ALTER TABLE ai_insights
ADD COLUMN metadata JSONB;

-- Create index for metadata queries (optional, for future use)
-- CREATE INDEX idx_ai_insights_metadata ON ai_insights USING GIN (metadata);
