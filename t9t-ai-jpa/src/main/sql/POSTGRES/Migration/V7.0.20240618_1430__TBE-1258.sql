CREATE SEQUENCE p28_dat_ai_chat_log_s;

CREATE TABLE p28_dat_ai_chat_log (
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
    , user_input varchar(65536)
    , function_pqon varchar(255)
    , function_parameter text
);

ALTER TABLE p28_dat_ai_chat_log ADD CONSTRAINT p28_dat_ai_chat_log_pk PRIMARY KEY (
    object_ref
);
CREATE INDEX p28_dat_ai_chat_log_i1 ON p28_dat_ai_chat_log (
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
FROM p28_dat_ai_chat_log t0;
