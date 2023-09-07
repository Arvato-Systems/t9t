-- TBE-1058: additional fields for user (POSTGRES)

ALTER TABLE p42_cfg_users ADD COLUMN identity_provider varchar(80);
ALTER TABLE p42_cfg_users ADD COLUMN only_external_auth boolean;

ALTER TABLE p42_his_users ADD COLUMN identity_provider varchar(80);
ALTER TABLE p42_his_users ADD COLUMN only_external_auth boolean;

COMMENT ON COLUMN p42_cfg_users.identity_provider IS 'for MS OpenID Connect / Azure AD: if present, requires that the user belongs to this tenant';
COMMENT ON COLUMN p42_cfg_users.only_external_auth IS 'set to true to disable internal authentication';

CREATE INDEX p42_cfg_users_i2 ON p42_cfg_users(
    user_id_ext
) WHERE user_id_ext IS NOT NULL;
