-- TBE-448: new columns for SchedulerSetupEntity - Postgres

ALTER TABLE p28_cfg_scheduler_setup ADD COLUMN run_on_node integer;

COMMENT ON COLUMN p28_cfg_scheduler_setup.run_on_node IS 'node / shard to use in a clustered environment';

ALTER TABLE p28_his_scheduler_setup ADD COLUMN run_on_node integer;

COMMENT ON COLUMN p28_his_scheduler_setup.run_on_node IS 'node / shard to use in a clustered environment';
