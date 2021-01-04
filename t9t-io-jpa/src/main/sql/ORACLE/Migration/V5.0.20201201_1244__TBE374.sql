-- TBE-374 - new columns in p42_cfg_data_sinks ORACLE
ALTER TABLE p42_cfg_data_sinks ADD lines_to_skip number(10);
ALTER TABLE p42_cfg_data_sinks ADD single_line_comment varchar2(8 char);

ALTER TABLE p42_his_data_sinks ADD lines_to_skip number(10);
ALTER TABLE p42_his_data_sinks ADD single_line_comment varchar2(8 char);

COMMENT ON COLUMN p42_cfg_data_sinks.lines_to_skip IS 'skip a number of initial lines. (does not count comment lines)';
COMMENT ON COLUMN p42_cfg_data_sinks.single_line_comment IS 'line starting with this line should be ignored';
