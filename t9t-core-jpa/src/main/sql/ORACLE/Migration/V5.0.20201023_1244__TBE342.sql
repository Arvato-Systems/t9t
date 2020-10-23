-- TBE-342: new tables for dynamic plugins (ORACLE)

-- This source has been automatically created by the bonaparte DSL (bonaparte.jpa addon). Do not modify, changes will be lost.
-- The bonaparte DSL is open source, licensed under Apache License, Version 2.0. It is based on Eclipse Xtext2.
-- The sources for bonaparte-DSL can be obtained at www.github.com/jpaw/bonaparte-dsl.git

CREATE TABLE p28_cfg_loaded_plugin (
    -- table columns of java class TrackingBase
    -- table columns of java class WriteTracking
      c_tech_user_id varchar2(16 char) DEFAULT SUBSTR(USER, 1, 8) NOT NULL
    , c_app_user_id varchar2(16 char) NOT NULL
    , c_timestamp date DEFAULT SYSDATE NOT NULL
    , c_process_ref number(20) NOT NULL
    -- table columns of java class FullTracking
    , m_tech_user_id varchar2(16 char) DEFAULT SUBSTR(USER, 1, 8) NOT NULL
    , m_app_user_id varchar2(16 char) NOT NULL
    , m_timestamp date DEFAULT SYSDATE NOT NULL
    , m_process_ref number(20) NOT NULL
    -- table columns of java class FullTrackingWithVersion
    , version number(10) NOT NULL
    -- table columns of java class InternalTenantRef42
    , tenant_ref number(20) NOT NULL
    -- table columns of java class AbstractRef
    -- table columns of java class Ref
    , object_ref number(20) NOT NULL
    -- table columns of java class LoadedPluginRef
    -- table columns of java class LoadedPluginDTO
    , is_active number(1) NOT NULL
    , priority number(10) NOT NULL
    , plugin_id varchar2(32 char) NOT NULL
    , plugin_version varchar2(16) NOT NULL
    , description varchar2(80 char) NOT NULL
    , when_loaded date NOT NULL
    , jar_file blob NOT NULL
) TABLESPACE rts42dat0D;

ALTER TABLE p28_cfg_loaded_plugin ADD CONSTRAINT p28_cfg_loaded_plugin_pk PRIMARY KEY (
    object_ref
) USING INDEX TABLESPACE rts42dat0I;
CREATE UNIQUE INDEX p28_cfg_loaded_plugin_u1 ON p28_cfg_loaded_plugin(
    tenant_ref, plugin_id
) TABLESPACE rts42dat0I;

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_cfg_loaded_plugin.c_tech_user_id IS 'noinsert removed, causes problems with H2 unit tests';
COMMENT ON COLUMN p28_cfg_loaded_plugin.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantRef42
COMMENT ON COLUMN p28_cfg_loaded_plugin.tenant_ref IS 'the multitenancy discriminator';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_cfg_loaded_plugin.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class LoadedPluginRef
-- comments for columns of java class LoadedPluginDTO
COMMENT ON COLUMN p28_cfg_loaded_plugin.priority IS 'defines the sequence of loading. Can not be rearranged';
COMMENT ON COLUMN p28_cfg_loaded_plugin.description IS 'documentation';
COMMENT ON COLUMN p28_cfg_loaded_plugin.jar_file IS 'the JAR file image';
-- This source has been automatically created by the bonaparte DSL (bonaparte.jpa addon). Do not modify, changes will be lost.
-- The bonaparte DSL is open source, licensed under Apache License, Version 2.0. It is based on Eclipse Xtext2.
-- The sources for bonaparte-DSL can be obtained at www.github.com/jpaw/bonaparte-dsl.git

CREATE TABLE p28_his_loaded_plugin (
    -- table columns of java class TrackingBase
    -- table columns of java class WriteTracking
      c_tech_user_id varchar2(16 char) DEFAULT SUBSTR(USER, 1, 8) NOT NULL
    , c_app_user_id varchar2(16 char) NOT NULL
    , c_timestamp date DEFAULT SYSDATE NOT NULL
    , c_process_ref number(20) NOT NULL
    -- table columns of java class FullTracking
    , m_tech_user_id varchar2(16 char) DEFAULT SUBSTR(USER, 1, 8) NOT NULL
    , m_app_user_id varchar2(16 char) NOT NULL
    , m_timestamp date DEFAULT SYSDATE NOT NULL
    , m_process_ref number(20) NOT NULL
    -- table columns of java class FullTrackingWithVersion
    , version number(10) NOT NULL
    -- table columns of java class InternalTenantRef42
    , tenant_ref number(20) NOT NULL
    , history_seq_ref   number(20) NOT NULL
    , history_change_type   varchar2(1 char) NOT NULL
    -- table columns of java class AbstractRef
    -- table columns of java class Ref
    , object_ref number(20) NOT NULL
    -- table columns of java class LoadedPluginRef
    -- table columns of java class LoadedPluginDTO
    , is_active number(1) NOT NULL
    , priority number(10) NOT NULL
    , plugin_id varchar2(32 char) NOT NULL
    , plugin_version varchar2(16) NOT NULL
    , description varchar2(80 char) NOT NULL
    , when_loaded date NOT NULL
    , jar_file blob NOT NULL
) TABLESPACE rts42dat0D;

ALTER TABLE p28_his_loaded_plugin ADD CONSTRAINT p28_his_loaded_plugin_pk PRIMARY KEY (
    object_ref, history_seq_ref
) USING INDEX TABLESPACE rts42dat0I;

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_his_loaded_plugin.c_tech_user_id IS 'noinsert removed, causes problems with H2 unit tests';
COMMENT ON COLUMN p28_his_loaded_plugin.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantRef42
COMMENT ON COLUMN p28_his_loaded_plugin.tenant_ref IS 'the multitenancy discriminator';
COMMENT ON COLUMN p28_his_loaded_plugin.history_seq_ref IS 'current sequence number of history entry';
COMMENT ON COLUMN p28_his_loaded_plugin.history_change_type IS 'type of change (C=create/insert, U=update, D=delete)';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_his_loaded_plugin.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class LoadedPluginRef
-- comments for columns of java class LoadedPluginDTO
COMMENT ON COLUMN p28_his_loaded_plugin.priority IS 'defines the sequence of loading. Can not be rearranged';
COMMENT ON COLUMN p28_his_loaded_plugin.description IS 'documentation';
COMMENT ON COLUMN p28_his_loaded_plugin.jar_file IS 'the JAR file image';
-- This source has been automatically created by the bonaparte DSL (bonaparte.jpa addon). Do not modify, changes will be lost.
-- The bonaparte DSL is open source, licensed under Apache License, Version 2.0. It is based on Eclipse Xtext2.
-- The sources for bonaparte-DSL can be obtained at www.github.com/jpaw/bonaparte-dsl.git

CREATE TABLE p28_dat_plugin_log (
    -- table columns of java class TrackingBase
    -- table columns of java class WriteTracking
      c_tech_user_id varchar2(16 char) DEFAULT SUBSTR(USER, 1, 8) NOT NULL
    , c_app_user_id varchar2(16 char) NOT NULL
    , c_timestamp date DEFAULT SYSDATE NOT NULL
    , c_process_ref number(20) NOT NULL
    -- table columns of java class InternalTenantRef42
    , tenant_ref number(20) NOT NULL
    -- table columns of java class AbstractRef
    -- table columns of java class Ref
    , object_ref number(20) NOT NULL
    -- table columns of java class PluginLogRef
    -- table columns of java class PluginLogDTO
    , is_active number(1) NOT NULL
    , priority number(10) NOT NULL
    , plugin_id varchar2(32 char) NOT NULL
    , plugin_version varchar2(16) NOT NULL
    , when_loaded date NOT NULL
    , when_removed date
) TABLESPACE rts42dat0D;

ALTER TABLE p28_dat_plugin_log ADD CONSTRAINT p28_dat_plugin_log_pk PRIMARY KEY (
    object_ref
) USING INDEX TABLESPACE rts42dat0I;

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_dat_plugin_log.c_tech_user_id IS 'noinsert removed, causes problems with H2 unit tests';
COMMENT ON COLUMN p28_dat_plugin_log.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class InternalTenantRef42
COMMENT ON COLUMN p28_dat_plugin_log.tenant_ref IS 'the multitenancy discriminator';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_dat_plugin_log.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class PluginLogRef
-- comments for columns of java class PluginLogDTO
COMMENT ON COLUMN p28_dat_plugin_log.priority IS 'defines the sequence of loading. Can not be rearranged';
