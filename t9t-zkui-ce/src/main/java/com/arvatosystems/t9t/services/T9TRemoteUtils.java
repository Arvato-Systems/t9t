/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arvatosystems.t9t.services;

import java.util.Collections;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.tfi.general.Constants;
import com.arvatosystems.t9t.tfi.services.ReturnCodeException;
import com.arvatosystems.t9t.tfi.services.ServiceResponseException;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.search.LeanGroupedSearchRequest;
import com.arvatosystems.t9t.base.search.LeanSearchRequest;
import com.arvatosystems.t9t.base.search.ReadAll28Response;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.search.SearchRequest;
import com.arvatosystems.t9t.client.connection.IRemoteConnection;

import de.jpaw.bonaparte.converter.StringConverterEmptyToNull;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.FalseFilter;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.util.ToStringHelper;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ExceptionUtil;

/**
 * RemoteUtils.
 *
 * @author INCI02
 */
@Singleton
public class T9TRemoteUtils {
    private static final int SANITY_MAX_RECORDS_UI     =  1000;    // 26 is not enough, because some screens do front end pagination
    private static final int SANITY_MAX_RECORDS_LEAN   =  5000;    // lean search requests have a compact result, and some require more than 1000 entries
    private static final int SANITY_MAX_RECORDS_EXPORT = 10000;

    private static final Logger LOGGER = LoggerFactory.getLogger(T9TRemoteUtils.class);
    static {
        // configure output level for UI
        ToStringHelper.maxList = 3;
        ToStringHelper.maxMap = 3;
        ToStringHelper.maxSet = 3;
    }

    protected final IRemoteConnection statelessServiceSession = Jdp.getRequired(IRemoteConnection.class);

    @SuppressWarnings("unchecked")
    public <T extends ServiceResponse> T executeAndHandle(
        RequestParameters requestParameters,
        Class<T> serviceResponseClass) throws ReturnCodeException {

        try {
            ServiceResponse response = execute(requestParameters);
            handleServiceResponseErrorCode(response);
            return (T)response;
        } catch (ApplicationException e) {
            String causeChain = ExceptionUtil.causeChain(e);
            LOGGER.error("Execution exception: {}", causeChain);
            throw new ReturnCodeException(e.getErrorCode(), e.getMessage(), causeChain);
        }
    }

    public void executeExpectOk(RequestParameters requestParameters) throws ServiceResponseException {
        executeExpectOk(requestParameters, ServiceResponse.class);
    }

    public int executeReturnOkCode(RequestParameters requestParameters) throws ServiceResponseException {
        ServiceResponse resp = executeExpectOk(requestParameters, ServiceResponse.class);
        return resp.getReturnCode();  // this could be 0 or 1 or 2...
    }

    public void executeIgnoreErr(RequestParameters requestParameters, int errorToIgnore) {
        try {
            ServiceResponse response = execute(requestParameters);
            if (response.getReturnCode() != 0) {
                if (response.getReturnCode() == errorToIgnore) {
                    LOGGER.debug("return code {} (expected and ignored", errorToIgnore);
                } else {
                    LOGGER.error("Bad return code {} for {}: {} {}",
                            response.getReturnCode(),
                            requestParameters.ret$PQON(),
                            response.getErrorDetails(), response.getErrorMessage());
                }
            }
        } catch (ApplicationException e) {
            String causeChain = ExceptionUtil.causeChain(e);
            LOGGER.error("Execution application exception {}, caused by {}",
                    e.getErrorCode(), causeChain);
            // TODO: alert popup
        } catch (Exception e) {
            String causeChain = ExceptionUtil.causeChain(e);
            LOGGER.error("Execution exception: {}", causeChain);
            // TODO: alert popup
        }
    }

    public <T extends ServiceResponse> T executeExpectOk(
        RequestParameters requestParameters,
        Class<T> serviceResponseClass) throws ServiceResponseException {
        try {
            requestParameters.treeWalkString(new StringConverterEmptyToNull(), true);  // convert empty data to nulls
            requestParameters.validate();
            ServiceResponse response = execute(requestParameters);
            if (!ApplicationException.isOk(response.getReturnCode())) {
                LOGGER.error("Bad return code {} for {}: {} {}",
                        response.getReturnCode(),
                        requestParameters.ret$PQON(),
                        response.getErrorDetails(), response.getErrorMessage());
                throw new ServiceResponseException(response.getReturnCode(),  response.getErrorMessage(), response.getErrorDetails());
            }
            return (T)response;
        }
        catch (ServiceResponseException e) {
            throw e;
        } catch (ApplicationException e) {
            String causeChain = ExceptionUtil.causeChain(e);
            LOGGER.error("Execution application exception {}, caused by {}",
                    e.getErrorCode(), causeChain);
            throw new ServiceResponseException(e.getErrorCode(),  causeChain,null);
        } catch (Exception e) {
                String causeChain = ExceptionUtil.causeChain(e);
                throw new ServiceResponseException(Constants.ErrorCodes.GENERAL_EXCEPTION,  causeChain,null);
        }
    }

    /** Low level remote call.
     * @throws ApplicationException */
    private ServiceResponse execute(RequestParameters requestParameters) throws ApplicationException {
        ApplicationSession session = ApplicationSession.get();

        requestParameters.treeWalkString(new StringConverterEmptyToNull(), true);  // convert empty data to nulls
        requestParameters.validate();  // then check if it is a valid request
        ServiceResponse resp = null;
        if (requestParameters instanceof AuthenticationRequest) {
            AuthenticationRequest ar = (AuthenticationRequest)requestParameters;
            LOGGER.debug("Sending t9t AuthenticationRequest of type {} - session params {}",
                    ar.getAuthenticationParameters() == null ? "NO AUTHPARAMS (Bug!)" : ar.getAuthenticationParameters().ret$PQON(),
                    ar.getSessionParameters()        == null ? "null"                 : ar.getSessionParameters()
            );
             // not disclosing....         logRequest(requestParameters);  // DELETE ME
            resp = statelessServiceSession.executeAuthenticationRequest(ar);
        } else {
            // regular request - can log without disclosing details
            logRequest(requestParameters);
            if (requestParameters instanceof SearchCriteria) {
                SearchCriteria sc = (SearchCriteria)requestParameters;
                // first, check for "always false" condition, then never send it to the BE
                if (sc.getSearchFilter() != null && sc.getSearchFilter() instanceof FalseFilter) {
                    // no results desired
                    LOGGER.debug("Search on FalseFilter for {} - shortcut to empty result set", requestParameters.ret$PQON());
                    if (requestParameters instanceof SearchRequest) {
                        ReadAllResponse<BonaPortable, TrackingBase> emptyResp = new ReadAllResponse<>();
                        emptyResp.setDataList(Collections.emptyList());
                        return emptyResp;
                    } else {
                        ReadAll28Response<BonaPortable, TrackingBase> emptyResp = new ReadAll28Response<>();
                        emptyResp.setDataList(Collections.emptyList());
                        return emptyResp;
                    }
                }
                int maxAllowed = sc.getSearchOutputTarget() != null ? SANITY_MAX_RECORDS_EXPORT
                    : (requestParameters instanceof LeanSearchRequest || requestParameters instanceof LeanGroupedSearchRequest)
                    ? SANITY_MAX_RECORDS_LEAN : SANITY_MAX_RECORDS_UI;
                if (sc.getLimit() <= 0 || sc.getLimit() > maxAllowed) {
                    LOGGER.warn("Unlimited / huge search requested - limiting to {} entries...", maxAllowed);
                    sc.setLimit(maxAllowed);
                }
            }
            resp = statelessServiceSession.execute(session.getAuthorizationHeader(), requestParameters);
        }
        // resp.treeWalkString(new StringConverterNullToEmpty(), true);   // ZK can deal with nulls now
        if (resp instanceof AuthenticationResponse) {
            AuthenticationResponse ar = (AuthenticationResponse) resp;
            session.setJwt(ar.getEncodedJwt());
        }
        logResponse(resp);
        return resp;
    }

    private void logRequest(RequestParameters requestParameters) {
        if (LOGGER.isTraceEnabled()) {
            // extensive output
            LOGGER.trace(">>>Request  {}: {}", requestParameters.ret$PQON(), ToStringHelper.toStringML(requestParameters));
        } else if (LOGGER.isDebugEnabled()) {
            // medium output
            LOGGER.debug(">>>Request  {}", requestParameters);
        } else {
            // short output
            LOGGER.info(">>>Request  {}", requestParameters.ret$PQON());
        }
    }
    private void logResponse(ServiceResponse resp) {
        if (LOGGER.isTraceEnabled()) {
            // extensive output
            LOGGER.trace("<<<Response  {}: {}", resp.ret$PQON(), ToStringHelper.toStringML(resp));
        } else if (LOGGER.isDebugEnabled()) {
            // medium output
            LOGGER.debug("<<<Response  {}, code {}", resp.ret$PQON(), resp.getReturnCode());
        } else {
            // short output
            LOGGER.info("<<<Response  {}, code {}", resp.ret$PQON(), resp.getReturnCode());
        }
    }

    public void returnCodeExceptionHandler(String message, Exception e) throws ReturnCodeException {
        if (e instanceof ReturnCodeException) {
            StringBuilder stack = new StringBuilder();
            try {
                String[] stackFrames = ExceptionUtils.getStackFrames(e);
                for (String stackFrame : stackFrames) {
                    if (!stackFrame.trim().startsWith("at")) { // first line
                        stack.append(stackFrame).append("\n");
                    } else { // only the trace frames
                     // add only the frames that starts  with: com.arvatosystems
                        if (stackFrame.contains("com.arvatosystems") && !stackFrame.contains(T9TRemoteUtils.class.getName())) {
                            stack.append(stackFrame).append("\n");
                        }
                    }
                }
            } catch (Exception internalException) {
                LOGGER.error("Internal Exception - please check!!!", internalException);
                stack.append(ExceptionUtils.getStackTrace(e));
            }
            LOGGER.error("{} {}", message, stack);
            throw (ReturnCodeException) e;
        }
        LOGGER.error(message, e);
        throw new ReturnCodeException(Constants.ErrorCodes.GENERAL_EXCEPTION, e.getMessage() != null ? e.getMessage() : e.toString(), null);
    }



    protected void handleServiceResponseErrorCode(ServiceResponse response) throws ReturnCodeException {
        if (response == null) {
            String message = "Received response was null!";
            LOGGER.error(message);
            throw new IllegalArgumentException(message);
        }
        if ((response.getReturnCode() != 0) && (response.getReturnCode() != T9tException.PASSWORD_EXPIRED)) {
            LOGGER.error("Error: {} - Message: {} -- {}", response.getReturnCode(), response.getErrorMessage(), response.getErrorDetails());
            throw new ReturnCodeException(response.getReturnCode(), response.getErrorMessage(), response.getErrorDetails());
        }
    }
}
