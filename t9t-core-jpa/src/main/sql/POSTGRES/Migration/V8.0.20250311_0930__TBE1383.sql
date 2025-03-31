-- TBE-1383: Extend data_change_request with count and parameter

-- drop views
DROP VIEW IF EXISTS p28_dat_data_change_request_nt;
DROP VIEW IF EXISTS p28_dat_data_change_request_v;

ALTER TABLE p28_dat_data_change_request ADD COLUMN IF NOT EXISTS count integer;
ALTER TABLE p28_dat_data_change_request ADD COLUMN IF NOT EXISTS parameter varchar(36);

COMMENT ON COLUMN p28_dat_data_change_request.count IS 'Only for special approvals: The number of items to be approved';
COMMENT ON COLUMN p28_dat_data_change_request.parameter IS 'Only for special approvals: Some descriptive parameter (for example a couponId)';

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
    , t0.count AS count
    , t0.parameter AS parameter
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
    , t0.count AS count
    , t0.parameter AS parameter
FROM p28_dat_data_change_request t0;
