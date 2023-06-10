-- TBE-1021: Increased precision of a timestamp column - POSTGRES

DROP VIEW IF EXISTS p28_int_message_v;
DROP VIEW IF EXISTS p28_int_message_nt;
ALTER TABLE p28_int_message ALTER COLUMN execution_started_at TYPE timestamp(3);

CREATE INDEX p28_int_message_i2 ON p28_int_message(
    execution_started_at
);
