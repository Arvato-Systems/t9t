-- TBE-940: Index improvement - POSTGRES

DROP INDEX p28_int_message_i1;

CREATE INDEX p28_int_message_i1 ON p28_int_message(
    message_id
) WHERE message_id IS NOT NULL;
