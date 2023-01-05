-- TBE-935: new column for ProcessDefinitionEntity

-- Add new column
ALTER TABLE p28_cfg_process_definition ADD COLUMN variant varchar(24);
ALTER TABLE p28_his_process_definition ADD COLUMN variant varchar(24);

-- Recreate views for p28_cfg_process_definition
CREATE OR REPLACE VIEW p28_cfg_process_definition_nt AS
SELECT
     t0.tenant_id                     AS tenant_id
     , t0.object_ref                 AS object_ref
     , t0.process_definition_id      AS process_definition_id
     , t0.is_active                  AS is_active
     , t0.name                       AS name
     , t0.factory_name               AS factory_name
     , t0.initial_params             AS initial_params
     , t0.workflow                   AS workflow
     , t0.always_restart_at_step1    AS always_restart_at_step1
     , t0.use_exclusive_lock         AS use_exclusive_lock
     , t0.engine                     AS engine
     , t0.jvm_lock_timeout_in_millis AS jvm_lock_timeout_in_millis
     , t0.variant                    AS variant
FROM p28_cfg_process_definition t0;

CREATE OR REPLACE VIEW p28_cfg_process_definition_v AS
SELECT
    t0.c_app_user_id                 AS c_app_user_id
     , t0.c_timestamp                AS c_timestamp
     , t0.c_process_ref              AS c_process_ref
     , t0.m_app_user_id              AS m_app_user_id
     , t0.m_timestamp                AS m_timestamp
     , t0.m_process_ref              AS m_process_ref
     , t0.version                    AS version
     , t0.tenant_id                  AS tenant_id
     , t0.object_ref                 AS object_ref
     , t0.process_definition_id      AS process_definition_id
     , t0.is_active                  AS is_active
     , t0.name                       AS name
     , t0.factory_name               AS factory_name
     , t0.initial_params             AS initial_params
     , t0.workflow                   AS workflow
     , t0.always_restart_at_step1    AS always_restart_at_step1
     , t0.use_exclusive_lock         AS use_exclusive_lock
     , t0.engine                     AS engine
     , t0.jvm_lock_timeout_in_millis AS jvm_lock_timeout_in_millis
     , t0.variant                    AS variant
FROM p28_cfg_process_definition t0;

-- Recreate trigger for p28_cfg_process_definition
CREATE OR REPLACE FUNCTION p28_cfg_process_definition_tp() RETURNS TRIGGER AS $p28_cfg_process_definition_td$
DECLARE
next_seq_ BIGINT;
BEGIN
SELECT NEXTVAL('cm_idgen_5009_seq') INTO next_seq_;
IF (TG_OP = 'INSERT') THEN
        INSERT INTO p28_his_process_definition (
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
            , process_definition_id
            , is_active
            , name
            , factory_name
            , initial_params
            , workflow
            , always_restart_at_step1
            , use_exclusive_lock
            , engine
            , jvm_lock_timeout_in_millis
            , variant
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
            , NEW.process_definition_id
            , NEW.is_active
            , NEW.name
            , NEW.factory_name
            , NEW.initial_params
            , NEW.workflow
            , NEW.always_restart_at_step1
            , NEW.use_exclusive_lock
            , NEW.engine
            , NEW.jvm_lock_timeout_in_millis
            , NEW.variant
        );
RETURN NEW;
END IF;
    IF (TG_OP = 'UPDATE') THEN
        -- deny attempts to change a primary key column
        IF OLD.object_ref <> NEW.object_ref THEN
            RAISE EXCEPTION 'Cannot change primary key column to different value';
END IF;
INSERT INTO p28_his_process_definition (
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
                                       , process_definition_id
                                       , is_active
                                       , name
                                       , factory_name
                                       , initial_params
                                       , workflow
                                       , always_restart_at_step1
                                       , use_exclusive_lock
                                       , engine
                                       , jvm_lock_timeout_in_millis
                                       , variant
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
         , NEW.process_definition_id
         , NEW.is_active
         , NEW.name
         , NEW.factory_name
         , NEW.initial_params
         , NEW.workflow
         , NEW.always_restart_at_step1
         , NEW.use_exclusive_lock
         , NEW.engine
         , NEW.jvm_lock_timeout_in_millis
         , NEW.variant
         );
RETURN NEW;
END IF;
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO p28_his_process_definition (
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
            , process_definition_id
            , is_active
            , name
            , factory_name
            , initial_params
            , workflow
            , always_restart_at_step1
            , use_exclusive_lock
            , engine
            , jvm_lock_timeout_in_millis
            , variant
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
            , OLD.process_definition_id
            , OLD.is_active
            , OLD.name
            , OLD.factory_name
            , OLD.initial_params
            , OLD.workflow
            , OLD.always_restart_at_step1
            , OLD.use_exclusive_lock
            , OLD.engine
            , OLD.jvm_lock_timeout_in_millis
            , OLD.variant
        );
RETURN OLD;
END IF;
RETURN NULL;
END;
$p28_cfg_process_definition_td$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS p28_cfg_process_definition_tr ON p28_cfg_process_definition;

CREATE TRIGGER p28_cfg_process_definition_tr
    AFTER INSERT OR DELETE OR UPDATE ON p28_cfg_process_definition
    FOR EACH ROW EXECUTE PROCEDURE p28_cfg_process_definition_tp();
