-- TBE-522: add initialPasswordExpiration to auth module cfg table - ORACLE
ALTER TABLE p28_cfg_auth_module_cfg ADD initial_password_expiration number(10) DEFAULT 0;
ALTER TABLE p28_his_auth_module_cfg ADD initial_password_expiration number(10) DEFAULT 0;

COMMENT ON COLUMN p28_cfg_auth_module_cfg.initial_password_expiration  IS 'Period in days a password change is required on a newly created user';
COMMENT ON COLUMN p28_his_auth_module_cfg.initial_password_expiration  IS 'Period in days a password change is required on a newly created user';
