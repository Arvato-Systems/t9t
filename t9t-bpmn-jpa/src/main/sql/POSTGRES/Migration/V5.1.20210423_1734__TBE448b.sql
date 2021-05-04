-- TBE-448: new columns for ProcessExecStatusEntity - Postgres

ALTER TABLE p28_dat_process_exec_status ADD COLUMN run_on_node integer;

COMMENT ON COLUMN p28_dat_process_exec_status.run_on_node IS 'node / shard to use in a clustered environment';
