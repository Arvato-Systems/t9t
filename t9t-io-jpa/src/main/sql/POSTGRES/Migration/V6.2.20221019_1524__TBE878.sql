-- TBE-878 - new column in p42_int_async_messages (POSTGRES)
DROP VIEW IF EXISTS p42_int_async_messages_v;
DROP VIEW IF EXISTS p42_int_async_messages_NT;

ALTER TABLE p42_int_async_messages ADD COLUMN error_details varchar(1024);

COMMENT ON COLUMN p42_int_async_messages.error_details IS 'additional description of the problem';
