--
-- Copyright (c) 2012 - 2018 Arvato Systems GmbH
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

DROP TABLE IF EXISTS p42_int_async_messages CASCADE;
DROP TABLE IF EXISTS p28_his_async_channel CASCADE;
DROP TABLE IF EXISTS p28_cfg_async_channel CASCADE;
DROP TABLE IF EXISTS p28_his_async_queue CASCADE;
DROP TABLE IF EXISTS p28_cfg_async_queue CASCADE;

-- This source has been automatically created by the bonaparte DSL (bonaparte.jpa addon). Do not modify, changes will be lost.
-- The bonaparte DSL is open source, licensed under Apache License, Version 2.0. It is based on Eclipse Xtext2.
-- The sources for bonaparte-DSL can be obtained at www.github.com/jpaw/bonaparte-dsl.git

CREATE TABLE p28_cfg_async_queue (
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
    -- table columns of java class AsyncQueueRef
    -- table columns of java class AsyncQueueDTO
    , async_queue_id varchar(16) NOT NULL
    , is_active boolean NOT NULL
    , description varchar(80) NOT NULL
    , default_serializer_qualifier varchar(32)
    , sender_qualifier varchar(32)
    , max_message_at_startup integer
    , timeout_idle_green integer
    , timeout_idle_red integer
    , timeout_external integer
    , wait_after_ext_error integer
    , wait_after_db_errors integer
    , max_age_in_seconds integer
    , purge_after_seconds integer
    , z text
);

ALTER TABLE p28_cfg_async_queue ADD CONSTRAINT p28_cfg_async_queue_pk PRIMARY KEY (
    object_ref
);
CREATE UNIQUE INDEX p28_cfg_async_queue_u1 ON p28_cfg_async_queue(
    tenant_ref, async_queue_id
);
GRANT SELECT,INSERT,UPDATE,DELETE ON p28_cfg_async_queue TO p42user;
GRANT SELECT ON p28_cfg_async_queue TO p42ro;
GRANT SELECT,INSERT,UPDATE,DELETE ON p28_cfg_async_queue TO p42rw;

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_cfg_async_queue.c_tech_user_id IS 'noinsert removed, causes problems with H2 unit tests';
COMMENT ON COLUMN p28_cfg_async_queue.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantRef42
COMMENT ON COLUMN p28_cfg_async_queue.tenant_ref IS 'the multitenancy discriminator';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_cfg_async_queue.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class AsyncQueueRef
-- comments for columns of java class AsyncQueueDTO
COMMENT ON COLUMN p28_cfg_async_queue.async_queue_id IS 'the ID of the queue';
COMMENT ON COLUMN p28_cfg_async_queue.is_active IS 'messages sent to inactive channels will be discarded (not generate an error!)';
COMMENT ON COLUMN p28_cfg_async_queue.description IS 'some descriptive text';
COMMENT ON COLUMN p28_cfg_async_queue.default_serializer_qualifier IS 'specific serialization code (for dependency injection)';
COMMENT ON COLUMN p28_cfg_async_queue.sender_qualifier IS 'specific serialization code (for dependency injection)';
COMMENT ON COLUMN p28_cfg_async_queue.max_message_at_startup IS 'how many persisted messages to load initially at max [100]';
COMMENT ON COLUMN p28_cfg_async_queue.timeout_idle_green IS 'sleep time in ms when queue is empty and last request was green [500 ms]';
COMMENT ON COLUMN p28_cfg_async_queue.timeout_idle_red IS 'sleep time in ms after an error [5000 ms]';
COMMENT ON COLUMN p28_cfg_async_queue.timeout_external IS 'max allowable duration of an external request [1000 ms]';
COMMENT ON COLUMN p28_cfg_async_queue.wait_after_ext_error IS 'sleep time in ms after an initial remote problem [10000 ms]';
COMMENT ON COLUMN p28_cfg_async_queue.wait_after_db_errors IS 'sleep time in ms after a database error [60000 ms]';
COMMENT ON COLUMN p28_cfg_async_queue.max_age_in_seconds IS 'can be used by monitoring to raise an alert if the queue''s pending messages are stalled for this amount of time';
COMMENT ON COLUMN p28_cfg_async_queue.purge_after_seconds IS 'a which age to delete sent messages';
COMMENT ON COLUMN p28_cfg_async_queue.z IS 'additional custom attributes';
-- This source has been automatically created by the bonaparte DSL (bonaparte.jpa addon). Do not modify, changes will be lost.
-- The bonaparte DSL is open source, licensed under Apache License, Version 2.0. It is based on Eclipse Xtext2.
-- The sources for bonaparte-DSL can be obtained at www.github.com/jpaw/bonaparte-dsl.git

CREATE TABLE p28_cfg_async_channel (
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
    -- table columns of java class AsyncChannelRef
    -- table columns of java class AsyncChannelDTO
    , async_channel_id varchar(16) NOT NULL
    , is_active boolean NOT NULL
    , description varchar(80) NOT NULL
    , async_queue_ref bigint
    , url varchar(255) NOT NULL
    , auth_type varchar(16)
    , auth_param varchar(255)
    , max_retries integer
    , payload_format varchar(1) NOT NULL
    , serializer_qualifier varchar(32)
    , timeout_in_ms integer
    , z text
);

ALTER TABLE p28_cfg_async_channel ADD CONSTRAINT p28_cfg_async_channel_pk PRIMARY KEY (
    object_ref
);
CREATE UNIQUE INDEX p28_cfg_async_channel_u1 ON p28_cfg_async_channel(
    tenant_ref, async_channel_id
);
GRANT SELECT,INSERT,UPDATE,DELETE ON p28_cfg_async_channel TO p42user;
GRANT SELECT ON p28_cfg_async_channel TO p42ro;
GRANT SELECT,INSERT,UPDATE,DELETE ON p28_cfg_async_channel TO p42rw;

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_cfg_async_channel.c_tech_user_id IS 'noinsert removed, causes problems with H2 unit tests';
COMMENT ON COLUMN p28_cfg_async_channel.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantRef42
COMMENT ON COLUMN p28_cfg_async_channel.tenant_ref IS 'the multitenancy discriminator';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_cfg_async_channel.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class AsyncChannelRef
-- comments for columns of java class AsyncChannelDTO
COMMENT ON COLUMN p28_cfg_async_channel.async_channel_id IS 'the ID of the channel';
COMMENT ON COLUMN p28_cfg_async_channel.is_active IS 'messages sent to inactive channels will be discarded (not generate an error!)';
COMMENT ON COLUMN p28_cfg_async_channel.description IS 'some descriptive text';
COMMENT ON COLUMN p28_cfg_async_channel.url IS 'remote URL';
COMMENT ON COLUMN p28_cfg_async_channel.auth_type IS 'basic, apikey etc... (for information / documentation in core, can be used by customizations)';
COMMENT ON COLUMN p28_cfg_async_channel.auth_param IS 'userID / password or API-Key';
COMMENT ON COLUMN p28_cfg_async_channel.max_retries IS 'can be used by monitoring to raise an alert after this number of attempts';
COMMENT ON COLUMN p28_cfg_async_channel.payload_format IS 'XML, JSON etc. (for information / documentation in core, can be used by customizations supporting multiple formats)';
COMMENT ON COLUMN p28_cfg_async_channel.serializer_qualifier IS 'specific serialization code (for dependency injection)';
COMMENT ON COLUMN p28_cfg_async_channel.timeout_in_ms IS 'if not null, overrides the default timeout sprecified in the XML';
COMMENT ON COLUMN p28_cfg_async_channel.z IS 'additional custom attributes';
-- This source has been automatically created by the bonaparte DSL (bonaparte.jpa addon). Do not modify, changes will be lost.
-- The bonaparte DSL is open source, licensed under Apache License, Version 2.0. It is based on Eclipse Xtext2.
-- The sources for bonaparte-DSL can be obtained at www.github.com/jpaw/bonaparte-dsl.git

CREATE TABLE p28_his_async_queue (
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
    , history_seq_ref   bigint NOT NULL
    , history_change_type   char(1) NOT NULL
    -- table columns of java class AbstractRef
    -- table columns of java class Ref
    , object_ref bigint NOT NULL
    -- table columns of java class AsyncQueueRef
    -- table columns of java class AsyncQueueDTO
    , async_queue_id varchar(16) NOT NULL
    , is_active boolean NOT NULL
    , description varchar(80) NOT NULL
    , default_serializer_qualifier varchar(32)
    , sender_qualifier varchar(32)
    , max_message_at_startup integer
    , timeout_idle_green integer
    , timeout_idle_red integer
    , timeout_external integer
    , wait_after_ext_error integer
    , wait_after_db_errors integer
    , max_age_in_seconds integer
    , purge_after_seconds integer
    , z text
);

ALTER TABLE p28_his_async_queue ADD CONSTRAINT p28_his_async_queue_pk PRIMARY KEY (
    object_ref, history_seq_ref
);
GRANT SELECT ON p28_his_async_queue TO p42rw;
GRANT SELECT ON p28_his_async_queue TO p42ro;

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_his_async_queue.c_tech_user_id IS 'noinsert removed, causes problems with H2 unit tests';
COMMENT ON COLUMN p28_his_async_queue.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantRef42
COMMENT ON COLUMN p28_his_async_queue.tenant_ref IS 'the multitenancy discriminator';
COMMENT ON COLUMN p28_his_async_queue.history_seq_ref IS 'current sequence number of history entry';
COMMENT ON COLUMN p28_his_async_queue.history_change_type IS 'type of change (C=create/insert, U=update, D=delete)';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_his_async_queue.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class AsyncQueueRef
-- comments for columns of java class AsyncQueueDTO
COMMENT ON COLUMN p28_his_async_queue.async_queue_id IS 'the ID of the queue';
COMMENT ON COLUMN p28_his_async_queue.is_active IS 'messages sent to inactive channels will be discarded (not generate an error!)';
COMMENT ON COLUMN p28_his_async_queue.description IS 'some descriptive text';
COMMENT ON COLUMN p28_his_async_queue.default_serializer_qualifier IS 'specific serialization code (for dependency injection)';
COMMENT ON COLUMN p28_his_async_queue.sender_qualifier IS 'specific serialization code (for dependency injection)';
COMMENT ON COLUMN p28_his_async_queue.max_message_at_startup IS 'how many persisted messages to load initially at max [100]';
COMMENT ON COLUMN p28_his_async_queue.timeout_idle_green IS 'sleep time in ms when queue is empty and last request was green [500 ms]';
COMMENT ON COLUMN p28_his_async_queue.timeout_idle_red IS 'sleep time in ms after an error [5000 ms]';
COMMENT ON COLUMN p28_his_async_queue.timeout_external IS 'max allowable duration of an external request [1000 ms]';
COMMENT ON COLUMN p28_his_async_queue.wait_after_ext_error IS 'sleep time in ms after an initial remote problem [10000 ms]';
COMMENT ON COLUMN p28_his_async_queue.wait_after_db_errors IS 'sleep time in ms after a database error [60000 ms]';
COMMENT ON COLUMN p28_his_async_queue.max_age_in_seconds IS 'can be used by monitoring to raise an alert if the queue''s pending messages are stalled for this amount of time';
COMMENT ON COLUMN p28_his_async_queue.purge_after_seconds IS 'a which age to delete sent messages';
COMMENT ON COLUMN p28_his_async_queue.z IS 'additional custom attributes';
-- This source has been automatically created by the bonaparte DSL (bonaparte.jpa addon). Do not modify, changes will be lost.
-- The bonaparte DSL is open source, licensed under Apache License, Version 2.0. It is based on Eclipse Xtext2.
-- The sources for bonaparte-DSL can be obtained at www.github.com/jpaw/bonaparte-dsl.git

CREATE TABLE p28_his_async_channel (
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
    , history_seq_ref   bigint NOT NULL
    , history_change_type   char(1) NOT NULL
    -- table columns of java class AbstractRef
    -- table columns of java class Ref
    , object_ref bigint NOT NULL
    -- table columns of java class AsyncChannelRef
    -- table columns of java class AsyncChannelDTO
    , async_channel_id varchar(16) NOT NULL
    , is_active boolean NOT NULL
    , description varchar(80) NOT NULL
    , async_queue_ref bigint
    , url varchar(255) NOT NULL
    , auth_type varchar(16)
    , auth_param varchar(255)
    , max_retries integer
    , payload_format varchar(1) NOT NULL
    , serializer_qualifier varchar(32)
    , timeout_in_ms integer
    , z text
);

ALTER TABLE p28_his_async_channel ADD CONSTRAINT p28_his_async_channel_pk PRIMARY KEY (
    object_ref, history_seq_ref
);
GRANT SELECT ON p28_his_async_channel TO p42rw;
GRANT SELECT ON p28_his_async_channel TO p42ro;

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_his_async_channel.c_tech_user_id IS 'noinsert removed, causes problems with H2 unit tests';
COMMENT ON COLUMN p28_his_async_channel.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantRef42
COMMENT ON COLUMN p28_his_async_channel.tenant_ref IS 'the multitenancy discriminator';
COMMENT ON COLUMN p28_his_async_channel.history_seq_ref IS 'current sequence number of history entry';
COMMENT ON COLUMN p28_his_async_channel.history_change_type IS 'type of change (C=create/insert, U=update, D=delete)';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_his_async_channel.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class AsyncChannelRef
-- comments for columns of java class AsyncChannelDTO
COMMENT ON COLUMN p28_his_async_channel.async_channel_id IS 'the ID of the channel';
COMMENT ON COLUMN p28_his_async_channel.is_active IS 'messages sent to inactive channels will be discarded (not generate an error!)';
COMMENT ON COLUMN p28_his_async_channel.description IS 'some descriptive text';
COMMENT ON COLUMN p28_his_async_channel.url IS 'remote URL';
COMMENT ON COLUMN p28_his_async_channel.auth_type IS 'basic, apikey etc... (for information / documentation in core, can be used by customizations)';
COMMENT ON COLUMN p28_his_async_channel.auth_param IS 'userID / password or API-Key';
COMMENT ON COLUMN p28_his_async_channel.max_retries IS 'can be used by monitoring to raise an alert after this number of attempts';
COMMENT ON COLUMN p28_his_async_channel.payload_format IS 'XML, JSON etc. (for information / documentation in core, can be used by customizations supporting multiple formats)';
COMMENT ON COLUMN p28_his_async_channel.serializer_qualifier IS 'specific serialization code (for dependency injection)';
COMMENT ON COLUMN p28_his_async_channel.timeout_in_ms IS 'if not null, overrides the default timeout sprecified in the XML';
COMMENT ON COLUMN p28_his_async_channel.z IS 'additional custom attributes';
-- This source has been automatically created by the bonaparte DSL (bonaparte.jpa addon). Do not modify, changes will be lost.
-- The bonaparte DSL is open source, licensed under Apache License, Version 2.0. It is based on Eclipse Xtext2.
-- The sources for bonaparte-DSL can be obtained at www.github.com/jpaw/bonaparte-dsl.git

CREATE TABLE p42_int_async_messages (
    -- table columns of java class TrackingBase
    -- table columns of java class WriteTracking
      c_tech_user_id varchar(16) DEFAULT CURRENT_USER NOT NULL
    , c_app_user_id varchar(16) NOT NULL
    , c_timestamp timestamp(0) DEFAULT CURRENT_TIMESTAMP NOT NULL
    , c_process_ref bigint NOT NULL
    -- table columns of java class InternalTenantRef42
    , tenant_ref bigint NOT NULL
    -- table columns of java class AbstractRef
    -- table columns of java class Ref
    , object_ref bigint NOT NULL
    -- table columns of java class AsyncMessageRef
    -- table columns of java class AsyncMessageDTO
    , async_channel_id varchar(16) NOT NULL
    , async_queue_ref bigint
    , status varchar(1)
    , when_sent timestamp(3)
    , last_attempt timestamp(3)
    , attempts integer NOT NULL
    , payload bytea NOT NULL
    , ref_type varchar(4)
    , ref_identifier varchar(36)
    , ref bigint
    , http_response_code integer
    , return_code integer
    , reference varchar(80)
);

ALTER TABLE p42_int_async_messages ADD CONSTRAINT p42_int_async_messages_pk PRIMARY KEY (
    object_ref
);
CREATE INDEX p42_int_async_messages_i1 ON p42_int_async_messages(
    status
);
GRANT SELECT,INSERT,UPDATE,DELETE ON p42_int_async_messages TO p42user;
GRANT SELECT ON p42_int_async_messages TO p42ro;
GRANT SELECT,INSERT,UPDATE,DELETE ON p42_int_async_messages TO p42rw;

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p42_int_async_messages.c_tech_user_id IS 'noinsert removed, causes problems with H2 unit tests';
COMMENT ON COLUMN p42_int_async_messages.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class InternalTenantRef42
COMMENT ON COLUMN p42_int_async_messages.tenant_ref IS 'the multitenancy discriminator';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p42_int_async_messages.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class AsyncMessageRef
-- comments for columns of java class AsyncMessageDTO
COMMENT ON COLUMN p42_int_async_messages.async_channel_id IS 'used to retrieve the URL and authentication parameters';
COMMENT ON COLUMN p42_int_async_messages.status IS 'specifies if the message must still be sent or has been sent';
COMMENT ON COLUMN p42_int_async_messages.when_sent IS 'defines when the message was initiated (which is before the commit of the sending transaction is done, and before the EventBus transmission)';
COMMENT ON COLUMN p42_int_async_messages.last_attempt IS 'null if no initial send attempt done, date of transmission or last attempt otherwise';
COMMENT ON COLUMN p42_int_async_messages.attempts IS 'number of send attempts so far (initially 0)';
COMMENT ON COLUMN p42_int_async_messages.payload IS 'the serialized message';
COMMENT ON COLUMN p42_int_async_messages.ref_type IS 'for debugging / maintenance: the type of reference';
COMMENT ON COLUMN p42_int_async_messages.ref_identifier IS 'for debugging / maintenance: order ID, customer ID etc.';
COMMENT ON COLUMN p42_int_async_messages.ref IS 'for debugging / maintenance: related objectRef';
COMMENT ON COLUMN p42_int_async_messages.http_response_code IS 'if the remote returned some http response code';
COMMENT ON COLUMN p42_int_async_messages.return_code IS 'last return code returned by receiver (in payload)';
COMMENT ON COLUMN p42_int_async_messages.reference IS 'a reference to the client object, or text response or additional error code';
