-- TBE-388: new columns for SchedulerSetupEntity - Oracle

ALTER TABLE p28_cfg_scheduler_setup ADD concurrency_type varchar2(1);
ALTER TABLE p28_cfg_scheduler_setup ADD concurrency_type_stale varchar2(1);
ALTER TABLE p28_cfg_scheduler_setup ADD concurrency_hook varchar2(32 char);
ALTER TABLE p28_cfg_scheduler_setup ADD time_limit number(10);
ALTER TABLE p28_cfg_scheduler_setup ADD mailing_group_id varchar2(16 char);

COMMENT ON COLUMN p28_cfg_scheduler_setup.concurrency_type IS 'Concurrency setting while within limits. If null, it defaults to RUN_PARALLEL';
COMMENT ON COLUMN p28_cfg_scheduler_setup.concurrency_type_stale IS 'Concurrency setting after time limit exceeded. If null, it defaults to RUN_PARALLEL';
COMMENT ON COLUMN p28_cfg_scheduler_setup.time_limit IS 'time limit for a single execution (in seconds). Once exceeded, the stale concurrency is used, and an email is generated (if the mailingGroup has been defined). If null, there is no limit';

ALTER TABLE p28_his_scheduler_setup ADD concurrency_type varchar2(1);
ALTER TABLE p28_his_scheduler_setup ADD concurrency_type_stale varchar2(1);
ALTER TABLE p28_his_scheduler_setup ADD concurrency_hook varchar2(32 char);
ALTER TABLE p28_his_scheduler_setup ADD time_limit number(10);
ALTER TABLE p28_his_scheduler_setup ADD mailing_group_id varchar2(16 char);

COMMENT ON COLUMN p28_his_scheduler_setup.concurrency_type IS 'Concurrency setting while within limits. If null, it defaults to RUN_PARALLEL';
COMMENT ON COLUMN p28_his_scheduler_setup.concurrency_type_stale IS 'Concurrency setting after time limit exceeded. If null, it defaults to RUN_PARALLEL';
COMMENT ON COLUMN p28_his_scheduler_setup.time_limit IS 'time limit for a single execution (in seconds). Once exceeded, the stale concurrency is used, and an email is generated (if the mailingGroup has been defined). If null, there is no limit';
