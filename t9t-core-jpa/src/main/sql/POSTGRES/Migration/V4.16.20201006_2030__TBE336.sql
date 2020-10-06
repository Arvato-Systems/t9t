-- TBE-336 - new column in p28_dat_slice_tracking POSTGRES
ALTER TABLE p28_dat_slice_tracking ADD COLUMN gap integer;

COMMENT ON COLUMN p28_dat_slice_tracking.gap IS 'the gap to not export (in seconds), if null, then 10 seconds are assumed.';
