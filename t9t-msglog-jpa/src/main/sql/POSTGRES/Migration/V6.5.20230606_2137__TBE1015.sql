-- TBE-1015: Additional columns in message log table - POSTGRES

ALTER TABLE p28_int_message ADD COLUMN hostname varchar(16);
ALTER TABLE p28_int_message ADD COLUMN server_type varchar(4);
ALTER TABLE p28_int_message ADD COLUMN partition integer;
ALTER TABLE p28_int_message ADD COLUMN processing_delay_in_millisecs integer;
ALTER TABLE p28_int_message ADD COLUMN transaction_origin_type varchar(1);

COMMENT ON COLUMN p28_int_message.hostname IS 'the (possibly abbreviated) hostname or K8s pod name';
COMMENT ON COLUMN p28_int_message.server_type IS 'the functional type of server / service (null for main)';
COMMENT ON COLUMN p28_int_message.partition IS 'in case called by a kafka topic consumer: which partition was used?';
COMMENT ON COLUMN p28_int_message.processing_delay_in_millisecs IS 'for messages transmitted via kafka: allows to measure the transmission latency';
COMMENT ON COLUMN p28_int_message.transaction_origin_type IS 'type of the initiator';

