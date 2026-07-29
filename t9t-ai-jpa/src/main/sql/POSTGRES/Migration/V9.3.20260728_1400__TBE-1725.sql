-- SQL migration for TBE-1725: Increase textChunk column size from varchar(65536) to varchar(4096000)

DROP VIEW IF EXISTS p28_cfg_ai_assistant_nt;
DROP VIEW IF EXISTS p28_cfg_ai_assistant_v;

DROP VIEW IF EXISTS p28_dat_ai_chat_log_nt;
DROP VIEW IF EXISTS p28_dat_ai_chat_log_v;

DROP VIEW IF EXISTS p28_cfg_ai_dto_assist_nt;
DROP VIEW IF EXISTS p28_cfg_ai_dto_assist_v;

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
