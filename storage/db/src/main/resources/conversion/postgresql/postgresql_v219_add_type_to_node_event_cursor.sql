ALTER TABLE node_event_cursor ADD COLUMN type VARCHAR(255) DEFAULT 'UNDEFINED' NOT NULL;
UPDATE node_event_cursor SET operator_id = 'metadata-operator-events', type = 'METADATA_EVENTS' WHERE operator_id = 'metadata-operator';