ALTER TABLE p28_cfg_async_channel ADD COLUMN IF NOT EXISTS client_id varchar(255);
ALTER TABLE p28_cfg_async_channel ADD COLUMN IF NOT EXISTS auth_server_url varchar(255);

ALTER TABLE p28_his_async_channel ADD COLUMN IF NOT EXISTS client_id varchar(255);
ALTER TABLE p28_his_async_channel ADD COLUMN IF NOT EXISTS auth_server_url varchar(255);

COMMENT ON COLUMN p28_cfg_async_channel.client_id IS 'Id sent to authentication server';
COMMENT ON COLUMN p28_cfg_async_channel.auth_server_url IS 'URL of the authentication server for oAuth2';
COMMENT ON COLUMN p28_his_async_channel.client_id IS 'Id sent to authentication server';
COMMENT ON COLUMN p28_his_async_channel.auth_server_url IS 'URL of the authentication server for oAuth2';

CREATE OR REPLACE FUNCTION p28_cfg_async_channel_tp() RETURNS TRIGGER AS $p28_cfg_async_channel_td$
DECLARE
    next_seq_ BIGINT;
BEGIN
    SELECT NEXTVAL('cm_idgen_5009_seq') INTO next_seq_;
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO p28_his_async_channel (
            history_seq_ref
            , history_change_type
            , object_ref
            , c_app_user_id
            , c_timestamp
            , c_process_ref
            , m_app_user_id
            , m_timestamp
            , m_process_ref
            , version
            , tenant_id
            , async_channel_id
            , is_active
            , description
            , async_queue_ref
            , url
            , auth_type
            , auth_param
            , idempotency_header
            , idempotency_header_type
            , max_retries
            , payload_format
            , serializer_qualifier
            , timeout_in_ms
            , delay_after_send
            , parallel
            , callback_url
            , z
            , client_id
            , auth_server_url
        ) VALUES (
            next_seq_, 'I'
            , NEW.object_ref
            , NEW.c_app_user_id
            , NEW.c_timestamp
            , NEW.c_process_ref
            , NEW.m_app_user_id
            , NEW.m_timestamp
            , NEW.m_process_ref
            , NEW.version
            , NEW.tenant_id
            , NEW.async_channel_id
            , NEW.is_active
            , NEW.description
            , NEW.async_queue_ref
            , NEW.url
            , NEW.auth_type
            , NEW.auth_param
            , NEW.idempotency_header
            , NEW.idempotency_header_type
            , NEW.max_retries
            , NEW.payload_format
            , NEW.serializer_qualifier
            , NEW.timeout_in_ms
            , NEW.delay_after_send
            , NEW.parallel
            , NEW.callback_url
            , NEW.z
            , NEW.client_id
            , NEW.auth_server_url
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'UPDATE') THEN
        -- deny attempts to change a primary key column
        IF OLD.object_ref <> NEW.object_ref THEN
            RAISE EXCEPTION 'Cannot change primary key column to different value';
        END IF;
        INSERT INTO p28_his_async_channel (
            history_seq_ref
            , history_change_type
            , object_ref
            , c_app_user_id
            , c_timestamp
            , c_process_ref
            , m_app_user_id
            , m_timestamp
            , m_process_ref
            , version
            , tenant_id
            , async_channel_id
            , is_active
            , description
            , async_queue_ref
            , url
            , auth_type
            , auth_param
            , idempotency_header
            , idempotency_header_type
            , max_retries
            , payload_format
            , serializer_qualifier
            , timeout_in_ms
            , delay_after_send
            , parallel
            , callback_url
            , z
            , client_id
            , auth_server_url
        ) VALUES (
            next_seq_, 'U'
            , NEW.object_ref
            , NEW.c_app_user_id
            , NEW.c_timestamp
            , NEW.c_process_ref
            , NEW.m_app_user_id
            , NEW.m_timestamp
            , NEW.m_process_ref
            , NEW.version
            , NEW.tenant_id
            , NEW.async_channel_id
            , NEW.is_active
            , NEW.description
            , NEW.async_queue_ref
            , NEW.url
            , NEW.auth_type
            , NEW.auth_param
            , NEW.idempotency_header
            , NEW.idempotency_header_type
            , NEW.max_retries
            , NEW.payload_format
            , NEW.serializer_qualifier
            , NEW.timeout_in_ms
            , NEW.delay_after_send
            , NEW.parallel
            , NEW.callback_url
            , NEW.z
            , NEW.client_id
            , NEW.auth_server_url
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO p28_his_async_channel (
            history_seq_ref
            , history_change_type
            , object_ref
            , c_app_user_id
            , c_timestamp
            , c_process_ref
            , m_app_user_id
            , m_timestamp
            , m_process_ref
            , version
            , tenant_id
            , async_channel_id
            , is_active
            , description
            , async_queue_ref
            , url
            , auth_type
            , auth_param
            , idempotency_header
            , idempotency_header_type
            , max_retries
            , payload_format
            , serializer_qualifier
            , timeout_in_ms
            , delay_after_send
            , parallel
            , callback_url
            , z
            , client_id
            , auth_server_url
        ) VALUES (
            next_seq_, 'D'
            , OLD.object_ref
            , OLD.c_app_user_id
            , OLD.c_timestamp
            , OLD.c_process_ref
            , OLD.m_app_user_id
            , OLD.m_timestamp
            , OLD.m_process_ref
            , OLD.version
            , OLD.tenant_id
            , OLD.async_channel_id
            , OLD.is_active
            , OLD.description
            , OLD.async_queue_ref
            , OLD.url
            , OLD.auth_type
            , OLD.auth_param
            , OLD.idempotency_header
            , OLD.idempotency_header_type
            , OLD.max_retries
            , OLD.payload_format
            , OLD.serializer_qualifier
            , OLD.timeout_in_ms
            , OLD.delay_after_send
            , OLD.parallel
            , OLD.callback_url
            , OLD.z
            , OLD.client_id
            , OLD.auth_server_url
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$p28_cfg_async_channel_td$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS p28_cfg_async_channel_tr ON p28_cfg_async_channel;

CREATE TRIGGER p28_cfg_async_channel_tr
    AFTER INSERT OR DELETE OR UPDATE ON p28_cfg_async_channel
    FOR EACH ROW EXECUTE PROCEDURE p28_cfg_async_channel_tp();

CREATE OR REPLACE VIEW p28_cfg_async_channel_nt AS SELECT
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
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
    , t0.idempotency_header AS idempotency_header
    , IdempotencyHeaderType2s(t0.idempotency_header_type) AS idempotency_header_type
    , t0.max_retries AS max_retries
    , t0.payload_format AS payload_format
    , t0.serializer_qualifier AS serializer_qualifier
    , t0.timeout_in_ms AS timeout_in_ms
    , t0.delay_after_send AS delay_after_send
    , t0.parallel AS parallel
    , t0.callback_url AS callback_url
    , t0.z AS z
    , t0.client_id AS client_id
    , t0.auth_server_url AS auth_server_url
FROM p28_cfg_async_channel t0;

CREATE OR REPLACE VIEW p28_cfg_async_channel_v AS SELECT
    -- columns of java class TrackingBase
    -- columns of java class WriteTracking
    t0.c_app_user_id AS c_app_user_id
    , t0.c_timestamp AS c_timestamp
    , t0.c_process_ref AS c_process_ref
    -- columns of java class FullTracking
    , t0.m_app_user_id AS m_app_user_id
    , t0.m_timestamp AS m_timestamp
    , t0.m_process_ref AS m_process_ref
    -- columns of java class FullTrackingWithVersion
    , t0.version AS version
    -- columns of java class InternalTenantId
    , t0.tenant_id AS tenant_id
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
    , t0.idempotency_header AS idempotency_header
    , IdempotencyHeaderType2s(t0.idempotency_header_type) AS idempotency_header_type
    , t0.max_retries AS max_retries
    , t0.payload_format AS payload_format
    , t0.serializer_qualifier AS serializer_qualifier
    , t0.timeout_in_ms AS timeout_in_ms
    , t0.delay_after_send AS delay_after_send
    , t0.parallel AS parallel
    , t0.callback_url AS callback_url
    , t0.z AS z
    , t0.client_id AS client_id
    , t0.auth_server_url AS auth_server_url
FROM p28_cfg_async_channel t0;
