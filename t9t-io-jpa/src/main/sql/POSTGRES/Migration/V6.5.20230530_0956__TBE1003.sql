-- TBE-1003: additional field in async message

ALTER TABLE p42_int_async_messages ADD COLUMN last_response_time integer;

COMMENT ON COLUMN p42_int_async_messages.last_response_time IS 'response time in milliseconds of last attempt (until response received or timeout)';
