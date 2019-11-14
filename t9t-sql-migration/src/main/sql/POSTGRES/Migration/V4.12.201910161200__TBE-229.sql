-- TBE-229 Extend async channel field length 

DROP VIEW IF EXISTS p28_cfg_async_channel_nt;
DROP VIEW IF EXISTS p28_cfg_async_channel_v;

ALTER TABLE p28_cfg_async_channel ALTER column url TYPE character varying(300);
ALTER TABLE p28_his_async_channel ALTER column url TYPE character varying(300);

CREATE OR REPLACE VIEW p28_cfg_async_channel_nt AS SELECT
    -- columns of java class InternalTenantRef42
    t0.tenant_ref AS tenant_ref
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class AsyncChannelRef
    -- columns of java class AsyncChannelDTO
    , t0.async_channel_id AS async_channel_id
    , t0.is_active AS is_active
    , t0.description AS description
    , t0.async_queue_ref AS async_queue_ref
    , t0.url AS url
    , t0.auth_type AS auth_type
    , t0.auth_param AS auth_param
    , t0.max_retries AS max_retries
    , t0.payload_format AS payload_format
    , t0.serializer_qualifier AS serializer_qualifier
    , t0.timeout_in_ms AS timeout_in_ms
    , t0.z AS z
FROM p28_cfg_async_channel t0;


CREATE OR REPLACE VIEW p28_cfg_async_channel_v AS SELECT
    -- columns of java class TrackingBase
    -- columns of java class WriteTracking
    t0.c_tech_user_id AS c_tech_user_id
    , t0.c_app_user_id AS c_app_user_id
    , t0.c_timestamp AS c_timestamp
    , t0.c_process_ref AS c_process_ref
    -- columns of java class FullTracking
    , t0.m_tech_user_id AS m_tech_user_id
    , t0.m_app_user_id AS m_app_user_id
    , t0.m_timestamp AS m_timestamp
    , t0.m_process_ref AS m_process_ref
    -- columns of java class FullTrackingWithVersion
    , t0.version AS version
    -- columns of java class InternalTenantRef42
    , t0.tenant_ref AS tenant_ref
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class AsyncChannelRef
    -- columns of java class AsyncChannelDTO
    , t0.async_channel_id AS async_channel_id
    , t0.is_active AS is_active
    , t0.description AS description
    , t0.async_queue_ref AS async_queue_ref
    , t0.url AS url
    , t0.auth_type AS auth_type
    , t0.auth_param AS auth_param
    , t0.max_retries AS max_retries
    , t0.payload_format AS payload_format
    , t0.serializer_qualifier AS serializer_qualifier
    , t0.timeout_in_ms AS timeout_in_ms
    , t0.z AS z
FROM p28_cfg_async_channel t0;