-- TBE-336 - new column in p28_dat_slice_tracking ORACLE
ALTER TABLE p28_dat_slice_tracking ADD gap NUMBER(10);

COMMENT ON COLUMN p28_dat_slice_tracking.gap IS 'the gap to not export (in seconds), if null, then 10 seconds are assumed.';
