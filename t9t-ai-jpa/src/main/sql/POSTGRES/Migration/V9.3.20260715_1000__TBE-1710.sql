-- SQL migration for TBE-1710: Add reasoning configuration to AI Assistant

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
    , t0.sync_status AS sync_status
    , t0.ai_assistant_id AS ai_assistant_id
    , t0.greeting AS greeting
    , t0.ai_name AS ai_name
    , t0.vector_db_provider AS vector_db_provider
    , t0.tts_provider AS tts_provider
    , t0.tts_model AS tts_model
    , t0.tts_instructions AS tts_instructions
    , t0.z AS z
    , t0.store AS store
    , t0.reasoning_effort AS reasoning_effort
    , t0.reasoning_context AS reasoning_context
    , t0.reasoning_mode AS reasoning_mode
    , t0.reasoning_summary AS reasoning_summary
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
    , t0.sync_status AS sync_status
    , t0.ai_assistant_id AS ai_assistant_id
    , t0.greeting AS greeting
    , t0.ai_name AS ai_name
    , t0.vector_db_provider AS vector_db_provider
    , t0.tts_provider AS tts_provider
    , t0.tts_model AS tts_model
    , t0.tts_instructions AS tts_instructions
    , t0.z AS z
    , t0.store AS store
    , t0.reasoning_effort AS reasoning_effort
    , t0.reasoning_context AS reasoning_context
    , t0.reasoning_mode AS reasoning_mode
    , t0.reasoning_summary AS reasoning_summary
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
