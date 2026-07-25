-- Set deprecated field write_tenant_id to NULL in all related data sink tables.
-- The field is always false and will be removed in a future release.

UPDATE p42_cfg_data_sinks
    SET write_tenant_id = NULL
    WHERE write_tenant_id IS NOT NULL;

-- NOTE: the table "p42_his_data_sinks" is not modified to allow history/backward analysis!
