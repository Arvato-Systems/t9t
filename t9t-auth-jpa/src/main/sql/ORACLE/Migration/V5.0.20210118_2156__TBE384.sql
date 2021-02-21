-- TBE-384: add external / secondary user ID to user table - ORACLE
ALTER TABLE p42_cfg_users ADD user_id_ext varchar2(36 char);
ALTER TABLE p42_his_users ADD user_id_ext varchar2(36 char);

COMMENT ON COLUMN p42_cfg_users.user_id_ext  IS 'external user ID - only required to be unique within tenant and orgUnit';
COMMENT ON COLUMN p42_his_users.user_id_ext  IS 'external user ID - only required to be unique within tenant and orgUnit';
