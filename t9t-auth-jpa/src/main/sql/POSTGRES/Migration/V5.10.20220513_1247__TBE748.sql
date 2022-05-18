-- TBE-748: add additional password requirements - POSTGRES
ALTER TABLE p28_cfg_auth_module_cfg ADD COLUMN password_min_digits integer DEFAULT 1;
ALTER TABLE p28_cfg_auth_module_cfg ADD COLUMN password_min_letters integer DEFAULT 2;
ALTER TABLE p28_cfg_auth_module_cfg ADD COLUMN password_min_other_chars integer DEFAULT 1;
ALTER TABLE p28_cfg_auth_module_cfg ADD COLUMN password_min_uppercase integer DEFAULT 1;
ALTER TABLE p28_cfg_auth_module_cfg ADD COLUMN password_min_lowercase integer DEFAULT 1;
ALTER TABLE p28_cfg_auth_module_cfg ADD COLUMN password_max_common_substring integer DEFAULT 0;

ALTER TABLE p28_his_auth_module_cfg ADD COLUMN password_min_digits integer DEFAULT 1;
ALTER TABLE p28_his_auth_module_cfg ADD COLUMN password_min_letters integer DEFAULT 2;
ALTER TABLE p28_his_auth_module_cfg ADD COLUMN password_min_other_chars integer DEFAULT 1;
ALTER TABLE p28_his_auth_module_cfg ADD COLUMN password_min_uppercase integer DEFAULT 1;
ALTER TABLE p28_his_auth_module_cfg ADD COLUMN password_min_lowercase integer DEFAULT 1;
ALTER TABLE p28_his_auth_module_cfg ADD COLUMN password_max_common_substring integer DEFAULT 0;

COMMENT ON COLUMN p28_cfg_auth_module_cfg.password_min_digits IS 'how many digits must be part of the password?';
COMMENT ON COLUMN p28_cfg_auth_module_cfg.password_min_letters IS 'how many letters (upper + lower case) must be part of the password?';
COMMENT ON COLUMN p28_cfg_auth_module_cfg.password_min_other_chars IS 'how many special characters must be part of the password?';
COMMENT ON COLUMN p28_cfg_auth_module_cfg.password_min_uppercase IS 'how many uppercase letters must be part of the password?';
COMMENT ON COLUMN p28_cfg_auth_module_cfg.password_min_lowercase IS 'how many lowercase letters must be part of the password?';
COMMENT ON COLUMN p28_cfg_auth_module_cfg.password_max_common_substring IS 'how many identical substring characters found in the password are allowed?';


COMMENT ON COLUMN p28_his_auth_module_cfg.password_min_digits IS 'how many digits must be part of the password?';
COMMENT ON COLUMN p28_his_auth_module_cfg.password_min_letters IS 'how many letters (upper + lower case) must be part of the password?';
COMMENT ON COLUMN p28_his_auth_module_cfg.password_min_other_chars IS 'how many special characters must be part of the password?';
COMMENT ON COLUMN p28_his_auth_module_cfg.password_min_uppercase IS 'how many uppercase letters must be part of the password?';
COMMENT ON COLUMN p28_his_auth_module_cfg.password_min_lowercase IS 'how many lowercase letters must be part of the password?';
COMMENT ON COLUMN p28_his_auth_module_cfg.password_max_common_substring IS 'how many identical substring characters found in the password are allowed?';
