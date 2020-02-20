-- TBE-236 add flag for external authentication
ALTER TABLE p42_cfg_users ADD external_auth number(1);
ALTER TABLE p42_his_users ADD external_auth number(1);
