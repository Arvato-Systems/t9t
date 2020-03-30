-- TBE-241 add response data sink

DROP VIEW IF EXISTS p42_cfg_data_sinks_nt;
DROP VIEW IF EXISTS p42_cfg_data_sinks_v;

ALTER TABLE p42_cfg_data_sinks ADD column response_data_sink_ref bigint;
ALTER TABLE p42_his_data_sinks ADD column response_data_sink_ref bigint;

CREATE OR REPLACE VIEW p42_cfg_data_sinks_nt AS SELECT
    -- columns of java class InternalTenantRef42
    t0.tenant_ref AS tenant_ref
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
FROM p42_cfg_data_sinks t0;

CREATE OR REPLACE VIEW p42_cfg_data_sinks_v AS SELECT
    -- columns of java class TrackingBase
    -- columns of java class WriteTracking
    t0.c_tech_user_id AS c_tech_user_id
    , t0.c_app_user_id AS c_app_user_id
    , t0.c_timestamp AS c_timestamp
    , t0.c_process_ref AS c_process_ref
    -- columns of java class FullTracking
    , t0.m_tech_user_id AS m_tech_user_id
    , t0.m_app_user_id AS m_app_user_id
    , t0.m_timestamp AS m_timestamp
    , t0.m_process_ref AS m_process_ref
    -- columns of java class FullTrackingWithVersion
    , t0.version AS version
    -- columns of java class InternalTenantRef42
    , t0.tenant_ref AS tenant_ref
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
FROM p42_cfg_data_sinks t0;
