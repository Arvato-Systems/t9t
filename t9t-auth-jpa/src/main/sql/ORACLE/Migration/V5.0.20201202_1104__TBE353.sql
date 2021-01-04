-- TBE-323: add default screen to user table - ORACLE
ALTER TABLE p42_cfg_users ADD default_screen_id varchar2(64 char);
ALTER TABLE p42_his_users ADD default_screen_id varchar2(64 char);

COMMENT ON COLUMN p42_cfg_users.default_screen_id IS 'default screen admin zk ui';
COMMENT ON COLUMN p42_his_users.default_screen_id IS 'default screen admin zk ui';
