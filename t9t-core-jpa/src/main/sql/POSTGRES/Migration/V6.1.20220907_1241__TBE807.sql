-- TBE-807: exchange of tenant discriminator columns (a28) - clean up tenant tables

DROP VIEW IF EXISTS p42_cfg_tenants_v;
DROP VIEW IF EXISTS p42_cfg_tenants_nt;

DROP INDEX if exists p42_cfg_tenants_u1;
ALTER TABLE p42_cfg_tenants DROP CONSTRAINT p42_cfg_tenants_pk;
ALTER TABLE p42_cfg_tenants DROP COLUMN object_ref;
ALTER TABLE p42_cfg_tenants ADD CONSTRAINT p42_cfg_tenants_pk PRIMARY KEY (tenant_id);

ALTER TABLE p42_his_tenants DROP CONSTRAINT p42_his_tenants_pk;
ALTER TABLE p42_his_tenants ADD PRIMARY KEY (history_seq_ref, tenant_id);

CREATE OR REPLACE VIEW p42_cfg_tenants_v AS SELECT
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
    -- columns of java class TenantDTO
    , t0.tenant_id AS tenant_id
    , t0.is_active AS is_active
    , t0.name AS name
    , t0.z AS z
    , t0.min_permissions AS min_permissions
    , t0.max_permissions AS max_permissions
    , UserLogLevelType2s(t0.log_level) AS log_level
    , UserLogLevelType2s(t0.log_level_errors) AS log_level_errors
    , t0.valid_from AS valid_from
    , t0.valid_to AS valid_to
    , t0.resource_restriction AS resource_restriction
    , t0.resource_is_wildcard AS resource_is_wildcard
    , t0.time_zone AS time_zone
FROM p42_cfg_tenants t0;

CREATE OR REPLACE VIEW p42_cfg_tenants_nt AS SELECT
    -- columns of java class TenantDTO
    t0.tenant_id AS tenant_id
    , t0.is_active AS is_active
    , t0.name AS name
    , t0.z AS z
    , t0.min_permissions AS min_permissions
    , t0.max_permissions AS max_permissions
    , UserLogLevelType2s(t0.log_level) AS log_level
    , UserLogLevelType2s(t0.log_level_errors) AS log_level_errors
    , t0.valid_from AS valid_from
    , t0.valid_to AS valid_to
    , t0.resource_restriction AS resource_restriction
    , t0.resource_is_wildcard AS resource_is_wildcard
    , t0.time_zone AS time_zone
FROM p42_cfg_tenants t0;
