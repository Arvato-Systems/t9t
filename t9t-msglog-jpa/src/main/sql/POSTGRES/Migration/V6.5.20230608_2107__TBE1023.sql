-- TBE-1023: Recreate message aggregation table - POSTGRES

DROP VIEW IF EXISTS p28_int_message_v;
DROP VIEW IF EXISTS p28_int_message_nt;

DROP TABLE IF EXISTS p28_dat_message_statistics CASCADE;

CREATE TABLE p28_dat_message_statistics (
    -- table columns of java class TrackingBase
    -- table columns of java class NoTracking
    -- table columns of java class AbstractRef
    -- table columns of java class Ref
      object_ref bigint NOT NULL
    -- table columns of java class MessageStatisticsRef
    -- table columns of java class MessageStatisticsDTO
    , slot_start timestamp(0) NOT NULL
    , tenant_id varchar(16) NOT NULL
    , hostname varchar(16) NOT NULL
    , server_type varchar(4) NOT NULL
    , partition integer NOT NULL
    , transaction_origin_type varchar(1) NOT NULL
    , user_id varchar(16) NOT NULL
    , request_parameter_pqon varchar(255) NOT NULL
    , count_ok integer NOT NULL
    , count_error integer NOT NULL
    , processing_time_max bigint NOT NULL
    , processing_time_total bigint NOT NULL
    , processing_delay_max bigint NOT NULL
    , processing_delay_total bigint NOT NULL
);

ALTER TABLE p28_dat_message_statistics ADD CONSTRAINT p28_dat_message_statistics_pk PRIMARY KEY (
    object_ref
);
CREATE INDEX p28_dat_message_statistics_i1 ON p28_dat_message_statistics(
    slot_start, tenant_id, hostname, server_type, partition, transaction_origin_type, user_id, request_parameter_pqon
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class NoTracking
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_dat_message_statistics.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class MessageStatisticsRef
-- comments for columns of java class MessageStatisticsDTO
COMMENT ON COLUMN p28_dat_message_statistics.tenant_id IS 'identifies the tenant';
COMMENT ON COLUMN p28_dat_message_statistics.hostname IS 'the (possibly abbreviated) hostname or K8s pod name';
COMMENT ON COLUMN p28_dat_message_statistics.server_type IS 'the functional type of server / service (null for main)';
COMMENT ON COLUMN p28_dat_message_statistics.partition IS 'in case called by a kafka topic consumer: which partition was used?';
COMMENT ON COLUMN p28_dat_message_statistics.transaction_origin_type IS 'type of the initiator';
COMMENT ON COLUMN p28_dat_message_statistics.request_parameter_pqon IS 'partially qualified name of the request';
COMMENT ON COLUMN p28_dat_message_statistics.count_ok IS 'requests with return code 0..199999999';
COMMENT ON COLUMN p28_dat_message_statistics.count_error IS 'requests with return code 200000000..999999999';
COMMENT ON COLUMN p28_dat_message_statistics.processing_time_max IS 'maximum processing time in ms';
COMMENT ON COLUMN p28_dat_message_statistics.processing_time_total IS 'sum of processing times in ms';
COMMENT ON COLUMN p28_dat_message_statistics.processing_delay_max IS 'how long after executionStartedAt was the received (for async messages)';
COMMENT ON COLUMN p28_dat_message_statistics.processing_delay_total IS 'how long after executionStartedAt was the received (for async messages)';
