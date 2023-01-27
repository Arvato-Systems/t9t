-- TBE-927: use partial indexes

DROP INDEX IF EXISTS p28_dat_process_exec_status_i3;
DROP INDEX IF EXISTS p28_dat_process_exec_status_i4;

CREATE INDEX p28_dat_process_exec_status_i3 ON p28_dat_process_exec_status(
    lock_ref
) WHERE lock_ref IS NOT NULL;
CREATE INDEX p28_dat_process_exec_status_i4 ON p28_dat_process_exec_status(
    lock_id
) WHERE lock_id IS NOT NULL;
