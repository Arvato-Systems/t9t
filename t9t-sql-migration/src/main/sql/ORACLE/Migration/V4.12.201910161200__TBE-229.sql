-- TBE-229 Extend async channel field length 
ALTER TABLE p28_cfg_async_channel MODIFY url varchar2(300 char);
ALTER TABLE p28_his_async_channel MODIFY url varchar2(300 char);