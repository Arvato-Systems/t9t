-- SQL migration for TBE-1725: Increase textChunk column size from varchar(65536) to varchar(4096000)

-- first add table/constraint/comments that were not created in earlier SQL migration
CREATE SEQUENCE IF NOT EXISTS p28_dat_ai_chat_log_s;
CREATE SEQUENCE IF NOT EXISTS p28_cfg_ai_dto_assist_s;

DROP TABLE IF EXISTS p28_dat_ai_chat_log CASCADE;
DROP TABLE IF EXISTS p28_cfg_ai_dto_assist CASCADE;
DROP TABLE IF EXISTS p28_his_ai_dto_assist CASCADE;

CREATE TABLE IF NOT EXISTS p28_dat_ai_chat_log (
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
    -- table columns of java class AiChatLogRef
    -- table columns of java class AiChatLogDTO
    , conversation_ref bigint NOT NULL
    , role_type varchar(1) NOT NULL
    , user_input varchar(4096000)
    , function_pqon varchar(255)
    , function_parameter bytea
    , sink_ref bigint
);

ALTER TABLE p28_dat_ai_chat_log DROP CONSTRAINT IF EXISTS p28_dat_ai_chat_log_pk;

ALTER TABLE p28_dat_ai_chat_log ADD CONSTRAINT p28_dat_ai_chat_log_pk PRIMARY KEY (
    object_ref
);
CREATE INDEX IF NOT EXISTS p28_dat_ai_chat_log_i1 ON p28_dat_ai_chat_log (
    conversation_ref
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_dat_ai_chat_log.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_dat_ai_chat_log.tenant_id IS 'the multitenancy discriminator';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_dat_ai_chat_log.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class AiChatLogRef
-- comments for columns of java class AiChatLogDTO
COMMENT ON COLUMN p28_dat_ai_chat_log.role_type IS 'determines whether the entry has been caused by user input or the model''s output';
COMMENT ON COLUMN p28_dat_ai_chat_log.user_input IS 'user''s request message';
COMMENT ON COLUMN p28_dat_ai_chat_log.function_pqon IS 'PQON of the invoked callback';
COMMENT ON COLUMN p28_dat_ai_chat_log.function_parameter IS 'parameters for the callback';
COMMENT ON COLUMN p28_dat_ai_chat_log.sink_ref IS 'uploaded or generated file';





CREATE TABLE IF NOT EXISTS p28_cfg_ai_dto_assist (
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
    -- table columns of java class AiDtoAssistRef
    -- table columns of java class AiDtoAssistDTO
    , pqon varchar(36) NOT NULL
    , description varchar(80) NOT NULL
    , ai_assistant_ref bigint NOT NULL
    , role varchar(65536) NOT NULL
    , dto_instructions varchar(65536) NOT NULL
    , dto_schema varchar(65536)
    , z text
);

ALTER TABLE p28_cfg_ai_dto_assist DROP CONSTRAINT IF EXISTS p28_cfg_ai_dto_assist_pk;

ALTER TABLE p28_cfg_ai_dto_assist ADD CONSTRAINT p28_cfg_ai_dto_assist_pk PRIMARY KEY (
    object_ref
);
CREATE UNIQUE INDEX IF NOT EXISTS p28_cfg_ai_dto_assist_u1 ON p28_cfg_ai_dto_assist (
    tenant_id, pqon
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_cfg_ai_dto_assist.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_cfg_ai_dto_assist.tenant_id IS 'the multitenancy discriminator';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_cfg_ai_dto_assist.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class AiDtoAssistRef
-- comments for columns of java class AiDtoAssistDTO
COMMENT ON COLUMN p28_cfg_ai_dto_assist.pqon IS 'PQON of the DTO to be created / edited';
COMMENT ON COLUMN p28_cfg_ai_dto_assist.description IS 'description / notes';
COMMENT ON COLUMN p28_cfg_ai_dto_assist.ai_assistant_ref IS 'which LLM to use / LLM parameters';
COMMENT ON COLUMN p28_cfg_ai_dto_assist.role IS 'role input for the LLM';
COMMENT ON COLUMN p28_cfg_ai_dto_assist.dto_instructions IS 'DTO specific instructions, will be pasted into the prompt';
COMMENT ON COLUMN p28_cfg_ai_dto_assist.dto_schema IS 'JSON schema for the DTO. If missing, it will be autogenerated';
COMMENT ON COLUMN p28_cfg_ai_dto_assist.z IS 'project specific extensions';





CREATE TABLE IF NOT EXISTS p28_his_ai_dto_assist (
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
    -- table columns of java class AiDtoAssistRef
    -- table columns of java class AiDtoAssistDTO
    , pqon varchar(36) NOT NULL
    , description varchar(80) NOT NULL
    , ai_assistant_ref bigint NOT NULL
    , role varchar(65536) NOT NULL
    , dto_instructions varchar(65536) NOT NULL
    , dto_schema varchar(65536)
    , z text
);

ALTER TABLE p28_his_ai_dto_assist DROP CONSTRAINT IF EXISTS p28_his_ai_dto_assist_pk;

ALTER TABLE p28_his_ai_dto_assist ADD CONSTRAINT p28_his_ai_dto_assist_pk PRIMARY KEY (
    object_ref, history_seq_ref
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_his_ai_dto_assist.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_his_ai_dto_assist.tenant_id IS 'the multitenancy discriminator';
COMMENT ON COLUMN p28_his_ai_dto_assist.history_seq_ref IS 'current sequence number of history entry';
COMMENT ON COLUMN p28_his_ai_dto_assist.history_change_type IS 'type of change (C=create/insert, U=update, D=delete)';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_his_ai_dto_assist.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class AiDtoAssistRef
-- comments for columns of java class AiDtoAssistDTO
COMMENT ON COLUMN p28_his_ai_dto_assist.pqon IS 'PQON of the DTO to be created / edited';
COMMENT ON COLUMN p28_his_ai_dto_assist.description IS 'description / notes';
COMMENT ON COLUMN p28_his_ai_dto_assist.ai_assistant_ref IS 'which LLM to use / LLM parameters';
COMMENT ON COLUMN p28_his_ai_dto_assist.role IS 'role input for the LLM';
COMMENT ON COLUMN p28_his_ai_dto_assist.dto_instructions IS 'DTO specific instructions, will be pasted into the prompt';
COMMENT ON COLUMN p28_his_ai_dto_assist.dto_schema IS 'JSON schema for the DTO. If missing, it will be autogenerated';
COMMENT ON COLUMN p28_his_ai_dto_assist.z IS 'project specific extensions';





-- now the columns are modified
DROP VIEW IF EXISTS p28_cfg_ai_assistant_nt;
DROP VIEW IF EXISTS p28_cfg_ai_assistant_v;

DROP VIEW IF EXISTS p28_dat_ai_chat_log_nt;
DROP VIEW IF EXISTS p28_dat_ai_chat_log_v;

DROP VIEW IF EXISTS p28_cfg_ai_dto_assist_nt;
DROP VIEW IF EXISTS p28_cfg_ai_dto_assist_v;

DROP TRIGGER IF EXISTS p28_cfg_ai_dto_assist_tr ON p28_cfg_ai_dto_assist;

ALTER TABLE p28_cfg_ai_assistant ALTER COLUMN instructions TYPE varchar(4096000);
ALTER TABLE p28_cfg_ai_assistant ALTER COLUMN tts_instructions TYPE varchar(4096000);

ALTER TABLE p28_his_ai_assistant ALTER COLUMN instructions TYPE varchar(4096000);
ALTER TABLE p28_his_ai_assistant ALTER COLUMN tts_instructions TYPE varchar(4096000);

ALTER TABLE p28_dat_ai_chat_log ALTER COLUMN user_input TYPE varchar(4096000);

ALTER TABLE p28_cfg_ai_dto_assist ALTER COLUMN role TYPE varchar(4096000);
ALTER TABLE p28_cfg_ai_dto_assist ALTER COLUMN dto_instructions TYPE varchar(4096000);
ALTER TABLE p28_cfg_ai_dto_assist ALTER COLUMN dto_schema TYPE varchar(4096000);

ALTER TABLE p28_his_ai_dto_assist ALTER COLUMN role TYPE varchar(4096000);
ALTER TABLE p28_his_ai_dto_assist ALTER COLUMN dto_instructions TYPE varchar(4096000);
ALTER TABLE p28_his_ai_dto_assist ALTER COLUMN dto_schema TYPE varchar(4096000);

CREATE OR REPLACE VIEW p28_cfg_ai_assistant_nt AS SELECT
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class AiAssistantRef
    -- columns of java class AiAssistantDTO
    , t0.assistant_id AS assistant_id
    , t0.description AS description
    , t0.is_active AS is_active
    , t0.language_code AS language_code
    , t0.ai_provider AS ai_provider
    , t0.model AS model
    , t0.instructions AS instructions
    , t0.temperature AS temperature
    , t0.top_p AS top_p
    , t0.max_tokens AS max_tokens
    , t0.document_access_permitted AS document_access_permitted
    , t0.tools_permitted AS tools_permitted
    , t0.execute_permitted AS execute_permitted
    , t0.metadata AS metadata
    , AiSyncStatusType2s(t0.sync_status) AS sync_status
    , t0.ai_assistant_id AS ai_assistant_id
    , t0.greeting AS greeting
    , t0.ai_name AS ai_name
    , t0.vector_db_provider AS vector_db_provider
    , t0.tts_provider AS tts_provider
    , t0.tts_model AS tts_model
    , t0.tts_instructions AS tts_instructions
    , t0.z AS z
    , t0.store AS store
    , ReasoningEffort2s(t0.reasoning_effort) AS reasoning_effort
    , ReasoningContext2s(t0.reasoning_context) AS reasoning_context
    , ReasoningMode2s(t0.reasoning_mode) AS reasoning_mode
    , ReasoningSummary2s(t0.reasoning_summary) AS reasoning_summary
FROM p28_cfg_ai_assistant t0;

CREATE OR REPLACE VIEW p28_cfg_ai_assistant_v AS SELECT
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
    -- columns of java class AiAssistantRef
    -- columns of java class AiAssistantDTO
    , t0.assistant_id AS assistant_id
    , t0.description AS description
    , t0.is_active AS is_active
    , t0.language_code AS language_code
    , t0.ai_provider AS ai_provider
    , t0.model AS model
    , t0.instructions AS instructions
    , t0.temperature AS temperature
    , t0.top_p AS top_p
    , t0.max_tokens AS max_tokens
    , t0.document_access_permitted AS document_access_permitted
    , t0.tools_permitted AS tools_permitted
    , t0.execute_permitted AS execute_permitted
    , t0.metadata AS metadata
    , AiSyncStatusType2s(t0.sync_status) AS sync_status
    , t0.ai_assistant_id AS ai_assistant_id
    , t0.greeting AS greeting
    , t0.ai_name AS ai_name
    , t0.vector_db_provider AS vector_db_provider
    , t0.tts_provider AS tts_provider
    , t0.tts_model AS tts_model
    , t0.tts_instructions AS tts_instructions
    , t0.z AS z
    , t0.store AS store
    , ReasoningEffort2s(t0.reasoning_effort) AS reasoning_effort
    , ReasoningContext2s(t0.reasoning_context) AS reasoning_context
    , ReasoningMode2s(t0.reasoning_mode) AS reasoning_mode
    , ReasoningSummary2s(t0.reasoning_summary) AS reasoning_summary
FROM p28_cfg_ai_assistant t0;

CREATE OR REPLACE VIEW p28_dat_ai_chat_log_nt AS SELECT
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class AiChatLogRef
    -- columns of java class AiChatLogDTO
    , t0.conversation_ref AS conversation_ref
    , AiRoleType2s(t0.role_type) AS role_type
    , t0.user_input AS user_input
    , t0.function_pqon AS function_pqon
    , t0.function_parameter AS function_parameter
    , t0.sink_ref AS sink_ref
FROM p28_dat_ai_chat_log t0;

CREATE OR REPLACE VIEW p28_dat_ai_chat_log_v AS SELECT
    -- columns of java class TrackingBase
    -- columns of java class WriteTracking
    t0.c_app_user_id AS c_app_user_id
    , t0.c_timestamp AS c_timestamp
    , t0.c_process_ref AS c_process_ref
    -- columns of java class InternalTenantId
    , t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class AiChatLogRef
    -- columns of java class AiChatLogDTO
    , t0.conversation_ref AS conversation_ref
    , AiRoleType2s(t0.role_type) AS role_type
    , t0.user_input AS user_input
    , t0.function_pqon AS function_pqon
    , t0.function_parameter AS function_parameter
    , t0.sink_ref AS sink_ref
FROM p28_dat_ai_chat_log t0;

CREATE OR REPLACE VIEW p28_cfg_ai_dto_assist_nt AS SELECT
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class AiDtoAssistRef
    -- columns of java class AiDtoAssistDTO
    , t0.pqon AS pqon
    , t0.description AS description
    , t0.ai_assistant_ref AS ai_assistant_ref
    , t0.role AS role
    , t0.dto_instructions AS dto_instructions
    , t0.dto_schema AS dto_schema
    , t0.z AS z
FROM p28_cfg_ai_dto_assist t0;

CREATE OR REPLACE VIEW p28_cfg_ai_dto_assist_v AS SELECT
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
    -- columns of java class AiDtoAssistRef
    -- columns of java class AiDtoAssistDTO
    , t0.pqon AS pqon
    , t0.description AS description
    , t0.ai_assistant_ref AS ai_assistant_ref
    , t0.role AS role
    , t0.dto_instructions AS dto_instructions
    , t0.dto_schema AS dto_schema
    , t0.z AS z
FROM p28_cfg_ai_dto_assist t0;





CREATE OR REPLACE FUNCTION p28_cfg_ai_dto_assist_tp() RETURNS TRIGGER AS $p28_cfg_ai_dto_assist_td$
DECLARE
    next_seq_ BIGINT;
BEGIN
    SELECT NEXTVAL('cm_idgen_5009_seq') INTO next_seq_;
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO p28_his_ai_dto_assist (
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
            , pqon
            , description
            , ai_assistant_ref
            , role
            , dto_instructions
            , dto_schema
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
            , NEW.pqon
            , NEW.description
            , NEW.ai_assistant_ref
            , NEW.role
            , NEW.dto_instructions
            , NEW.dto_schema
            , NEW.z
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'UPDATE') THEN
        -- deny attempts to change a primary key column
        IF OLD.object_ref <> NEW.object_ref THEN
            RAISE EXCEPTION 'Cannot change primary key column to different value';
        END IF;
        INSERT INTO p28_his_ai_dto_assist (
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
            , pqon
            , description
            , ai_assistant_ref
            , role
            , dto_instructions
            , dto_schema
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
            , NEW.pqon
            , NEW.description
            , NEW.ai_assistant_ref
            , NEW.role
            , NEW.dto_instructions
            , NEW.dto_schema
            , NEW.z
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO p28_his_ai_dto_assist (
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
            , pqon
            , description
            , ai_assistant_ref
            , role
            , dto_instructions
            , dto_schema
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
            , OLD.pqon
            , OLD.description
            , OLD.ai_assistant_ref
            , OLD.role
            , OLD.dto_instructions
            , OLD.dto_schema
            , OLD.z
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$p28_cfg_ai_dto_assist_td$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS p28_cfg_ai_dto_assist_tr ON p28_cfg_ai_dto_assist;

CREATE TRIGGER p28_cfg_ai_dto_assist_tr
    AFTER INSERT OR DELETE OR UPDATE ON p28_cfg_ai_dto_assist
    FOR EACH ROW EXECUTE PROCEDURE p28_cfg_ai_dto_assist_tp();
