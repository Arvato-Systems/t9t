/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.services.impl;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.IRemoteConnection;
import com.arvatosystems.t9t.base.StringTrimmer;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.search.LeanGroupedSearchRequest;
import com.arvatosystems.t9t.base.search.LeanSearchRequest;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.zkui.exceptions.ReturnCodeException;
import com.arvatosystems.t9t.zkui.exceptions.ServiceResponseException;
import com.arvatosystems.t9t.zkui.services.IT9tRemoteUtils;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.Constants;

import de.jpaw.bonaparte.converter.StringConverterEmptyToNull;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.DataConverter;
import de.jpaw.bonaparte.pojos.api.FalseFilter;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;
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
public class T9tRemoteUtils implements IT9tRemoteUtils {
    private static final int SANITY_MAX_RECORDS_UI     =  1000;    // 26 is not enough, because some screens do front end pagination
    private static final int SANITY_MAX_RECORDS_LEAN   =  5000;    // lean search requests have a compact result, and some require more than 1000 entries
    private static final int SANITY_MAX_RECORDS_EXPORT = 10000;

    private static final Logger LOGGER = LoggerFactory.getLogger(T9tRemoteUtils.class);
    private static final DataConverter<String, AlphanumericElementaryDataItem> STRING_TRIMMER = new StringTrimmer();

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
            throw new ReturnCodeException(e.getErrorCode(), e.getMessage(), null);
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
            requestParameters.treeWalkString(STRING_TRIMMER, true);  // trim fields, then convert empty data to nulls
            requestParameters.validate();
            ServiceResponse response = execute(requestParameters);
            if (!ApplicationException.isOk(response.getReturnCode())) {
                LOGGER.error("Bad return code {} for {}: {} {}",
                        response.getReturnCode(),
                        requestParameters.ret$PQON(),
                        response.getErrorDetails(), response.getErrorMessage());
                final boolean exposeDetails = isExposeDetailAllowed(response.getReturnCode());
                throw new ServiceResponseException(response.getReturnCode(),  response.getErrorMessage(),
                    exposeDetails ? response.getErrorDetails() : null);
            }
            return (T)response;
        } catch (ServiceResponseException e) {
            throw e;
        } catch (ApplicationException e) {
            String causeChain = ExceptionUtil.causeChain(e);
            LOGGER.error("Execution application exception {}, caused by {}", e.getErrorCode(), causeChain);
            throw new ServiceResponseException(e.getErrorCode(), null, null);  // do not expose stack traces to the user!
        } catch (Exception e) {
            String causeChain = ExceptionUtil.causeChain(e);
            throw new ServiceResponseException(Constants.ErrorCodes.GENERAL_EXCEPTION, null, null);  // do not expose stack traces to the user!
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
                    ReadAllResponse<BonaPortable, TrackingBase> emptyResp = new ReadAllResponse<>();
                    emptyResp.setDataList(Collections.emptyList());
                    return emptyResp;
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
        }
    }
    private void logResponse(ServiceResponse resp) {
        if (LOGGER.isTraceEnabled()) {
            // extensive output
            LOGGER.trace("<<<Response  {}: {}", resp.ret$PQON(), ToStringHelper.toStringML(resp));
        } else if (LOGGER.isDebugEnabled()) {
            // medium output
            LOGGER.debug("<<<Response  {}, code {}", resp.ret$PQON(), resp.getReturnCode());
        }
    }

    @Override
    public void returnCodeExceptionHandler(String message, Exception e) throws ReturnCodeException {
        LOGGER.error(message, e);
        throw new ReturnCodeException(Constants.ErrorCodes.GENERAL_EXCEPTION, e.getMessage() != null ? e.getMessage() : e.toString(), null);
    }

    protected void handleServiceResponseErrorCode(ServiceResponse response) throws ReturnCodeException {
        if (response == null) {
            String message = "Received response was null!";
            LOGGER.error(message);
            throw new IllegalArgumentException(message);
        }
        if (!ApplicationException.isOk(response.getReturnCode()) && (response.getReturnCode() != T9tException.PASSWORD_EXPIRED)) {
            LOGGER.error("Error: {} - Message: {} -- {}", response.getReturnCode(), response.getErrorMessage(), response.getErrorDetails());
            final boolean exposeDetails = isExposeDetailAllowed(response.getReturnCode());
            throw new ReturnCodeException(response.getReturnCode(), response.getErrorMessage(), exposeDetails ? response.getErrorDetails() : null);
        }
    }

    private boolean isExposeDetailAllowed(final int returnCode) {
        if (ApplicationException.isOk(returnCode)) {
            return true;
        }
        final int classification = returnCode / ApplicationException.CLASSIFICATION_FACTOR;
        switch (classification) {
        case ApplicationException.CL_INTERNAL_LOGIC_ERROR:
        case ApplicationException.CL_DATABASE_ERROR:
            return false;
        default:
            return true;
        }
    }
}
