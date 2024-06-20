ALTER TABLE p28_cfg_doc_config ADD COLUMN return_path varchar(255);
ALTER TABLE p28_his_doc_config ADD COLUMN return_path varchar(255);
ALTER TABLE p28_cfg_doc_email_cfg ADD COLUMN return_path varchar(255);
ALTER TABLE p28_his_doc_email_cfg ADD COLUMN return_path varchar(255);

CREATE OR REPLACE FUNCTION p28_cfg_doc_config_tp() RETURNS TRIGGER AS $p28_cfg_doc_config_td$
DECLARE
    next_seq_ BIGINT;
BEGIN
    SELECT NEXTVAL('cm_idgen_5009_seq') INTO next_seq_;
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO p28_his_doc_config (
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
            , mapped_id
            , forward_to_channel
            , forward_to_address
            , communication_format
            , description
            , use_cids
            , email_config_per_selector
            , email_body_template_id
            , email_subject, default_from, default_reply_to, extra_to, extra_cc, extra_bcc, subject_type, replace_from, replace_reply_to, replace_to, replace_cc, replace_bcc, store_email, send_spooled, return_path
            , alternate_template_id
            , follow_mapped_id
            , time_zone
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
            , NEW.mapped_id
            , NEW.forward_to_channel
            , NEW.forward_to_address
            , NEW.communication_format
            , NEW.description
            , NEW.use_cids
            , NEW.email_config_per_selector
            , NEW.email_body_template_id
            , NEW.email_subject, NEW.default_from, NEW.default_reply_to, NEW.extra_to, NEW.extra_cc, NEW.extra_bcc, NEW.subject_type, NEW.replace_from, NEW.replace_reply_to, NEW.replace_to, NEW.replace_cc, NEW.replace_bcc, NEW.store_email, NEW.send_spooled, NEW.return_path
            , NEW.alternate_template_id
            , NEW.follow_mapped_id
            , NEW.time_zone
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'UPDATE') THEN
        -- deny attempts to change a primary key column
        IF OLD.object_ref <> NEW.object_ref THEN
            RAISE EXCEPTION 'Cannot change primary key column to different value';
        END IF;
        INSERT INTO p28_his_doc_config (
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
            , mapped_id
            , forward_to_channel
            , forward_to_address
            , communication_format
            , description
            , use_cids
            , email_config_per_selector
            , email_body_template_id
            , email_subject, default_from, default_reply_to, extra_to, extra_cc, extra_bcc, subject_type, replace_from, replace_reply_to, replace_to, replace_cc, replace_bcc, store_email, send_spooled, return_path
            , alternate_template_id
            , follow_mapped_id
            , time_zone
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
            , NEW.mapped_id
            , NEW.forward_to_channel
            , NEW.forward_to_address
            , NEW.communication_format
            , NEW.description
            , NEW.use_cids
            , NEW.email_config_per_selector
            , NEW.email_body_template_id
            , NEW.email_subject, NEW.default_from, NEW.default_reply_to, NEW.extra_to, NEW.extra_cc, NEW.extra_bcc, NEW.subject_type, NEW.replace_from, NEW.replace_reply_to, NEW.replace_to, NEW.replace_cc, NEW.replace_bcc, NEW.store_email, NEW.send_spooled, NEW.return_path
            , NEW.alternate_template_id
            , NEW.follow_mapped_id
            , NEW.time_zone
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO p28_his_doc_config (
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
            , mapped_id
            , forward_to_channel
            , forward_to_address
            , communication_format
            , description
            , use_cids
            , email_config_per_selector
            , email_body_template_id
            , email_subject, default_from, default_reply_to, extra_to, extra_cc, extra_bcc, subject_type, replace_from, replace_reply_to, replace_to, replace_cc, replace_bcc, store_email, send_spooled, return_path
            , alternate_template_id
            , follow_mapped_id
            , time_zone
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
            , OLD.mapped_id
            , OLD.forward_to_channel
            , OLD.forward_to_address
            , OLD.communication_format
            , OLD.description
            , OLD.use_cids
            , OLD.email_config_per_selector
            , OLD.email_body_template_id
            , OLD.email_subject, OLD.default_from, OLD.default_reply_to, OLD.extra_to, OLD.extra_cc, OLD.extra_bcc, OLD.subject_type, OLD.replace_from, OLD.replace_reply_to, OLD.replace_to, OLD.replace_cc, OLD.replace_bcc, OLD.store_email, OLD.send_spooled, OLD.return_path
            , OLD.alternate_template_id
            , OLD.follow_mapped_id
            , OLD.time_zone
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$p28_cfg_doc_config_td$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS p28_cfg_doc_config_tr ON p28_cfg_doc_config;

CREATE TRIGGER p28_cfg_doc_config_tr
    AFTER INSERT OR DELETE OR UPDATE ON p28_cfg_doc_config
    FOR EACH ROW EXECUTE PROCEDURE p28_cfg_doc_config_tp();

CREATE OR REPLACE VIEW p28_cfg_doc_config_nt AS SELECT
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class DocConfigRef
    -- columns of java class DocConfigDTO
    , t0.document_id AS document_id
    , t0.mapped_id AS mapped_id
    , t0.forward_to_channel AS forward_to_channel
    , t0.forward_to_address AS forward_to_address
    , t0.communication_format AS communication_format
    , t0.description AS description
    , t0.use_cids AS use_cids
    , t0.email_config_per_selector AS email_config_per_selector
    , t0.email_body_template_id AS email_body_template_id
    , t0.email_subject AS email_subject
    , t0.default_from AS default_from
    , t0.default_reply_to AS default_reply_to
    , t0.extra_to AS extra_to
    , t0.extra_cc AS extra_cc
    , t0.extra_bcc AS extra_bcc
    , TemplateType2s(t0.subject_type) AS subject_type
    , t0.replace_from AS replace_from
    , t0.replace_reply_to AS replace_reply_to
    , t0.replace_to AS replace_to
    , t0.replace_cc AS replace_cc
    , t0.replace_bcc AS replace_bcc
    , t0.store_email AS store_email
    , t0.send_spooled AS send_spooled
    , t0.alternate_template_id AS alternate_template_id
    , t0.follow_mapped_id AS follow_mapped_id
    , t0.time_zone AS time_zone
    , t0.return_path AS return_path
FROM p28_cfg_doc_config t0;

CREATE OR REPLACE VIEW p28_cfg_doc_config_v AS SELECT
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
    -- columns of java class DocConfigRef
    -- columns of java class DocConfigDTO
    , t0.document_id AS document_id
    , t0.mapped_id AS mapped_id
    , t0.forward_to_channel AS forward_to_channel
    , t0.forward_to_address AS forward_to_address
    , t0.communication_format AS communication_format
    , t0.description AS description
    , t0.use_cids AS use_cids
    , t0.email_config_per_selector AS email_config_per_selector
    , t0.email_body_template_id AS email_body_template_id
    , t0.email_subject AS email_subject
    , t0.default_from AS default_from
    , t0.default_reply_to AS default_reply_to
    , t0.extra_to AS extra_to
    , t0.extra_cc AS extra_cc
    , t0.extra_bcc AS extra_bcc
    , TemplateType2s(t0.subject_type) AS subject_type
    , t0.replace_from AS replace_from
    , t0.replace_reply_to AS replace_reply_to
    , t0.replace_to AS replace_to
    , t0.replace_cc AS replace_cc
    , t0.replace_bcc AS replace_bcc
    , t0.store_email AS store_email
    , t0.send_spooled AS send_spooled
    , t0.alternate_template_id AS alternate_template_id
    , t0.follow_mapped_id AS follow_mapped_id
    , t0.time_zone AS time_zone
    , t0.return_path AS return_path
FROM p28_cfg_doc_config t0;

CREATE OR REPLACE FUNCTION p28_cfg_doc_email_cfg_tp() RETURNS TRIGGER AS $p28_cfg_doc_email_cfg_td$
DECLARE
    next_seq_ BIGINT;
BEGIN
    SELECT NEXTVAL('cm_idgen_5009_seq') INTO next_seq_;
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO p28_his_doc_email_cfg (
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
            , email_subject, default_from, default_reply_to, extra_to, extra_cc, extra_bcc, subject_type, replace_from, replace_reply_to, replace_to, replace_cc, replace_bcc, store_email, send_spooled, return_path
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
            , NEW.email_subject, NEW.default_from, NEW.default_reply_to, NEW.extra_to, NEW.extra_cc, NEW.extra_bcc, NEW.subject_type, NEW.replace_from, NEW.replace_reply_to, NEW.replace_to, NEW.replace_cc, NEW.replace_bcc, NEW.store_email, NEW.send_spooled, NEW.return_path
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'UPDATE') THEN
        -- deny attempts to change a primary key column
        IF OLD.object_ref <> NEW.object_ref THEN
            RAISE EXCEPTION 'Cannot change primary key column to different value';
        END IF;
        INSERT INTO p28_his_doc_email_cfg (
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
            , email_subject, default_from, default_reply_to, extra_to, extra_cc, extra_bcc, subject_type, replace_from, replace_reply_to, replace_to, replace_cc, replace_bcc, store_email, send_spooled, return_path
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
            , NEW.email_subject, NEW.default_from, NEW.default_reply_to, NEW.extra_to, NEW.extra_cc, NEW.extra_bcc, NEW.subject_type, NEW.replace_from, NEW.replace_reply_to, NEW.replace_to, NEW.replace_cc, NEW.replace_bcc, NEW.store_email, NEW.send_spooled, NEW.return_path
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO p28_his_doc_email_cfg (
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
            , email_subject, default_from, default_reply_to, extra_to, extra_cc, extra_bcc, subject_type, replace_from, replace_reply_to, replace_to, replace_cc, replace_bcc, store_email, send_spooled, return_path
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
            , OLD.email_subject, OLD.default_from, OLD.default_reply_to, OLD.extra_to, OLD.extra_cc, OLD.extra_bcc, OLD.subject_type, OLD.replace_from, OLD.replace_reply_to, OLD.replace_to, OLD.replace_cc, OLD.replace_bcc, OLD.store_email, OLD.send_spooled, OLD.return_path
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$p28_cfg_doc_email_cfg_td$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS p28_cfg_doc_email_cfg_tr ON p28_cfg_doc_email_cfg;

CREATE TRIGGER p28_cfg_doc_email_cfg_tr
    AFTER INSERT OR DELETE OR UPDATE ON p28_cfg_doc_email_cfg
    FOR EACH ROW EXECUTE PROCEDURE p28_cfg_doc_email_cfg_tp();

CREATE OR REPLACE VIEW p28_cfg_doc_email_cfg_nt AS SELECT
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class DocEmailCfgRef
    -- columns of java class DocEmailCfgDTO
    , t0.document_id AS document_id
    , t0.entity_id AS entity_id
    , t0.language_code AS language_code
    , t0.country_code AS country_code
    , t0.currency_code AS currency_code
    , t0.prio AS prio
    , t0.email_subject AS email_subject
    , t0.default_from AS default_from
    , t0.default_reply_to AS default_reply_to
    , t0.extra_to AS extra_to
    , t0.extra_cc AS extra_cc
    , t0.extra_bcc AS extra_bcc
    , TemplateType2s(t0.subject_type) AS subject_type
    , t0.replace_from AS replace_from
    , t0.replace_reply_to AS replace_reply_to
    , t0.replace_to AS replace_to
    , t0.replace_cc AS replace_cc
    , t0.replace_bcc AS replace_bcc
    , t0.store_email AS store_email
    , t0.send_spooled AS send_spooled
    , t0.return_path AS return_path
FROM p28_cfg_doc_email_cfg t0;

CREATE OR REPLACE VIEW p28_cfg_doc_email_cfg_v AS SELECT
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
    -- columns of java class DocEmailCfgRef
    -- columns of java class DocEmailCfgDTO
    , t0.document_id AS document_id
    , t0.entity_id AS entity_id
    , t0.language_code AS language_code
    , t0.country_code AS country_code
    , t0.currency_code AS currency_code
    , t0.prio AS prio
    , t0.email_subject AS email_subject
    , t0.default_from AS default_from
    , t0.default_reply_to AS default_reply_to
    , t0.extra_to AS extra_to
    , t0.extra_cc AS extra_cc
    , t0.extra_bcc AS extra_bcc
    , TemplateType2s(t0.subject_type) AS subject_type
    , t0.replace_from AS replace_from
    , t0.replace_reply_to AS replace_reply_to
    , t0.replace_to AS replace_to
    , t0.replace_cc AS replace_cc
    , t0.replace_bcc AS replace_bcc
    , t0.store_email AS store_email
    , t0.send_spooled AS send_spooled
    , t0.return_path AS return_path
FROM p28_cfg_doc_email_cfg t0;
