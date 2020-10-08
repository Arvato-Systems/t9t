-- TBE-337 - new columns in p42_cfg_data_sinks ORACLE
ALTER TABLE p42_cfg_data_sinks ADD xml_header_elements varchar2(100 char);
ALTER TABLE p42_cfg_data_sinks ADD xml_footer_elements varchar2(100 char);
ALTER TABLE p42_his_data_sinks ADD xml_header_elements varchar2(100 char);
ALTER TABLE p42_his_data_sinks ADD xml_footer_elements varchar2(100 char);

COMMENT ON COLUMN p42_cfg_data_sinks.xml_header_elements IS 'scalar header information';
COMMENT ON COLUMN p42_cfg_data_sinks.xml_footer_elements IS 'scalar footer information';
