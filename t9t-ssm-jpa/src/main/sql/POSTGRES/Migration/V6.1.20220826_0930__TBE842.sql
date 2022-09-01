-- TBE-842: new columns for SchedulerSetupEntity - Postgres

ALTER TABLE p28_cfg_scheduler_setup ADD COLUMN scheduler_environment varchar(8);

COMMENT ON COLUMN p28_cfg_scheduler_setup.scheduler_environment IS 'allows to select a subset of scheduled tasks - all will be used if null';

ALTER TABLE p28_his_scheduler_setup ADD COLUMN scheduler_environment varchar(8);

COMMENT ON COLUMN p28_his_scheduler_setup.scheduler_environment IS 'allows to select a subset of scheduled tasks - all will be used if null';
