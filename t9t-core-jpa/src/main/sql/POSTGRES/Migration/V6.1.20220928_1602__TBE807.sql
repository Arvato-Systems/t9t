-- TBE-807: 2nd amendment: also recreate indexes dropped by CASCADE
CREATE UNIQUE INDEX p42_cfg_roles_u1 ON p42_cfg_roles(
    tenant_id, role_id
);
CREATE INDEX p28_dat_bpmn2_message_queue_i1 ON p28_dat_bpmn2_message_queue(
    tenant_id, retry_counter
);
CREATE UNIQUE INDEX p28_cfg_process_definition_u1 ON p28_cfg_process_definition(
    tenant_id, process_definition_id
);
CREATE INDEX p28_dat_process_exec_status_i1 ON p28_dat_process_exec_status(
    tenant_id, yield_until, process_definition_id
);
CREATE UNIQUE INDEX p28_cfg_canned_request_u1 ON p28_cfg_canned_request(
    tenant_id, request_id
);
CREATE UNIQUE INDEX p28_cfg_listener_config_u1 ON p28_cfg_listener_config(
    tenant_id, classification
);
CREATE UNIQUE INDEX p28_cfg_loaded_plugin_u1 ON p28_cfg_loaded_plugin(
    tenant_id, plugin_id
);
CREATE UNIQUE INDEX p28_dat_slice_tracking_u1 ON p28_dat_slice_tracking(
    tenant_id, data_sink_id, id
);
CREATE UNIQUE INDEX p42_cfg_subscriber_config_u1 ON p42_cfg_subscriber_config(
    tenant_id, event_i_d, handler_class_name
);
CREATE UNIQUE INDEX p28_cfg_doc_component_u1 ON p28_cfg_doc_component(
    tenant_id, document_id, entity_id, country_code, currency_code, language_code
);
CREATE UNIQUE INDEX p28_cfg_doc_config_u1 ON p28_cfg_doc_config(
    tenant_id, document_id
);
CREATE UNIQUE INDEX p28_cfg_doc_email_cfg_u1 ON p28_cfg_doc_email_cfg(
    tenant_id, document_id, entity_id, country_code, currency_code, language_code
);
CREATE UNIQUE INDEX p28_cfg_doc_template_u1 ON p28_cfg_doc_template(
    tenant_id, document_id, entity_id, country_code, currency_code, language_code
);
CREATE UNIQUE INDEX p28_cfg_mailing_group_u1 ON p28_cfg_mailing_group(
    tenant_id, mailing_group_id
);
CREATE UNIQUE INDEX p28_cfg_lean_grid_config_u1 ON p28_cfg_lean_grid_config(
    tenant_id, grid_id, variant, user_ref
);
CREATE UNIQUE INDEX p42_cfg_config_u1 ON p42_cfg_config(
    tenant_id, config_group, config_key, generic_ref1, generic_ref2
);
CREATE UNIQUE INDEX p28_cfg_async_channel_u1 ON p28_cfg_async_channel(
    tenant_id, async_channel_id
);
CREATE UNIQUE INDEX p28_cfg_async_queue_u1 ON p28_cfg_async_queue(
    tenant_id, async_queue_id
);
CREATE UNIQUE INDEX p28_cfg_csv_configuration_u1 ON p28_cfg_csv_configuration(
    tenant_id, csv_configuration_id
);
CREATE UNIQUE INDEX p42_cfg_data_sinks_u1 ON p42_cfg_data_sinks(
    tenant_id, data_sink_id
);
CREATE INDEX p42_dat_sinks_i1 ON p42_dat_sinks(
    data_sink_ref, tenant_id, c_timestamp
);
CREATE INDEX p42_dat_sinks_i3 ON p42_dat_sinks(
    file_or_queue_name, tenant_id
);
CREATE INDEX p28_dat_message_statistics_i1 ON p28_dat_message_statistics(
    day, tenant_id, request_parameter_pqon, user_id
);
CREATE UNIQUE INDEX p42_cfg_report_config_u1 ON p42_cfg_report_config(
    tenant_id, report_config_id
);
CREATE UNIQUE INDEX p42_cfg_report_params_u1 ON p42_cfg_report_params(
    tenant_id, report_params_id
);
CREATE UNIQUE INDEX p28_cfg_scheduler_setup_u1 ON p28_cfg_scheduler_setup(
    tenant_id, scheduler_id
);
CREATE UNIQUE INDEX p28_cfg_voice_application_u1 ON p28_cfg_voice_application(
    tenant_id, application_id
);
CREATE UNIQUE INDEX p28_cfg_voice_response_u1 ON p28_cfg_voice_response(
    tenant_id, application_ref, language_code, key
);
CREATE UNIQUE INDEX p28_cfg_voice_user_u2 ON p28_cfg_voice_user(
    tenant_id, application_ref, provider_id
);
