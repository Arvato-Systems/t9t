package com.arvatosystems.t9t.jetty.kafka.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.authz.api.QuerySinglePermissionRequest;
import com.arvatosystems.t9t.authz.api.QuerySinglePermissionResponse;
import com.arvatosystems.t9t.base.IKafkaRequestTransmitter;
import com.arvatosystems.t9t.base.IRemoteConnection;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.jetty.impl.T9tRestProcessor;
import com.arvatosystems.t9t.rest.services.IT9tRestProcessor;
import com.arvatosystems.t9t.rest.utils.RestUtils;
import com.arvatosystems.t9t.xml.GenericResult;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.MessageParserException;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.dp.Specializes;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ExceptionUtil;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Singleton
@Specializes
public class T9tRestProcessorViaKafka extends T9tRestProcessor implements IT9tRestProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tRestProcessorViaKafka.class);

    protected final boolean enableKafka = RestUtils.checkIfSet("t9t.restapi.kafka", Boolean.FALSE);
    protected final IKafkaRequestTransmitter kafkaTransmitter = Jdp.getRequired(IKafkaRequestTransmitter.class);
    protected final IRemoteConnection connection = Jdp.getRequired(IRemoteConnection.class);

    protected final Cache<String, Boolean> authenticationCache = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES).<String, Boolean>build();

    @Override
    public boolean kafkaAvailable() {
        return enableKafka;
    }

    /**
     * Sends a request to the correct backend server node via kafka.
     *
     * @param authentication
     *          the contents of REST request's http <code>Authorization</code> parameter.
     *          Will be used as authentication in the backend. The permission is checked upfront (cached)
     *          in order to be able to provide immediate feedback to the caller about correct permissions.
     *          Also required in order to avoid DoS (unauthenticated callers filling the kafka topic).
     * @param partition
     *          a numeric value which will be used to determine the correct partition to use (will be taken
     *          modulus the actual number of partitions of the topic). Usually the hashCode of the customerId,
     *          orderId, or a productId or skuId.
     * @param request
     *          the request which should be processed.
     * @return the http response status code
     */
    protected Response.Status sendToServer(final String authentication, final String partitionKey, final RequestParameters request,
      final GenericResult payload) {
        // first, check if permissions exist
        if (authentication == null) {
            return Status.UNAUTHORIZED;
        }
        final String trimmedAuth = authentication.trim();
        if (trimmedAuth.length() == 0) {
            return Status.UNAUTHORIZED;
        }
        final Boolean isAuthenticated = authenticationCache.getIfPresent(trimmedAuth);
        if (Boolean.FALSE.equals(isAuthenticated)) {
            return Status.FORBIDDEN;
        }
        if (isAuthenticated == null) {
            // must query backend
            try {
                final QuerySinglePermissionRequest permRq = new QuerySinglePermissionRequest();
                permRq.setPermissionType(PermissionType.BACKEND);
                permRq.setResourceId(request.ret$PQON());
                final ServiceResponse resp = connection.execute(trimmedAuth, permRq);
                if (ApplicationException.isOk(resp.getReturnCode()) && resp instanceof QuerySinglePermissionResponse) {
                    final QuerySinglePermissionResponse qspr = (QuerySinglePermissionResponse)resp;
                    if (!qspr.getPermissions().contains(OperationType.EXECUTE)) {
                        // forbidden
                        authenticationCache.put(trimmedAuth, Boolean.FALSE);
                        return Status.FORBIDDEN;
                    }
                    // here: OK!
                    authenticationCache.put(trimmedAuth, Boolean.TRUE);
                    // fall through
                } else {
                    // some error
                    return Status.INTERNAL_SERVER_ERROR;
                }
            } catch (Exception e) {
                LOGGER.error("Cannot determine permissions:", e);
                return Status.INTERNAL_SERVER_ERROR;
            }
        }
        // at this point, we know it is authenticated
        kafkaTransmitter.write(request, partitionKey, request.getMessageId());
        // everything is fine!
        payload.setReturnCode(0);
        payload.setErrorMessage("OK");
        return Status.OK;
    }

    @Override
    public <T extends BonaPortable, R extends RequestParameters> void performAsyncBackendRequestViaKafka(final HttpHeaders httpHeaders,
        final AsyncResponse resp, final String pathInfo, final List<T> inputData, final Function<T, R> requestParameterConverter,
        final Function<R, String> partitionKeyExtractor) {
        if (!enableKafka || !kafkaTransmitter.initialized()) {
            // fall back to sync method
            super.performAsyncBackendRequest(httpHeaders, resp, pathInfo, inputData, requestParameterConverter, (Function<List<T>, RequestParameters>)null);
            return;
        }
        Response.Status result = Status.BAD_REQUEST;  // default response
        final GenericResult payload = new GenericResult();
        payload.setReturnCode(T9tException.GENERAL_EXCEPTION);
        payload.setErrorMessage(ApplicationException.codeToString(payload.getReturnCode()));
        if (T9tUtil.isEmpty(inputData)) {
            LOGGER.error("No data record posted in payload for {}", pathInfo);
            payload.setReturnCode(MessageParserException.EMPTY_BUT_REQUIRED_FIELD);
            payload.setErrorMessage(ApplicationException.codeToString(payload.getReturnCode()));
            payload.setErrorDetails("Data records null or empty list");
        } else if (inputData.size() != 1) {
            LOGGER.error("Too many records posted: {} for {}", inputData.size(), pathInfo);
            payload.setReturnCode(T9tException.TOO_MANY_RECORDS);
            payload.setErrorMessage(ApplicationException.codeToString(payload.getReturnCode()));
        } else {
            try {
                final R rq = requestParameterConverter.apply(inputData.get(0));
                final String partitionKey = partitionKeyExtractor.apply(rq);
                if (partitionKey == null) {
                    LOGGER.error("No partition key available for object in path {}", pathInfo);
                    payload.setReturnCode(MessageParserException.EMPTY_BUT_REQUIRED_FIELD);
                    payload.setErrorMessage(ApplicationException.codeToString(payload.getReturnCode()));
                    payload.setErrorDetails("Partition key");
                } else {
                    LOGGER.debug("Sending object of key {} via path {}", partitionKey, pathInfo);
                    result = sendToServer(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION), partitionKey, rq, payload);
                }
            } catch (final ApplicationException e) {
                LOGGER.error("Exception during request conversion for {}: {}: {}", pathInfo, e.getMessage(), ExceptionUtil.causeChain(e));
                LOGGER.error("Return code = {}, details = {}", e.getErrorCode(), e.getMessage());
                payload.setReturnCode(e.getErrorCode());
                payload.setErrorMessage(e.getMessage());
            } catch (final Exception e) {
                LOGGER.error("Exception during request conversion for {}: {}: {}", pathInfo, e.getMessage(), ExceptionUtil.causeChain(e));
            }
        }
        // create a response from the status we got (we never return a payload except a possible generic response)
        returnAsyncResult(httpHeaders.getHeaderString(HttpHeaders.ACCEPT), resp, result, payload);
    }
}
