-- Table
CREATE TABLE p28_dat_statistics_aggregation (
    -- table columns of java class TrackingBase
    -- table columns of java class NoTracking
    -- table columns of java class InternalTenantId
      tenant_id varchar(16) NOT NULL
    -- table columns of java class AbstractRef
    -- table columns of java class Ref
    , object_ref bigint NOT NULL
    -- table columns of java class StatisticsAggregationRef
    -- table columns of java class StatisticsAggregationDTO
    , process_id varchar(30) NOT NULL
    , slot_start timestamp(0) NOT NULL
    , number_of_runs integer NOT NULL
    , records_processed integer NOT NULL
    , records_error integer NOT NULL
    , count1 integer NOT NULL
    , count2 integer NOT NULL
    , count3 integer NOT NULL
    , count4 integer NOT NULL
);

ALTER TABLE p28_dat_statistics_aggregation ADD CONSTRAINT p28_dat_statistics_aggregation_pk PRIMARY KEY (
    object_ref
);
CREATE UNIQUE INDEX p28_dat_statistics_aggregation_u1 ON p28_dat_statistics_aggregation(
    tenant_id, process_id, slot_start
);
CREATE INDEX p28_dat_statistics_aggregation_i2 ON p28_dat_statistics_aggregation(
    slot_start
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class NoTracking
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_dat_statistics_aggregation.tenant_id IS 'the multitenancy discriminator';
-- comments for columns of java class AbstractRef
-- comments for columns of java class Ref
COMMENT ON COLUMN p28_dat_statistics_aggregation.object_ref IS 'objectRef, as a primary key it cannot be changed and, if persisted, is never null';
-- comments for columns of java class StatisticsAggregationRef
-- comments for columns of java class StatisticsAggregationDTO
COMMENT ON COLUMN p28_dat_statistics_aggregation.slot_start IS 'beginning of time slot';
COMMENT ON COLUMN p28_dat_statistics_aggregation.number_of_runs IS 'how many records of StatisticsDTO have been aggregated';
COMMENT ON COLUMN p28_dat_statistics_aggregation.records_processed IS 'number of processed records';
COMMENT ON COLUMN p28_dat_statistics_aggregation.records_error IS 'number of records for which processing failed';
COMMENT ON COLUMN p28_dat_statistics_aggregation.count1 IS 'process specific informations';

-- Sequence
CREATE SEQUENCE p28_dat_statistics_aggregation_s;

-- Views
CREATE OR REPLACE VIEW p28_dat_statistics_aggregation_nt AS SELECT
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class StatisticsAggregationRef
    -- columns of java class StatisticsAggregationDTO
    , t0.process_id AS process_id
    , t0.slot_start AS slot_start
    , t0.number_of_runs AS number_of_runs
    , t0.records_processed AS records_processed
    , t0.records_error AS records_error
    , t0.count1 AS count1
    , t0.count2 AS count2
    , t0.count3 AS count3
    , t0.count4 AS count4
FROM p28_dat_statistics_aggregation t0;

CREATE OR REPLACE VIEW p28_dat_statistics_aggregation_v AS SELECT
    -- columns of java class TrackingBase
    -- columns of java class NoTracking
    -- columns of java class InternalTenantId
    t0.tenant_id AS tenant_id
    -- columns of java class AbstractRef
    -- columns of java class Ref
    , t0.object_ref AS object_ref
    -- columns of java class StatisticsAggregationRef
    -- columns of java class StatisticsAggregationDTO
    , t0.process_id AS process_id
    , t0.slot_start AS slot_start
    , t0.number_of_runs AS number_of_runs
    , t0.records_processed AS records_processed
    , t0.records_error AS records_error
    , t0.count1 AS count1
    , t0.count2 AS count2
    , t0.count3 AS count3
    , t0.count4 AS count4
FROM p28_dat_statistics_aggregation t0;
