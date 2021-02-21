-- TBE-384: add external / secondary user ID to user table - POSTGRES
ALTER TABLE p42_cfg_users ADD COLUMN user_id_ext varchar(36);
ALTER TABLE p42_his_users ADD COLUMN user_id_ext varchar(36);

COMMENT ON COLUMN p42_cfg_users.user_id_ext  IS 'external user ID - only required to be unique within tenant and orgUnit';
COMMENT ON COLUMN p42_his_users.user_id_ext  IS 'external user ID - only required to be unique within tenant and orgUnit';
