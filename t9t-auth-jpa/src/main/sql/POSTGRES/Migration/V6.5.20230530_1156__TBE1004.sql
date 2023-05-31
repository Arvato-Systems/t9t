-- TBE-1004: additional field in session log

ALTER TABLE p28_dat_session ADD COLUMN api_key_ref bigint;

COMMENT ON COLUMN p28_dat_session.api_key_ref IS 'references the API key used to log in, in case the authentication was by API key';
