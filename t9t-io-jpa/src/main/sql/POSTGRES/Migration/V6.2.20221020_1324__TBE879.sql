-- TBE-879 - new columns in p28_cfg_async_queue (POSTGRES)
DROP VIEW IF EXISTS p28_cfg_async_queue_v;
DROP VIEW IF EXISTS p28_cfg_async_queue_nt;

ALTER TABLE p28_cfg_async_queue ADD COLUMN kafka_bootstrap_servers varchar(1024);
ALTER TABLE p28_cfg_async_queue ADD COLUMN kafka_topic varchar(36);

ALTER TABLE p28_his_async_queue ADD COLUMN kafka_bootstrap_servers varchar(1024);
ALTER TABLE p28_his_async_queue ADD COLUMN kafka_topic varchar(36);

COMMENT ON COLUMN p28_cfg_async_queue.kafka_bootstrap_servers IS 'comma-separated list of bootstrap servers (default in server config XML file)';
COMMENT ON COLUMN p28_cfg_async_queue.kafka_topic IS 'the topic to use (required for kafka based messaging)';
