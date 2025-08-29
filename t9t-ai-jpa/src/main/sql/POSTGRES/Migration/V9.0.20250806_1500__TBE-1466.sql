CREATE SEQUENCE p28_dat_ai_prompt_s;

CREATE TABLE p28_dat_ai_prompt (
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
    -- table columns of java class AiPromptRef
    -- table columns of java class AiPromptDTO
    , prompt_id varchar(36) NOT NULL
    , is_active boolean NOT NULL
    , title varchar(80) NOT NULL
    , description varchar(4000) NOT NULL
    , prompt varchar(64000) NOT NULL
    , parameters bytea NOT NULL
);

ALTER TABLE p28_dat_ai_prompt ADD CONSTRAINT p28_dat_ai_prompt_pk PRIMARY KEY (
    object_ref
);
CREATE UNIQUE INDEX p28_dat_ai_prompt_u1 ON p28_dat_ai_prompt (
    tenant_id, prompt_id
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class WriteTracking
COMMENT ON COLUMN p28_dat_ai_prompt.c_timestamp IS 'noinsert removed, causes problems with H2 unit tests';
-- comments for columns of java class FullTracking
-- comments for columns of java class FullTrackingWithVersion
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_dat_ai_prompt.tenant_id IS 'the multitenancy discriminator';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_dat_ai_prompt.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class AiPromptRef
-- comments for columns of java class AiPromptDTO
COMMENT ON COLUMN p28_dat_ai_prompt.prompt_id IS 'the MCP name';
COMMENT ON COLUMN p28_dat_ai_prompt.is_active IS 'set inactive to hide this prompt';
COMMENT ON COLUMN p28_dat_ai_prompt.title IS 'the title sent to the LLM';
COMMENT ON COLUMN p28_dat_ai_prompt.description IS 'a slightly longer description of the prompt';
COMMENT ON COLUMN p28_dat_ai_prompt.prompt IS 'the prompt itself, with parameters';
COMMENT ON COLUMN p28_dat_ai_prompt.parameters IS 'parameters to the prompt';

CREATE OR REPLACE VIEW p28_dat_ai_prompt_nt AS SELECT
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class AiPromptRef
    -- columns of java class AiPromptDTO
    , t0.prompt_id AS prompt_id
    , t0.is_active AS is_active
    , t0.title AS title
    , t0.description AS description
    , t0.prompt AS prompt
    , t0.parameters AS parameters
FROM p28_dat_ai_prompt t0;

CREATE OR REPLACE VIEW p28_dat_ai_prompt_v AS SELECT
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
    -- columns of java class AiPromptRef
    -- columns of java class AiPromptDTO
    , t0.prompt_id AS prompt_id
    , t0.is_active AS is_active
    , t0.title AS title
    , t0.description AS description
    , t0.prompt AS prompt
    , t0.parameters AS parameters
FROM p28_dat_ai_prompt t0;
