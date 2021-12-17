-- TBE-650: modify type of messageId column - ORACLE

ALTER TABLE p28_int_message MODIFY ( message_id RAW(16) );
