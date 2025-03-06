-- TBE-2381: Generic config (POSTGRES)
DROP SEQUENCE IF EXISTS p42_cfg_generic_config_s;
CREATE SEQUENCE p42_cfg_generic_config_s;

DROP TABLE IF EXISTS p42_cfg_generic_config;
CREATE TABLE p42_cfg_generic_config (
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
    -- table columns of java class GenericConfigRef
    -- table columns of java class GenericConfigDTO
    , config_group varchar(16) NOT NULL
    , config_key varchar(36) NOT NULL
    , is_active boolean NOT NULL
    , name varchar(80) NOT NULL
    , str1 varchar(16)
    , str2 varchar(16)
    , int1 integer
    , int2 integer
    , bool1 boolean
    , bool2 boolean
    , value1 decimal(18,6)
    , value2 decimal(18,6)
    , z text
);

ALTER TABLE p42_cfg_generic_config DROP CONSTRAINT IF EXISTS p42_cfg_generic_config_pk;
ALTER TABLE p42_cfg_generic_config ADD CONSTRAINT p42_cfg_generic_config_pk PRIMARY KEY (
    object_ref
);

DROP INDEX IF EXISTS p42_cfg_generic_config_u1;
CREATE UNIQUE INDEX p42_cfg_generic_config_u1 ON p42_cfg_generic_config (
    tenant_id, config_group, config_key
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p42_cfg_generic_config.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p42_cfg_generic_config.tenant_id IS 'the multitenancy discriminator';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p42_cfg_generic_config.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class GenericConfigRef
-- comments for columns of java class GenericConfigDTO
COMMENT ON COLUMN p42_cfg_generic_config.config_group IS 'the fixed key (application index)';
COMMENT ON COLUMN p42_cfg_generic_config.config_key IS 'a concatenated key of additional key parameters, "-" if not required.';
COMMENT ON COLUMN p42_cfg_generic_config.name IS 'the displayed name of the entry';

-- HISTORY TABLE
DROP TABLE IF EXISTS p42_his_generic_config;
CREATE TABLE p42_his_generic_config (
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
    -- table columns of java class GenericConfigRef
    -- table columns of java class GenericConfigDTO
    , config_group varchar(16) NOT NULL
    , config_key varchar(36) NOT NULL
    , is_active boolean NOT NULL
    , name varchar(80) NOT NULL
    , str1 varchar(16)
    , str2 varchar(16)
    , int1 integer
    , int2 integer
    , bool1 boolean
    , bool2 boolean
    , value1 decimal(18,6)
    , value2 decimal(18,6)
    , z text
);

ALTER TABLE p42_his_generic_config DROP CONSTRAINT IF EXISTS p42_his_generic_config_pk;
ALTER TABLE p42_his_generic_config ADD CONSTRAINT p42_his_generic_config_pk PRIMARY KEY (
    object_ref, history_seq_ref
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p42_his_generic_config.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p42_his_generic_config.tenant_id IS 'the multitenancy discriminator';
COMMENT ON COLUMN p42_his_generic_config.history_seq_ref IS 'current sequence number of history entry';
COMMENT ON COLUMN p42_his_generic_config.history_change_type IS 'type of change (C=create/insert, U=update, D=delete)';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p42_his_generic_config.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class GenericConfigRef
-- comments for columns of java class GenericConfigDTO
COMMENT ON COLUMN p42_his_generic_config.config_group IS 'the fixed key (application index)';
COMMENT ON COLUMN p42_his_generic_config.config_key IS 'a concatenated key of additional key parameters, "-" if not required.';
COMMENT ON COLUMN p42_his_generic_config.name IS 'the displayed name of the entry';

-- VIEWS
CREATE OR REPLACE VIEW p42_cfg_generic_config_nt AS SELECT
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class GenericConfigRef
    -- columns of java class GenericConfigDTO
    , t0.config_group AS config_group
    , t0.config_key AS config_key
    , t0.is_active AS is_active
    , t0.name AS name
    , t0.str1 AS str1
    , t0.str2 AS str2
    , t0.int1 AS int1
    , t0.int2 AS int2
    , t0.bool1 AS bool1
    , t0.bool2 AS bool2
    , t0.value1 AS value1
    , t0.value2 AS value2
    , t0.z AS z
FROM p42_cfg_generic_config t0;

CREATE OR REPLACE VIEW p42_cfg_generic_config_v AS SELECT
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
    -- columns of java class GenericConfigRef
    -- columns of java class GenericConfigDTO
    , t0.config_group AS config_group
    , t0.config_key AS config_key
    , t0.is_active AS is_active
    , t0.name AS name
    , t0.str1 AS str1
    , t0.str2 AS str2
    , t0.int1 AS int1
    , t0.int2 AS int2
    , t0.bool1 AS bool1
    , t0.bool2 AS bool2
    , t0.value1 AS value1
    , t0.value2 AS value2
    , t0.z AS z
FROM p42_cfg_generic_config t0;



CREATE OR REPLACE FUNCTION p42_cfg_generic_config_tp() RETURNS TRIGGER AS $p42_cfg_generic_config_td$
DECLARE
    next_seq_ BIGINT;
BEGIN
    SELECT NEXTVAL('cm_idgen_5009_seq') INTO next_seq_;
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO p42_his_generic_config (
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
            , config_group
            , config_key
            , is_active
            , name
            , str1
            , str2
            , int1
            , int2
            , bool1
            , bool2
            , value1
            , value2
            , z
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
            , NEW.config_group
            , NEW.config_key
            , NEW.is_active
            , NEW.name
            , NEW.str1
            , NEW.str2
            , NEW.int1
            , NEW.int2
            , NEW.bool1
            , NEW.bool2
            , NEW.value1
            , NEW.value2
            , NEW.z
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'UPDATE') THEN
        -- deny attempts to change a primary key column
        IF OLD.object_ref <> NEW.object_ref THEN
            RAISE EXCEPTION 'Cannot change primary key column to different value';
        END IF;
        INSERT INTO p42_his_generic_config (
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
            , config_group
            , config_key
            , is_active
            , name
            , str1
            , str2
            , int1
            , int2
            , bool1
            , bool2
            , value1
            , value2
            , z
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
            , NEW.config_group
            , NEW.config_key
            , NEW.is_active
            , NEW.name
            , NEW.str1
            , NEW.str2
            , NEW.int1
            , NEW.int2
            , NEW.bool1
            , NEW.bool2
            , NEW.value1
            , NEW.value2
            , NEW.z
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO p42_his_generic_config (
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
            , config_group
            , config_key
            , is_active
            , name
            , str1
            , str2
            , int1
            , int2
            , bool1
            , bool2
            , value1
            , value2
            , z
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
            , OLD.config_group
            , OLD.config_key
            , OLD.is_active
            , OLD.name
            , OLD.str1
            , OLD.str2
            , OLD.int1
            , OLD.int2
            , OLD.bool1
            , OLD.bool2
            , OLD.value1
            , OLD.value2
            , OLD.z
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$p42_cfg_generic_config_td$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS p42_cfg_generic_config_tr ON p42_cfg_generic_config;

CREATE TRIGGER p42_cfg_generic_config_tr
    AFTER INSERT OR DELETE OR UPDATE ON p42_cfg_generic_config
    FOR EACH ROW EXECUTE PROCEDURE p42_cfg_generic_config_tp();

