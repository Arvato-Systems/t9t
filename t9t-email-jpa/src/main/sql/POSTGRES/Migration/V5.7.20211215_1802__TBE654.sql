-- TBE-654: - Update email column size (POSTGRES)

-- Step 1: Drop views
DROP VIEW p28_dat_email_nt;
DROP VIEW p28_dat_email_v;

-- Step 2: Alter column type
ALTER TABLE p28_dat_email ALTER COLUMN email_to TYPE varchar(32767);
ALTER TABLE p28_dat_email ALTER COLUMN email_cc TYPE varchar(32767);
ALTER TABLE p28_dat_email ALTER COLUMN email_bcc TYPE varchar(32767);

-- Step 3: Recreate views
CREATE OR REPLACE VIEW p28_dat_email_nt AS SELECT
    -- columns of java class InternalTenantRef42
    t0.tenant_ref AS tenant_ref
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class EmailRef
    -- columns of java class EmailDTO
    , t0.message_id AS message_id
    , t0.email_subject AS email_subject
    , t0.email_from AS email_from
    , t0.reply_to AS reply_to
    , t0.email_to AS email_to
    , t0.email_cc AS email_cc
    , t0.email_bcc AS email_bcc
    , t0.number_of_attachments AS number_of_attachments
    , EmailStatus2s(t0.email_status) AS email_status
    , t0.bounced AS bounced
    , t0.bounce_message AS bounce_message
FROM p28_dat_email t0;

CREATE OR REPLACE VIEW p28_dat_email_v AS SELECT
    -- columns of java class TrackingBase
    -- columns of java class WriteTracking
    t0.c_tech_user_id AS c_tech_user_id
    , t0.c_app_user_id AS c_app_user_id
    , t0.c_timestamp AS c_timestamp
    , t0.c_process_ref AS c_process_ref
    -- columns of java class FullTracking
    , t0.m_tech_user_id AS m_tech_user_id
    , t0.m_app_user_id AS m_app_user_id
    , t0.m_timestamp AS m_timestamp
    , t0.m_process_ref AS m_process_ref
    -- columns of java class FullTrackingWithVersion
    , t0.version AS version
    -- columns of java class InternalTenantRef42
    , t0.tenant_ref AS tenant_ref
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class EmailRef
    -- columns of java class EmailDTO
    , t0.message_id AS message_id
    , t0.email_subject AS email_subject
    , t0.email_from AS email_from
    , t0.reply_to AS reply_to
    , t0.email_to AS email_to
    , t0.email_cc AS email_cc
    , t0.email_bcc AS email_bcc
    , t0.number_of_attachments AS number_of_attachments
    , EmailStatus2s(t0.email_status) AS email_status
    , t0.bounced AS bounced
    , t0.bounce_message AS bounce_message
FROM p28_dat_email t0;
