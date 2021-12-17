-- TBE-654: - Update email column size (POSTGRES)

-- Step 1: Drop views
DROP VIEW p28_cfg_doc_config_v;
DROP VIEW p28_cfg_doc_config_nt;
DROP VIEW p28_cfg_doc_email_cfg_v;
DROP VIEW p28_cfg_doc_email_cfg_nt;

-- Step 2: Alter column type
ALTER TABLE p28_cfg_doc_config ALTER COLUMN extra_to TYPE varchar(32767);
ALTER TABLE p28_cfg_doc_config ALTER COLUMN extra_cc TYPE varchar(32767);
ALTER TABLE p28_cfg_doc_config ALTER COLUMN extra_bcc TYPE varchar(32767);

ALTER TABLE p28_his_doc_config ALTER COLUMN extra_to TYPE varchar(32767);
ALTER TABLE p28_his_doc_config ALTER COLUMN extra_cc TYPE varchar(32767);
ALTER TABLE p28_his_doc_config ALTER COLUMN extra_bcc TYPE varchar(32767);

ALTER TABLE p28_cfg_doc_email_cfg ALTER COLUMN extra_to TYPE varchar(32767);
ALTER TABLE p28_cfg_doc_email_cfg ALTER COLUMN extra_cc TYPE varchar(32767);
ALTER TABLE p28_cfg_doc_email_cfg ALTER COLUMN extra_bcc TYPE varchar(32767);

ALTER TABLE p28_his_doc_email_cfg ALTER COLUMN extra_to TYPE varchar(32767);
ALTER TABLE p28_his_doc_email_cfg ALTER COLUMN extra_cc TYPE varchar(32767);
ALTER TABLE p28_his_doc_email_cfg ALTER COLUMN extra_bcc TYPE varchar(32767);

-- Step 3: Recreate views
CREATE OR REPLACE VIEW p28_cfg_doc_config_v AS SELECT
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
FROM p28_cfg_doc_config t0;

CREATE OR REPLACE VIEW p28_cfg_doc_config_nt AS SELECT
    -- columns of java class InternalTenantRef42
    t0.tenant_ref AS tenant_ref
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
FROM p28_cfg_doc_config t0;

CREATE OR REPLACE VIEW p28_cfg_doc_email_cfg_v AS SELECT
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
FROM p28_cfg_doc_email_cfg t0;

CREATE OR REPLACE VIEW p28_cfg_doc_email_cfg_nt AS SELECT
    -- columns of java class InternalTenantRef42
    t0.tenant_ref AS tenant_ref
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
FROM p28_cfg_doc_email_cfg t0;
