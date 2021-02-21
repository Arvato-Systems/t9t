-- TBE-388: new columns for SchedulerSetupEntity - MSSQLSERVER

ALTER TABLE p28_cfg_scheduler_setup ADD concurrency_type varchar(1);
ALTER TABLE p28_cfg_scheduler_setup ADD concurrency_type_stale varchar(1);
ALTER TABLE p28_cfg_scheduler_setup ADD concurrency_hook nvarchar(32);
ALTER TABLE p28_cfg_scheduler_setup ADD time_limit int;
ALTER TABLE p28_cfg_scheduler_setup ADD mailing_group_id nvarchar(16);

ALTER TABLE p28_his_scheduler_setup ADD concurrency_type varchar(1);
ALTER TABLE p28_his_scheduler_setup ADD concurrency_type_stale varchar(1);
ALTER TABLE p28_his_scheduler_setup ADD concurrency_hook nvarchar(32);
ALTER TABLE p28_his_scheduler_setup ADD time_limit int;
ALTER TABLE p28_his_scheduler_setup ADD mailing_group_id nvarchar(16);
