-- TBE-856: enhancement of new column for SchedulerSetupEntity - Postgres

ALTER TABLE p28_cfg_scheduler_setup ALTER COLUMN scheduler_environment type varchar(36);

COMMENT ON COLUMN p28_cfg_scheduler_setup.scheduler_environment IS 'allows to select a subset of scheduled tasks - all will be used if null';

ALTER TABLE p28_his_scheduler_setup ALTER COLUMN scheduler_environment type varchar(36);

COMMENT ON COLUMN p28_his_scheduler_setup.scheduler_environment IS 'allows to select a subset of scheduled tasks - all will be used if null';
