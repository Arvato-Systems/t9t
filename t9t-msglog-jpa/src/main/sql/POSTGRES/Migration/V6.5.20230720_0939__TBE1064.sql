-- TBE-1064: Additional index on messageDTO - POSTGRES

ALTER TABLE p28_int_message ADD COLUMN essential_key varchar(36);

CREATE INDEX p28_int_message_i4 ON p28_int_message(
    essential_key
) WHERE essential_key IS NOT NULL;
