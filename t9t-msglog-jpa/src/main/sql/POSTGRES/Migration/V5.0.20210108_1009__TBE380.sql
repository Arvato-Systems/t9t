-- TBE-380: add message statistics table - POSTGRES
CREATE TABLE p28_dat_message_statistics (
    -- table columns of java class TrackingBase
    -- table columns of java class WriteTracking
      c_tech_user_id varchar(16) DEFAULT CURRENT_USER NOT NULL
    , c_app_user_id varchar(16) NOT NULL
    , c_timestamp timestamp(0) DEFAULT CURRENT_TIMESTAMP NOT NULL
    , c_process_ref bigint NOT NULL
    -- table columns of java class AbstractRef
    -- table columns of java class Ref
    , object_ref bigint NOT NULL
    -- table columns of java class MessageStatisticsRef
    -- table columns of java class MessageStatisticsDTO
    , tenant_ref bigint NOT NULL
    , user_id varchar(16) NOT NULL
    , day date NOT NULL
    , request_parameter_pqon varchar(255) NOT NULL
    , count_ok integer NOT NULL
    , count_error integer NOT NULL
    , processing_time_min bigint NOT NULL
    , processing_time_max bigint NOT NULL
    , processing_time_total bigint NOT NULL
);

ALTER TABLE p28_dat_message_statistics ADD CONSTRAINT p28_dat_message_statistics_pk PRIMARY KEY (
    object_ref
);
CREATE INDEX p28_dat_message_statistics_i1 ON p28_dat_message_statistics(
    day, tenant_ref, request_parameter_pqon, user_id
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_dat_message_statistics.c_tech_user_id IS 'noinsert removed, causes problems with H2 unit tests';
COMMENT ON COLUMN p28_dat_message_statistics.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_dat_message_statistics.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class MessageStatisticsRef
-- comments for columns of java class MessageStatisticsDTO
COMMENT ON COLUMN p28_dat_message_statistics.tenant_ref IS 'identifies the tenant';
COMMENT ON COLUMN p28_dat_message_statistics.request_parameter_pqon IS 'partially qualified name of the request';
COMMENT ON COLUMN p28_dat_message_statistics.count_ok IS 'requests with return code 0..199999999';
COMMENT ON COLUMN p28_dat_message_statistics.count_error IS 'requests with return code 200000000..999999999';
COMMENT ON COLUMN p28_dat_message_statistics.processing_time_min IS 'minimum processing time in ms';
COMMENT ON COLUMN p28_dat_message_statistics.processing_time_max IS 'maximum processing time in ms';
COMMENT ON COLUMN p28_dat_message_statistics.processing_time_total IS 'sum of processing times in ms';
