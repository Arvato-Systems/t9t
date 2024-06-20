ALTER TABLE p28_dat_email ADD COLUMN return_path varchar(255);
ALTER TABLE p28_cfg_email_module_cfg ADD COLUMN default_return_path varchar(255);
ALTER TABLE p28_his_email_module_cfg ADD COLUMN default_return_path varchar(255);

CREATE OR REPLACE VIEW p28_dat_email_nt AS SELECT
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class EmailRef
    -- columns of java class EmailDTO
    , t0.message_id AS message_id
    , t0.email_subject AS email_subject
    , t0.email_from AS email_from
    , t0.reply_to AS reply_to
    , t0.email_to AS email_to
    , t0.email_cc AS email_cc
    , t0.email_bcc AS email_bcc
    , t0.number_of_attachments AS number_of_attachments
    , EmailStatus2s(t0.email_status) AS email_status
    , t0.bounced AS bounced
    , t0.bounce_message AS bounce_message
    , t0.return_path AS return_path
FROM p28_dat_email t0;

CREATE OR REPLACE VIEW p28_dat_email_v AS SELECT
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
    -- columns of java class EmailRef
    -- columns of java class EmailDTO
    , t0.message_id AS message_id
    , t0.email_subject AS email_subject
    , t0.email_from AS email_from
    , t0.reply_to AS reply_to
    , t0.email_to AS email_to
    , t0.email_cc AS email_cc
    , t0.email_bcc AS email_bcc
    , t0.number_of_attachments AS number_of_attachments
    , EmailStatus2s(t0.email_status) AS email_status
    , t0.bounced AS bounced
    , t0.bounce_message AS bounce_message
    , t0.return_path AS return_path
FROM p28_dat_email t0;

CREATE OR REPLACE FUNCTION p28_cfg_email_module_cfg_tp() RETURNS TRIGGER AS $p28_cfg_email_module_cfg_td$
DECLARE
    next_seq_ BIGINT;
BEGIN
    SELECT NEXTVAL('cm_idgen_5009_seq') INTO next_seq_;
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO p28_his_email_module_cfg (
            history_seq_ref
            , history_change_type
            , tenant_id
            , c_app_user_id
            , c_timestamp
            , c_process_ref
            , m_app_user_id
            , m_timestamp
            , m_process_ref
            , version
            , z
            , implementation
            , smtp_server_transport
            , smtp_server_address
            , smtp_server_port
            , smtp_server_user_id
            , smtp_server_password
            , smtp_server_tls
            , default_return_path
        ) VALUES (
            next_seq_, 'I'
            , NEW.tenant_id
            , NEW.c_app_user_id
            , NEW.c_timestamp
            , NEW.c_process_ref
            , NEW.m_app_user_id
            , NEW.m_timestamp
            , NEW.m_process_ref
            , NEW.version
            , NEW.z
            , NEW.implementation
            , NEW.smtp_server_transport
            , NEW.smtp_server_address
            , NEW.smtp_server_port
            , NEW.smtp_server_user_id
            , NEW.smtp_server_password
            , NEW.smtp_server_tls
            , NEW.default_return_path
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'UPDATE') THEN
        -- deny attempts to change a primary key column
        IF OLD.tenant_id <> NEW.tenant_id THEN
            RAISE EXCEPTION 'Cannot change primary key column to different value';
        END IF;
        INSERT INTO p28_his_email_module_cfg (
            history_seq_ref
            , history_change_type
            , tenant_id
            , c_app_user_id
            , c_timestamp
            , c_process_ref
            , m_app_user_id
            , m_timestamp
            , m_process_ref
            , version
            , z
            , implementation
            , smtp_server_transport
            , smtp_server_address
            , smtp_server_port
            , smtp_server_user_id
            , smtp_server_password
            , smtp_server_tls
            , default_return_path
        ) VALUES (
            next_seq_, 'U'
            , NEW.tenant_id
            , NEW.c_app_user_id
            , NEW.c_timestamp
            , NEW.c_process_ref
            , NEW.m_app_user_id
            , NEW.m_timestamp
            , NEW.m_process_ref
            , NEW.version
            , NEW.z
            , NEW.implementation
            , NEW.smtp_server_transport
            , NEW.smtp_server_address
            , NEW.smtp_server_port
            , NEW.smtp_server_user_id
            , NEW.smtp_server_password
            , NEW.smtp_server_tls
            , NEW.default_return_path
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO p28_his_email_module_cfg (
            history_seq_ref
            , history_change_type
            , tenant_id
            , c_app_user_id
            , c_timestamp
            , c_process_ref
            , m_app_user_id
            , m_timestamp
            , m_process_ref
            , version
            , z
            , implementation
            , smtp_server_transport
            , smtp_server_address
            , smtp_server_port
            , smtp_server_user_id
            , smtp_server_password
            , smtp_server_tls
            , default_return_path
        ) VALUES (
            next_seq_, 'D'
            , OLD.tenant_id
            , OLD.c_app_user_id
            , OLD.c_timestamp
            , OLD.c_process_ref
            , OLD.m_app_user_id
            , OLD.m_timestamp
            , OLD.m_process_ref
            , OLD.version
            , OLD.z
            , OLD.implementation
            , OLD.smtp_server_transport
            , OLD.smtp_server_address
            , OLD.smtp_server_port
            , OLD.smtp_server_user_id
            , OLD.smtp_server_password
            , OLD.smtp_server_tls
            , OLD.default_return_path
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$p28_cfg_email_module_cfg_td$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS p28_cfg_email_module_cfg_tr ON p28_cfg_email_module_cfg;

CREATE TRIGGER p28_cfg_email_module_cfg_tr
    AFTER INSERT OR DELETE OR UPDATE ON p28_cfg_email_module_cfg
    FOR EACH ROW EXECUTE PROCEDURE p28_cfg_email_module_cfg_tp();

CREATE OR REPLACE VIEW p28_cfg_email_module_cfg_nt AS SELECT
    -- columns of java class CompositeKeyBase
    -- columns of java class ModuleConfigDTO
    t0.z AS z
    -- columns of java class EmailModuleCfgDTO
    , t0.implementation AS implementation
    , t0.smtp_server_transport AS smtp_server_transport
    , t0.smtp_server_address AS smtp_server_address
    , t0.smtp_server_port AS smtp_server_port
    , t0.smtp_server_user_id AS smtp_server_user_id
    , t0.smtp_server_password AS smtp_server_password
    , t0.smtp_server_tls AS smtp_server_tls
    , t0.default_return_path AS default_return_path
FROM p28_cfg_email_module_cfg t0;

CREATE OR REPLACE VIEW p28_cfg_email_module_cfg_v AS SELECT
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
    -- columns of java class CompositeKeyBase
    -- columns of java class ModuleConfigDTO
    , t0.z AS z
    -- columns of java class EmailModuleCfgDTO
    , t0.implementation AS implementation
    , t0.smtp_server_transport AS smtp_server_transport
    , t0.smtp_server_address AS smtp_server_address
    , t0.smtp_server_port AS smtp_server_port
    , t0.smtp_server_user_id AS smtp_server_user_id
    , t0.smtp_server_password AS smtp_server_password
    , t0.smtp_server_tls AS smtp_server_tls
    , t0.default_return_path AS default_return_path
FROM p28_cfg_email_module_cfg t0;
