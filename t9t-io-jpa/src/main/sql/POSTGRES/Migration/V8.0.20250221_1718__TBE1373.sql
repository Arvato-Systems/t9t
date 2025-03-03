-- TBE-1373: additional script to renew the correct views (POSTGRES)

DROP VIEW IF EXISTS p42_int_async_messages_v;
DROP VIEW IF EXISTS p42_int_async_messages_nt;

CREATE OR REPLACE VIEW p42_int_async_messages_nt AS SELECT
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class AsyncMessageRef
    -- columns of java class AsyncMessageDTO
    , t0.async_channel_id AS async_channel_id
    , t0.async_queue_ref AS async_queue_ref
    , ExportStatusEnum2s(t0.status) AS status
    , t0.when_sent AS when_sent
    , t0.last_attempt AS last_attempt
    , t0.attempts AS attempts
    , t0.payload AS payload
    , t0.ref_type AS ref_type
    , t0.ref_identifier AS ref_identifier
    , t0.ref AS ref
    , t0.http_response_code AS http_response_code
    , t0.return_code AS return_code
    , t0.reference AS reference
    , t0.error_details AS error_details
    , t0.last_response_time AS last_response_time
FROM p42_int_async_messages t0;

CREATE OR REPLACE VIEW p42_int_async_messages_v AS SELECT
    -- columns of java class TrackingBase
    -- columns of java class WriteTrackingMs
    t0.c_app_user_id AS c_app_user_id
    , t0.c_timestamp AS c_timestamp
    , t0.c_process_ref AS c_process_ref
    -- columns of java class InternalTenantId
    , t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class AsyncMessageRef
    -- columns of java class AsyncMessageDTO
    , t0.async_channel_id AS async_channel_id
    , t0.async_queue_ref AS async_queue_ref
    , ExportStatusEnum2s(t0.status) AS status
    , t0.when_sent AS when_sent
    , t0.last_attempt AS last_attempt
    , t0.attempts AS attempts
    , t0.payload AS payload
    , t0.ref_type AS ref_type
    , t0.ref_identifier AS ref_identifier
    , t0.ref AS ref
    , t0.http_response_code AS http_response_code
    , t0.return_code AS return_code
    , t0.reference AS reference
    , t0.error_details AS error_details
    , t0.last_response_time AS last_response_time
FROM p42_int_async_messages t0;
