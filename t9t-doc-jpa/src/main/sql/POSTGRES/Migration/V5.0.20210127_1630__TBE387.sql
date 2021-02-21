-- TBE-387: migration of MailingGroupEntity (POSTGRES)

DROP INDEX p28_cfg_report_mailing_u1;
ALTER TABLE p28_cfg_report_mailing RENAME TO p28_cfg_mailing_group;
ALTER TABLE p28_cfg_mailing_group ADD COLUMN description varchar(80);
ALTER TABLE p28_cfg_mailing_group RENAME CONSTRAINT p28_cfg_report_mailing_pk TO p28_cfg_mailing_group_pk;
CREATE UNIQUE INDEX p28_cfg_mailing_group_u1 ON p28_cfg_mailing_group(
    tenant_ref, mailing_group_id
);
COMMENT ON COLUMN p28_cfg_mailing_group.description IS 'the description of this group';

-- History
ALTER TABLE p28_his_report_mailing RENAME TO p28_his_mailing_group;
ALTER TABLE p28_his_mailing_group ADD COLUMN description varchar(80);
ALTER TABLE p28_his_mailing_group RENAME CONSTRAINT p28_his_report_mailing_pk TO p28_his_mailing_group_pk;
COMMENT ON COLUMN p28_his_mailing_group.description IS 'the description of this group';

UPDATE p28_cfg_mailing_group SET description = mailing_group_id WHERE description IS NULL;
UPDATE p28_his_mailing_group SET description = mailing_group_id WHERE description IS NULL;
--COMMIT;

ALTER TABLE p28_cfg_mailing_group ALTER COLUMN description SET NOT NULL;
ALTER TABLE p28_his_mailing_group ALTER COLUMN description SET NOT NULL;
