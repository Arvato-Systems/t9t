-- TBE-1441 : T9t level ariba metrics views/functions

-- ariba metrics for workflows (t9t)

drop function if exists arv_workflows;

CREATE OR REPLACE FUNCTION arv_workflows(
    from_timestamp TIMESTAMP,
    filter_tenant_id TEXT
)
RETURNS TABLE (
    contentid TEXT,
    metricsvalue1 TEXT,
    metricsvalue2 TEXT,
    metricsvalue3 TEXT,
    metricsvalue4 TEXT,
    metricsvalue5 TEXT,
    z TEXT,
    tenantid TEXT,
    ctimestamp TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        'wf|step|code|user|err' AS contentid,
        p.process_definition_id ::text AS metricsvalue1,
        p.next_step ::text AS metricsvalue2,
        COALESCE(p.return_code, 0)::TEXT AS metricsvalue3,
        p.c_app_user_id ::text AS metricsvalue4,
        COUNT(*)::TEXT AS metricsvalue5,
        CASE
            WHEN LENGTH(STRING_AGG(DISTINCT p.error_details, E'\n')) > 1000
            THEN LEFT(STRING_AGG(DISTINCT p.error_details, E'\n'), 997) || '...'
            ELSE STRING_AGG(DISTINCT p.error_details, E'\n')
        END AS z,
        p.tenant_id ::text AS tenantid,
        MAX(p.c_timestamp) AS ctimestamp
    FROM
        p28_dat_process_exec_status p
    WHERE
        p.c_timestamp > from_timestamp
        AND p.tenant_id = filter_tenant_id
    GROUP BY
        p.process_definition_id,
        p.next_step,
        p.return_code,
        p.c_app_user_id,
        p.tenant_id;
END;
$$ LANGUAGE plpgsql;

-- ariba metrics for transactions (t9t)

drop function if exists arv_transactions_err;

CREATE OR REPLACE FUNCTION arv_transactions_err(
    from_timestamp TIMESTAMP,
    filter_tenant_id TEXT
)
RETURNS TABLE (
    contentId TEXT,
    metricsvalue1 TEXT,
    metricsvalue2 TEXT,
    metricsvalue3 TEXT,
    metricsvalue4 TEXT,
    metricsvalue5 TEXT,
    z TEXT,
    tenantid TEXT,
    ctimestamp TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        'pqon|code|orig-usr|avg|#'::TEXT AS contentId,
        request_parameter_pqon::TEXT AS metricsvalue1,
        return_code::TEXT AS metricsvalue2,
        (transaction_origin_type || '-' || user_id)::TEXT AS metricsvalue3,
        ROUND(AVG(processing_time_in_millisecs + processing_delay_in_millisecs))::TEXT AS metricsvalue4,
        COUNT(*)::TEXT AS metricsvalue5,
        CASE
            WHEN LENGTH(STRING_AGG(DISTINCT error_details, E'\n')) > 1000
            THEN LEFT(STRING_AGG(DISTINCT error_details, E'\n'), 997) || '...'
            ELSE STRING_AGG(DISTINCT error_details, E'\n')
        END AS z,
        tenant_id::TEXT AS tenantid,
        MAX(execution_started_at) AS ctimestamp
    FROM
        p28_int_message
    WHERE
        return_code > 0
        AND execution_started_at > from_timestamp
        AND tenant_id = filter_tenant_id
    GROUP BY
        request_parameter_pqon,
        return_code,
        user_id,
        transaction_origin_type,
        tenant_id;
END;
$$ LANGUAGE plpgsql;

-- ariba metrics for async messages (t9t)

drop function if exists arv_async_msg;

CREATE OR REPLACE FUNCTION arv_async_msg(
    from_timestamp TIMESTAMP,
    filter_tenant_id TEXT
)
RETURNS TABLE (
    contentid TEXT,
    metricsvalue1 TEXT,
    metricsvalue2 TEXT,
    metricsvalue3 TEXT,
    metricsvalue4 TEXT,
    metricsvalue5 TEXT,
    z TEXT,
    tenantid TEXT,
    ctimestamp TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        'chn-type|rspcode|avgRespT|avgAtmp|#' AS contentid,
        am.async_channel_id || ' - ' || am.ref_type AS metricsvalue1,
        coalesce(am.http_response_code,0)::TEXT AS metricsvalue2,
        ROUND(AVG(am.last_response_time))::TEXT AS metricsvalue3,
        ROUND(AVG(am.attempts))::TEXT AS metricsvalue4,
        COUNT(*)::TEXT AS metricsvalue5,
        CASE
            WHEN COALESCE(am.http_response_code, 0) >= 400 THEN
                CASE
                    WHEN LENGTH(STRING_AGG(DISTINCT am.error_details, E'\n')) > 1000
                    THEN LEFT(STRING_AGG(DISTINCT am.error_details, E'\n'), 997) || '...'
                    ELSE STRING_AGG(DISTINCT am.error_details, E'\n')
                END
            ELSE NULL
        END AS z,
        am.tenant_id::TEXT,
        MAX(am.c_timestamp) AS c_timestamp
    FROM
        p42_int_async_messages am
    WHERE
        am.c_timestamp > from_timestamp
        AND am.tenant_id = filter_tenant_id
    GROUP BY
        am.tenant_id,
        am.async_channel_id,
        am.ref_type,
        am.status,
        am.http_response_code
     order BY metricsvalue1, metricsvalue2
     ;
END;
$$ LANGUAGE plpgsql;
