package com.arvatosystems.t9t.ariba.be.request;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.ariba.AribaException;
import com.arvatosystems.t9t.ariba.MetricsFlat001;
import com.arvatosystems.t9t.ariba.MetricsFlatImport;
import com.arvatosystems.t9t.ariba.request.ExportAribaMetricsRequest;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.out.be.jackson.T9tJacksonGenericSerializer;
import com.arvatosystems.t9t.out.jpa.impl.GenericRemoterViaDirectHttp;
import com.arvatosystems.t9t.out.services.IGenericRemoter;

import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

/**
 * Handles the export of metrics to Ariba by querying database views for metrics data
 *
 */
public class ExportAribaMetricsRequestHandler extends AbstractRequestHandler<ExportAribaMetricsRequest> {

    private final IGenericRemoter genericRemoter = getRemoterInstance();

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportAribaMetricsRequestHandler.class);

    private static final String METRICS_ID_VIEW_PATTERN = "^arv_[A-Za-z0-9_]+$"; // prevent SQL injection by allowing only specific prefixes

    private static final String SQL_SELECT = "SELECT contentId, metricsValue1, metricsValue2, metricsValue3, metricsValue4, metricsValue5, z, cTimestamp FROM ";
    private static final String SQL_WHERE = " WHERE tenantId = :tenantId AND cTimestamp >= :startDate";
    private static final String SQL_FUNCTION = "(:startDate, :tenantId)";

    private final Provider<PersistenceProviderJPA> jpaContextProvider = Jdp.getProvider(PersistenceProviderJPA.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, ExportAribaMetricsRequest req) throws Exception {

        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime startDateTime = now.minusHours(req.getMaxAgeInHours());
        final boolean isFunction = Boolean.TRUE.equals(req.getIsFunction());

        for (final String metricsId : req.getMetricsIds()) {

            if (!metricsId.matches(METRICS_ID_VIEW_PATTERN)) {
                LOGGER.error("Invalid metricsId: {}. Must match pattern {}", metricsId, METRICS_ID_VIEW_PATTERN);
                throw new T9tException(AribaException.INVALID_CONFIGURATION, "Invalid metricsId. Must match pattern " + METRICS_ID_VIEW_PATTERN + ", but is: " + metricsId);
            }

            LOGGER.info("Processing metric / viewId: {}", metricsId);
            final String sql = new StringBuilder(SQL_SELECT).append(metricsId).append(isFunction ? SQL_FUNCTION : SQL_WHERE).toString();

            final Query query = getEntityManager().createNativeQuery(sql);
            query.setParameter("tenantId", ctx.tenantId);
            query.setParameter("startDate", startDateTime);

            @SuppressWarnings("unchecked")
            final List<Object[]> queryResults = query.getResultList();

            if (queryResults.isEmpty()) {
                LOGGER.debug("No results found for metrics- / viewId: {} and startDateTime {}.", metricsId, startDateTime);
                continue; // Skip to the next metric if no results found
            } else {
                LOGGER.debug("Found {} results for metrics- / viewId: {} and startDateTime {}. Sending records to ariba.", queryResults.size(), metricsId, startDateTime);

                // Map query results to MetricsResult instances
                final List<MetricsFlat001> metricsResults = new ArrayList<>(queryResults.size());
                final LocalDateTime exportTimestamp = LocalDateTime.now();
                for (final Object[] row : queryResults) {
                    if (row.length != 8) { // 8 columns expected for MetricsFlat001, TODO: update in case of schema changes
                        throw new T9tException(AribaException.ARIBA_UNEXPECTED_RESULT_COLUMN_NUMBER, "Invalid number of columns in result set for metricsId: " + metricsId);
                    }

                    LocalDateTime recordTimestamp = null;
                    if (row[7] != null && row[7] instanceof LocalDateTime) {
                        recordTimestamp = (LocalDateTime) row[7];
                    } else if (row[7] != null && row[7] instanceof java.util.Date) {
                        recordTimestamp = ((java.util.Date) row[7]).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    } else {
                        LOGGER.warn("Unexpected type for timestamp in row: {}", row[7]);
                    }

                    metricsResults.add(new MetricsFlat001(
                         req.getSystemId(),
                         req.getSystemStage(),
                         metricsId,
                         recordTimestamp != null ? recordTimestamp : exportTimestamp,
                        (String) row[0], // contentId
                        (String) row[1], // metricsValue1
                        (String) row[2], // metricsValue2
                        (String) row[3], // metricsValue3
                        (String) row[4], // metricsValue4
                        (String) row[5], // metricsValue5
                        (String) row[6]  // z
                    ));
                }

                genericRemoter.send(new MetricsFlatImport(metricsResults));
            }
        }
        return ok();
    }

    private EntityManager getEntityManager() {
        return jpaContextProvider.get().getEntityManager();
    }

    /**
     * @return an instance of IGenericRemoter configured for Ariba metrics export.
     */
    private IGenericRemoter getRemoterInstance() {
        final T9tJacksonGenericSerializer serializer = new T9tJacksonGenericSerializer();
        return new GenericRemoterViaDirectHttp(// no constructor with minimal parameters available, so we use the full constructor. TODO review parameters
                "ARIBA",
                serializer::serialize,
                0,       // no max number of requests
                4,       // max number of parallel requests
                0L,      // no sleep time
                20_000L, // request timeout in ms
                20_000L  //  request timeout in ms
                );
    }
}
