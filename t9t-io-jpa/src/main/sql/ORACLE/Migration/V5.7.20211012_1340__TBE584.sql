-- TBE-584 - new columns in p42_cfg_data_sinks ORACLE
ALTER TABLE p42_cfg_data_sinks ADD input_processing_type varchar2(1);
ALTER TABLE p42_cfg_data_sinks ADD input_processing_parallel number(10);
ALTER TABLE p42_cfg_data_sinks ADD input_processing_splitter varchar2(32 char);
ALTER TABLE p42_cfg_data_sinks ADD input_processing_target varchar2(255 char);

COMMENT ON COLUMN p42_cfg_data_sinks.input_processing_type IS 'defines how / where to process input records (default is LOCAL)';
COMMENT ON COLUMN p42_cfg_data_sinks.input_processing_parallel IS 'defines the number of parallel threads to use for processing';
COMMENT ON COLUMN p42_cfg_data_sinks.input_processing_splitter IS 'method to determine partition to use (if blank and inputProcessingParallel > 1: use round robin)';
COMMENT ON COLUMN p42_cfg_data_sinks.input_processing_target IS 'required for KAFKA or REMOTE: the topic or the remote path to use for processing';
