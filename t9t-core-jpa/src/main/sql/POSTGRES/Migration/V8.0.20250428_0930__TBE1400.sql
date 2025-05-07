-- TBE-1400: Send E-Mail for pending config-change approvals

ALTER TABLE p28_cfg_change_work_flow_config ADD COLUMN IF NOT EXISTS send_email boolean;
ALTER TABLE p28_his_change_work_flow_config ADD COLUMN IF NOT EXISTS send_email boolean;

COMMENT ON COLUMN p28_cfg_change_work_flow_config.send_email IS 'If true, then send email for status change';
COMMENT ON COLUMN p28_his_change_work_flow_config.send_email IS 'If true, then send email for status change';

-- This source has been automatically created by the bonaparte DSL (bonaparte.jpa addon). Do not modify, changes will be lost.
-- The bonaparte DSL is open source, licensed under Apache License, Version 2.0. It is based on Eclipse Xtext2.
-- The sources for bonaparte-DSL can be obtained at www.github.com/jpaw/bonaparte-dsl.git

CREATE OR REPLACE FUNCTION p28_cfg_change_work_flow_config_tp() RETURNS TRIGGER AS $p28_cfg_change_work_flow_config_td$
DECLARE
    next_seq_ BIGINT;
BEGIN
    SELECT NEXTVAL('cm_idgen_5009_seq') INTO next_seq_;
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO p28_his_change_work_flow_config (
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
            , pqon
            , approval_required_for_create
            , approval_required_for_update
            , approval_required_for_delete
            , approval_required_for_deactivation
            , approval_required_for_activation
            , separate_activation
            , private_change_ids
            , enforce_four_eyes
            , screen_location
            , view_model_id
            , send_email
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
            , NEW.pqon
            , NEW.approval_required_for_create
            , NEW.approval_required_for_update
            , NEW.approval_required_for_delete
            , NEW.approval_required_for_deactivation
            , NEW.approval_required_for_activation
            , NEW.separate_activation
            , NEW.private_change_ids
            , NEW.enforce_four_eyes
            , NEW.screen_location
            , NEW.view_model_id
            , NEW.send_email
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'UPDATE') THEN
        -- deny attempts to change a primary key column
        IF OLD.object_ref <> NEW.object_ref THEN
            RAISE EXCEPTION 'Cannot change primary key column to different value';
        END IF;
        INSERT INTO p28_his_change_work_flow_config (
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
            , pqon
            , approval_required_for_create
            , approval_required_for_update
            , approval_required_for_delete
            , approval_required_for_deactivation
            , approval_required_for_activation
            , separate_activation
            , private_change_ids
            , enforce_four_eyes
            , screen_location
            , view_model_id
            , send_email
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
            , NEW.pqon
            , NEW.approval_required_for_create
            , NEW.approval_required_for_update
            , NEW.approval_required_for_delete
            , NEW.approval_required_for_deactivation
            , NEW.approval_required_for_activation
            , NEW.separate_activation
            , NEW.private_change_ids
            , NEW.enforce_four_eyes
            , NEW.screen_location
            , NEW.view_model_id
            , NEW.send_email
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO p28_his_change_work_flow_config (
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
            , pqon
            , approval_required_for_create
            , approval_required_for_update
            , approval_required_for_delete
            , approval_required_for_deactivation
            , approval_required_for_activation
            , separate_activation
            , private_change_ids
            , enforce_four_eyes
            , screen_location
            , view_model_id
            , send_email
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
            , OLD.pqon
            , OLD.approval_required_for_create
            , OLD.approval_required_for_update
            , OLD.approval_required_for_delete
            , OLD.approval_required_for_deactivation
            , OLD.approval_required_for_activation
            , OLD.separate_activation
            , OLD.private_change_ids
            , OLD.enforce_four_eyes
            , OLD.screen_location
            , OLD.view_model_id
            , OLD.send_email
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$p28_cfg_change_work_flow_config_td$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS p28_cfg_change_work_flow_config_tr ON p28_cfg_change_work_flow_config;

CREATE TRIGGER p28_cfg_change_work_flow_config_tr
    AFTER INSERT OR DELETE OR UPDATE ON p28_cfg_change_work_flow_config
    FOR EACH ROW EXECUTE PROCEDURE p28_cfg_change_work_flow_config_tp();

CREATE OR REPLACE VIEW p28_cfg_change_work_flow_config_nt AS SELECT
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class ChangeWorkFlowConfigRef
    -- columns of java class ChangeWorkFlowConfigDTO
    , t0.pqon AS pqon
    , t0.approval_required_for_create AS approval_required_for_create
    , t0.approval_required_for_update AS approval_required_for_update
    , t0.approval_required_for_delete AS approval_required_for_delete
    , t0.approval_required_for_deactivation AS approval_required_for_deactivation
    , t0.approval_required_for_activation AS approval_required_for_activation
    , t0.separate_activation AS separate_activation
    , t0.private_change_ids AS private_change_ids
    , t0.enforce_four_eyes AS enforce_four_eyes
    , t0.screen_location AS screen_location
    , t0.view_model_id AS view_model_id
    , t0.send_email AS send_email
FROM p28_cfg_change_work_flow_config t0;

CREATE OR REPLACE VIEW p28_cfg_change_work_flow_config_v AS SELECT
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
    -- columns of java class ChangeWorkFlowConfigRef
    -- columns of java class ChangeWorkFlowConfigDTO
    , t0.pqon AS pqon
    , t0.approval_required_for_create AS approval_required_for_create
    , t0.approval_required_for_update AS approval_required_for_update
    , t0.approval_required_for_delete AS approval_required_for_delete
    , t0.approval_required_for_deactivation AS approval_required_for_deactivation
    , t0.approval_required_for_activation AS approval_required_for_activation
    , t0.separate_activation AS separate_activation
    , t0.private_change_ids AS private_change_ids
    , t0.enforce_four_eyes AS enforce_four_eyes
    , t0.screen_location AS screen_location
    , t0.view_model_id AS view_model_id
    , t0.send_email AS send_email
FROM p28_cfg_change_work_flow_config t0;
