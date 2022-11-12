ALTER TABLE p42_dat_passwords ADD COLUMN reset_password_hash bytea;
ALTER TABLE p42_dat_passwords ADD COLUMN when_last_password_reset timestamp(0);

COMMENT ON COLUMN p42_dat_passwords.reset_password_hash IS 'salted hash';
COMMENT ON COLUMN p42_dat_passwords.when_last_password_reset IS 'when "reset my password" has been used last time';
