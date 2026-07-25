DO $$
BEGIN
    BEGIN
        ALTER TABLE p42_his_tenants ALTER COLUMN object_ref DROP NOT NULL;
    EXCEPTION
        WHEN undefined_column THEN RAISE NOTICE 'Column object_ref does not exist in table p42_his_tenants. Skipping.';
    END;
END $$;
