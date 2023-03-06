CREATE TABLE p28_cfg_translations (
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
    -- table columns of java class TranslationsRef
    -- table columns of java class TranslationsKey
    , category varchar(1) NOT NULL
    , qualifier varchar(255) NOT NULL
    , id varchar(30) NOT NULL
    , language_code varchar(32) NOT NULL
    -- table columns of java class TranslationsDTO
    , text varchar(2000) NOT NULL
);

ALTER TABLE p28_cfg_translations ADD CONSTRAINT p28_cfg_translations_pk PRIMARY KEY (
    object_ref
);
CREATE UNIQUE INDEX p28_cfg_translations_u1 ON p28_cfg_translations(
    tenant_id, category, id, qualifier, language_code
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_cfg_translations.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_cfg_translations.tenant_id IS 'the multitenancy discriminator';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_cfg_translations.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class TranslationsRef
-- comments for columns of java class TranslationsKey
COMMENT ON COLUMN p28_cfg_translations.qualifier IS 'screen or grid ID or package name or enum PQON';
COMMENT ON COLUMN p28_cfg_translations.id IS 'restricted to 30 due to Oracle column name compatibility';
-- comments for columns of java class TranslationsDTO

CREATE TABLE p28_cfg_trns_module_cfg (
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
    -- table columns of java class CompositeKeyBase
    -- table columns of java class ModuleConfigDTO
    , z text
    -- table columns of java class TrnsModuleCfgDTO
    , attempt_local_tenant boolean NOT NULL
    , attempt_dialects boolean NOT NULL
);

ALTER TABLE p28_cfg_trns_module_cfg ADD CONSTRAINT p28_cfg_trns_module_cfg_pk PRIMARY KEY (
    tenant_id
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_cfg_trns_module_cfg.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_cfg_trns_module_cfg.tenant_id IS 'the multitenancy discriminator';
-- comments for columns of java class CompositeKeyBase
-- comments for columns of java class ModuleConfigDTO
COMMENT ON COLUMN p28_cfg_trns_module_cfg.z IS 'custom parameters: exist for all modules by default';
-- comments for columns of java class TrnsModuleCfgDTO
COMMENT ON COLUMN p28_cfg_trns_module_cfg.attempt_local_tenant IS 'if false, only the global tenant will be check for translations';
COMMENT ON COLUMN p28_cfg_trns_module_cfg.attempt_dialects IS 'if false, only 2 letter ISO639 codes will be checked for translations';

CREATE TABLE p28_his_translations (
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
    -- table columns of java class TranslationsRef
    -- table columns of java class TranslationsKey
    , category varchar(1) NOT NULL
    , qualifier varchar(255) NOT NULL
    , id varchar(30) NOT NULL
    , language_code varchar(32) NOT NULL
    -- table columns of java class TranslationsDTO
    , text varchar(2000) NOT NULL
);

ALTER TABLE p28_his_translations ADD CONSTRAINT p28_his_translations_pk PRIMARY KEY (
    object_ref, history_seq_ref
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_his_translations.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_his_translations.tenant_id IS 'the multitenancy discriminator';
COMMENT ON COLUMN p28_his_translations.history_seq_ref IS 'current sequence number of history entry';
COMMENT ON COLUMN p28_his_translations.history_change_type IS 'type of change (C=create/insert, U=update, D=delete)';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_his_translations.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class TranslationsRef
-- comments for columns of java class TranslationsKey
COMMENT ON COLUMN p28_his_translations.qualifier IS 'screen or grid ID or package name or enum PQON';
COMMENT ON COLUMN p28_his_translations.id IS 'restricted to 30 due to Oracle column name compatibility';
-- comments for columns of java class TranslationsDTO

CREATE TABLE p28_his_trns_module_cfg (
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
    -- table columns of java class CompositeKeyBase
    -- table columns of java class ModuleConfigDTO
    , z text
    -- table columns of java class TrnsModuleCfgDTO
    , attempt_local_tenant boolean NOT NULL
    , attempt_dialects boolean NOT NULL
);

ALTER TABLE p28_his_trns_module_cfg ADD CONSTRAINT p28_his_trns_module_cfg_pk PRIMARY KEY (
    tenant_id, history_seq_ref
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_his_trns_module_cfg.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_his_trns_module_cfg.tenant_id IS 'the multitenancy discriminator';
COMMENT ON COLUMN p28_his_trns_module_cfg.history_seq_ref IS 'current sequence number of history entry';
COMMENT ON COLUMN p28_his_trns_module_cfg.history_change_type IS 'type of change (C=create/insert, U=update, D=delete)';
-- comments for columns of java class CompositeKeyBase
-- comments for columns of java class ModuleConfigDTO
COMMENT ON COLUMN p28_his_trns_module_cfg.z IS 'custom parameters: exist for all modules by default';
-- comments for columns of java class TrnsModuleCfgDTO
COMMENT ON COLUMN p28_his_trns_module_cfg.attempt_local_tenant IS 'if false, only the global tenant will be check for translations';
COMMENT ON COLUMN p28_his_trns_module_cfg.attempt_dialects IS 'if false, only 2 letter ISO639 codes will be checked for translations';

CREATE OR REPLACE FUNCTION p28_cfg_translations_tp() RETURNS TRIGGER AS $p28_cfg_translations_td$
DECLARE
    next_seq_ BIGINT;
BEGIN
    SELECT NEXTVAL('cm_idgen_5009_seq') INTO next_seq_;
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO p28_his_translations (
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
            , category
            , qualifier
            , id
            , language_code
            , text
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
            , NEW.category
            , NEW.qualifier
            , NEW.id
            , NEW.language_code
            , NEW.text
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'UPDATE') THEN
        -- deny attempts to change a primary key column
        IF OLD.object_ref <> NEW.object_ref THEN
            RAISE EXCEPTION 'Cannot change primary key column to different value';
        END IF;
        INSERT INTO p28_his_translations (
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
            , category
            , qualifier
            , id
            , language_code
            , text
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
            , NEW.category
            , NEW.qualifier
            , NEW.id
            , NEW.language_code
            , NEW.text
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO p28_his_translations (
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
            , category
            , qualifier
            , id
            , language_code
            , text
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
            , OLD.category
            , OLD.qualifier
            , OLD.id
            , OLD.language_code
            , OLD.text
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$p28_cfg_translations_td$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS p28_cfg_translations_tr ON p28_cfg_translations;

CREATE TRIGGER p28_cfg_translations_tr
    AFTER INSERT OR DELETE OR UPDATE ON p28_cfg_translations
    FOR EACH ROW EXECUTE PROCEDURE p28_cfg_translations_tp();

CREATE OR REPLACE FUNCTION p28_cfg_trns_module_cfg_tp() RETURNS TRIGGER AS $p28_cfg_trns_module_cfg_td$
DECLARE
    next_seq_ BIGINT;
BEGIN
    SELECT NEXTVAL('cm_idgen_5009_seq') INTO next_seq_;
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO p28_his_trns_module_cfg (
            history_seq_ref
            , history_change_type
            , tenant_id
            , c_app_user_id
            , c_timestamp
            , c_process_ref
            , m_app_user_id
            , m_timestamp
            , m_process_ref
            , version
            , z
            , attempt_local_tenant
            , attempt_dialects
        ) VALUES (
            next_seq_, 'I'
            , NEW.tenant_id
            , NEW.c_app_user_id
            , NEW.c_timestamp
            , NEW.c_process_ref
            , NEW.m_app_user_id
            , NEW.m_timestamp
            , NEW.m_process_ref
            , NEW.version
            , NEW.z
            , NEW.attempt_local_tenant
            , NEW.attempt_dialects
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'UPDATE') THEN
        -- deny attempts to change a primary key column
        IF OLD.tenant_id <> NEW.tenant_id THEN
            RAISE EXCEPTION 'Cannot change primary key column to different value';
        END IF;
        INSERT INTO p28_his_trns_module_cfg (
            history_seq_ref
            , history_change_type
            , tenant_id
            , c_app_user_id
            , c_timestamp
            , c_process_ref
            , m_app_user_id
            , m_timestamp
            , m_process_ref
            , version
            , z
            , attempt_local_tenant
            , attempt_dialects
        ) VALUES (
            next_seq_, 'U'
            , NEW.tenant_id
            , NEW.c_app_user_id
            , NEW.c_timestamp
            , NEW.c_process_ref
            , NEW.m_app_user_id
            , NEW.m_timestamp
            , NEW.m_process_ref
            , NEW.version
            , NEW.z
            , NEW.attempt_local_tenant
            , NEW.attempt_dialects
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO p28_his_trns_module_cfg (
            history_seq_ref
            , history_change_type
            , tenant_id
            , c_app_user_id
            , c_timestamp
            , c_process_ref
            , m_app_user_id
            , m_timestamp
            , m_process_ref
            , version
            , z
            , attempt_local_tenant
            , attempt_dialects
        ) VALUES (
            next_seq_, 'D'
            , OLD.tenant_id
            , OLD.c_app_user_id
            , OLD.c_timestamp
            , OLD.c_process_ref
            , OLD.m_app_user_id
            , OLD.m_timestamp
            , OLD.m_process_ref
            , OLD.version
            , OLD.z
            , OLD.attempt_local_tenant
            , OLD.attempt_dialects
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$p28_cfg_trns_module_cfg_td$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS p28_cfg_trns_module_cfg_tr ON p28_cfg_trns_module_cfg;

CREATE TRIGGER p28_cfg_trns_module_cfg_tr
    AFTER INSERT OR DELETE OR UPDATE ON p28_cfg_trns_module_cfg
    FOR EACH ROW EXECUTE PROCEDURE p28_cfg_trns_module_cfg_tp();

-- convert a token (as stored in DB tables) of enum t9t.base.trns.TextCategory into the more readable symbolic constant string
CREATE OR REPLACE FUNCTION TextCategory2s(token VARCHAR) RETURNS VARCHAR
    IMMUTABLE STRICT
    AS $$
DECLARE
BEGIN
    IF token = 'D' THEN
        RETURN 'DEFAULT';
    END IF;
    IF token = 'H' THEN
        RETURN 'HEADER';
    END IF;
    IF token = 'U' THEN
        RETURN 'UI_LABEL';
    END IF;
    IF token = 'E' THEN
        RETURN 'ENUM';
    END IF;
    RETURN '~';  -- token for undefined mapping
END;
$$ LANGUAGE plpgsql;

-- convert a constant string of enum t9t.base.trns.TextCategory into the token used for DB table storage
CREATE OR REPLACE FUNCTION TextCategory2t(token VARCHAR) RETURNS VARCHAR
    IMMUTABLE STRICT
    AS $$
DECLARE
BEGIN
    IF token = 'DEFAULT' THEN
        RETURN 'D';
    END IF;
    IF token = 'HEADER' THEN
        RETURN 'H';
    END IF;
    IF token = 'UI_LABEL' THEN
        RETURN 'U';
    END IF;
    IF token = 'ENUM' THEN
        RETURN 'E';
    END IF;
    RETURN '~';  -- token for undefined mapping
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE VIEW p28_cfg_translations_nt AS SELECT
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class TranslationsRef
    -- columns of java class TranslationsKey
    , TextCategory2s(t0.category) AS category
    , t0.qualifier AS qualifier
    , t0.id AS id
    , t0.language_code AS language_code
    -- columns of java class TranslationsDTO
    , t0.text AS text
FROM p28_cfg_translations t0;

CREATE OR REPLACE VIEW p28_cfg_translations_v AS SELECT
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
    -- columns of java class TranslationsRef
    -- columns of java class TranslationsKey
    , TextCategory2s(t0.category) AS category
    , t0.qualifier AS qualifier
    , t0.id AS id
    , t0.language_code AS language_code
    -- columns of java class TranslationsDTO
    , t0.text AS text
FROM p28_cfg_translations t0;

CREATE OR REPLACE VIEW p28_cfg_trns_module_cfg_nt AS SELECT
    -- columns of java class CompositeKeyBase
    -- columns of java class ModuleConfigDTO
    t0.z AS z
    -- columns of java class TrnsModuleCfgDTO
    , t0.attempt_local_tenant AS attempt_local_tenant
    , t0.attempt_dialects AS attempt_dialects
FROM p28_cfg_trns_module_cfg t0;

CREATE OR REPLACE VIEW p28_cfg_trns_module_cfg_v AS SELECT
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
    -- columns of java class CompositeKeyBase
    -- columns of java class ModuleConfigDTO
    , t0.z AS z
    -- columns of java class TrnsModuleCfgDTO
    , t0.attempt_local_tenant AS attempt_local_tenant
    , t0.attempt_dialects AS attempt_dialects
FROM p28_cfg_trns_module_cfg t0;
