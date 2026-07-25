-- SQL migration for TBE-1713: Add time zone to scheduler setup

DROP VIEW IF EXISTS p28_cfg_scheduler_setup_nt;
DROP VIEW IF EXISTS p28_cfg_scheduler_setup_v;

ALTER TABLE p28_cfg_scheduler_setup ADD COLUMN IF NOT EXISTS time_zone varchar(64);
ALTER TABLE p28_his_scheduler_setup ADD COLUMN IF NOT EXISTS time_zone varchar(64);

COMMENT ON COLUMN p28_cfg_scheduler_setup.time_zone IS 'time zone identifier (e.g. "Europe/Berlin") used for cron trigger evaluation';

CREATE OR REPLACE VIEW p28_cfg_scheduler_setup_nt AS SELECT
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class SchedulerSetupRef
    -- columns of java class SchedulerSetupDTO
    , t0.scheduler_id AS scheduler_id
    , t0.is_active AS is_active
    , t0.name AS name
    , t0.request AS request
    , t0.user_id AS user_id
    , t0.api_key AS api_key
    , t0.language_code AS language_code
    , t0.valid_from AS valid_from
    , t0.valid_to AS valid_to
    , t0.recurrency_type AS recurrency_type
    , t0.execution_time AS execution_time
    , t0.start_hour AS start_hour
    , t0.end_hour AS end_hour
    , t0.interval_minutes AS interval_minutes
    , t0.interval_offset AS interval_offset
    , t0.set_of_weekdays AS set_of_weekdays
    , t0.repeat_count AS repeat_count
    , t0.interval_milliseconds AS interval_milliseconds
    , t0.z AS z
    , t0.cron_expression AS cron_expression
    , t0.additional_permissions AS additional_permissions
    , t0.concurrency_type AS concurrency_type
    , t0.concurrency_type_stale AS concurrency_type_stale
    , t0.concurrency_hook AS concurrency_hook
    , t0.time_limit AS time_limit
    , t0.mailing_group_id AS mailing_group_id
    , t0.run_on_node AS run_on_node
    , t0.scheduler_environment AS scheduler_environment
    , t0.time_zone AS time_zone
FROM p28_cfg_scheduler_setup t0;

CREATE OR REPLACE VIEW p28_cfg_scheduler_setup_v AS SELECT
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
    -- columns of java class SchedulerSetupRef
    -- columns of java class SchedulerSetupDTO
    , t0.scheduler_id AS scheduler_id
    , t0.is_active AS is_active
    , t0.name AS name
    , t0.request AS request
    , t0.user_id AS user_id
    , t0.api_key AS api_key
    , t0.language_code AS language_code
    , t0.valid_from AS valid_from
    , t0.valid_to AS valid_to
    , t0.recurrency_type AS recurrency_type
    , t0.execution_time AS execution_time
    , t0.start_hour AS start_hour
    , t0.end_hour AS end_hour
    , t0.interval_minutes AS interval_minutes
    , t0.interval_offset AS interval_offset
    , t0.set_of_weekdays AS set_of_weekdays
    , t0.repeat_count AS repeat_count
    , t0.interval_milliseconds AS interval_milliseconds
    , t0.z AS z
    , t0.cron_expression AS cron_expression
    , t0.additional_permissions AS additional_permissions
    , t0.concurrency_type AS concurrency_type
    , t0.concurrency_type_stale AS concurrency_type_stale
    , t0.concurrency_hook AS concurrency_hook
    , t0.time_limit AS time_limit
    , t0.mailing_group_id AS mailing_group_id
    , t0.run_on_node AS run_on_node
    , t0.scheduler_environment AS scheduler_environment
    , t0.time_zone AS time_zone
FROM p28_cfg_scheduler_setup t0;

CREATE OR REPLACE FUNCTION p28_cfg_scheduler_setup_tp() RETURNS TRIGGER AS $p28_cfg_scheduler_setup_td$
DECLARE
    next_seq_ BIGINT;
BEGIN
    SELECT NEXTVAL('cm_idgen_5009_seq') INTO next_seq_;
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO p28_his_scheduler_setup (
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
            , scheduler_id
            , is_active
            , name
            , request
            , user_id
            , api_key
            , language_code
            , valid_from
            , valid_to
            , recurrency_type
            , execution_time
            , start_hour
            , end_hour
            , interval_minutes
            , interval_offset
            , set_of_weekdays
            , repeat_count
            , interval_milliseconds
            , z
            , cron_expression
            , additional_permissions
            , concurrency_type
            , concurrency_type_stale
            , concurrency_hook
            , time_limit
            , mailing_group_id
            , run_on_node
            , scheduler_environment
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
            , NEW.scheduler_id
            , NEW.is_active
            , NEW.name
            , NEW.request
            , NEW.user_id
            , NEW.api_key
            , NEW.language_code
            , NEW.valid_from
            , NEW.valid_to
            , NEW.recurrency_type
            , NEW.execution_time
            , NEW.start_hour
            , NEW.end_hour
            , NEW.interval_minutes
            , NEW.interval_offset
            , NEW.set_of_weekdays
            , NEW.repeat_count
            , NEW.interval_milliseconds
            , NEW.z
            , NEW.cron_expression
            , NEW.additional_permissions
            , NEW.concurrency_type
            , NEW.concurrency_type_stale
            , NEW.concurrency_hook
            , NEW.time_limit
            , NEW.mailing_group_id
            , NEW.run_on_node
            , NEW.scheduler_environment
            , NEW.time_zone
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'UPDATE') THEN
        -- deny attempts to change a primary key column
        IF OLD.object_ref <> NEW.object_ref THEN
            RAISE EXCEPTION 'Cannot change primary key column to different value';
        END IF;
        INSERT INTO p28_his_scheduler_setup (
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
            , scheduler_id
            , is_active
            , name
            , request
            , user_id
            , api_key
            , language_code
            , valid_from
            , valid_to
            , recurrency_type
            , execution_time
            , start_hour
            , end_hour
            , interval_minutes
            , interval_offset
            , set_of_weekdays
            , repeat_count
            , interval_milliseconds
            , z
            , cron_expression
            , additional_permissions
            , concurrency_type
            , concurrency_type_stale
            , concurrency_hook
            , time_limit
            , mailing_group_id
            , run_on_node
            , scheduler_environment
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
            , NEW.scheduler_id
            , NEW.is_active
            , NEW.name
            , NEW.request
            , NEW.user_id
            , NEW.api_key
            , NEW.language_code
            , NEW.valid_from
            , NEW.valid_to
            , NEW.recurrency_type
            , NEW.execution_time
            , NEW.start_hour
            , NEW.end_hour
            , NEW.interval_minutes
            , NEW.interval_offset
            , NEW.set_of_weekdays
            , NEW.repeat_count
            , NEW.interval_milliseconds
            , NEW.z
            , NEW.cron_expression
            , NEW.additional_permissions
            , NEW.concurrency_type
            , NEW.concurrency_type_stale
            , NEW.concurrency_hook
            , NEW.time_limit
            , NEW.mailing_group_id
            , NEW.run_on_node
            , NEW.scheduler_environment
            , NEW.time_zone
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO p28_his_scheduler_setup (
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
            , scheduler_id
            , is_active
            , name
            , request
            , user_id
            , api_key
            , language_code
            , valid_from
            , valid_to
            , recurrency_type
            , execution_time
            , start_hour
            , end_hour
            , interval_minutes
            , interval_offset
            , set_of_weekdays
            , repeat_count
            , interval_milliseconds
            , z
            , cron_expression
            , additional_permissions
            , concurrency_type
            , concurrency_type_stale
            , concurrency_hook
            , time_limit
            , mailing_group_id
            , run_on_node
            , scheduler_environment
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
            , OLD.scheduler_id
            , OLD.is_active
            , OLD.name
            , OLD.request
            , OLD.user_id
            , OLD.api_key
            , OLD.language_code
            , OLD.valid_from
            , OLD.valid_to
            , OLD.recurrency_type
            , OLD.execution_time
            , OLD.start_hour
            , OLD.end_hour
            , OLD.interval_minutes
            , OLD.interval_offset
            , OLD.set_of_weekdays
            , OLD.repeat_count
            , OLD.interval_milliseconds
            , OLD.z
            , OLD.cron_expression
            , OLD.additional_permissions
            , OLD.concurrency_type
            , OLD.concurrency_type_stale
            , OLD.concurrency_hook
            , OLD.time_limit
            , OLD.mailing_group_id
            , OLD.run_on_node
            , OLD.scheduler_environment
            , OLD.time_zone
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$p28_cfg_scheduler_setup_td$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS p28_cfg_scheduler_setup_tr ON p28_cfg_scheduler_setup;

CREATE TRIGGER p28_cfg_scheduler_setup_tr
    AFTER INSERT OR DELETE OR UPDATE ON p28_cfg_scheduler_setup
    FOR EACH ROW EXECUTE PROCEDURE p28_cfg_scheduler_setup_tp();
