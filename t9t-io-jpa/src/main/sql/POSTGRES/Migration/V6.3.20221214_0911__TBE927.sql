-- TBE-927: convert indexes to partial indexes

DROP INDEX p42_dat_sinks_i2;

CREATE INDEX p42_dat_sinks_i2 ON p42_dat_sinks(
    camel_transfer_status
) WHERE camel_transfer_status IS NOT NULL;

DROP INDEX p42_int_async_messages_i1;

CREATE INDEX p42_int_async_messages_i1 ON p42_int_async_messages(
    status
) WHERE status IS NOT NULL;
