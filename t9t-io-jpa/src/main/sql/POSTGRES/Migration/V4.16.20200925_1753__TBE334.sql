-- TBE-334 - new columns in p42_dat_sinks POSTGRES
ALTER TABLE p42_dat_sinks ADD COLUMN processed boolean;
ALTER TABLE p42_dat_sinks ADD COLUMN generic_id1 varchar(36);
ALTER TABLE p42_dat_sinks ADD COLUMN generic_id2 varchar(36);

COMMENT ON COLUMN p42_dat_sinks.processed IS 'keeps track, if this data export has been processed further (for example emailed, printed etc.)';
COMMENT ON COLUMN p42_dat_sinks.generic_id1 IS 'some alphanumeric ID';
COMMENT ON COLUMN p42_dat_sinks.generic_id2 IS 'some alphanumeric ID';
