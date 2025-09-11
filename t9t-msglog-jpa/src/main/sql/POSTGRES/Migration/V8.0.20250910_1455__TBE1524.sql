-- TBE-1524: Additional counter fields for slow transactions in transaction aggregation table - POSTGRES

DROP VIEW IF EXISTS p28_dat_message_statistics_nt;
DROP VIEW IF EXISTS p28_dat_message_statistics_v;

ALTER TABLE p28_dat_message_statistics ADD COLUMN IF NOT EXISTS count_long1 integer;
ALTER TABLE p28_dat_message_statistics ADD COLUMN IF NOT EXISTS count_long2 integer;
ALTER TABLE p28_dat_message_statistics ADD COLUMN IF NOT EXISTS count_long3 integer;

COMMENT ON COLUMN p28_dat_message_statistics.count_long1 IS 'counter for long running transactions, step 1 (by default >= 1 second)';
COMMENT ON COLUMN p28_dat_message_statistics.count_long2 IS 'counter for long running transactions, step 2 (by default >= 2 seconds)';
COMMENT ON COLUMN p28_dat_message_statistics.count_long3 IS 'counter for long running transactions, step 3 (by default >= 5 seconds)';

CREATE OR REPLACE VIEW p28_dat_message_statistics_nt AS SELECT
    -- columns of java class AbstractRef
    -- columns of java class Ref
    t0.object_ref AS object_ref
    -- columns of java class MessageStatisticsRef
    -- columns of java class MessageStatisticsDTO
    , t0.slot_start AS slot_start
    , t0.tenant_id AS tenant_id
    , t0.hostname AS hostname
    , t0.server_type AS server_type
    , t0.partition AS partition
    , TransactionOriginType2s(t0.transaction_origin_type) AS transaction_origin_type
    , t0.user_id AS user_id
    , t0.request_parameter_pqon AS request_parameter_pqon
    , t0.count_ok AS count_ok
    , t0.count_error AS count_error
    , t0.processing_time_max AS processing_time_max
    , t0.processing_time_total AS processing_time_total
    , t0.processing_delay_max AS processing_delay_max
    , t0.processing_delay_total AS processing_delay_total
    , t0.retries_done AS retries_done
    , t0.count_long1 AS count_long1
    , t0.count_long2 AS count_long2
    , t0.count_long3 AS count_long3
FROM p28_dat_message_statistics t0;

CREATE OR REPLACE VIEW p28_dat_message_statistics_v AS SELECT
    -- columns of java class TrackingBase
    -- columns of java class NoTracking
    -- columns of java class AbstractRef
    -- columns of java class Ref
    t0.object_ref AS object_ref
    -- columns of java class MessageStatisticsRef
    -- columns of java class MessageStatisticsDTO
    , t0.slot_start AS slot_start
    , t0.tenant_id AS tenant_id
    , t0.hostname AS hostname
    , t0.server_type AS server_type
    , t0.partition AS partition
    , TransactionOriginType2s(t0.transaction_origin_type) AS transaction_origin_type
    , t0.user_id AS user_id
    , t0.request_parameter_pqon AS request_parameter_pqon
    , t0.count_ok AS count_ok
    , t0.count_error AS count_error
    , t0.processing_time_max AS processing_time_max
    , t0.processing_time_total AS processing_time_total
    , t0.processing_delay_max AS processing_delay_max
    , t0.processing_delay_total AS processing_delay_total
    , t0.retries_done AS retries_done
    , t0.count_long1 AS count_long1
    , t0.count_long2 AS count_long2
    , t0.count_long3 AS count_long3
FROM p28_dat_message_statistics t0;
