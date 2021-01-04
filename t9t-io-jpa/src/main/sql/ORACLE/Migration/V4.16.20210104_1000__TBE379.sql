-- TBE-379 - new columns in p42_dat_sinks ORACLE
ALTER TABLE p42_dat_sinks ADD version number(10);
UPDATE p42_dat_sinks SET version = 0;
