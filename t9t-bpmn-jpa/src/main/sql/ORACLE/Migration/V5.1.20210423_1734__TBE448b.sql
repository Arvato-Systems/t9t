-- TBE-448: new columns for ProcessExecStatusEntity - ORACLE

ALTER TABLE p28_dat_process_exec_status ADD run_on_node number(10);

COMMENT ON COLUMN p28_dat_process_exec_status.run_on_node IS 'node / shard to use in a clustered environment';
