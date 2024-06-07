
CREATE TABLE p28_dat_record_events (
    -- table columns of java class TrackingBase
    -- table columns of java class WriteTracking
      c_app_user_id varchar(16) NOT NULL
    , c_timestamp timestamp(0) DEFAULT CURRENT_TIMESTAMP NOT NULL
    , c_process_ref bigint NOT NULL
    -- table columns of java class InternalTenantId
    , tenant_id varchar(16) NOT NULL
    -- table columns of java class AbstractRef
    -- table columns of java class Ref
    , object_ref bigint NOT NULL
    -- table columns of java class RecordEventsRef
    -- table columns of java class RecordEventsDTO
    , event_source varchar(8) NOT NULL
    , event_severity varchar(1) NOT NULL
    , id1 uuid
    , id2 varchar(36)
    , status varchar(8) NOT NULL
    , status_message varchar(80)
);

ALTER TABLE p28_dat_record_events ADD CONSTRAINT p28_dat_record_events_pk PRIMARY KEY (
    object_ref
);
CREATE INDEX p28_dat_record_events_i1 ON p28_dat_record_events (
    id1
);
CREATE INDEX p28_dat_record_events_i2 ON p28_dat_record_events (
    id2
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_dat_record_events.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_dat_record_events.tenant_id IS 'the multitenancy discriminator';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_dat_record_events.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class RecordEventsRef
-- comments for columns of java class RecordEventsDTO
COMMENT ON COLUMN p28_dat_record_events.event_source IS 'unique identifier per source';
COMMENT ON COLUMN p28_dat_record_events.event_severity IS 'event level';
COMMENT ON COLUMN p28_dat_record_events.id1 IS 'event specific ID 1';
COMMENT ON COLUMN p28_dat_record_events.id2 IS 'event specific ID 2';
COMMENT ON COLUMN p28_dat_record_events.status IS 'status value';
COMMENT ON COLUMN p28_dat_record_events.status_message IS 'additional text';
