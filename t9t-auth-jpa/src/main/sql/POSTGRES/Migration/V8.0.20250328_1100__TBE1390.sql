-- TBE-1390: BAM-998: Add configuration for high risk emails in AuthModuleCfg


ALTER TABLE p28_cfg_auth_module_cfg ADD COLUMN notify_email_change boolean;
ALTER TABLE p28_cfg_auth_module_cfg ADD COLUMN notify_password_change boolean;
ALTER TABLE p28_cfg_auth_module_cfg ADD COLUMN notify_password_reset boolean;
ALTER TABLE p28_cfg_auth_module_cfg ADD COLUMN notify_password_forgotten boolean;

COMMENT ON COLUMN p28_cfg_auth_module_cfg.notify_email_change IS 'flag for user notification about high risk (email change)';
COMMENT ON COLUMN p28_cfg_auth_module_cfg.notify_password_change IS 'flag for user notification about high risk (password change)';
COMMENT ON COLUMN p28_cfg_auth_module_cfg.notify_password_reset IS 'flag for user notification about high risk (password reset)';
COMMENT ON COLUMN p28_cfg_auth_module_cfg.notify_password_forgotten IS 'flag for user notification about high risk (password forgotten)';

ALTER TABLE p28_his_auth_module_cfg ADD COLUMN notify_email_change boolean;
ALTER TABLE p28_his_auth_module_cfg ADD COLUMN notify_password_change boolean;
ALTER TABLE p28_his_auth_module_cfg ADD COLUMN notify_password_reset boolean;
ALTER TABLE p28_his_auth_module_cfg ADD COLUMN notify_password_forgotten boolean;

COMMENT ON COLUMN p28_his_auth_module_cfg.notify_email_change IS 'flag for user notification about high risk (email change)';
COMMENT ON COLUMN p28_his_auth_module_cfg.notify_password_change IS 'flag for user notification about high risk (password change)';
COMMENT ON COLUMN p28_his_auth_module_cfg.notify_password_reset IS 'flag for user notification about high risk (password reset)';
COMMENT ON COLUMN p28_his_auth_module_cfg.notify_password_forgotten IS 'flag for user notification about high risk (password forgotten)';

CREATE OR REPLACE VIEW p28_cfg_auth_module_cfg_nt AS SELECT
    -- columns of java class CompositeKeyBase
    -- columns of java class ModuleConfigDTO
    t0.z AS z
    -- columns of java class AuthModuleCfgDTO
    , t0.max_token_validity_in_minutes AS max_token_validity_in_minutes
    , t0.password_minimum_length AS password_minimum_length
    , t0.password_differ_previous_n AS password_differ_previous_n
    , t0.password_expiration_in_days AS password_expiration_in_days
    , t0.password_reset_duration_in_sec AS password_reset_duration_in_sec
    , t0.password_throttling_after_x AS password_throttling_after_x
    , t0.password_throttling_duration AS password_throttling_duration
    , t0.password_blocking_period AS password_blocking_period
    , t0.initial_password_expiration AS initial_password_expiration
    , t0.password_min_digits AS password_min_digits
    , t0.password_min_letters AS password_min_letters
    , t0.password_min_other_chars AS password_min_other_chars
    , t0.password_min_uppercase AS password_min_uppercase
    , t0.password_min_lowercase AS password_min_lowercase
    , t0.password_max_common_substring AS password_max_common_substring
    , t0.password_check_case_insensitive AS password_check_case_insensitive
    , t0.password_check_start_with AS password_check_start_with
    , t0.notify_email_change AS notify_email_change
    , t0.notify_password_change AS notify_password_change
    , t0.notify_password_reset AS notify_password_reset
    , t0.notify_password_forgotten AS notify_password_forgotten
FROM p28_cfg_auth_module_cfg t0;

CREATE OR REPLACE VIEW p28_cfg_auth_module_cfg_v AS SELECT
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
    -- columns of java class AuthModuleCfgDTO
    , t0.max_token_validity_in_minutes AS max_token_validity_in_minutes
    , t0.password_minimum_length AS password_minimum_length
    , t0.password_differ_previous_n AS password_differ_previous_n
    , t0.password_expiration_in_days AS password_expiration_in_days
    , t0.password_reset_duration_in_sec AS password_reset_duration_in_sec
    , t0.password_throttling_after_x AS password_throttling_after_x
    , t0.password_throttling_duration AS password_throttling_duration
    , t0.password_blocking_period AS password_blocking_period
    , t0.initial_password_expiration AS initial_password_expiration
    , t0.password_min_digits AS password_min_digits
    , t0.password_min_letters AS password_min_letters
    , t0.password_min_other_chars AS password_min_other_chars
    , t0.password_min_uppercase AS password_min_uppercase
    , t0.password_min_lowercase AS password_min_lowercase
    , t0.password_max_common_substring AS password_max_common_substring
    , t0.password_check_case_insensitive AS password_check_case_insensitive
    , t0.password_check_start_with AS password_check_start_with
    , t0.notify_email_change AS notify_email_change
    , t0.notify_password_change AS notify_password_change
    , t0.notify_password_reset AS notify_password_reset
    , t0.notify_password_forgotten AS notify_password_forgotten
FROM p28_cfg_auth_module_cfg t0;

CREATE OR REPLACE FUNCTION p28_cfg_auth_module_cfg_tp() RETURNS TRIGGER AS $p28_cfg_auth_module_cfg_td$
DECLARE
    next_seq_ BIGINT;
BEGIN
    SELECT NEXTVAL('cm_idgen_5009_seq') INTO next_seq_;
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO p28_his_auth_module_cfg (
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
            , max_token_validity_in_minutes
            , password_minimum_length
            , password_differ_previous_n
            , password_expiration_in_days
            , password_reset_duration_in_sec
            , password_throttling_after_x
            , password_throttling_duration
            , password_blocking_period
            , initial_password_expiration
            , password_min_digits
            , password_min_letters
            , password_min_other_chars
            , password_min_uppercase
            , password_min_lowercase
            , password_max_common_substring
            , password_check_case_insensitive
            , password_check_start_with
            , notify_email_change
            , notify_password_change
            , notify_password_reset
            , notify_password_forgotten
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
            , NEW.max_token_validity_in_minutes
            , NEW.password_minimum_length
            , NEW.password_differ_previous_n
            , NEW.password_expiration_in_days
            , NEW.password_reset_duration_in_sec
            , NEW.password_throttling_after_x
            , NEW.password_throttling_duration
            , NEW.password_blocking_period
            , NEW.initial_password_expiration
            , NEW.password_min_digits
            , NEW.password_min_letters
            , NEW.password_min_other_chars
            , NEW.password_min_uppercase
            , NEW.password_min_lowercase
            , NEW.password_max_common_substring
            , NEW.password_check_case_insensitive
            , NEW.password_check_start_with
            , NEW.notify_email_change
            , NEW.notify_password_change
            , NEW.notify_password_reset
            , NEW.notify_password_forgotten
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'UPDATE') THEN
        -- deny attempts to change a primary key column
        IF OLD.tenant_id <> NEW.tenant_id THEN
            RAISE EXCEPTION 'Cannot change primary key column to different value';
        END IF;
        INSERT INTO p28_his_auth_module_cfg (
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
            , max_token_validity_in_minutes
            , password_minimum_length
            , password_differ_previous_n
            , password_expiration_in_days
            , password_reset_duration_in_sec
            , password_throttling_after_x
            , password_throttling_duration
            , password_blocking_period
            , initial_password_expiration
            , password_min_digits
            , password_min_letters
            , password_min_other_chars
            , password_min_uppercase
            , password_min_lowercase
            , password_max_common_substring
            , password_check_case_insensitive
            , password_check_start_with
            , notify_email_change
            , notify_password_change
            , notify_password_reset
            , notify_password_forgotten
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
            , NEW.max_token_validity_in_minutes
            , NEW.password_minimum_length
            , NEW.password_differ_previous_n
            , NEW.password_expiration_in_days
            , NEW.password_reset_duration_in_sec
            , NEW.password_throttling_after_x
            , NEW.password_throttling_duration
            , NEW.password_blocking_period
            , NEW.initial_password_expiration
            , NEW.password_min_digits
            , NEW.password_min_letters
            , NEW.password_min_other_chars
            , NEW.password_min_uppercase
            , NEW.password_min_lowercase
            , NEW.password_max_common_substring
            , NEW.password_check_case_insensitive
            , NEW.password_check_start_with
            , NEW.notify_email_change
            , NEW.notify_password_change
            , NEW.notify_password_reset
            , NEW.notify_password_forgotten
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO p28_his_auth_module_cfg (
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
            , max_token_validity_in_minutes
            , password_minimum_length
            , password_differ_previous_n
            , password_expiration_in_days
            , password_reset_duration_in_sec
            , password_throttling_after_x
            , password_throttling_duration
            , password_blocking_period
            , initial_password_expiration
            , password_min_digits
            , password_min_letters
            , password_min_other_chars
            , password_min_uppercase
            , password_min_lowercase
            , password_max_common_substring
            , password_check_case_insensitive
            , password_check_start_with
            , notify_email_change
            , notify_password_change
            , notify_password_reset
            , notify_password_forgotten
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
            , OLD.max_token_validity_in_minutes
            , OLD.password_minimum_length
            , OLD.password_differ_previous_n
            , OLD.password_expiration_in_days
            , OLD.password_reset_duration_in_sec
            , OLD.password_throttling_after_x
            , OLD.password_throttling_duration
            , OLD.password_blocking_period
            , OLD.initial_password_expiration
            , OLD.password_min_digits
            , OLD.password_min_letters
            , OLD.password_min_other_chars
            , OLD.password_min_uppercase
            , OLD.password_min_lowercase
            , OLD.password_max_common_substring
            , OLD.password_check_case_insensitive
            , OLD.password_check_start_with
            , OLD.notify_email_change
            , OLD.notify_password_change
            , OLD.notify_password_reset
            , OLD.notify_password_forgotten
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$p28_cfg_auth_module_cfg_td$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS p28_cfg_auth_module_cfg_tr ON p28_cfg_auth_module_cfg;

CREATE TRIGGER p28_cfg_auth_module_cfg_tr
    AFTER INSERT OR DELETE OR UPDATE ON p28_cfg_auth_module_cfg
    FOR EACH ROW EXECUTE PROCEDURE p28_cfg_auth_module_cfg_tp();
