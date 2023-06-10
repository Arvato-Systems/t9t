-- TBE-1021: Increased precision of a timestamp column - POSTGRES

DROP VIEW IF EXISTS p42_int_async_messages_v;
DROP VIEW IF EXISTS p42_int_async_messages_nt;
ALTER TABLE p42_int_async_messages ALTER COLUMN c_timestamp TYPE timestamp(3);

DROP VIEW IF EXISTS p42_int_outbound_messages_v;
DROP VIEW IF EXISTS p42_int_outbound_messages_nt;
ALTER TABLE p42_int_outbound_messages ALTER COLUMN c_timestamp TYPE timestamp(3);

