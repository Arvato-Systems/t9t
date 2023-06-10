-- TBE-1006: additional fields in async channel

ALTER TABLE p28_cfg_async_channel ADD COLUMN idempotency_header_type varchar(1);
ALTER TABLE p28_cfg_async_channel ADD COLUMN idempotency_header varchar(16);
ALTER TABLE p28_his_async_channel ADD COLUMN idempotency_header_type varchar(1);
ALTER TABLE p28_his_async_channel ADD COLUMN idempotency_header varchar(16);

COMMENT ON COLUMN p28_cfg_async_channel.idempotency_header IS 'the http header variable to use for idempotency';
COMMENT ON COLUMN p28_cfg_async_channel.idempotency_header_type IS 'which type of header to generate';


