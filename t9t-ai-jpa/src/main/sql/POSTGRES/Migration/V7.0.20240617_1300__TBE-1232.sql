CREATE SEQUENCE p28_cfg_ai_assistant_s;
CREATE SEQUENCE p28_dat_ai_conversation_s;
CREATE SEQUENCE p28_dat_ai_user_status_s;

CREATE TABLE p28_cfg_ai_assistant (
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

ALTER TABLE p28_cfg_ai_assistant ADD CONSTRAINT p28_cfg_ai_assistant_pk PRIMARY KEY (
    object_ref
);
CREATE UNIQUE INDEX p28_cfg_ai_assistant_u1 ON p28_cfg_ai_assistant (
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
COMMENT ON COLUMN p28_cfg_ai_assistant.vector_db_provider IS 'selects the vector DB implementation';
COMMENT ON COLUMN p28_cfg_ai_assistant.tts_provider IS 'selects the TTS provider (parlor, OpenAI, VoiceCraft, ...)';
COMMENT ON COLUMN p28_cfg_ai_assistant.tts_model IS 'some TTS providers have an enumeration of voices only (OpenAI)';
COMMENT ON COLUMN p28_cfg_ai_assistant.tts_instructions IS 'some TTS providers allow a detailed description of the desired speaker';

CREATE TABLE p28_his_ai_assistant (
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
COMMENT ON COLUMN p28_his_ai_assistant.vector_db_provider IS 'selects the vector DB implementation';
COMMENT ON COLUMN p28_his_ai_assistant.tts_provider IS 'selects the TTS provider (parlor, OpenAI, VoiceCraft, ...)';
COMMENT ON COLUMN p28_his_ai_assistant.tts_model IS 'some TTS providers have an enumeration of voices only (OpenAI)';
COMMENT ON COLUMN p28_his_ai_assistant.tts_instructions IS 'some TTS providers allow a detailed description of the desired speaker';

CREATE TABLE p28_cfg_ai_module_cfg (
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
    -- table columns of java class AiModuleCfgDTO
);

ALTER TABLE p28_cfg_ai_module_cfg ADD CONSTRAINT p28_cfg_ai_module_cfg_pk PRIMARY KEY (
    tenant_id
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_cfg_ai_module_cfg.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_cfg_ai_module_cfg.tenant_id IS 'the multitenancy discriminator';
-- comments for columns of java class CompositeKeyBase
-- comments for columns of java class ModuleConfigDTO
COMMENT ON COLUMN p28_cfg_ai_module_cfg.z IS 'custom parameters: exist for all modules by default';
-- comments for columns of java class AiModuleCfgDTO

CREATE TABLE p28_his_ai_module_cfg (
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
    -- table columns of java class AiModuleCfgDTO
);

ALTER TABLE p28_his_ai_module_cfg ADD CONSTRAINT p28_his_ai_module_cfg_pk PRIMARY KEY (
    tenant_id, history_seq_ref
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_his_ai_module_cfg.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_his_ai_module_cfg.tenant_id IS 'the multitenancy discriminator';
COMMENT ON COLUMN p28_his_ai_module_cfg.history_seq_ref IS 'current sequence number of history entry';
COMMENT ON COLUMN p28_his_ai_module_cfg.history_change_type IS 'type of change (C=create/insert, U=update, D=delete)';
-- comments for columns of java class CompositeKeyBase
-- comments for columns of java class ModuleConfigDTO
COMMENT ON COLUMN p28_his_ai_module_cfg.z IS 'custom parameters: exist for all modules by default';
-- comments for columns of java class AiModuleCfgDTO

CREATE TABLE p28_dat_ai_conversation (
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
    -- table columns of java class AiConversationRef
    -- table columns of java class AiConversationDTO
    , user_id varchar(16) NOT NULL
    , ai_assistant_ref bigint NOT NULL
    , created_by_session_ref bigint NOT NULL
    , provider_thread_id varchar(64)
    , number_of_messages integer NOT NULL
    , number_of_files_added integer NOT NULL
    , file_references text
    , business_id varchar(36)
    , business_ref bigint
    , z text
);

ALTER TABLE p28_dat_ai_conversation ADD CONSTRAINT p28_dat_ai_conversation_pk PRIMARY KEY (
    object_ref
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_dat_ai_conversation.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_dat_ai_conversation.tenant_id IS 'the multitenancy discriminator';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_dat_ai_conversation.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class AiConversationRef
-- comments for columns of java class AiConversationDTO
COMMENT ON COLUMN p28_dat_ai_conversation.user_id IS 'the application user who runs the conversation';
COMMENT ON COLUMN p28_dat_ai_conversation.ai_assistant_ref IS 'the assistant used';
COMMENT ON COLUMN p28_dat_ai_conversation.created_by_session_ref IS 'allow to auto-switch threads after a relog';
COMMENT ON COLUMN p28_dat_ai_conversation.provider_thread_id IS 'the conversation ID (threadId) of the provider';
COMMENT ON COLUMN p28_dat_ai_conversation.number_of_messages IS 'how many messages have been done in this conversation';
COMMENT ON COLUMN p28_dat_ai_conversation.number_of_files_added IS 'how many files have been uploaded in this conversation';
COMMENT ON COLUMN p28_dat_ai_conversation.file_references IS 'maps file names to provider specific references';
COMMENT ON COLUMN p28_dat_ai_conversation.business_id IS 'for example a customer ID, once identified';
COMMENT ON COLUMN p28_dat_ai_conversation.business_ref IS 'for example an order ref';
COMMENT ON COLUMN p28_dat_ai_conversation.z IS 'project specific extensions';

CREATE TABLE p28_dat_ai_user_status (
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
    -- table columns of java class AiUserStatusRef
    -- table columns of java class AiUserStatusDTO
    , user_id varchar(16) NOT NULL
    , preferred_assistant_ref bigint
    , current_conversation_ref bigint
);

ALTER TABLE p28_dat_ai_user_status ADD CONSTRAINT p28_dat_ai_user_status_pk PRIMARY KEY (
    object_ref
);
CREATE UNIQUE INDEX p28_dat_ai_user_status_u1 ON p28_dat_ai_user_status (
    tenant_id, user_id
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_dat_ai_user_status.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_dat_ai_user_status.tenant_id IS 'the multitenancy discriminator';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_dat_ai_user_status.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class AiUserStatusRef
-- comments for columns of java class AiUserStatusDTO

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

CREATE OR REPLACE FUNCTION p28_cfg_ai_module_cfg_tp() RETURNS TRIGGER AS $p28_cfg_ai_module_cfg_td$
DECLARE
    next_seq_ BIGINT;
BEGIN
    SELECT NEXTVAL('cm_idgen_5009_seq') INTO next_seq_;
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO p28_his_ai_module_cfg (
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
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'UPDATE') THEN
        -- deny attempts to change a primary key column
        IF OLD.tenant_id <> NEW.tenant_id THEN
            RAISE EXCEPTION 'Cannot change primary key column to different value';
        END IF;
        INSERT INTO p28_his_ai_module_cfg (
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
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO p28_his_ai_module_cfg (
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
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$p28_cfg_ai_module_cfg_td$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS p28_cfg_ai_module_cfg_tr ON p28_cfg_ai_module_cfg;

CREATE TRIGGER p28_cfg_ai_module_cfg_tr
    AFTER INSERT OR DELETE OR UPDATE ON p28_cfg_ai_module_cfg
    FOR EACH ROW EXECUTE PROCEDURE p28_cfg_ai_module_cfg_tp();

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
FROM p28_cfg_ai_assistant t0;

CREATE OR REPLACE VIEW p28_cfg_ai_module_cfg_nt AS SELECT
    -- columns of java class CompositeKeyBase
    -- columns of java class ModuleConfigDTO
    t0.z AS z
    -- columns of java class AiModuleCfgDTO
FROM p28_cfg_ai_module_cfg t0;

CREATE OR REPLACE VIEW p28_cfg_ai_module_cfg_v AS SELECT
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
    -- columns of java class AiModuleCfgDTO
FROM p28_cfg_ai_module_cfg t0;

CREATE OR REPLACE VIEW p28_dat_ai_conversation_nt AS SELECT
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class AiConversationRef
    -- columns of java class AiConversationDTO
    , t0.user_id AS user_id
    , t0.ai_assistant_ref AS ai_assistant_ref
    , t0.created_by_session_ref AS created_by_session_ref
    , t0.provider_thread_id AS provider_thread_id
    , t0.number_of_messages AS number_of_messages
    , t0.number_of_files_added AS number_of_files_added
    , t0.file_references AS file_references
    , t0.business_id AS business_id
    , t0.business_ref AS business_ref
    , t0.z AS z
FROM p28_dat_ai_conversation t0;

CREATE OR REPLACE VIEW p28_dat_ai_conversation_v AS SELECT
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
    -- columns of java class AiConversationRef
    -- columns of java class AiConversationDTO
    , t0.user_id AS user_id
    , t0.ai_assistant_ref AS ai_assistant_ref
    , t0.created_by_session_ref AS created_by_session_ref
    , t0.provider_thread_id AS provider_thread_id
    , t0.number_of_messages AS number_of_messages
    , t0.number_of_files_added AS number_of_files_added
    , t0.file_references AS file_references
    , t0.business_id AS business_id
    , t0.business_ref AS business_ref
    , t0.z AS z
FROM p28_dat_ai_conversation t0;

CREATE OR REPLACE VIEW p28_dat_ai_user_status_nt AS SELECT
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class AiUserStatusRef
    -- columns of java class AiUserStatusDTO
    , t0.user_id AS user_id
    , t0.preferred_assistant_ref AS preferred_assistant_ref
    , t0.current_conversation_ref AS current_conversation_ref
FROM p28_dat_ai_user_status t0;

CREATE OR REPLACE VIEW p28_dat_ai_user_status_v AS SELECT
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
    -- columns of java class AiUserStatusRef
    -- columns of java class AiUserStatusDTO
    , t0.user_id AS user_id
    , t0.preferred_assistant_ref AS preferred_assistant_ref
    , t0.current_conversation_ref AS current_conversation_ref
FROM p28_dat_ai_user_status t0;
