-- TBE-238 add supervisor
ALTER TABLE p42_cfg_users ADD supervisor_ref number(10);
ALTER TABLE p42_his_users ADD supervisor_ref number(10);
