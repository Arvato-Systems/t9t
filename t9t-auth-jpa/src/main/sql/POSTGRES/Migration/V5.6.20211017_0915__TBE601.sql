-- TBE-601: add time zone to tenant cfg table - POSTGRES
ALTER TABLE p42_cfg_tenants ADD COLUMN time_zone varchar(64);
ALTER TABLE p42_his_tenants ADD COLUMN time_zone varchar(64);

COMMENT ON COLUMN p42_cfg_tenants.time_zone IS 'a time zone identifier such as "Europe/Berlin", to define the default time zone of the tenant';
COMMENT ON COLUMN p42_his_tenants.time_zone IS 'a time zone identifier such as "Europe/Berlin", to define the default time zone of the tenant';
