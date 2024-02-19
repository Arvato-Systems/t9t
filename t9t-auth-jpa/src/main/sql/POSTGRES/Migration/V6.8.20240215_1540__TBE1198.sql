-- TBE-1198: additional field storage location for tenant logo (POSTGRES)

ALTER TABLE p28_cfg_tenant_logo ADD COLUMN logo_media_storage_location varchar(1);
ALTER TABLE p28_his_tenant_logo ADD COLUMN logo_media_storage_location varchar(1);

COMMENT ON COLUMN p28_cfg_tenant_logo.logo_media_storage_location IS 'null for inline data, otherwise the type of storage';

CREATE OR REPLACE FUNCTION p28_cfg_tenant_logo_tp() RETURNS TRIGGER AS $p28_cfg_tenant_logo_td$
DECLARE
    next_seq_ BIGINT;
BEGIN
    SELECT NEXTVAL('cm_idgen_5009_seq') INTO next_seq_;
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO p28_his_tenant_logo (
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
            , logo_media_type, logo_text, logo_raw_data, logo_z, logo_media_storage_location
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
            , NEW.logo_media_type, NEW.logo_text, NEW.logo_raw_data, NEW.logo_z, NEW.logo_media_storage_location
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'UPDATE') THEN
        -- deny attempts to change a primary key column
        IF OLD.tenant_id <> NEW.tenant_id THEN
            RAISE EXCEPTION 'Cannot change primary key column to different value';
        END IF;
        INSERT INTO p28_his_tenant_logo (
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
            , logo_media_type, logo_text, logo_raw_data, logo_z, logo_media_storage_location
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
            , NEW.logo_media_type, NEW.logo_text, NEW.logo_raw_data, NEW.logo_z, NEW.logo_media_storage_location
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO p28_his_tenant_logo (
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
            , logo_media_type, logo_text, logo_raw_data, logo_z, logo_media_storage_location
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
            , OLD.logo_media_type, OLD.logo_text, OLD.logo_raw_data, OLD.logo_z, OLD.logo_media_storage_location
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$p28_cfg_tenant_logo_td$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS p28_cfg_tenant_logo_tr ON p28_cfg_tenant_logo;

CREATE TRIGGER p28_cfg_tenant_logo_tr
    AFTER INSERT OR DELETE OR UPDATE ON p28_cfg_tenant_logo
    FOR EACH ROW EXECUTE PROCEDURE p28_cfg_tenant_logo_tp();
