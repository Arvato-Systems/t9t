-- TBE-334 - new columns in p42_dat_sinks ORACLE
ALTER TABLE p42_dat_sinks ADD processed number(1);
ALTER TABLE p42_dat_sinks ADD generic_id1 varchar2(36 char);
ALTER TABLE p42_dat_sinks ADD generic_id2 varchar2(36 char);

COMMENT ON COLUMN p42_dat_sinks.processed IS 'keeps track, if this data export has been processed further (for example emailed, printed etc.)';
COMMENT ON COLUMN p42_dat_sinks.generic_id1 IS 'some alphanumeric ID';
COMMENT ON COLUMN p42_dat_sinks.generic_id2 IS 'some alphanumeric ID';
