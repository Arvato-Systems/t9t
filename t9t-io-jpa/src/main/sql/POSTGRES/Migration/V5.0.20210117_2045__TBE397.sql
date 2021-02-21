-- TBE-397 - new columns in p42_cfg_data_sinks POSTGRES
DROP VIEW IF EXISTS p42_cfg_data_sinks_nt;
DROP VIEW IF EXISTS p42_cfg_data_sinks_v;
ALTER TABLE p42_cfg_data_sinks ADD COLUMN retention_period_files integer;
ALTER TABLE p42_cfg_data_sinks ADD COLUMN retention_period_sinks integer;

ALTER TABLE p42_his_data_sinks ADD COLUMN retention_period_files integer;
ALTER TABLE p42_his_data_sinks ADD COLUMN retention_period_sinks integer;

COMMENT ON COLUMN p42_cfg_data_sinks.retention_period_files IS 'the number of days after which an associated file can be deleted (never if null)';
COMMENT ON COLUMN p42_cfg_data_sinks.retention_period_sinks IS 'the number of days after which an associated sink entry can be deleted (never if null)';