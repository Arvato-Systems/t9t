-- TBE-884: new columns for ProcessExecStatusEntity - Postgres

ALTER TABLE p28_dat_process_exec_status ADD COLUMN lock_ref bigint;
ALTER TABLE p28_dat_process_exec_status ADD COLUMN lock_id varchar(36);

COMMENT ON COLUMN p28_dat_process_exec_status.lock_ref IS 'if non null, this reference should be locked for execution';
COMMENT ON COLUMN p28_dat_process_exec_status.lock_id IS 'if non null, this reference should be locked for execution';

CREATE INDEX p28_dat_process_exec_status_i3 ON p28_dat_process_exec_status(
    lock_ref
);
CREATE INDEX p28_dat_process_exec_status_i4 ON p28_dat_process_exec_status(
    lock_id
);
