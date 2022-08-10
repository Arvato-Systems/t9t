-- TBE-807: exchange of tenant discriminator columns (a28)

ALTER TABLE p28_cfg_auth_module_cfg ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_auth_module_cfg SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_auth_module_cfg.tenant_ref;
ALTER TABLE p28_cfg_auth_module_cfg ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_auth_module_cfg DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_auth_module_cfg ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_auth_module_cfg SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_auth_module_cfg.tenant_ref;
ALTER TABLE p28_his_auth_module_cfg ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_auth_module_cfg DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_tenant_logo ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_tenant_logo SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_tenant_logo.tenant_ref;
ALTER TABLE p28_cfg_tenant_logo ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_tenant_logo DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_tenant_logo ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_tenant_logo SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_tenant_logo.tenant_ref;
ALTER TABLE p28_his_tenant_logo ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_tenant_logo DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_cfg_roles ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_cfg_roles SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_cfg_roles.tenant_ref;
ALTER TABLE p42_cfg_roles ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_cfg_roles DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_his_roles ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_his_roles SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_his_roles.tenant_ref;
ALTER TABLE p42_his_roles ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_his_roles DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_cfg_users ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_cfg_users SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_cfg_users.tenant_ref;
ALTER TABLE p42_cfg_users ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_cfg_users DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_his_users ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_his_users SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_his_users.tenant_ref;
ALTER TABLE p42_his_users ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_his_users DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_cfg_role_to_permissions ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_cfg_role_to_permissions SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_cfg_role_to_permissions.tenant_ref;
ALTER TABLE p42_cfg_role_to_permissions ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_cfg_role_to_permissions DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_his_role_to_permissions ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_his_role_to_permissions SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_his_role_to_permissions.tenant_ref;
ALTER TABLE p42_his_role_to_permissions ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_his_role_to_permissions DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_cfg_user_tenant_roles ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_cfg_user_tenant_roles SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_cfg_user_tenant_roles.tenant_ref;
ALTER TABLE p42_cfg_user_tenant_roles ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_cfg_user_tenant_roles DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_his_user_tenant_roles ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_his_user_tenant_roles SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_his_user_tenant_roles.tenant_ref;
ALTER TABLE p42_his_user_tenant_roles ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_his_user_tenant_roles DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_cfg_api_key ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_cfg_api_key SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_cfg_api_key.tenant_ref;
ALTER TABLE p42_cfg_api_key ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_cfg_api_key DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_his_api_key ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_his_api_key SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_his_api_key.tenant_ref;
ALTER TABLE p42_his_api_key ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_his_api_key DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_dat_session ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_dat_session SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_dat_session.tenant_ref;
ALTER TABLE p28_dat_session ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_dat_session DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_dat_bpmn2_message_queue ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_dat_bpmn2_message_queue SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_dat_bpmn2_message_queue.tenant_ref;
ALTER TABLE p28_dat_bpmn2_message_queue ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_dat_bpmn2_message_queue DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_process_definition ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_process_definition SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_process_definition.tenant_ref;
ALTER TABLE p28_cfg_process_definition ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_process_definition DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_process_definition ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_process_definition SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_process_definition.tenant_ref;
ALTER TABLE p28_his_process_definition ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_process_definition DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_dat_process_exec_status ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_dat_process_exec_status SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_dat_process_exec_status.tenant_ref;
ALTER TABLE p28_dat_process_exec_status ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_dat_process_exec_status DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_dat_statistics ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_dat_statistics SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_dat_statistics.tenant_ref;
ALTER TABLE p42_dat_statistics ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_dat_statistics DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_dat_slice_tracking ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_dat_slice_tracking SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_dat_slice_tracking.tenant_ref;
ALTER TABLE p28_dat_slice_tracking ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_dat_slice_tracking DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_dat_bucket_counter ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_dat_bucket_counter SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_dat_bucket_counter.tenant_ref;
ALTER TABLE p28_dat_bucket_counter ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_dat_bucket_counter DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_dat_bucket_entry ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_dat_bucket_entry SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_dat_bucket_entry.tenant_ref;
ALTER TABLE p28_dat_bucket_entry ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_dat_bucket_entry DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_canned_request ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_canned_request SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_canned_request.tenant_ref;
ALTER TABLE p28_cfg_canned_request ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_canned_request DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_canned_request ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_canned_request SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_canned_request.tenant_ref;
ALTER TABLE p28_his_canned_request ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_canned_request DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_cfg_module_config ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_cfg_module_config SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_cfg_module_config.tenant_ref;
ALTER TABLE p42_cfg_module_config ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_cfg_module_config DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_his_module_config ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_his_module_config SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_his_module_config.tenant_ref;
ALTER TABLE p42_his_module_config ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_his_module_config DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_cfg_subscriber_config ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_cfg_subscriber_config SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_cfg_subscriber_config.tenant_ref;
ALTER TABLE p42_cfg_subscriber_config ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_cfg_subscriber_config DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_his_subscriber_config ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_his_subscriber_config SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_his_subscriber_config.tenant_ref;
ALTER TABLE p42_his_subscriber_config ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_his_subscriber_config DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_listener_config ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_listener_config SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_listener_config.tenant_ref;
ALTER TABLE p28_cfg_listener_config ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_listener_config DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_listener_config ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_listener_config SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_listener_config.tenant_ref;
ALTER TABLE p28_his_listener_config ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_listener_config DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_loaded_plugin ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_loaded_plugin SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_loaded_plugin.tenant_ref;
ALTER TABLE p28_cfg_loaded_plugin ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_loaded_plugin DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_loaded_plugin ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_loaded_plugin SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_loaded_plugin.tenant_ref;
ALTER TABLE p28_his_loaded_plugin ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_loaded_plugin DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_dat_plugin_log ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_dat_plugin_log SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_dat_plugin_log.tenant_ref;
ALTER TABLE p28_dat_plugin_log ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_dat_plugin_log DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_doc_module_cfg ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_doc_module_cfg SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_doc_module_cfg.tenant_ref;
ALTER TABLE p28_cfg_doc_module_cfg ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_doc_module_cfg DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_doc_module_cfg ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_doc_module_cfg SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_doc_module_cfg.tenant_ref;
ALTER TABLE p28_his_doc_module_cfg ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_doc_module_cfg DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_doc_config ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_doc_config SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_doc_config.tenant_ref;
ALTER TABLE p28_cfg_doc_config ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_doc_config DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_doc_config ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_doc_config SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_doc_config.tenant_ref;
ALTER TABLE p28_his_doc_config ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_doc_config DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_doc_email_cfg ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_doc_email_cfg SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_doc_email_cfg.tenant_ref;
ALTER TABLE p28_cfg_doc_email_cfg ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_doc_email_cfg DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_doc_email_cfg ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_doc_email_cfg SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_doc_email_cfg.tenant_ref;
ALTER TABLE p28_his_doc_email_cfg ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_doc_email_cfg DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_doc_template ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_doc_template SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_doc_template.tenant_ref;
ALTER TABLE p28_cfg_doc_template ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_doc_template DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_doc_template ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_doc_template SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_doc_template.tenant_ref;
ALTER TABLE p28_his_doc_template ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_doc_template DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_doc_component ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_doc_component SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_doc_component.tenant_ref;
ALTER TABLE p28_cfg_doc_component ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_doc_component DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_doc_component ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_doc_component SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_doc_component.tenant_ref;
ALTER TABLE p28_his_doc_component ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_doc_component DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_mailing_group ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_mailing_group SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_mailing_group.tenant_ref;
ALTER TABLE p28_cfg_mailing_group ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_mailing_group DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_mailing_group ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_mailing_group SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_mailing_group.tenant_ref;
ALTER TABLE p28_his_mailing_group ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_mailing_group DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_email_module_cfg ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_email_module_cfg SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_email_module_cfg.tenant_ref;
ALTER TABLE p28_cfg_email_module_cfg ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_email_module_cfg DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_email_module_cfg ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_email_module_cfg SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_email_module_cfg.tenant_ref;
ALTER TABLE p28_his_email_module_cfg ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_email_module_cfg DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_dat_email ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_dat_email SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_dat_email.tenant_ref;
ALTER TABLE p28_dat_email ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_dat_email DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_dat_email_attachments ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_dat_email_attachments SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_dat_email_attachments.tenant_ref;
ALTER TABLE p28_dat_email_attachments ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_dat_email_attachments DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_cfg_config ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_cfg_config SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_cfg_config.tenant_ref;
ALTER TABLE p42_cfg_config ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_cfg_config DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_his_config ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_his_config SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_his_config.tenant_ref;
ALTER TABLE p42_his_config ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_his_config DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_lean_grid_config ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_lean_grid_config SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_lean_grid_config.tenant_ref;
ALTER TABLE p28_cfg_lean_grid_config ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_lean_grid_config DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_lean_grid_config ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_lean_grid_config SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_lean_grid_config.tenant_ref;
ALTER TABLE p28_his_lean_grid_config ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_lean_grid_config DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_csv_configuration ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_csv_configuration SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_csv_configuration.tenant_ref;
ALTER TABLE p28_cfg_csv_configuration ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_csv_configuration DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_csv_configuration ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_csv_configuration SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_csv_configuration.tenant_ref;
ALTER TABLE p28_his_csv_configuration ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_csv_configuration DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_cfg_data_sinks ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_cfg_data_sinks SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_cfg_data_sinks.tenant_ref;
ALTER TABLE p42_cfg_data_sinks ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_cfg_data_sinks DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_his_data_sinks ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_his_data_sinks SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_his_data_sinks.tenant_ref;
ALTER TABLE p42_his_data_sinks ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_his_data_sinks DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_dat_sinks ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_dat_sinks SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_dat_sinks.tenant_ref;
ALTER TABLE p42_dat_sinks ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_dat_sinks DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_int_outbound_messages ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_int_outbound_messages SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_int_outbound_messages.tenant_ref;
ALTER TABLE p42_int_outbound_messages ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_int_outbound_messages DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_async_queue ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_async_queue SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_async_queue.tenant_ref;
ALTER TABLE p28_cfg_async_queue ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_async_queue DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_async_queue ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_async_queue SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_async_queue.tenant_ref;
ALTER TABLE p28_his_async_queue ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_async_queue DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_async_channel ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_async_channel SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_async_channel.tenant_ref;
ALTER TABLE p28_cfg_async_channel ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_async_channel DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_async_channel ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_async_channel SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_async_channel.tenant_ref;
ALTER TABLE p28_his_async_channel ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_async_channel DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_int_async_messages ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_int_async_messages SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_int_async_messages.tenant_ref;
ALTER TABLE p42_int_async_messages ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_int_async_messages DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_int_message ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_int_message SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_int_message.tenant_ref;
ALTER TABLE p28_int_message ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_int_message DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_dat_message_statistics ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_dat_message_statistics SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_dat_message_statistics.tenant_ref;
ALTER TABLE p28_dat_message_statistics ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_dat_message_statistics DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_cfg_report_config ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_cfg_report_config SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_cfg_report_config.tenant_ref;
ALTER TABLE p42_cfg_report_config ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_cfg_report_config DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_his_report_config ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_his_report_config SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_his_report_config.tenant_ref;
ALTER TABLE p42_his_report_config ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_his_report_config DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_cfg_report_params ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_cfg_report_params SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_cfg_report_params.tenant_ref;
ALTER TABLE p42_cfg_report_params ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_cfg_report_params DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p42_his_report_params ADD COLUMN tenant_id VARCHAR(16);
UPDATE p42_his_report_params SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p42_his_report_params.tenant_ref;
ALTER TABLE p42_his_report_params ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p42_his_report_params DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_solr_module_cfg ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_solr_module_cfg SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_solr_module_cfg.tenant_ref;
ALTER TABLE p28_cfg_solr_module_cfg ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_solr_module_cfg DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_solr_module_cfg ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_solr_module_cfg SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_solr_module_cfg.tenant_ref;
ALTER TABLE p28_his_solr_module_cfg ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_solr_module_cfg DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_scheduler_setup ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_scheduler_setup SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_scheduler_setup.tenant_ref;
ALTER TABLE p28_cfg_scheduler_setup ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_scheduler_setup DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_scheduler_setup ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_scheduler_setup SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_scheduler_setup.tenant_ref;
ALTER TABLE p28_his_scheduler_setup ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_scheduler_setup DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_voice_module_cfg ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_voice_module_cfg SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_voice_module_cfg.tenant_ref;
ALTER TABLE p28_cfg_voice_module_cfg ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_voice_module_cfg DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_voice_module_cfg ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_voice_module_cfg SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_voice_module_cfg.tenant_ref;
ALTER TABLE p28_his_voice_module_cfg ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_voice_module_cfg DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_voice_application ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_voice_application SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_voice_application.tenant_ref;
ALTER TABLE p28_cfg_voice_application ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_voice_application DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_voice_application ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_voice_application SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_voice_application.tenant_ref;
ALTER TABLE p28_his_voice_application ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_voice_application DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_voice_user ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_voice_user SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_voice_user.tenant_ref;
ALTER TABLE p28_cfg_voice_user ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_voice_user DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_voice_user ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_voice_user SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_voice_user.tenant_ref;
ALTER TABLE p28_his_voice_user ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_voice_user DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_cfg_voice_response ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_cfg_voice_response SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_cfg_voice_response.tenant_ref;
ALTER TABLE p28_cfg_voice_response ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_cfg_voice_response DROP COLUMN tenant_ref CASCADE;

ALTER TABLE p28_his_voice_response ADD COLUMN tenant_id VARCHAR(16);
UPDATE p28_his_voice_response SET tenant_id = p42_cfg_tenants.tenant_id FROM p42_cfg_tenants WHERE p42_cfg_tenants.object_ref = p28_his_voice_response.tenant_ref;
ALTER TABLE p28_his_voice_response ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE p28_his_voice_response DROP COLUMN tenant_ref CASCADE;
