-----------------------------------------------------
-- TBE-1402: extend field sizes in email module cfg
-----------------------------------------------------

DROP VIEW IF EXISTS p28_cfg_email_module_cfg_nt;
DROP VIEW IF EXISTS p28_cfg_email_module_cfg_v;

ALTER TABLE p28_cfg_email_module_cfg ALTER COLUMN smtp_server_address TYPE VARCHAR(255);
ALTER TABLE p28_his_email_module_cfg ALTER COLUMN smtp_server_address TYPE VARCHAR(255);
ALTER TABLE p28_cfg_email_module_cfg ALTER COLUMN smtp_server_user_id TYPE VARCHAR(36);
ALTER TABLE p28_his_email_module_cfg ALTER COLUMN smtp_server_user_id TYPE VARCHAR(36);

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
