-- TBE-341: add table for new message queue (POSTGRES)

-- This source has been automatically created by the bonaparte DSL (bonaparte.jpa addon). Do not modify, changes will be lost.
-- The bonaparte DSL is open source, licensed under Apache License, Version 2.0. It is based on Eclipse Xtext2.
-- The sources for bonaparte-DSL can be obtained at www.github.com/jpaw/bonaparte-dsl.git

CREATE TABLE p28_dat_bpmn2_message_queue (
    -- table columns of java class TrackingBase
    -- table columns of java class WriteTracking
      c_tech_user_id varchar(16) DEFAULT CURRENT_USER NOT NULL
    , c_app_user_id varchar(16) NOT NULL
    , c_timestamp timestamp(0) DEFAULT CURRENT_TIMESTAMP NOT NULL
    , c_process_ref bigint NOT NULL
    -- table columns of java class FullTracking
    , m_tech_user_id varchar(16) DEFAULT CURRENT_USER NOT NULL
    , m_app_user_id varchar(16) NOT NULL
    , m_timestamp timestamp(0) DEFAULT CURRENT_TIMESTAMP NOT NULL
    , m_process_ref bigint NOT NULL
    -- table columns of java class FullTrackingWithVersion
    , version integer NOT NULL
    -- table columns of java class InternalTenantRef42
    , tenant_ref bigint NOT NULL
    -- table columns of java class AbstractRef
    -- table columns of java class Ref
    , object_ref bigint NOT NULL
    -- table columns of java class Bpmn2MessageQueueRef
    -- table columns of java class Bpmn2MessageQueueDTO
    , message_name varchar(255) NOT NULL
    , business_key varchar(255)
    , payload bytea
    , retry_counter integer
    , return_code integer
    , error_details varchar(512)
);

ALTER TABLE p28_dat_bpmn2_message_queue ADD CONSTRAINT p28_dat_bpmn2_message_queue_pk PRIMARY KEY (
    object_ref
);
CREATE INDEX p28_dat_bpmn2_message_queue_i1 ON p28_dat_bpmn2_message_queue(
    tenant_ref, retry_counter
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_dat_bpmn2_message_queue.c_tech_user_id IS 'noinsert removed, causes problems with H2 unit tests';
COMMENT ON COLUMN p28_dat_bpmn2_message_queue.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantRef42
COMMENT ON COLUMN p28_dat_bpmn2_message_queue.tenant_ref IS 'the multitenancy discriminator';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_dat_bpmn2_message_queue.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class Bpmn2MessageQueueRef
-- comments for columns of java class Bpmn2MessageQueueDTO
COMMENT ON COLUMN p28_dat_bpmn2_message_queue.message_name IS 'Message name to trigger in BPMN';
COMMENT ON COLUMN p28_dat_bpmn2_message_queue.business_key IS 'Business key on process instance level for target selection';
COMMENT ON COLUMN p28_dat_bpmn2_message_queue.payload IS 'Variables to set on target process';
COMMENT ON COLUMN p28_dat_bpmn2_message_queue.retry_counter IS 'Counter of delivery retries left (if missing, no delivery retries will be done)';
COMMENT ON COLUMN p28_dat_bpmn2_message_queue.return_code IS 'Error code if last message delivery failed';
COMMENT ON COLUMN p28_dat_bpmn2_message_queue.error_details IS 'Further information about the error location';
