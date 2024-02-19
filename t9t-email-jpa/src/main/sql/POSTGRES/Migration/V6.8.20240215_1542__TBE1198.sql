-- TBE-1198: additional field storage location for email attachments (POSTGRES)

ALTER TABLE p28_dat_email_attachments ADD COLUMN media_storage_location varchar(1);

