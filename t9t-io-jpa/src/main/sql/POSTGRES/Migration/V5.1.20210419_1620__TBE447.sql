-- TBE-447 - new columns in p42_cfg_data_sinks POSTGRES
DROP VIEW IF EXISTS p42_cfg_data_sinks_nt;
DROP VIEW IF EXISTS p42_cfg_data_sinks_v;
ALTER TABLE p42_cfg_data_sinks ADD COLUMN api_key uuid;
ALTER TABLE p42_cfg_data_sinks ADD COLUMN bootstrap_servers varchar(1024);

ALTER TABLE p42_his_data_sinks ADD COLUMN api_key uuid;
ALTER TABLE p42_his_data_sinks ADD COLUMN bootstrap_servers varchar(1024);

COMMENT ON COLUMN p42_cfg_data_sinks.api_key IS 'the API key to use for data imports (kafka or Camel)';
COMMENT ON COLUMN p42_cfg_data_sinks.bootstrap_servers IS 'Kafka bootstrap servers for data exports';