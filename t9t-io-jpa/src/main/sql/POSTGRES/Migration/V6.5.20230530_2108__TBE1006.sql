-- TBE-1006: additional fields in async channel

ALTER TABLE p28_cfg_async_channel ADD COLUMN delay_after_send integer;
ALTER TABLE p28_his_async_channel ADD COLUMN delay_after_send integer;
ALTER TABLE p28_cfg_async_channel ADD COLUMN parallel boolean;
ALTER TABLE p28_his_async_channel ADD COLUMN parallel boolean;
ALTER TABLE p28_cfg_async_channel ADD COLUMN callback_url varchar(512);
ALTER TABLE p28_his_async_channel ADD COLUMN callback_url varchar(512);

COMMENT ON COLUMN p28_cfg_async_channel.delay_after_send IS 'if not null, causes the sender to pause this duration in ms after every send (mainly for parallel mode to avoid overruns)';
COMMENT ON COLUMN p28_cfg_async_channel.parallel IS 'process asynchronously - more than 1 at a time';
COMMENT ON COLUMN p28_cfg_async_channel.callback_url IS 'any URL for a callback';
