-- TBE-379 - new columns in p42_dat_sinks POSTGRES
ALTER TABLE p42_dat_sinks ADD COLUMN version integer;
UPDATE p42_dat_sinks SET version = 0;
