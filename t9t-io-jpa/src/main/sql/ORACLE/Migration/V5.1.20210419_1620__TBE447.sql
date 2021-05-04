-- TBE-447 - new columns in p42_cfg_data_sinks ORACLE
ALTER TABLE p42_cfg_data_sinks ADD api_key raw(16);
ALTER TABLE p42_cfg_data_sinks ADD bootstrap_servers varchar2(1024 char);

ALTER TABLE p42_his_data_sinks ADD api_key raw(16);
ALTER TABLE p42_his_data_sinks ADD bootstrap_servers varchar2(1024 char);

COMMENT ON COLUMN p42_cfg_data_sinks.api_key IS 'the API key to use for data imports (kafka or Camel)';
COMMENT ON COLUMN p42_cfg_data_sinks.bootstrap_servers IS 'Kafka bootstrap servers for data exports';