-- TBE-353: add default screen to user table - POSTGRES
ALTER TABLE p42_cfg_users ADD COLUMN default_screen_id varchar(64);
ALTER TABLE p42_his_users ADD COLUMN default_screen_id varchar(64);

COMMENT ON COLUMN p42_cfg_users.default_screen_id IS 'default screen admin zk ui';
COMMENT ON COLUMN p42_his_users.default_screen_id IS 'default screen admin zk ui';
