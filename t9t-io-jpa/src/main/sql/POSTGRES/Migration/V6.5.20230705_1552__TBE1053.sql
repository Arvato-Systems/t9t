-- TBE-1053 - new columns in p42_cfg_data_sinks (POSTGRES)

ALTER TABLE p42_cfg_data_sinks ADD COLUMN json_write_pqon boolean;
ALTER TABLE p42_cfg_data_sinks ADD COLUMN json_write_nulls boolean;
ALTER TABLE p42_cfg_data_sinks ADD COLUMN json_use_enum_tokens boolean;
ALTER TABLE p42_cfg_data_sinks ADD COLUMN write_header_row boolean;

ALTER TABLE p42_his_data_sinks ADD COLUMN json_write_pqon boolean;
ALTER TABLE p42_his_data_sinks ADD COLUMN json_write_nulls boolean;
ALTER TABLE p42_his_data_sinks ADD COLUMN json_use_enum_tokens boolean;
ALTER TABLE p42_his_data_sinks ADD COLUMN write_header_row boolean;

COMMENT ON COLUMN p42_cfg_data_sinks.compute_file_size IS 'compute and store file size after data export';
COMMENT ON COLUMN p42_cfg_data_sinks.json_write_pqon IS 'write PQON info for Bonaparte JSON serializers (JSON, JSON-Kafka)';
COMMENT ON COLUMN p42_cfg_data_sinks.json_write_nulls IS 'explicitly export nulls (bonaparte and jackson)';
COMMENT ON COLUMN p42_cfg_data_sinks.json_use_enum_tokens IS 'read or write tokens or ordinals instead of name for Bonaparte JSON serializers (JSON, JSON-Kafka)';
COMMENT ON COLUMN p42_cfg_data_sinks.write_header_row IS 'write a header row if available for CSV, XLS, XLSX exporters';
