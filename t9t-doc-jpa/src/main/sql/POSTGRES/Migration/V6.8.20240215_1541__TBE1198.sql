-- TBE-1198: additional field storage location for doc component (POSTGRES)

ALTER TABLE p28_cfg_doc_component ADD COLUMN media_storage_location varchar(1);
ALTER TABLE p28_his_doc_component ADD COLUMN media_storage_location varchar(1);

COMMENT ON COLUMN p28_cfg_doc_component.media_storage_location IS 'null for inline data, otherwise the type of storage';

CREATE OR REPLACE FUNCTION p28_cfg_doc_component_tp() RETURNS TRIGGER AS $p28_cfg_doc_component_td$
DECLARE
    next_seq_ BIGINT;
BEGIN
    SELECT NEXTVAL('cm_idgen_5009_seq') INTO next_seq_;
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO p28_his_doc_component (
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
            , document_id
            , entity_id
            , language_code
            , country_code
            , currency_code
            , prio
            , name
            , media_type, text, raw_data, z, media_storage_location
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
            , NEW.document_id
            , NEW.entity_id
            , NEW.language_code
            , NEW.country_code
            , NEW.currency_code
            , NEW.prio
            , NEW.name
            , NEW.media_type, NEW.text, NEW.raw_data, NEW.z, NEW.media_storage_location
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'UPDATE') THEN
        -- deny attempts to change a primary key column
        IF OLD.object_ref <> NEW.object_ref THEN
            RAISE EXCEPTION 'Cannot change primary key column to different value';
        END IF;
        INSERT INTO p28_his_doc_component (
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
            , document_id
            , entity_id
            , language_code
            , country_code
            , currency_code
            , prio
            , name
            , media_type, text, raw_data, z, media_storage_location
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
            , NEW.document_id
            , NEW.entity_id
            , NEW.language_code
            , NEW.country_code
            , NEW.currency_code
            , NEW.prio
            , NEW.name
            , NEW.media_type, NEW.text, NEW.raw_data, NEW.z, NEW.media_storage_location
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO p28_his_doc_component (
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
            , document_id
            , entity_id
            , language_code
            , country_code
            , currency_code
            , prio
            , name
            , media_type, text, raw_data, z, media_storage_location
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
            , OLD.document_id
            , OLD.entity_id
            , OLD.language_code
            , OLD.country_code
            , OLD.currency_code
            , OLD.prio
            , OLD.name
            , OLD.media_type, OLD.text, OLD.raw_data, OLD.z, OLD.media_storage_location
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$p28_cfg_doc_component_td$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS p28_cfg_doc_component_tr ON p28_cfg_doc_component;

CREATE TRIGGER p28_cfg_doc_component_tr
    AFTER INSERT OR DELETE OR UPDATE ON p28_cfg_doc_component
    FOR EACH ROW EXECUTE PROCEDURE p28_cfg_doc_component_tp();
