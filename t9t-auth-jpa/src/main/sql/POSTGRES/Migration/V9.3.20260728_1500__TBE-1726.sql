-- SQL migration for TBE-1726: Add firstName and language to UserDTO

DROP VIEW IF EXISTS p42_cfg_users_nt;
DROP VIEW IF EXISTS p42_cfg_users_v;

ALTER TABLE p42_cfg_users
    ADD COLUMN IF NOT EXISTS language varchar(5)
    , ADD COLUMN IF NOT EXISTS first_name varchar(80);

ALTER TABLE p42_his_users
    ADD COLUMN IF NOT EXISTS language varchar(5)
    , ADD COLUMN IF NOT EXISTS first_name varchar(80);

COMMENT ON COLUMN p42_cfg_users.language IS 'user language as a BCP47 language tag';
COMMENT ON COLUMN p42_cfg_users.first_name IS 'first name of the user';

CREATE OR REPLACE VIEW p42_cfg_users_nt AS SELECT
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
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
    , t0.log_level AS log_level
    , t0.log_level_errors AS log_level_errors
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
    , t0.default_screen_id AS default_screen_id
    , t0.user_id_ext AS user_id_ext
    , t0.identity_provider AS identity_provider
    , t0.only_external_auth AS only_external_auth
    , t0.language AS language
    , t0.first_name AS first_name
FROM p42_cfg_users t0;

CREATE OR REPLACE VIEW p42_cfg_users_v AS SELECT
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
    -- columns of java class UserRef
    -- columns of java class UserDTO
    , t0.user_id AS user_id
    , t0.is_active AS is_active
    , t0.name AS name
    , t0.z AS z
    , t0.min_permissions AS min_permissions
    , t0.max_permissions AS max_permissions
    , t0.log_level AS log_level
    , t0.log_level_errors AS log_level_errors
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
    , t0.default_screen_id AS default_screen_id
    , t0.user_id_ext AS user_id_ext
    , t0.identity_provider AS identity_provider
    , t0.only_external_auth AS only_external_auth
    , t0.language AS language
    , t0.first_name AS first_name
FROM p42_cfg_users t0;

CREATE OR REPLACE FUNCTION p42_cfg_users_tp() RETURNS TRIGGER AS $p42_cfg_users_td$
DECLARE
    next_seq_ BIGINT;
BEGIN
    SELECT NEXTVAL('cm_idgen_5009_seq') INTO next_seq_;
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO p42_his_users (
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
            , user_id
            , is_active
            , name
            , z
            , min_permissions
            , max_permissions
            , log_level
            , log_level_errors
            , valid_from
            , valid_to
            , resource_restriction
            , resource_is_wildcard
            , role_ref
            , is_technical
            , email_address
            , office
            , department
            , job_title
            , phone_no
            , mobile_phone_no
            , external_auth
            , supervisor_ref
            , salutation
            , org_unit
            , default_screen_id
            , user_id_ext
            , identity_provider
            , only_external_auth
            , language
            , first_name
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
            , NEW.user_id
            , NEW.is_active
            , NEW.name
            , NEW.z
            , NEW.min_permissions
            , NEW.max_permissions
            , NEW.log_level
            , NEW.log_level_errors
            , NEW.valid_from
            , NEW.valid_to
            , NEW.resource_restriction
            , NEW.resource_is_wildcard
            , NEW.role_ref
            , NEW.is_technical
            , NEW.email_address
            , NEW.office
            , NEW.department
            , NEW.job_title
            , NEW.phone_no
            , NEW.mobile_phone_no
            , NEW.external_auth
            , NEW.supervisor_ref
            , NEW.salutation
            , NEW.org_unit
            , NEW.default_screen_id
            , NEW.user_id_ext
            , NEW.identity_provider
            , NEW.only_external_auth
            , NEW.language
            , NEW.first_name
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'UPDATE') THEN
        -- deny attempts to change a primary key column
        IF OLD.object_ref <> NEW.object_ref THEN
            RAISE EXCEPTION 'Cannot change primary key column to different value';
        END IF;
        INSERT INTO p42_his_users (
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
            , user_id
            , is_active
            , name
            , z
            , min_permissions
            , max_permissions
            , log_level
            , log_level_errors
            , valid_from
            , valid_to
            , resource_restriction
            , resource_is_wildcard
            , role_ref
            , is_technical
            , email_address
            , office
            , department
            , job_title
            , phone_no
            , mobile_phone_no
            , external_auth
            , supervisor_ref
            , salutation
            , org_unit
            , default_screen_id
            , user_id_ext
            , identity_provider
            , only_external_auth
            , language
            , first_name
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
            , NEW.user_id
            , NEW.is_active
            , NEW.name
            , NEW.z
            , NEW.min_permissions
            , NEW.max_permissions
            , NEW.log_level
            , NEW.log_level_errors
            , NEW.valid_from
            , NEW.valid_to
            , NEW.resource_restriction
            , NEW.resource_is_wildcard
            , NEW.role_ref
            , NEW.is_technical
            , NEW.email_address
            , NEW.office
            , NEW.department
            , NEW.job_title
            , NEW.phone_no
            , NEW.mobile_phone_no
            , NEW.external_auth
            , NEW.supervisor_ref
            , NEW.salutation
            , NEW.org_unit
            , NEW.default_screen_id
            , NEW.user_id_ext
            , NEW.identity_provider
            , NEW.only_external_auth
            , NEW.language
            , NEW.first_name
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO p42_his_users (
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
            , user_id
            , is_active
            , name
            , z
            , min_permissions
            , max_permissions
            , log_level
            , log_level_errors
            , valid_from
            , valid_to
            , resource_restriction
            , resource_is_wildcard
            , role_ref
            , is_technical
            , email_address
            , office
            , department
            , job_title
            , phone_no
            , mobile_phone_no
            , external_auth
            , supervisor_ref
            , salutation
            , org_unit
            , default_screen_id
            , user_id_ext
            , identity_provider
            , only_external_auth
            , language
            , first_name
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
            , OLD.user_id
            , OLD.is_active
            , OLD.name
            , OLD.z
            , OLD.min_permissions
            , OLD.max_permissions
            , OLD.log_level
            , OLD.log_level_errors
            , OLD.valid_from
            , OLD.valid_to
            , OLD.resource_restriction
            , OLD.resource_is_wildcard
            , OLD.role_ref
            , OLD.is_technical
            , OLD.email_address
            , OLD.office
            , OLD.department
            , OLD.job_title
            , OLD.phone_no
            , OLD.mobile_phone_no
            , OLD.external_auth
            , OLD.supervisor_ref
            , OLD.salutation
            , OLD.org_unit
            , OLD.default_screen_id
            , OLD.user_id_ext
            , OLD.identity_provider
            , OLD.only_external_auth
            , OLD.language
            , OLD.first_name
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$p42_cfg_users_td$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS p42_cfg_users_tr ON p42_cfg_users;

CREATE TRIGGER p42_cfg_users_tr
    AFTER INSERT OR DELETE OR UPDATE ON p42_cfg_users
    FOR EACH ROW EXECUTE PROCEDURE p42_cfg_users_tp();
