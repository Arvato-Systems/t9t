-- TBE-323: add salutation and orgUnit to user table - ORACLE

ALTER TABLE p42_cfg_users ADD salutation varchar2(80 char);
ALTER TABLE p42_cfg_users ADD org_unit varchar2(16 char);

ALTER TABLE p42_his_users ADD salutation varchar2(80 char);
ALTER TABLE p42_his_users ADD org_unit varchar2(16 char);

COMMENT ON COLUMN p42_cfg_users.salutation IS 'salutation';
COMMENT ON COLUMN p42_cfg_users.org_unit IS 'org unit ID or provider ID';

COMMENT ON COLUMN p42_his_users.salutation IS 'salutation';
COMMENT ON COLUMN p42_his_users.org_unit IS 'org unit ID or provider ID';

CREATE OR REPLACE VIEW p42_cfg_users_nt AS SELECT
    -- columns of java class InternalTenantRef42
    t0.tenant_ref AS tenant_ref
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class UserRef
    -- columns of java class UserDTO
    , t0.user_id AS user_id
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
    , t0.role_ref AS role_ref
    , t0.is_technical AS is_technical
    , t0.email_address AS email_address
    , t0.office AS office
    , t0.department AS department
    , t0.job_title AS job_title
    , t0.phone_no AS phone_no
    , t0.mobile_phone_no AS mobile_phone_no
    , t0.external_auth AS external_auth
    , t0.supervisor_ref AS supervisor_ref
    , t0.salutation AS salutation
    , t0.org_unit AS org_unit
FROM p42_cfg_users t0;

CREATE OR REPLACE VIEW p42_cfg_users_v AS SELECT
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
    -- columns of java class UserRef
    -- columns of java class UserDTO
    , t0.user_id AS user_id
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
    , t0.role_ref AS role_ref
    , t0.is_technical AS is_technical
    , t0.email_address AS email_address
    , t0.office AS office
    , t0.department AS department
    , t0.job_title AS job_title
    , t0.phone_no AS phone_no
    , t0.mobile_phone_no AS mobile_phone_no
    , t0.external_auth AS external_auth
    , t0.supervisor_ref AS supervisor_ref
    , t0.salutation AS salutation
    , t0.org_unit AS org_unit
FROM p42_cfg_users t0;
