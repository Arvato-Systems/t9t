-- TBE-1373: increase size of reference field of table p42_int_async_messages (POSTGRES)

DROP VIEW IF EXISTS p42_int_async_messages_v;
DROP VIEW IF EXISTS p42_int_async_messages_nt;

ALTER TABLE p42_int_async_messages ALTER COLUMN reference TYPE varchar(1024);

CREATE OR REPLACE VIEW p28_dat_async_message_statistics_nt AS SELECT
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class AsyncMessageStatisticsRef
    -- columns of java class AsyncMessageStatisticsDTO
    , t0.slot_start AS slot_start
    , t0.async_channel_id AS async_channel_id
    , t0.ref_type AS ref_type
    , ExportStatusEnum2s(t0.status) AS status
    , t0.http_response_code AS http_response_code
    , t0.count AS count
    , t0.attempts AS attempts
    , t0.response_time AS response_time
FROM p28_dat_async_message_statistics t0;

CREATE OR REPLACE VIEW p28_dat_async_message_statistics_v AS SELECT
    -- columns of java class TrackingBase
    -- columns of java class NoTracking
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class AsyncMessageStatisticsRef
    -- columns of java class AsyncMessageStatisticsDTO
    , t0.slot_start AS slot_start
    , t0.async_channel_id AS async_channel_id
    , t0.ref_type AS ref_type
    , ExportStatusEnum2s(t0.status) AS status
    , t0.http_response_code AS http_response_code
    , t0.count AS count
    , t0.attempts AS attempts
    , t0.response_time AS response_time
FROM p28_dat_async_message_statistics t0;
