-- TBE-1034: Additional indexes on async messages (POSTGRES)

CREATE INDEX p42_int_async_messages_i3 ON p42_int_async_messages(
    ref
) WHERE ref IS NOT NULL;
CREATE INDEX p42_int_async_messages_i4 ON p42_int_async_messages(
    ref_identifier
) WHERE ref_identifier IS NOT NULL;

COMMENT ON COLUMN p42_int_async_messages.ref_identifier IS 'for debugging / maintenance: refers to the ID of a specific cause: delivery order ID, customer ID, return ID etc.';
COMMENT ON COLUMN p42_int_async_messages.ref IS 'for debugging / maintenance: related objectRef for a group of messages (for example sales order reference)';
