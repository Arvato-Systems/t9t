-- TBE-1051: Additional index on messageDTO - POSTGRES

CREATE INDEX p28_int_message_i3 ON p28_int_message(
    invoking_process_ref
) WHERE invoking_process_ref IS NOT NULL;
