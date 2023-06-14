-- TBE-1025: new tables for ticket status

CREATE SEQUENCE p28_dat_update_status_s;
CREATE SEQUENCE p28_dat_update_status_log_s;

-- This source has been automatically created by the bonaparte DSL (bonaparte.jpa addon). Do not modify, changes will be lost.
-- The bonaparte DSL is open source, licensed under Apache License, Version 2.0. It is based on Eclipse Xtext2.
-- The sources for bonaparte-DSL can be obtained at www.github.com/jpaw/bonaparte-dsl.git

CREATE TABLE p28_dat_update_status (
    -- table columns of java class TrackingBase
    -- table columns of java class WriteTracking
      c_app_user_id varchar(16) NOT NULL
    , c_timestamp timestamp(0) DEFAULT CURRENT_TIMESTAMP NOT NULL
    , c_process_ref bigint NOT NULL
    -- table columns of java class FullTracking
    , m_app_user_id varchar(16) NOT NULL
    , m_timestamp timestamp(0) DEFAULT CURRENT_TIMESTAMP NOT NULL
    , m_process_ref bigint NOT NULL
    -- table columns of java class InternalTenantId
    , tenant_id varchar(16) NOT NULL
    -- table columns of java class AbstractRef
    -- table columns of java class Ref
    , object_ref bigint NOT NULL
    -- table columns of java class UpdateStatusRef
    -- table columns of java class UpdateStatusDTO
    , apply_sequence_id varchar(32) NOT NULL
    , ticket_id varchar(20) NOT NULL
    , description varchar(80) NOT NULL
    , update_apply_status varchar(1) NOT NULL
);

ALTER TABLE p28_dat_update_status ADD CONSTRAINT p28_dat_update_status_pk PRIMARY KEY (
    object_ref
);
CREATE UNIQUE INDEX p28_dat_update_status_u1 ON p28_dat_update_status(
    tenant_id, ticket_id
);
CREATE UNIQUE INDEX p28_dat_update_status_u2 ON p28_dat_update_status(
    tenant_id, apply_sequence_id
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_dat_update_status.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_dat_update_status.tenant_id IS 'the multitenancy discriminator';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_dat_update_status.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class UpdateStatusRef
-- comments for columns of java class UpdateStatusDTO

-- This source has been automatically created by the bonaparte DSL (bonaparte.jpa addon). Do not modify, changes will be lost.
-- The bonaparte DSL is open source, licensed under Apache License, Version 2.0. It is based on Eclipse Xtext2.
-- The sources for bonaparte-DSL can be obtained at www.github.com/jpaw/bonaparte-dsl.git

CREATE TABLE p28_dat_update_status_log (
    -- table columns of java class TrackingBase
    -- table columns of java class WriteTrackingMs
      c_app_user_id varchar(16) NOT NULL
    , c_timestamp timestamp(3) DEFAULT CURRENT_TIMESTAMP NOT NULL
    , c_process_ref bigint NOT NULL
    -- table columns of java class InternalTenantId
    , tenant_id varchar(16) NOT NULL
    -- table columns of java class AbstractRef
    -- table columns of java class Ref
    , object_ref bigint NOT NULL
    -- table columns of java class UpdateStatusLogRef
    -- table columns of java class UpdateStatusLogDTO
    , ticket_ref bigint NOT NULL
    , new_status varchar(1) NOT NULL
);

ALTER TABLE p28_dat_update_status_log ADD CONSTRAINT p28_dat_update_status_log_pk PRIMARY KEY (
    object_ref
);
CREATE INDEX p28_dat_update_status_log_i1 ON p28_dat_update_status_log(
    ticket_ref
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTrackingMs
COMMENT ON COLUMN p28_dat_update_status_log.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_dat_update_status_log.tenant_id IS 'the multitenancy discriminator';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_dat_update_status_log.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class UpdateStatusLogRef
-- comments for columns of java class UpdateStatusLogDTO
