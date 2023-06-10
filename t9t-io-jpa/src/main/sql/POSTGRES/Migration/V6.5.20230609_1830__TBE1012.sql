DROP TABLE p28_dat_async_message_statistics CASCADE;

-- TABLE
CREATE TABLE p28_dat_async_message_statistics (
    -- table columns of java class TrackingBase
    -- table columns of java class NoTracking
    -- table columns of java class InternalTenantId
      tenant_id varchar(16) NOT NULL
    -- table columns of java class AbstractRef
    -- table columns of java class Ref
    , object_ref bigint NOT NULL
    -- table columns of java class AsyncMessageStatisticsRef
    -- table columns of java class AsyncMessageStatisticsDTO
    , slot_start timestamp(0) NOT NULL
    , async_channel_id varchar(16) NOT NULL
    , ref_type varchar(4) NOT NULL
    , status varchar(1)
    , http_response_code integer NOT NULL
    , count integer NOT NULL
    , attempts integer NOT NULL
    , response_time bigint NOT NULL
);

ALTER TABLE p28_dat_async_message_statistics ADD CONSTRAINT p28_dat_async_message_statistics_pk PRIMARY KEY (
    object_ref
);
CREATE UNIQUE INDEX p28_dat_async_message_statistics_u1 ON p28_dat_async_message_statistics(
    tenant_id, slot_start, async_channel_id, ref_type, status, http_response_code
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class NoTracking
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_dat_async_message_statistics.tenant_id IS 'the multitenancy discriminator';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_dat_async_message_statistics.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class AsyncMessageStatisticsRef
-- comments for columns of java class AsyncMessageStatisticsDTO
COMMENT ON COLUMN p28_dat_async_message_statistics.slot_start IS 'beginning of time slot';
COMMENT ON COLUMN p28_dat_async_message_statistics.async_channel_id IS 'used to retrieve the URL and authentication parameters';
COMMENT ON COLUMN p28_dat_async_message_statistics.ref_type IS 'for debugging / maintenance: the type of reference';
COMMENT ON COLUMN p28_dat_async_message_statistics.status IS 'specifies if the message must still be sent or has been sent';
COMMENT ON COLUMN p28_dat_async_message_statistics.http_response_code IS 'if the remote returned some http response code, or 0 for none';
COMMENT ON COLUMN p28_dat_async_message_statistics.count IS 'the number of messages';
COMMENT ON COLUMN p28_dat_async_message_statistics.attempts IS 'number of send attempts so far (initially 0)';
COMMENT ON COLUMN p28_dat_async_message_statistics.response_time IS 'aggregated figures of lastResponseTime';

-- VIEW
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

-- SEQUENCE
CREATE SEQUENCE p28_dat_async_message_statistics_s;
