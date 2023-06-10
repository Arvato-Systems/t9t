-- TBE-1012: new table p28_dat_async_message_statistics and new index on p42_int_async_messages

CREATE INDEX p42_int_async_messages_i2 ON p42_int_async_messages(
    c_timestamp
);


CREATE TABLE p28_dat_async_message_statistics (
    -- table columns of java class TrackingBase
    -- table columns of java class NoTracking
    -- table columns of java class InternalTenantId
      tenant_id varchar(16) NOT NULL
    -- table columns of java class CompositeKeyBase
    -- table columns of java class AsyncMessageStatisticsDTO
    , day date NOT NULL
    , hour integer NOT NULL
    , async_channel_id varchar(16) NOT NULL
    , ref_type varchar(4) NOT NULL
    , status varchar(1) NOT NULL
    , http_response_code integer NOT NULL
    , count integer NOT NULL
    , attempts integer NOT NULL
    , response_time bigint NOT NULL
);

ALTER TABLE p28_dat_async_message_statistics ADD CONSTRAINT p28_dat_async_message_statistics_pk PRIMARY KEY (
    day, hour, async_channel_id, ref_type, status, http_response_code
);

-- comments for columns of java class TrackingBase
-- comments for columns of java class NoTracking
-- comments for columns of java class InternalTenantId
COMMENT ON COLUMN p28_dat_async_message_statistics.tenant_id IS 'the multitenancy discriminator';
-- comments for columns of java class CompositeKeyBase
-- comments for columns of java class AsyncMessageStatisticsDTO
COMMENT ON COLUMN p28_dat_async_message_statistics.day IS 'date portion of cTimestamp of AsyncMessageDTO';
COMMENT ON COLUMN p28_dat_async_message_statistics.hour IS 'hour portion of cTimestamp of AsyncMessageDTO';
COMMENT ON COLUMN p28_dat_async_message_statistics.async_channel_id IS 'used to retrieve the URL and authentication parameters';
COMMENT ON COLUMN p28_dat_async_message_statistics.ref_type IS 'for debugging / maintenance: the type of reference';
COMMENT ON COLUMN p28_dat_async_message_statistics.status IS 'specifies if the message must still be sent or has been sent';
COMMENT ON COLUMN p28_dat_async_message_statistics.http_response_code IS 'if the remote returned some http response code, or 0 for none';
COMMENT ON COLUMN p28_dat_async_message_statistics.count IS 'the number of messages';
COMMENT ON COLUMN p28_dat_async_message_statistics.attempts IS 'number of send attempts so far (initially 0)';
COMMENT ON COLUMN p28_dat_async_message_statistics.response_time IS 'aggregated figures of lastResponseTime';
