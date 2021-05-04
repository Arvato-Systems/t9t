-- TBE-448: new columns for SchedulerSetupEntity - ORACLE

ALTER TABLE p28_cfg_scheduler_setup ADD run_on_node number(10);

COMMENT ON COLUMN p28_cfg_scheduler_setup.run_on_node IS 'node / shard to use in a clustered environment';

ALTER TABLE p28_his_scheduler_setup ADD run_on_node number(10);

COMMENT ON COLUMN p28_his_scheduler_setup.run_on_node IS 'node / shard to use in a clustered environment';
