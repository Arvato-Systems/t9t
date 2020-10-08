-- TBE-337 - new columns in p42_cfg_data_sinks POSTGRES
ALTER TABLE p42_cfg_data_sinks ADD COLUMN xml_header_elements varchar(100);
ALTER TABLE p42_cfg_data_sinks ADD COLUMN xml_footer_elements varchar(100);
ALTER TABLE p42_his_data_sinks ADD COLUMN xml_header_elements varchar(100);
ALTER TABLE p42_his_data_sinks ADD COLUMN xml_footer_elements varchar(100);

COMMENT ON COLUMN p42_cfg_data_sinks.xml_header_elements IS 'scalar header information';
COMMENT ON COLUMN p42_cfg_data_sinks.xml_footer_elements IS 'scalar footer information';
