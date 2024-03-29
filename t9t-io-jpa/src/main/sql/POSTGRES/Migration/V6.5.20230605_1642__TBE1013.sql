-- TBE-1013: extend size of columns

DROP VIEW IF EXISTS p42_cfg_data_sinks_v;
DROP VIEW IF EXISTS p42_cfg_data_sinks_nt;
DROP TRIGGER IF EXISTS p42_cfg_data_sinks_tr ON p42_cfg_data_sinks;

ALTER TABLE p42_cfg_data_sinks ALTER COLUMN camel_route TYPE varchar(511);
ALTER TABLE p42_his_data_sinks ALTER COLUMN camel_route TYPE varchar(511);

CREATE OR REPLACE FUNCTION p42_cfg_data_sinks_tp() RETURNS TRIGGER AS $p42_cfg_data_sinks_td$
DECLARE
    next_seq_ BIGINT;
BEGIN
    SELECT NEXTVAL('cm_idgen_5009_seq') INTO next_seq_;
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO p42_his_data_sinks (
            history_seq_ref
            , history_change_type
            , object_ref
            , c_app_user_id
            , c_timestamp
            , c_process_ref
            , m_app_user_id
            , m_timestamp
            , m_process_ref
            , version
            , tenant_id
            , data_sink_id
            , is_active
            , comm_target_channel_type
            , comm_format_type
            , file_or_queue_name_pattern
            , compressed
            , originator_module
            , pre_transformer_name
            , comm_format_name
            , category
            , callback_ref
            , grid_id
            , output_encoding
            , generic_parameter1
            , generic_parameter2
            , camel_route
            , success_routing_strategy
            , success_dest_pattern
            , failed_routing_strategy
            , failure_dest_pattern
            , jaxb_context_path
            , environment
            , log_messages
            , is_input
            , csv_configuration_ref
            , base_class_pqon
            , xml_default_namespace
            , xml_root_element_name
            , xml_record_name
            , xml_namespace_prefix
            , write_tenant_id
            , description
            , max_number_of_records
            , import_queue_name
            , national_number_format
            , chunk_size
            , lazy_open
            , skip_zero_record_sink_refs
            , store_import_using_filepattern
            , unwrap_tracking
            , z
            , camel_execution
            , check_duplicate_filename
            , xml_namespace_mappings
            , copy_to_async_channel
            , camel_format_is_fmt_route
            , response_data_sink_ref
            , xml_header_elements
            , xml_footer_elements
            , lines_to_skip
            , single_line_comment
            , retention_period_files
            , retention_period_sinks
            , api_key
            , bootstrap_servers
            , input_processing_type
            , input_processing_parallel
            , input_processing_splitter
            , input_processing_target
            , compute_file_size
        ) VALUES (
            next_seq_, 'I'
            , NEW.object_ref
            , NEW.c_app_user_id
            , NEW.c_timestamp
            , NEW.c_process_ref
            , NEW.m_app_user_id
            , NEW.m_timestamp
            , NEW.m_process_ref
            , NEW.version
            , NEW.tenant_id
            , NEW.data_sink_id
            , NEW.is_active
            , NEW.comm_target_channel_type
            , NEW.comm_format_type
            , NEW.file_or_queue_name_pattern
            , NEW.compressed
            , NEW.originator_module
            , NEW.pre_transformer_name
            , NEW.comm_format_name
            , NEW.category
            , NEW.callback_ref
            , NEW.grid_id
            , NEW.output_encoding
            , NEW.generic_parameter1
            , NEW.generic_parameter2
            , NEW.camel_route
            , NEW.success_routing_strategy
            , NEW.success_dest_pattern
            , NEW.failed_routing_strategy
            , NEW.failure_dest_pattern
            , NEW.jaxb_context_path
            , NEW.environment
            , NEW.log_messages
            , NEW.is_input
            , NEW.csv_configuration_ref
            , NEW.base_class_pqon
            , NEW.xml_default_namespace
            , NEW.xml_root_element_name
            , NEW.xml_record_name
            , NEW.xml_namespace_prefix
            , NEW.write_tenant_id
            , NEW.description
            , NEW.max_number_of_records
            , NEW.import_queue_name
            , NEW.national_number_format
            , NEW.chunk_size
            , NEW.lazy_open
            , NEW.skip_zero_record_sink_refs
            , NEW.store_import_using_filepattern
            , NEW.unwrap_tracking
            , NEW.z
            , NEW.camel_execution
            , NEW.check_duplicate_filename
            , NEW.xml_namespace_mappings
            , NEW.copy_to_async_channel
            , NEW.camel_format_is_fmt_route
            , NEW.response_data_sink_ref
            , NEW.xml_header_elements
            , NEW.xml_footer_elements
            , NEW.lines_to_skip
            , NEW.single_line_comment
            , NEW.retention_period_files
            , NEW.retention_period_sinks
            , NEW.api_key
            , NEW.bootstrap_servers
            , NEW.input_processing_type
            , NEW.input_processing_parallel
            , NEW.input_processing_splitter
            , NEW.input_processing_target
            , NEW.compute_file_size
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'UPDATE') THEN
        -- deny attempts to change a primary key column
        IF OLD.object_ref <> NEW.object_ref THEN
            RAISE EXCEPTION 'Cannot change primary key column to different value';
        END IF;
        INSERT INTO p42_his_data_sinks (
            history_seq_ref
            , history_change_type
            , object_ref
            , c_app_user_id
            , c_timestamp
            , c_process_ref
            , m_app_user_id
            , m_timestamp
            , m_process_ref
            , version
            , tenant_id
            , data_sink_id
            , is_active
            , comm_target_channel_type
            , comm_format_type
            , file_or_queue_name_pattern
            , compressed
            , originator_module
            , pre_transformer_name
            , comm_format_name
            , category
            , callback_ref
            , grid_id
            , output_encoding
            , generic_parameter1
            , generic_parameter2
            , camel_route
            , success_routing_strategy
            , success_dest_pattern
            , failed_routing_strategy
            , failure_dest_pattern
            , jaxb_context_path
            , environment
            , log_messages
            , is_input
            , csv_configuration_ref
            , base_class_pqon
            , xml_default_namespace
            , xml_root_element_name
            , xml_record_name
            , xml_namespace_prefix
            , write_tenant_id
            , description
            , max_number_of_records
            , import_queue_name
            , national_number_format
            , chunk_size
            , lazy_open
            , skip_zero_record_sink_refs
            , store_import_using_filepattern
            , unwrap_tracking
            , z
            , camel_execution
            , check_duplicate_filename
            , xml_namespace_mappings
            , copy_to_async_channel
            , camel_format_is_fmt_route
            , response_data_sink_ref
            , xml_header_elements
            , xml_footer_elements
            , lines_to_skip
            , single_line_comment
            , retention_period_files
            , retention_period_sinks
            , api_key
            , bootstrap_servers
            , input_processing_type
            , input_processing_parallel
            , input_processing_splitter
            , input_processing_target
            , compute_file_size
        ) VALUES (
            next_seq_, 'U'
            , NEW.object_ref
            , NEW.c_app_user_id
            , NEW.c_timestamp
            , NEW.c_process_ref
            , NEW.m_app_user_id
            , NEW.m_timestamp
            , NEW.m_process_ref
            , NEW.version
            , NEW.tenant_id
            , NEW.data_sink_id
            , NEW.is_active
            , NEW.comm_target_channel_type
            , NEW.comm_format_type
            , NEW.file_or_queue_name_pattern
            , NEW.compressed
            , NEW.originator_module
            , NEW.pre_transformer_name
            , NEW.comm_format_name
            , NEW.category
            , NEW.callback_ref
            , NEW.grid_id
            , NEW.output_encoding
            , NEW.generic_parameter1
            , NEW.generic_parameter2
            , NEW.camel_route
            , NEW.success_routing_strategy
            , NEW.success_dest_pattern
            , NEW.failed_routing_strategy
            , NEW.failure_dest_pattern
            , NEW.jaxb_context_path
            , NEW.environment
            , NEW.log_messages
            , NEW.is_input
            , NEW.csv_configuration_ref
            , NEW.base_class_pqon
            , NEW.xml_default_namespace
            , NEW.xml_root_element_name
            , NEW.xml_record_name
            , NEW.xml_namespace_prefix
            , NEW.write_tenant_id
            , NEW.description
            , NEW.max_number_of_records
            , NEW.import_queue_name
            , NEW.national_number_format
            , NEW.chunk_size
            , NEW.lazy_open
            , NEW.skip_zero_record_sink_refs
            , NEW.store_import_using_filepattern
            , NEW.unwrap_tracking
            , NEW.z
            , NEW.camel_execution
            , NEW.check_duplicate_filename
            , NEW.xml_namespace_mappings
            , NEW.copy_to_async_channel
            , NEW.camel_format_is_fmt_route
            , NEW.response_data_sink_ref
            , NEW.xml_header_elements
            , NEW.xml_footer_elements
            , NEW.lines_to_skip
            , NEW.single_line_comment
            , NEW.retention_period_files
            , NEW.retention_period_sinks
            , NEW.api_key
            , NEW.bootstrap_servers
            , NEW.input_processing_type
            , NEW.input_processing_parallel
            , NEW.input_processing_splitter
            , NEW.input_processing_target
            , NEW.compute_file_size
        );
        RETURN NEW;
    END IF;
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO p42_his_data_sinks (
            history_seq_ref
            , history_change_type
            , object_ref
            , c_app_user_id
            , c_timestamp
            , c_process_ref
            , m_app_user_id
            , m_timestamp
            , m_process_ref
            , version
            , tenant_id
            , data_sink_id
            , is_active
            , comm_target_channel_type
            , comm_format_type
            , file_or_queue_name_pattern
            , compressed
            , originator_module
            , pre_transformer_name
            , comm_format_name
            , category
            , callback_ref
            , grid_id
            , output_encoding
            , generic_parameter1
            , generic_parameter2
            , camel_route
            , success_routing_strategy
            , success_dest_pattern
            , failed_routing_strategy
            , failure_dest_pattern
            , jaxb_context_path
            , environment
            , log_messages
            , is_input
            , csv_configuration_ref
            , base_class_pqon
            , xml_default_namespace
            , xml_root_element_name
            , xml_record_name
            , xml_namespace_prefix
            , write_tenant_id
            , description
            , max_number_of_records
            , import_queue_name
            , national_number_format
            , chunk_size
            , lazy_open
            , skip_zero_record_sink_refs
            , store_import_using_filepattern
            , unwrap_tracking
            , z
            , camel_execution
            , check_duplicate_filename
            , xml_namespace_mappings
            , copy_to_async_channel
            , camel_format_is_fmt_route
            , response_data_sink_ref
            , xml_header_elements
            , xml_footer_elements
            , lines_to_skip
            , single_line_comment
            , retention_period_files
            , retention_period_sinks
            , api_key
            , bootstrap_servers
            , input_processing_type
            , input_processing_parallel
            , input_processing_splitter
            , input_processing_target
            , compute_file_size
        ) VALUES (
            next_seq_, 'D'
            , OLD.object_ref
            , OLD.c_app_user_id
            , OLD.c_timestamp
            , OLD.c_process_ref
            , OLD.m_app_user_id
            , OLD.m_timestamp
            , OLD.m_process_ref
            , OLD.version
            , OLD.tenant_id
            , OLD.data_sink_id
            , OLD.is_active
            , OLD.comm_target_channel_type
            , OLD.comm_format_type
            , OLD.file_or_queue_name_pattern
            , OLD.compressed
            , OLD.originator_module
            , OLD.pre_transformer_name
            , OLD.comm_format_name
            , OLD.category
            , OLD.callback_ref
            , OLD.grid_id
            , OLD.output_encoding
            , OLD.generic_parameter1
            , OLD.generic_parameter2
            , OLD.camel_route
            , OLD.success_routing_strategy
            , OLD.success_dest_pattern
            , OLD.failed_routing_strategy
            , OLD.failure_dest_pattern
            , OLD.jaxb_context_path
            , OLD.environment
            , OLD.log_messages
            , OLD.is_input
            , OLD.csv_configuration_ref
            , OLD.base_class_pqon
            , OLD.xml_default_namespace
            , OLD.xml_root_element_name
            , OLD.xml_record_name
            , OLD.xml_namespace_prefix
            , OLD.write_tenant_id
            , OLD.description
            , OLD.max_number_of_records
            , OLD.import_queue_name
            , OLD.national_number_format
            , OLD.chunk_size
            , OLD.lazy_open
            , OLD.skip_zero_record_sink_refs
            , OLD.store_import_using_filepattern
            , OLD.unwrap_tracking
            , OLD.z
            , OLD.camel_execution
            , OLD.check_duplicate_filename
            , OLD.xml_namespace_mappings
            , OLD.copy_to_async_channel
            , OLD.camel_format_is_fmt_route
            , OLD.response_data_sink_ref
            , OLD.xml_header_elements
            , OLD.xml_footer_elements
            , OLD.lines_to_skip
            , OLD.single_line_comment
            , OLD.retention_period_files
            , OLD.retention_period_sinks
            , OLD.api_key
            , OLD.bootstrap_servers
            , OLD.input_processing_type
            , OLD.input_processing_parallel
            , OLD.input_processing_splitter
            , OLD.input_processing_target
            , OLD.compute_file_size
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$p42_cfg_data_sinks_td$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS p42_cfg_data_sinks_tr ON p42_cfg_data_sinks;

CREATE TRIGGER p42_cfg_data_sinks_tr
    AFTER INSERT OR DELETE OR UPDATE ON p42_cfg_data_sinks
    FOR EACH ROW EXECUTE PROCEDURE p42_cfg_data_sinks_tp();
