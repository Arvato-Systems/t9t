-- TBE-1321 - new columns in p42_cfg_data_sinks (POSTGRES)

DROP VIEW IF EXISTS p42_cfg_data_sinks_nt;
DROP VIEW IF EXISTS p42_cfg_data_sinks_v;


ALTER TABLE p42_cfg_data_sinks ADD COLUMN IF NOT EXISTS encryption_id varchar(16);

ALTER TABLE p42_his_data_sinks ADD COLUMN encryption_id varchar(16);

COMMENT ON COLUMN p42_cfg_data_sinks.encryption_id IS 'if specified, then the output will be encrypted. The configuration sits in the server.xml file';


CREATE OR REPLACE VIEW p42_cfg_data_sinks_nt AS SELECT
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class DataSinkRef
    -- columns of java class DataSinkDTO
    , t0.data_sink_id AS data_sink_id
    , t0.is_active AS is_active
    , CommunicationTargetChannelType2s(t0.comm_target_channel_type) AS comm_target_channel_type
    , t0.comm_format_type AS comm_format_type
    , t0.file_or_queue_name_pattern AS file_or_queue_name_pattern
    , t0.compressed AS compressed
    , t0.originator_module AS originator_module
    , t0.pre_transformer_name AS pre_transformer_name
    , t0.comm_format_name AS comm_format_name
    , t0.category AS category
    , t0.callback_ref AS callback_ref
    , t0.grid_id AS grid_id
    , t0.output_encoding AS output_encoding
    , t0.generic_parameter1 AS generic_parameter1
    , t0.generic_parameter2 AS generic_parameter2
    , t0.camel_route AS camel_route
    , CamelPostProcStrategy2s(t0.success_routing_strategy) AS success_routing_strategy
    , t0.success_dest_pattern AS success_dest_pattern
    , CamelPostProcStrategy2s(t0.failed_routing_strategy) AS failed_routing_strategy
    , t0.failure_dest_pattern AS failure_dest_pattern
    , t0.jaxb_context_path AS jaxb_context_path
    , t0.environment AS environment
    , t0.log_messages AS log_messages
    , t0.is_input AS is_input
    , t0.csv_configuration_ref AS csv_configuration_ref
    , t0.base_class_pqon AS base_class_pqon
    , t0.xml_default_namespace AS xml_default_namespace
    , t0.xml_root_element_name AS xml_root_element_name
    , t0.xml_record_name AS xml_record_name
    , t0.xml_namespace_prefix AS xml_namespace_prefix
    , t0.write_tenant_id AS write_tenant_id
    , t0.description AS description
    , t0.max_number_of_records AS max_number_of_records
    , t0.import_queue_name AS import_queue_name
    , t0.national_number_format AS national_number_format
    , t0.chunk_size AS chunk_size
    , t0.lazy_open AS lazy_open
    , t0.skip_zero_record_sink_refs AS skip_zero_record_sink_refs
    , t0.store_import_using_filepattern AS store_import_using_filepattern
    , t0.unwrap_tracking AS unwrap_tracking
    , t0.z AS z
    , CamelExecutionScheduleType2s(t0.camel_execution) AS camel_execution
    , t0.check_duplicate_filename AS check_duplicate_filename
    , t0.xml_namespace_mappings AS xml_namespace_mappings
    , t0.copy_to_async_channel AS copy_to_async_channel
    , t0.camel_format_is_fmt_route AS camel_format_is_fmt_route
    , t0.response_data_sink_ref AS response_data_sink_ref
    , t0.xml_header_elements AS xml_header_elements
    , t0.xml_footer_elements AS xml_footer_elements
    , t0.lines_to_skip AS lines_to_skip
    , t0.single_line_comment AS single_line_comment
    , t0.retention_period_files AS retention_period_files
    , t0.retention_period_sinks AS retention_period_sinks
    , t0.api_key AS api_key
    , t0.bootstrap_servers AS bootstrap_servers
    , InputProcessingType2s(t0.input_processing_type) AS input_processing_type
    , t0.input_processing_parallel AS input_processing_parallel
    , t0.input_processing_splitter AS input_processing_splitter
    , t0.input_processing_target AS input_processing_target
    , t0.compute_file_size AS compute_file_size
    , t0.json_write_pqon AS json_write_pqon
    , t0.json_write_nulls AS json_write_nulls
    , t0.json_use_enum_tokens AS json_use_enum_tokens
    , t0.write_header_row AS write_header_row
    , t0.buffer_size AS buffer_size
    , t0.record_size AS record_size
    , t0.encryption_id AS encryption_id
FROM p42_cfg_data_sinks t0;

CREATE OR REPLACE VIEW p42_cfg_data_sinks_v AS SELECT
    -- columns of java class TrackingBase
    -- columns of java class WriteTracking
    t0.c_app_user_id AS c_app_user_id
    , t0.c_timestamp AS c_timestamp
    , t0.c_process_ref AS c_process_ref
    -- columns of java class FullTracking
    , t0.m_app_user_id AS m_app_user_id
    , t0.m_timestamp AS m_timestamp
    , t0.m_process_ref AS m_process_ref
    -- columns of java class FullTrackingWithVersion
    , t0.version AS version
    -- columns of java class InternalTenantId
    , t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class DataSinkRef
    -- columns of java class DataSinkDTO
    , t0.data_sink_id AS data_sink_id
    , t0.is_active AS is_active
    , CommunicationTargetChannelType2s(t0.comm_target_channel_type) AS comm_target_channel_type
    , t0.comm_format_type AS comm_format_type
    , t0.file_or_queue_name_pattern AS file_or_queue_name_pattern
    , t0.compressed AS compressed
    , t0.originator_module AS originator_module
    , t0.pre_transformer_name AS pre_transformer_name
    , t0.comm_format_name AS comm_format_name
    , t0.category AS category
    , t0.callback_ref AS callback_ref
    , t0.grid_id AS grid_id
    , t0.output_encoding AS output_encoding
    , t0.generic_parameter1 AS generic_parameter1
    , t0.generic_parameter2 AS generic_parameter2
    , t0.camel_route AS camel_route
    , CamelPostProcStrategy2s(t0.success_routing_strategy) AS success_routing_strategy
    , t0.success_dest_pattern AS success_dest_pattern
    , CamelPostProcStrategy2s(t0.failed_routing_strategy) AS failed_routing_strategy
    , t0.failure_dest_pattern AS failure_dest_pattern
    , t0.jaxb_context_path AS jaxb_context_path
    , t0.environment AS environment
    , t0.log_messages AS log_messages
    , t0.is_input AS is_input
    , t0.csv_configuration_ref AS csv_configuration_ref
    , t0.base_class_pqon AS base_class_pqon
    , t0.xml_default_namespace AS xml_default_namespace
    , t0.xml_root_element_name AS xml_root_element_name
    , t0.xml_record_name AS xml_record_name
    , t0.xml_namespace_prefix AS xml_namespace_prefix
    , t0.write_tenant_id AS write_tenant_id
    , t0.description AS description
    , t0.max_number_of_records AS max_number_of_records
    , t0.import_queue_name AS import_queue_name
    , t0.national_number_format AS national_number_format
    , t0.chunk_size AS chunk_size
    , t0.lazy_open AS lazy_open
    , t0.skip_zero_record_sink_refs AS skip_zero_record_sink_refs
    , t0.store_import_using_filepattern AS store_import_using_filepattern
    , t0.unwrap_tracking AS unwrap_tracking
    , t0.z AS z
    , CamelExecutionScheduleType2s(t0.camel_execution) AS camel_execution
    , t0.check_duplicate_filename AS check_duplicate_filename
    , t0.xml_namespace_mappings AS xml_namespace_mappings
    , t0.copy_to_async_channel AS copy_to_async_channel
    , t0.camel_format_is_fmt_route AS camel_format_is_fmt_route
    , t0.response_data_sink_ref AS response_data_sink_ref
    , t0.xml_header_elements AS xml_header_elements
    , t0.xml_footer_elements AS xml_footer_elements
    , t0.lines_to_skip AS lines_to_skip
    , t0.single_line_comment AS single_line_comment
    , t0.retention_period_files AS retention_period_files
    , t0.retention_period_sinks AS retention_period_sinks
    , t0.api_key AS api_key
    , t0.bootstrap_servers AS bootstrap_servers
    , InputProcessingType2s(t0.input_processing_type) AS input_processing_type
    , t0.input_processing_parallel AS input_processing_parallel
    , t0.input_processing_splitter AS input_processing_splitter
    , t0.input_processing_target AS input_processing_target
    , t0.compute_file_size AS compute_file_size
    , t0.json_write_pqon AS json_write_pqon
    , t0.json_write_nulls AS json_write_nulls
    , t0.json_use_enum_tokens AS json_use_enum_tokens
    , t0.write_header_row AS write_header_row
    , t0.buffer_size AS buffer_size
    , t0.record_size AS record_size
    , t0.encryption_id AS encryption_id
FROM p42_cfg_data_sinks t0;

-- This source has been automatically created by the bonaparte DSL (bonaparte.jpa addon). Do not modify, changes will be lost.
-- The bonaparte DSL is open source, licensed under Apache License, Version 2.0. It is based on Eclipse Xtext2.
-- The sources for bonaparte-DSL can be obtained at www.github.com/jpaw/bonaparte-dsl.git

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
            , json_write_pqon
            , json_write_nulls
            , json_use_enum_tokens
            , write_header_row
            , buffer_size
            , record_size
            , encryption_id
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
            , NEW.json_write_pqon
            , NEW.json_write_nulls
            , NEW.json_use_enum_tokens
            , NEW.write_header_row
            , NEW.buffer_size
            , NEW.record_size
            , NEW.encryption_id
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
            , json_write_pqon
            , json_write_nulls
            , json_use_enum_tokens
            , write_header_row
            , buffer_size
            , record_size
            , encryption_id
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
            , NEW.json_write_pqon
            , NEW.json_write_nulls
            , NEW.json_use_enum_tokens
            , NEW.write_header_row
            , NEW.buffer_size
            , NEW.record_size
            , NEW.encryption_id
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
            , json_write_pqon
            , json_write_nulls
            , json_use_enum_tokens
            , write_header_row
            , buffer_size
            , record_size
            , encryption_id
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
            , OLD.json_write_pqon
            , OLD.json_write_nulls
            , OLD.json_use_enum_tokens
            , OLD.write_header_row
            , OLD.buffer_size
            , OLD.record_size
            , OLD.encryption_id
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

