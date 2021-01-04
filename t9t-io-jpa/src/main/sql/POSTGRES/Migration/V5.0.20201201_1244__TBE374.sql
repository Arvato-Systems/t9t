-- TBE-374 - new columns in p42_cfg_data_sinks POSTGRES
DROP VIEW IF EXISTS p42_cfg_data_sinks_nt;
DROP VIEW IF EXISTS p42_cfg_data_sinks_v;
ALTER TABLE p42_cfg_data_sinks ADD COLUMN lines_to_skip integer;
ALTER TABLE p42_cfg_data_sinks ADD COLUMN single_line_comment varchar(8);

ALTER TABLE p42_his_data_sinks ADD COLUMN lines_to_skip integer;
ALTER TABLE p42_his_data_sinks ADD COLUMN single_line_comment varchar(8);

COMMENT ON COLUMN p42_cfg_data_sinks.lines_to_skip IS 'skip a number of initial lines. (does not count comment lines)';
COMMENT ON COLUMN p42_cfg_data_sinks.single_line_comment IS 'line starting with this line should be ignored';
