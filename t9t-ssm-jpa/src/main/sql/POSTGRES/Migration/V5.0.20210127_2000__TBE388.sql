-- TBE-388: new columns for SchedulerSetupEntity - Postgres

ALTER TABLE p28_cfg_scheduler_setup ADD COLUMN concurrency_type varchar(1);
ALTER TABLE p28_cfg_scheduler_setup ADD COLUMN concurrency_type_stale varchar(1);
ALTER TABLE p28_cfg_scheduler_setup ADD COLUMN concurrency_hook varchar(32);
ALTER TABLE p28_cfg_scheduler_setup ADD COLUMN time_limit integer;
ALTER TABLE p28_cfg_scheduler_setup ADD COLUMN mailing_group_id varchar(16);

COMMENT ON COLUMN p28_cfg_scheduler_setup.concurrency_type IS 'Concurrency setting while within limits. If null, it defaults to RUN_PARALLEL';
COMMENT ON COLUMN p28_cfg_scheduler_setup.concurrency_type_stale IS 'Concurrency setting after time limit exceeded. If null, it defaults to RUN_PARALLEL';
COMMENT ON COLUMN p28_cfg_scheduler_setup.time_limit IS 'time limit for a single execution (in seconds). Once exceeded, the stale concurrency is used, and an email is generated (if the mailingGroup has been defined). If null, there is no limit';

ALTER TABLE p28_his_scheduler_setup ADD COLUMN concurrency_type varchar(1);
ALTER TABLE p28_his_scheduler_setup ADD COLUMN concurrency_type_stale varchar(1);
ALTER TABLE p28_his_scheduler_setup ADD COLUMN concurrency_hook varchar(32);
ALTER TABLE p28_his_scheduler_setup ADD COLUMN time_limit integer;
ALTER TABLE p28_his_scheduler_setup ADD COLUMN mailing_group_id varchar(16);

COMMENT ON COLUMN p28_his_scheduler_setup.concurrency_type IS 'Concurrency setting while within limits. If null, it defaults to RUN_PARALLEL';
COMMENT ON COLUMN p28_his_scheduler_setup.concurrency_type_stale IS 'Concurrency setting after time limit exceeded. If null, it defaults to RUN_PARALLEL';
COMMENT ON COLUMN p28_his_scheduler_setup.time_limit IS 'time limit for a single execution (in seconds). Once exceeded, the stale concurrency is used, and an email is generated (if the mailingGroup has been defined). If null, there is no limit';
