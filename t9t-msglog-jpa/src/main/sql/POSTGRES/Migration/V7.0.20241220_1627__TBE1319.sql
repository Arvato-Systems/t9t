-- TBE-1319: Additional counter field for number of retries done - POSTGRES

ALTER TABLE p28_int_message ADD COLUMN IF NOT EXISTS retries_done integer;
ALTER TABLE p28_dat_message_statistics ADD COLUMN IF NOT EXISTS retries_done integer;

COMMENT ON COLUMN p28_int_message.transaction_origin_type IS 'type of the initiator';
COMMENT ON COLUMN p28_int_message.essential_key IS 'a request specific key field';
COMMENT ON COLUMN p28_int_message.retries_done IS 'how many retries have been performed';
COMMENT ON COLUMN p28_dat_message_statistics.retries_done IS 'how many retries have been performed for all requests';

CREATE OR REPLACE VIEW p28_dat_message_statistics_nt AS SELECT
    -- columns of java class AbstractRef
    -- columns of java class Ref
    t0.object_ref AS object_ref
    -- columns of java class MessageStatisticsRef
    -- columns of java class MessageStatisticsDTO
    , t0.slot_start AS slot_start
    , t0.tenant_id AS tenant_id
    , t0.hostname AS hostname
    , t0.server_type AS server_type
    , t0.partition AS partition
    , TransactionOriginType2s(t0.transaction_origin_type) AS transaction_origin_type
    , t0.user_id AS user_id
    , t0.request_parameter_pqon AS request_parameter_pqon
    , t0.count_ok AS count_ok
    , t0.count_error AS count_error
    , t0.processing_time_max AS processing_time_max
    , t0.processing_time_total AS processing_time_total
    , t0.processing_delay_max AS processing_delay_max
    , t0.processing_delay_total AS processing_delay_total
    , t0.retries_done AS retries_done
FROM p28_dat_message_statistics t0;

CREATE OR REPLACE VIEW p28_dat_message_statistics_v AS SELECT
    -- columns of java class TrackingBase
    -- columns of java class NoTracking
    -- columns of java class AbstractRef
    -- columns of java class Ref
    t0.object_ref AS object_ref
    -- columns of java class MessageStatisticsRef
    -- columns of java class MessageStatisticsDTO
    , t0.slot_start AS slot_start
    , t0.tenant_id AS tenant_id
    , t0.hostname AS hostname
    , t0.server_type AS server_type
    , t0.partition AS partition
    , TransactionOriginType2s(t0.transaction_origin_type) AS transaction_origin_type
    , t0.user_id AS user_id
    , t0.request_parameter_pqon AS request_parameter_pqon
    , t0.count_ok AS count_ok
    , t0.count_error AS count_error
    , t0.processing_time_max AS processing_time_max
    , t0.processing_time_total AS processing_time_total
    , t0.processing_delay_max AS processing_delay_max
    , t0.processing_delay_total AS processing_delay_total
    , t0.retries_done AS retries_done
FROM p28_dat_message_statistics t0;

CREATE OR REPLACE VIEW p28_int_message_nt AS SELECT
    -- columns of java class AbstractRef
    -- columns of java class Ref
    t0.object_ref AS object_ref
    -- columns of java class MessageRef
    -- columns of java class MessageDTO
    , t0.session_ref AS session_ref
    , t0.tenant_id AS tenant_id
    , t0.record_no AS record_no
    , t0.message_id AS message_id
    , RetryAdviceType2s(t0.idempotency_behaviour) AS idempotency_behaviour
    , t0.user_id AS user_id
    , t0.execution_started_at AS execution_started_at
    , t0.language_code AS language_code
    , t0.planned_run_date AS planned_run_date
    , t0.invoking_process_ref AS invoking_process_ref
    , t0.request_parameter_pqon AS request_parameter_pqon
    , t0.request_parameters AS request_parameters
    , t0.response AS response
    , t0.processing_time_in_millisecs AS processing_time_in_millisecs
    , t0.return_code AS return_code
    , t0.error_details AS error_details
    , t0.rerun_by_process_ref AS rerun_by_process_ref
    , t0.hostname AS hostname
    , t0.server_type AS server_type
    , t0.partition AS partition
    , t0.processing_delay_in_millisecs AS processing_delay_in_millisecs
    , TransactionOriginType2s(t0.transaction_origin_type) AS transaction_origin_type
    , t0.essential_key AS essential_key
    , t0.retries_done AS retries_done
FROM p28_int_message t0;

CREATE OR REPLACE VIEW p28_int_message_v AS SELECT
    -- columns of java class TrackingBase
    -- columns of java class NoTracking
    -- columns of java class AbstractRef
    -- columns of java class Ref
    t0.object_ref AS object_ref
    -- columns of java class MessageRef
    -- columns of java class MessageDTO
    , t0.session_ref AS session_ref
    , t0.tenant_id AS tenant_id
    , t0.record_no AS record_no
    , t0.message_id AS message_id
    , RetryAdviceType2s(t0.idempotency_behaviour) AS idempotency_behaviour
    , t0.user_id AS user_id
    , t0.execution_started_at AS execution_started_at
    , t0.language_code AS language_code
    , t0.planned_run_date AS planned_run_date
    , t0.invoking_process_ref AS invoking_process_ref
    , t0.request_parameter_pqon AS request_parameter_pqon
    , t0.request_parameters AS request_parameters
    , t0.response AS response
    , t0.processing_time_in_millisecs AS processing_time_in_millisecs
    , t0.return_code AS return_code
    , t0.error_details AS error_details
    , t0.rerun_by_process_ref AS rerun_by_process_ref
    , t0.hostname AS hostname
    , t0.server_type AS server_type
    , t0.partition AS partition
    , t0.processing_delay_in_millisecs AS processing_delay_in_millisecs
    , TransactionOriginType2s(t0.transaction_origin_type) AS transaction_origin_type
    , t0.essential_key AS essential_key
    , t0.retries_done AS retries_done
FROM p28_int_message t0;
