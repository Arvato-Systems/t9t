-- TBE-397 - new columns in p42_cfg_data_sinks ORACLE

ALTER TABLE p42_cfg_data_sinks ADD retention_period_files NUMBER(10);
ALTER TABLE p42_cfg_data_sinks ADD retention_period_sinks NUMBER(10);

ALTER TABLE p42_his_data_sinks ADD retention_period_files NUMBER(10);
ALTER TABLE p42_his_data_sinks ADD retention_period_sinks NUMBER(10);

COMMENT ON COLUMN p42_cfg_data_sinks.retention_period_files IS 'the number of days after which an associated file can be deleted (never if null)';
COMMENT ON COLUMN p42_cfg_data_sinks.retention_period_sinks IS 'the number of days after which an associated sink entry can be deleted (never if null)';
