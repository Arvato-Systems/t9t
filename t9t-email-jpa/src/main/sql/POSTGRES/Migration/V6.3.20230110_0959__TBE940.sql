-- TBE-940: Index improvement - POSTGRES

DROP INDEX p28_dat_email_i2;
CREATE INDEX p28_dat_email_i2 ON p28_dat_email(
    email_status
) WHERE email_status IS NOT NULL;

