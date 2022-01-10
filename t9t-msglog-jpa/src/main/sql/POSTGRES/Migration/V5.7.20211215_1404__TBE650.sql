-- TBE-650: modify type of messageId column - POSTGRES

DROP VIEW IF EXISTS p28_int_message_v;
DROP VIEW IF EXISTS p28_int_message_nt;

-- ALTER TABLE p28_int_message ALTER COLUMN message_id TYPE UUID;   is NOT working, also not with USING message_id::UUID

DROP TABLE p28_int_message;

CREATE TABLE p28_int_message (
    -- table columns of java class TrackingBase
    -- table columns of java class NoTracking
    -- table columns of java class AbstractRef
    -- table columns of java class Ref
      object_ref bigint NOT NULL
    -- table columns of java class MessageRef
    -- table columns of java class MessageDTO
    , session_ref bigint NOT NULL
    , tenant_ref bigint NOT NULL
    , record_no integer
    , message_id uuid
    , idempotency_behaviour varchar(1)
    , user_id varchar(16) NOT NULL
    , execution_started_at timestamp(0) NOT NULL
    , language_code varchar(5)
    , planned_run_date timestamp(0)
    , invoking_process_ref bigint
    , request_parameter_pqon varchar(255) NOT NULL
    , request_parameters bytea
    , response bytea
    , processing_time_in_millisecs bigint
    , return_code integer
    , error_details varchar(512)
    , rerun_by_process_ref bigint
);

ALTER TABLE p28_int_message ADD CONSTRAINT p28_int_message_pk PRIMARY KEY (
    object_ref
);
CREATE INDEX p28_int_message_i1 ON p28_int_message(
    message_id
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class NoTracking
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_int_message.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class MessageRef
-- comments for columns of java class MessageDTO
COMMENT ON COLUMN p28_int_message.session_ref IS 'identifies file or queue or socket which accepted the record';
COMMENT ON COLUMN p28_int_message.tenant_ref IS 'identifies file or queue or socket which accepted the record';
COMMENT ON COLUMN p28_int_message.record_no IS 'record of the transmission';
COMMENT ON COLUMN p28_int_message.execution_started_at IS 'separate field as the record may be persisted late in the DB';
COMMENT ON COLUMN p28_int_message.planned_run_date IS 'optional scheduled run date of the call (only for messages from the scheduler)';
COMMENT ON COLUMN p28_int_message.invoking_process_ref IS 'for asynchronous requests, the request which initiated this one';
COMMENT ON COLUMN p28_int_message.request_parameter_pqon IS 'partially qualified name of the request';
COMMENT ON COLUMN p28_int_message.request_parameters IS '16 MB - 1 Byte is max. allowed message length';
COMMENT ON COLUMN p28_int_message.response IS 'SLA relevant fields';
COMMENT ON COLUMN p28_int_message.processing_time_in_millisecs IS 'responseHeader fields:';
COMMENT ON COLUMN p28_int_message.error_details IS 'further information about the error location';
COMMENT ON COLUMN p28_int_message.rerun_by_process_ref IS 'reference from the original request to the rerun - references this entity';
