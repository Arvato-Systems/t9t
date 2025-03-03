CREATE SEQUENCE p28_cfg_change_work_flow_config_s;
CREATE SEQUENCE p28_dat_data_change_request_s;

CREATE TABLE p28_cfg_change_work_flow_config (
    -- table columns of java class TrackingBase
    -- table columns of java class WriteTracking
      c_app_user_id varchar(16) NOT NULL
    , c_timestamp timestamp(0) DEFAULT CURRENT_TIMESTAMP NOT NULL
    , c_process_ref bigint NOT NULL
    -- table columns of java class FullTracking
    , m_app_user_id varchar(16) NOT NULL
    , m_timestamp timestamp(0) DEFAULT CURRENT_TIMESTAMP NOT NULL
    , m_process_ref bigint NOT NULL
    -- table columns of java class FullTrackingWithVersion
    , version integer NOT NULL
    -- table columns of java class InternalTenantId
    , tenant_id varchar(16) NOT NULL
    -- table columns of java class AbstractRef
    -- table columns of java class Ref
    , object_ref bigint NOT NULL
    -- table columns of java class ChangeWorkFlowConfigRef
    -- table columns of java class ChangeWorkFlowConfigDTO
    , pqon varchar(255) NOT NULL
    , approval_required_for_create boolean NOT NULL
    , approval_required_for_update boolean NOT NULL
    , approval_required_for_delete boolean NOT NULL
    , approval_required_for_deactivation boolean NOT NULL
    , approval_required_for_activation boolean NOT NULL
    , separate_activation boolean NOT NULL
    , private_change_ids boolean NOT NULL
    , enforce_four_eyes boolean NOT NULL
    , screen_location varchar(100)
    , view_model_id varchar(50)
);

ALTER TABLE p28_cfg_change_work_flow_config ADD CONSTRAINT p28_cfg_change_work_flow_config_pk PRIMARY KEY (
    object_ref
);
CREATE UNIQUE INDEX p28_cfg_change_work_flow_config_u1 ON p28_cfg_change_work_flow_config (
    tenant_id, pqon
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_cfg_change_work_flow_config.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_cfg_change_work_flow_config.tenant_id IS 'the multitenancy discriminator';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_cfg_change_work_flow_config.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class ChangeWorkFlowConfigRef
-- comments for columns of java class ChangeWorkFlowConfigDTO
COMMENT ON COLUMN p28_cfg_change_work_flow_config.pqon IS 'data (table) class PQON for which change work flow is needed';
COMMENT ON COLUMN p28_cfg_change_work_flow_config.approval_required_for_create IS 'if true, approvals are required to create new records';
COMMENT ON COLUMN p28_cfg_change_work_flow_config.approval_required_for_update IS 'if true, approvals are required to update records';
COMMENT ON COLUMN p28_cfg_change_work_flow_config.approval_required_for_delete IS 'if true, approvals are required to delete records';
COMMENT ON COLUMN p28_cfg_change_work_flow_config.approval_required_for_deactivation IS 'If true, deactivations will be submitted for approval as "UPDATE" requests with the only change setting "isActive" to false.';
COMMENT ON COLUMN p28_cfg_change_work_flow_config.approval_required_for_activation IS 'If true, activations will be submitted for approval as "UPDATE" requests with the only change setting "isActive" to true.';
COMMENT ON COLUMN p28_cfg_change_work_flow_config.separate_activation IS 'If true, activation must be manually triggered';
COMMENT ON COLUMN p28_cfg_change_work_flow_config.private_change_ids IS 'If true, then only the user who submitted a change can make further changes to it or resubmit it. Otherwise, everyone with change permissions can work on any pending change.';
COMMENT ON COLUMN p28_cfg_change_work_flow_config.enforce_four_eyes IS 'If true, then every change must be approved by a different user than the one who submitted it.';
COMMENT ON COLUMN p28_cfg_change_work_flow_config.screen_location IS 'location of UI screen to display and edit the changes';
COMMENT ON COLUMN p28_cfg_change_work_flow_config.view_model_id IS 'mainly use to get key from data to search and display original data record';

CREATE TABLE p28_his_change_work_flow_config (
    -- table columns of java class TrackingBase
    -- table columns of java class WriteTracking
      c_app_user_id varchar(16) NOT NULL
    , c_timestamp timestamp(0) DEFAULT CURRENT_TIMESTAMP NOT NULL
    , c_process_ref bigint NOT NULL
    -- table columns of java class FullTracking
    , m_app_user_id varchar(16) NOT NULL
    , m_timestamp timestamp(0) DEFAULT CURRENT_TIMESTAMP NOT NULL
    , m_process_ref bigint NOT NULL
    -- table columns of java class FullTrackingWithVersion
    , version integer NOT NULL
    -- table columns of java class InternalTenantId
    , tenant_id varchar(16) NOT NULL
    , history_seq_ref   bigint NOT NULL
    , history_change_type   char(1) NOT NULL
    -- table columns of java class AbstractRef
    -- table columns of java class Ref
    , object_ref bigint NOT NULL
    -- table columns of java class ChangeWorkFlowConfigRef
    -- table columns of java class ChangeWorkFlowConfigDTO
    , pqon varchar(255) NOT NULL
    , approval_required_for_create boolean NOT NULL
    , approval_required_for_update boolean NOT NULL
    , approval_required_for_delete boolean NOT NULL
    , approval_required_for_deactivation boolean NOT NULL
    , approval_required_for_activation boolean NOT NULL
    , separate_activation boolean NOT NULL
    , private_change_ids boolean NOT NULL
    , enforce_four_eyes boolean NOT NULL
    , screen_location varchar(100)
    , view_model_id varchar(50)
);

ALTER TABLE p28_his_change_work_flow_config ADD CONSTRAINT p28_his_change_work_flow_config_pk PRIMARY KEY (
    object_ref, history_seq_ref
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_his_change_work_flow_config.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_his_change_work_flow_config.tenant_id IS 'the multitenancy discriminator';
COMMENT ON COLUMN p28_his_change_work_flow_config.history_seq_ref IS 'current sequence number of history entry';
COMMENT ON COLUMN p28_his_change_work_flow_config.history_change_type IS 'type of change (C=create/insert, U=update, D=delete)';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_his_change_work_flow_config.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class ChangeWorkFlowConfigRef
-- comments for columns of java class ChangeWorkFlowConfigDTO
COMMENT ON COLUMN p28_his_change_work_flow_config.pqon IS 'data (table) class PQON for which change work flow is needed';
COMMENT ON COLUMN p28_his_change_work_flow_config.approval_required_for_create IS 'if true, approvals are required to create new records';
COMMENT ON COLUMN p28_his_change_work_flow_config.approval_required_for_update IS 'if true, approvals are required to update records';
COMMENT ON COLUMN p28_his_change_work_flow_config.approval_required_for_delete IS 'if true, approvals are required to delete records';
COMMENT ON COLUMN p28_his_change_work_flow_config.approval_required_for_deactivation IS 'If true, deactivations will be submitted for approval as "UPDATE" requests with the only change setting "isActive" to false.';
COMMENT ON COLUMN p28_his_change_work_flow_config.approval_required_for_activation IS 'If true, activations will be submitted for approval as "UPDATE" requests with the only change setting "isActive" to true.';
COMMENT ON COLUMN p28_his_change_work_flow_config.separate_activation IS 'If true, activation must be manually triggered';
COMMENT ON COLUMN p28_his_change_work_flow_config.private_change_ids IS 'If true, then only the user who submitted a change can make further changes to it or resubmit it. Otherwise, everyone with change permissions can work on any pending change.';
COMMENT ON COLUMN p28_his_change_work_flow_config.enforce_four_eyes IS 'If true, then every change must be approved by a different user than the one who submitted it.';
COMMENT ON COLUMN p28_his_change_work_flow_config.screen_location IS 'location of UI screen to display and edit the changes';
COMMENT ON COLUMN p28_his_change_work_flow_config.view_model_id IS 'mainly use to get key from data to search and display original data record';

CREATE TABLE p28_dat_data_change_request (
    -- table columns of java class TrackingBase
    -- table columns of java class WriteTracking
      c_app_user_id varchar(16) NOT NULL
    , c_timestamp timestamp(0) DEFAULT CURRENT_TIMESTAMP NOT NULL
    , c_process_ref bigint NOT NULL
    -- table columns of java class FullTracking
    , m_app_user_id varchar(16) NOT NULL
    , m_timestamp timestamp(0) DEFAULT CURRENT_TIMESTAMP NOT NULL
    , m_process_ref bigint NOT NULL
    -- table columns of java class FullTrackingWithVersion
    , version integer NOT NULL
    -- table columns of java class InternalTenantId
    , tenant_id varchar(16) NOT NULL
    -- table columns of java class AbstractRef
    -- table columns of java class Ref
    , object_ref bigint NOT NULL
    -- table columns of java class DataChangeRequestRef
    -- table columns of java class DataChangeRequestDTO
    , pqon varchar(255) NOT NULL
    , change_id varchar(16) NOT NULL
    , key bytea NOT NULL
    , crud_request bytea NOT NULL
    , status varchar(1) NOT NULL
    , user_id_created varchar(16) NOT NULL
    , when_created timestamp(0) NOT NULL
    , user_id_modified varchar(16) NOT NULL
    , when_last_modified timestamp(0) NOT NULL
    , user_id_submitted varchar(16)
    , when_submitted timestamp(0)
    , text_submitted varchar(255)
    , user_id_approve varchar(16)
    , when_decided timestamp(0)
    , text_decision varchar(255)
    , user_id_activated varchar(16)
    , when_activated timestamp(0)
);

ALTER TABLE p28_dat_data_change_request ADD CONSTRAINT p28_dat_data_change_request_pk PRIMARY KEY (
    object_ref
);
CREATE UNIQUE INDEX p28_dat_data_change_request_u1 ON p28_dat_data_change_request (
    tenant_id, pqon, change_id, key
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_dat_data_change_request.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_dat_data_change_request.tenant_id IS 'the multitenancy discriminator';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_dat_data_change_request.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class DataChangeRequestRef
-- comments for columns of java class DataChangeRequestDTO
COMMENT ON COLUMN p28_dat_data_change_request.pqon IS 'data (table) class PQON for which the change is requested';
COMMENT ON COLUMN p28_dat_data_change_request.change_id IS 'a unique identifier of the change';
COMMENT ON COLUMN p28_dat_data_change_request.key IS 'primary key of the data object which is being changed';
COMMENT ON COLUMN p28_dat_data_change_request.crud_request IS 'the actual CRUD request which will be used to apply the change';
COMMENT ON COLUMN p28_dat_data_change_request.status IS 'current status of the change work flow';
COMMENT ON COLUMN p28_dat_data_change_request.user_id_created IS 'user who initiated the change';
COMMENT ON COLUMN p28_dat_data_change_request.when_created IS 'when the change was initiated';
COMMENT ON COLUMN p28_dat_data_change_request.user_id_modified IS 'user who last modified the change';
COMMENT ON COLUMN p28_dat_data_change_request.when_last_modified IS 'when the change was last modified';
COMMENT ON COLUMN p28_dat_data_change_request.user_id_submitted IS 'user who submitted the change for approval';
COMMENT ON COLUMN p28_dat_data_change_request.when_submitted IS 'when the change was submitted for approval';
COMMENT ON COLUMN p28_dat_data_change_request.text_submitted IS 'description of the changes, entered by the submitter';
COMMENT ON COLUMN p28_dat_data_change_request.user_id_approve IS 'user ID who has approved (or rejected) the approval';
COMMENT ON COLUMN p28_dat_data_change_request.when_decided IS 'when the decision was made (approval or rejection)';
COMMENT ON COLUMN p28_dat_data_change_request.text_decision IS 'additional explanation for the decision';
COMMENT ON COLUMN p28_dat_data_change_request.user_id_activated IS 'user ID who has activated (move the change to live table) the change';
COMMENT ON COLUMN p28_dat_data_change_request.when_activated IS 'when the change was activated';

-- convert a token (as stored in DB tables) of enum t9t.changeRequest.ChangeWorkFlowStatus into the more readable symbolic constant string
CREATE OR REPLACE FUNCTION ChangeWorkFlowStatus2s(token VARCHAR) RETURNS VARCHAR
    IMMUTABLE STRICT
    AS $$
DECLARE
BEGIN
    IF token = 'W' THEN
        RETURN 'WORK_IN_PROGRESS';
    END IF;
    IF token = 'R' THEN
        RETURN 'TO_REVIEW';
    END IF;
    IF token = 'A' THEN
        RETURN 'APPROVED';
    END IF;
    IF token = 'J' THEN
        RETURN 'REJECTED';
    END IF;
    IF token = 'V' THEN
        RETURN 'ACTIVATED';
    END IF;
    IF token = 'C' THEN
        RETURN 'CONFLICT';
    END IF;
    RETURN '~';  -- token for undefined mapping
END;
$$ LANGUAGE plpgsql;

-- convert a constant string of enum t9t.changeRequest.ChangeWorkFlowStatus into the token used for DB table storage
CREATE OR REPLACE FUNCTION ChangeWorkFlowStatus2t(token VARCHAR) RETURNS VARCHAR
    IMMUTABLE STRICT
    AS $$
DECLARE
BEGIN
    IF token = 'WORK_IN_PROGRESS' THEN
        RETURN 'W';
    END IF;
    IF token = 'TO_REVIEW' THEN
        RETURN 'R';
    END IF;
    IF token = 'APPROVED' THEN
        RETURN 'A';
    END IF;
    IF token = 'REJECTED' THEN
        RETURN 'J';
    END IF;
    IF token = 'ACTIVATED' THEN
        RETURN 'V';
    END IF;
    IF token = 'CONFLICT' THEN
        RETURN 'C';
    END IF;
    RETURN '~';  -- token for undefined mapping
END;
$$ LANGUAGE plpgsql;


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
FROM p28_cfg_change_work_flow_config t0;

CREATE OR REPLACE VIEW p28_dat_data_change_request_nt AS SELECT
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class DataChangeRequestRef
    -- columns of java class DataChangeRequestDTO
    , t0.pqon AS pqon
    , t0.change_id AS change_id
    , t0.key AS key
    , t0.crud_request AS crud_request
    , ChangeWorkFlowStatus2s(t0.status) AS status
    , t0.user_id_created AS user_id_created
    , t0.when_created AS when_created
    , t0.user_id_modified AS user_id_modified
    , t0.when_last_modified AS when_last_modified
    , t0.user_id_submitted AS user_id_submitted
    , t0.when_submitted AS when_submitted
    , t0.text_submitted AS text_submitted
    , t0.user_id_approve AS user_id_approve
    , t0.when_decided AS when_decided
    , t0.text_decision AS text_decision
    , t0.user_id_activated AS user_id_activated
    , t0.when_activated AS when_activated
FROM p28_dat_data_change_request t0;

CREATE OR REPLACE VIEW p28_dat_data_change_request_v AS SELECT
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
    -- columns of java class DataChangeRequestRef
    -- columns of java class DataChangeRequestDTO
    , t0.pqon AS pqon
    , t0.change_id AS change_id
    , t0.key AS key
    , t0.crud_request AS crud_request
    , ChangeWorkFlowStatus2s(t0.status) AS status
    , t0.user_id_created AS user_id_created
    , t0.when_created AS when_created
    , t0.user_id_modified AS user_id_modified
    , t0.when_last_modified AS when_last_modified
    , t0.user_id_submitted AS user_id_submitted
    , t0.when_submitted AS when_submitted
    , t0.text_submitted AS text_submitted
    , t0.user_id_approve AS user_id_approve
    , t0.when_decided AS when_decided
    , t0.text_decision AS text_decision
    , t0.user_id_activated AS user_id_activated
    , t0.when_activated AS when_activated
FROM p28_dat_data_change_request t0;
