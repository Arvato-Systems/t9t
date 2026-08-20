-- SQL migration for TBE-1710: Add reasoning configuration to AI Assistant

-- first add table/constraint/comments that were not created in earlier SQL migration
CREATE SEQUENCE IF NOT EXISTS p28_cfg_ai_assistant_s;

DROP TABLE IF EXISTS p28_cfg_ai_assistant CASCADE;
DROP TABLE IF EXISTS p28_his_ai_assistant CASCADE;

CREATE TABLE IF NOT EXISTS p28_cfg_ai_assistant (
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
    -- table columns of java class AiAssistantRef
    -- table columns of java class AiAssistantDTO
    , assistant_id varchar(36) NOT NULL
    , description varchar(80) NOT NULL
    , is_active boolean NOT NULL
    , language_code varchar(5) NOT NULL
    , ai_provider varchar(32) NOT NULL
    , model varchar(64) NOT NULL
    , instructions varchar(65536) NOT NULL
    , temperature real
    , top_p real
    , max_tokens integer
    , document_access_permitted boolean NOT NULL
    , tools_permitted boolean NOT NULL
    , execute_permitted boolean NOT NULL
    , metadata text
    , sync_status varchar(3)
    , ai_assistant_id varchar(64)
    , greeting varchar(80) NOT NULL
    , ai_name varchar(80)
    , vector_db_provider varchar(32)
    , tts_provider varchar(32)
    , tts_model varchar(64)
    , tts_instructions varchar(65536)
    , z text
);

ALTER TABLE p28_cfg_ai_assistant DROP CONSTRAINT IF EXISTS p28_cfg_ai_assistant_pk;

ALTER TABLE p28_cfg_ai_assistant ADD CONSTRAINT p28_cfg_ai_assistant_pk PRIMARY KEY (
    object_ref
);
CREATE UNIQUE INDEX IF NOT EXISTS p28_cfg_ai_assistant_u1 ON p28_cfg_ai_assistant (
    tenant_id, assistant_id
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_cfg_ai_assistant.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_cfg_ai_assistant.tenant_id IS 'the multitenancy discriminator';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_cfg_ai_assistant.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class AiAssistantRef
-- comments for columns of java class AiAssistantDTO
COMMENT ON COLUMN p28_cfg_ai_assistant.description IS 'the name / description of the assistant';
COMMENT ON COLUMN p28_cfg_ai_assistant.language_code IS 'the language to use';
COMMENT ON COLUMN p28_cfg_ai_assistant.ai_provider IS 'selects the chat service implementation';
COMMENT ON COLUMN p28_cfg_ai_assistant.model IS 'the model to use (the AI provider''s ID)';
COMMENT ON COLUMN p28_cfg_ai_assistant.instructions IS 'the model''s instructions';
COMMENT ON COLUMN p28_cfg_ai_assistant.temperature IS 'temperature for the chat model';
COMMENT ON COLUMN p28_cfg_ai_assistant.top_p IS 'top-P selection';
COMMENT ON COLUMN p28_cfg_ai_assistant.max_tokens IS 'if set, limits the cost of the response';
COMMENT ON COLUMN p28_cfg_ai_assistant.document_access_permitted IS 'if the assistant has access to documents for simple RAG';
COMMENT ON COLUMN p28_cfg_ai_assistant.tools_permitted IS 'if tool access (t9t procedures) is allowed at all (detailed permissions handled elsewhere)';
COMMENT ON COLUMN p28_cfg_ai_assistant.execute_permitted IS 'if the assistant is allowed to run code in a sandbox (OpenAI specific, expensive!)';
COMMENT ON COLUMN p28_cfg_ai_assistant.metadata IS 'assistant metadata / parameters';
COMMENT ON COLUMN p28_cfg_ai_assistant.sync_status IS 'specifies if the assistant has been created at the provider';
COMMENT ON COLUMN p28_cfg_ai_assistant.ai_assistant_id IS 'the ID in the provider''s namespace';
COMMENT ON COLUMN p28_cfg_ai_assistant.greeting IS 'the initial greeting of the assistant';
COMMENT ON COLUMN p28_cfg_ai_assistant.ai_name IS 'the fictional name of the AI (instead of just default "AI chat")';
COMMENT ON COLUMN p28_cfg_ai_assistant.vector_db_provider IS 'selects the vector DB implementation (no longer supported)';
COMMENT ON COLUMN p28_cfg_ai_assistant.tts_provider IS 'selects the TTS provider (parlor, OpenAI, VoiceCraft, ...)';
COMMENT ON COLUMN p28_cfg_ai_assistant.tts_model IS 'some TTS providers have an enumeration of voices only (OpenAI)';
COMMENT ON COLUMN p28_cfg_ai_assistant.tts_instructions IS 'some TTS providers allow a detailed description of the desired speaker';





CREATE TABLE IF NOT EXISTS p28_his_ai_assistant (
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
    -- table columns of java class AiAssistantRef
    -- table columns of java class AiAssistantDTO
    , assistant_id varchar(36) NOT NULL
    , description varchar(80) NOT NULL
    , is_active boolean NOT NULL
    , language_code varchar(5) NOT NULL
    , ai_provider varchar(32) NOT NULL
    , model varchar(64) NOT NULL
    , instructions varchar(65536) NOT NULL
    , temperature real
    , top_p real
    , max_tokens integer
    , document_access_permitted boolean NOT NULL
    , tools_permitted boolean NOT NULL
    , execute_permitted boolean NOT NULL
    , metadata text
    , sync_status varchar(3)
    , ai_assistant_id varchar(64)
    , greeting varchar(80) NOT NULL
    , ai_name varchar(80)
    , vector_db_provider varchar(32)
    , tts_provider varchar(32)
    , tts_model varchar(64)
    , tts_instructions varchar(65536)
    , z text
);

ALTER TABLE p28_his_ai_assistant ADD CONSTRAINT p28_his_ai_assistant_pk PRIMARY KEY (
    object_ref, history_seq_ref
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_his_ai_assistant.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_his_ai_assistant.tenant_id IS 'the multitenancy discriminator';
COMMENT ON COLUMN p28_his_ai_assistant.history_seq_ref IS 'current sequence number of history entry';
COMMENT ON COLUMN p28_his_ai_assistant.history_change_type IS 'type of change (C=create/insert, U=update, D=delete)';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_his_ai_assistant.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class AiAssistantRef
-- comments for columns of java class AiAssistantDTO
COMMENT ON COLUMN p28_his_ai_assistant.description IS 'the name / description of the assistant';
COMMENT ON COLUMN p28_his_ai_assistant.language_code IS 'the language to use';
COMMENT ON COLUMN p28_his_ai_assistant.ai_provider IS 'selects the chat service implementation';
COMMENT ON COLUMN p28_his_ai_assistant.model IS 'the model to use (the AI provider''s ID)';
COMMENT ON COLUMN p28_his_ai_assistant.instructions IS 'the model''s instructions';
COMMENT ON COLUMN p28_his_ai_assistant.temperature IS 'temperature for the chat model';
COMMENT ON COLUMN p28_his_ai_assistant.top_p IS 'top-P selection';
COMMENT ON COLUMN p28_his_ai_assistant.max_tokens IS 'if set, limits the cost of the response';
COMMENT ON COLUMN p28_his_ai_assistant.document_access_permitted IS 'if the assistant has access to documents for simple RAG';
COMMENT ON COLUMN p28_his_ai_assistant.tools_permitted IS 'if tool access (t9t procedures) is allowed at all (detailed permissions handled elsewhere)';
COMMENT ON COLUMN p28_his_ai_assistant.execute_permitted IS 'if the assistant is allowed to run code in a sandbox (OpenAI specific, expensive!)';
COMMENT ON COLUMN p28_his_ai_assistant.metadata IS 'assistant metadata / parameters';
COMMENT ON COLUMN p28_his_ai_assistant.sync_status IS 'specifies if the assistant has been created at the provider';
COMMENT ON COLUMN p28_his_ai_assistant.ai_assistant_id IS 'the ID in the provider''s namespace';
COMMENT ON COLUMN p28_his_ai_assistant.greeting IS 'the initial greeting of the assistant';
COMMENT ON COLUMN p28_his_ai_assistant.ai_name IS 'the fictional name of the AI (instead of just default "AI chat")';
COMMENT ON COLUMN p28_his_ai_assistant.vector_db_provider IS 'selects the vector DB implementation (no longer supported)';
COMMENT ON COLUMN p28_his_ai_assistant.tts_provider IS 'selects the TTS provider (parlor, OpenAI, VoiceCraft, ...)';
COMMENT ON COLUMN p28_his_ai_assistant.tts_model IS 'some TTS providers have an enumeration of voices only (OpenAI)';
COMMENT ON COLUMN p28_his_ai_assistant.tts_instructions IS 'some TTS providers allow a detailed description of the desired speaker';





-- now the new columns are added
DROP VIEW IF EXISTS p28_cfg_ai_assistant_nt;
DROP VIEW IF EXISTS p28_cfg_ai_assistant_v;

ALTER TABLE p28_cfg_ai_assistant
    ADD COLUMN IF NOT EXISTS store boolean
    , ADD COLUMN IF NOT EXISTS reasoning_effort smallint
    , ADD COLUMN IF NOT EXISTS reasoning_context smallint
    , ADD COLUMN IF NOT EXISTS reasoning_mode smallint
    , ADD COLUMN IF NOT EXISTS reasoning_summary smallint;

COMMENT ON COLUMN p28_cfg_ai_assistant.store IS 'OpenAI Responses API: store response for subsequent reference';
COMMENT ON COLUMN p28_cfg_ai_assistant.reasoning_effort IS 'strength of reasoning';
COMMENT ON COLUMN p28_cfg_ai_assistant.reasoning_context IS '"auto" or "current_turn" or "all_turns" for OpenAI';
COMMENT ON COLUMN p28_cfg_ai_assistant.reasoning_mode IS '"standard" or "pro" for OpenAI';
COMMENT ON COLUMN p28_cfg_ai_assistant.reasoning_summary IS '"auto" or "concise" or "detailed" for OpenAI';

ALTER TABLE p28_his_ai_assistant
    ADD COLUMN IF NOT EXISTS store boolean
    , ADD COLUMN IF NOT EXISTS reasoning_effort smallint
    , ADD COLUMN IF NOT EXISTS reasoning_context smallint
    , ADD COLUMN IF NOT EXISTS reasoning_mode smallint
    , ADD COLUMN IF NOT EXISTS reasoning_summary smallint;





-- convert a token (as stored in DB tables) of enum t9t.ai.AiRoleType into the more readable symbolic constant string
CREATE OR REPLACE FUNCTION AiRoleType2s(token VARCHAR) RETURNS VARCHAR
    IMMUTABLE STRICT
    AS $$
DECLARE
BEGIN
    IF token = 'U' THEN
        RETURN 'USER';
    END IF;
    IF token = 'S' THEN
        RETURN 'SYSTEM';
    END IF;
    RETURN '~';  -- token for undefined mapping
END;
$$ LANGUAGE plpgsql;

-- convert a constant string of enum t9t.ai.AiRoleType into the token used for DB table storage
CREATE OR REPLACE FUNCTION AiRoleType2t(token VARCHAR) RETURNS VARCHAR
    IMMUTABLE STRICT
    AS $$
DECLARE
BEGIN
    IF token = 'USER' THEN
        RETURN 'U';
    END IF;
    IF token = 'SYSTEM' THEN
        RETURN 'S';
    END IF;
    RETURN '~';  -- token for undefined mapping
END;
$$ LANGUAGE plpgsql;





-- convert a token (as stored in DB tables) of enum t9t.ai.AiSyncStatusType into the more readable symbolic constant string
CREATE OR REPLACE FUNCTION AiSyncStatusType2s(token VARCHAR) RETURNS VARCHAR
    IMMUTABLE STRICT
    AS $$
DECLARE
BEGIN
    IF token = 'OK' THEN
        RETURN 'PROCESSED';
    END IF;
    IF token = 'EXP' THEN
        RETURN 'TO_BE_UPDATED';
    END IF;
    IF token = 'ERR' THEN
        RETURN 'ERROR';
    END IF;
    RETURN '~';  -- token for undefined mapping
END;
$$ LANGUAGE plpgsql;

-- convert a constant string of enum t9t.ai.AiSyncStatusType into the token used for DB table storage
CREATE OR REPLACE FUNCTION AiSyncStatusType2t(token VARCHAR) RETURNS VARCHAR
    IMMUTABLE STRICT
    AS $$
DECLARE
BEGIN
    IF token = 'PROCESSED' THEN
        RETURN 'OK';
    END IF;
    IF token = 'TO_BE_UPDATED' THEN
        RETURN 'EXP';
    END IF;
    IF token = 'ERROR' THEN
        RETURN 'ERR';
    END IF;
    RETURN '~';  -- token for undefined mapping
END;
$$ LANGUAGE plpgsql;





-- convert a token (as stored in DB tables) of enum t9t.ai.ReasoningContext into the more readable symbolic constant string
CREATE OR REPLACE FUNCTION ReasoningContext2s(token INTEGER) RETURNS VARCHAR
    IMMUTABLE STRICT
    AS $$
DECLARE
BEGIN
    IF token = 0 THEN
        RETURN 'AUTO';
    END IF;
    IF token = 1 THEN
        RETURN 'CURRENT_TURN';
    END IF;
    IF token = 2 THEN
        RETURN 'ALL_TURNS';
    END IF;
    RETURN '~';  -- token for undefined mapping
END;
$$ LANGUAGE plpgsql;

-- convert a constant string of enum t9t.ai.ReasoningContext into the token used for DB table storage (which matches the Java enum ordinal())
CREATE OR REPLACE FUNCTION ReasoningContext2t(token VARCHAR) RETURNS INTEGER
    IMMUTABLE STRICT
    AS $$
DECLARE
BEGIN
    IF token = 'AUTO' THEN
        RETURN 0;
    END IF;
    IF token = 'CURRENT_TURN' THEN
        RETURN 1;
    END IF;
    IF token = 'ALL_TURNS' THEN
        RETURN 2;
    END IF;
    RETURN -1;  -- token for undefined mapping
END;
$$ LANGUAGE plpgsql;





-- convert a token (as stored in DB tables) of enum t9t.ai.ReasoningEffort into the more readable symbolic constant string
CREATE OR REPLACE FUNCTION ReasoningEffort2s(token INTEGER) RETURNS VARCHAR
    IMMUTABLE STRICT
    AS $$
DECLARE
BEGIN
    IF token = 0 THEN
        RETURN 'NONE';
    END IF;
    IF token = 1 THEN
        RETURN 'MINIMAL';
    END IF;
    IF token = 2 THEN
        RETURN 'LOW';
    END IF;
    IF token = 3 THEN
        RETURN 'MEDIUM';
    END IF;
    IF token = 4 THEN
        RETURN 'HIGH';
    END IF;
    IF token = 5 THEN
        RETURN 'XHIGH';
    END IF;
    IF token = 6 THEN
        RETURN 'MAX';
    END IF;
    RETURN '~';  -- token for undefined mapping
END;
$$ LANGUAGE plpgsql;

-- convert a constant string of enum t9t.ai.ReasoningEffort into the token used for DB table storage (which matches the Java enum ordinal())
CREATE OR REPLACE FUNCTION ReasoningEffort2t(token VARCHAR) RETURNS INTEGER
    IMMUTABLE STRICT
    AS $$
DECLARE
BEGIN
    IF token = 'NONE' THEN
        RETURN 0;
    END IF;
    IF token = 'MINIMAL' THEN
        RETURN 1;
    END IF;
    IF token = 'LOW' THEN
        RETURN 2;
    END IF;
    IF token = 'MEDIUM' THEN
        RETURN 3;
    END IF;
    IF token = 'HIGH' THEN
        RETURN 4;
    END IF;
    IF token = 'XHIGH' THEN
        RETURN 5;
    END IF;
    IF token = 'MAX' THEN
        RETURN 6;
    END IF;
    RETURN -1;  -- token for undefined mapping
END;
$$ LANGUAGE plpgsql;





-- convert a token (as stored in DB tables) of enum t9t.ai.ReasoningMode into the more readable symbolic constant string
CREATE OR REPLACE FUNCTION ReasoningMode2s(token INTEGER) RETURNS VARCHAR
    IMMUTABLE STRICT
    AS $$
DECLARE
BEGIN
    IF token = 0 THEN
        RETURN 'STANDARD';
    END IF;
    IF token = 1 THEN
        RETURN 'PRO';
    END IF;
    RETURN '~';  -- token for undefined mapping
END;
$$ LANGUAGE plpgsql;

-- convert a constant string of enum t9t.ai.ReasoningMode into the token used for DB table storage (which matches the Java enum ordinal())
CREATE OR REPLACE FUNCTION ReasoningMode2t(token VARCHAR) RETURNS INTEGER
    IMMUTABLE STRICT
    AS $$
DECLARE
BEGIN
    IF token = 'STANDARD' THEN
        RETURN 0;
    END IF;
    IF token = 'PRO' THEN
        RETURN 1;
    END IF;
    RETURN -1;  -- token for undefined mapping
END;
$$ LANGUAGE plpgsql;





-- convert a token (as stored in DB tables) of enum t9t.ai.ReasoningSummary into the more readable symbolic constant string
CREATE OR REPLACE FUNCTION ReasoningSummary2s(token INTEGER) RETURNS VARCHAR
    IMMUTABLE STRICT
    AS $$
DECLARE
BEGIN
    IF token = 0 THEN
        RETURN 'AUTO';
    END IF;
    IF token = 1 THEN
        RETURN 'CONCISE';
    END IF;
    IF token = 2 THEN
        RETURN 'DETAILED';
    END IF;
    RETURN '~';  -- token for undefined mapping
END;
$$ LANGUAGE plpgsql;

-- convert a constant string of enum t9t.ai.ReasoningSummary into the token used for DB table storage (which matches the Java enum ordinal())
CREATE OR REPLACE FUNCTION ReasoningSummary2t(token VARCHAR) RETURNS INTEGER
    IMMUTABLE STRICT
    AS $$
DECLARE
BEGIN
    IF token = 'AUTO' THEN
        RETURN 0;
    END IF;
    IF token = 'CONCISE' THEN
        RETURN 1;
    END IF;
    IF token = 'DETAILED' THEN
        RETURN 2;
    END IF;
    RETURN -1;  -- token for undefined mapping
END;
$$ LANGUAGE plpgsql;





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





CREATE OR REPLACE FUNCTION p28_cfg_ai_assistant_tp() RETURNS TRIGGER AS $p28_cfg_ai_assistant_td$
DECLARE
    next_seq_ BIGINT;
BEGIN
    SELECT NEXTVAL('cm_idgen_5009_seq') INTO next_seq_;
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO p28_his_ai_assistant (
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
            , assistant_id
            , description
            , is_active
            , language_code
            , ai_provider
            , model
            , instructions
            , temperature
            , top_p
            , max_tokens
            , document_access_permitted
            , tools_permitted
            , execute_permitted
            , metadata
            , sync_status
            , ai_assistant_id
            , greeting
            , ai_name
            , vector_db_provider
            , tts_provider
            , tts_model
            , tts_instructions
            , z
            , store
            , reasoning_effort
            , reasoning_context
            , reasoning_mode
            , reasoning_summary
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
            , NEW.assistant_id
            , NEW.description
            , NEW.is_active
            , NEW.language_code
            , NEW.ai_provider
            , NEW.model
            , NEW.instructions
            , NEW.temperature
            , NEW.top_p
            , NEW.max_tokens
            , NEW.document_access_permitted
            , NEW.tools_permitted
            , NEW.execute_permitted
            , NEW.metadata
            , NEW.sync_status
            , NEW.ai_assistant_id
            , NEW.greeting
            , NEW.ai_name
            , NEW.vector_db_provider
            , NEW.tts_provider
            , NEW.tts_model
            , NEW.tts_instructions
            , NEW.z
            , NEW.store
            , NEW.reasoning_effort
            , NEW.reasoning_context
            , NEW.reasoning_mode
            , NEW.reasoning_summary
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'UPDATE') THEN
        -- deny attempts to change a primary key column
        IF OLD.object_ref <> NEW.object_ref THEN
            RAISE EXCEPTION 'Cannot change primary key column to different value';
        END IF;
        INSERT INTO p28_his_ai_assistant (
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
            , assistant_id
            , description
            , is_active
            , language_code
            , ai_provider
            , model
            , instructions
            , temperature
            , top_p
            , max_tokens
            , document_access_permitted
            , tools_permitted
            , execute_permitted
            , metadata
            , sync_status
            , ai_assistant_id
            , greeting
            , ai_name
            , vector_db_provider
            , tts_provider
            , tts_model
            , tts_instructions
            , z
            , store
            , reasoning_effort
            , reasoning_context
            , reasoning_mode
            , reasoning_summary
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
            , NEW.assistant_id
            , NEW.description
            , NEW.is_active
            , NEW.language_code
            , NEW.ai_provider
            , NEW.model
            , NEW.instructions
            , NEW.temperature
            , NEW.top_p
            , NEW.max_tokens
            , NEW.document_access_permitted
            , NEW.tools_permitted
            , NEW.execute_permitted
            , NEW.metadata
            , NEW.sync_status
            , NEW.ai_assistant_id
            , NEW.greeting
            , NEW.ai_name
            , NEW.vector_db_provider
            , NEW.tts_provider
            , NEW.tts_model
            , NEW.tts_instructions
            , NEW.z
            , NEW.store
            , NEW.reasoning_effort
            , NEW.reasoning_context
            , NEW.reasoning_mode
            , NEW.reasoning_summary
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO p28_his_ai_assistant (
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
            , assistant_id
            , description
            , is_active
            , language_code
            , ai_provider
            , model
            , instructions
            , temperature
            , top_p
            , max_tokens
            , document_access_permitted
            , tools_permitted
            , execute_permitted
            , metadata
            , sync_status
            , ai_assistant_id
            , greeting
            , ai_name
            , vector_db_provider
            , tts_provider
            , tts_model
            , tts_instructions
            , z
            , store
            , reasoning_effort
            , reasoning_context
            , reasoning_mode
            , reasoning_summary
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
            , OLD.assistant_id
            , OLD.description
            , OLD.is_active
            , OLD.language_code
            , OLD.ai_provider
            , OLD.model
            , OLD.instructions
            , OLD.temperature
            , OLD.top_p
            , OLD.max_tokens
            , OLD.document_access_permitted
            , OLD.tools_permitted
            , OLD.execute_permitted
            , OLD.metadata
            , OLD.sync_status
            , OLD.ai_assistant_id
            , OLD.greeting
            , OLD.ai_name
            , OLD.vector_db_provider
            , OLD.tts_provider
            , OLD.tts_model
            , OLD.tts_instructions
            , OLD.z
            , OLD.store
            , OLD.reasoning_effort
            , OLD.reasoning_context
            , OLD.reasoning_mode
            , OLD.reasoning_summary
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$p28_cfg_ai_assistant_td$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS p28_cfg_ai_assistant_tr ON p28_cfg_ai_assistant;

CREATE TRIGGER p28_cfg_ai_assistant_tr
    AFTER INSERT OR DELETE OR UPDATE ON p28_cfg_ai_assistant
    FOR EACH ROW EXECUTE PROCEDURE p28_cfg_ai_assistant_tp();
