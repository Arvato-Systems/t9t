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
package com.arvatosystems.t9t.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceRequest;
import com.arvatosystems.t9t.base.api.ServiceRequestHeader;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.PermissionType;

import de.jpaw.bonaparte.core.BonaPortableFactory;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ByteArray;

/**
 * Utility class in charge of providing common utility functionality to be used in the scope of the overall message processing.
 */
public final class MessagingUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingUtil.class);
    public static final String JPAW_PACKAGE_PREFIX        = "de.jpaw";                          // all jpaw classes (some are needed for JSOn)
    public static final String BONAPARTE_PACKAGE_PREFIX   = "de.jpaw.bonaparte";                // just the bonaparte libraries
    public static final String TWENTYEIGHT_PACKAGE_PREFIX = "com.arvatosystems.t9t";            // prefix for t9t and a28

    static private final String DEFAULT_LANGUAGE = "en";

    public static String [] PACKAGES_TO_SCAN_FOR_XENUMS = {
        JPAW_PACKAGE_PREFIX,
        TWENTYEIGHT_PACKAGE_PREFIX
    };

    /**
     * Initialization method to install a suitable package prefix resolution for the Bonaparte parsers.
     *
     */
    public static void initializeBonaparteParsers() {
        BonaPortableFactory.addToPackagePrefixMap("t9t", TWENTYEIGHT_PACKAGE_PREFIX);               // and of course everything else starting with "t9t"
    }

    /** Get the list of languages to examine, with fallbacks. */
    public static String [] getLanguagesWithFallback(String language) {
        return getLanguagesWithFallback(language, DEFAULT_LANGUAGE);
    }

    /** Get the list of languages to examine, with fallbacks. */
    public static String [] getLanguagesWithFallback(String language, String defaultLanguage) {
        if (language == null) {
            return new String[] { defaultLanguage };
        }
        final boolean isDefault = language.startsWith(defaultLanguage);

        if (language.length() <= 2) {
            return isDefault ?
                new String[] { defaultLanguage } :
                new String[] { language, defaultLanguage };
        } else if (language.length() <= 5) {
            return isDefault ?
                new String[] { language, language.substring(0, 2) } :
                new String[] { language, language.substring(0, 2), defaultLanguage };
        } else {
            return isDefault ?
                new String[] { language, language.substring(0, 5), language.substring(0, 2) } :
                new String[] { language, language.substring(0, 5), language.substring(0, 2), defaultLanguage };
        }
    }

    /** converts a request class PQON to the corresponding resource ID. */
    public static String toPerm(String requestClassPqon) {
        return PermissionType.BACKEND.getToken() + "." + (requestClassPqon.endsWith("Request") ? requestClassPqon.substring(0, requestClassPqon.length() - 7) : requestClassPqon);
    }

    public static String truncField(Object text, int maxLength) {
        if (text == null)
            return null;
        return truncField(text.toString(), maxLength);
    }

    public static String truncField(String text, int maxLength) {
        return text == null || text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
    public static String truncErrorDetails(String text) {
        return truncField(text, ServiceResponse.meta$$errorDetails.getLength());
    }
    public static String truncErrorMessage(String text) {
        return truncField(text, ServiceResponse.meta$$errorMessage.getLength());
    }

    /** Normalize the line end characters (\r\n or \r to \n) so they can be compared in unit tests cross platform. */
    public static String normalizeEOLs(String in) {
        return in.replaceAll("\\r\\n?", "\n");
    }

    /**
     * Method acts as object factory by creating new {@link ServiceRequest} instances based on the given input parameters and fills in default values.
     *
     * @param requestParameters
     *            The request parameters the service request shall include
     * @return The new {@link ServiceRequest} instance
     */
    public static ServiceRequest createServiceRequest(RequestParameters requestParameters) {
        // Create and fill the service request
        return new ServiceRequest(null, requestParameters, null);
    }

    /**
     * Methods handles the creation of error related {@link ServiceResponse} objects based on the given input parameters.
     * This method is to be used ONLY outside the context of the Messaging class.
     * The reason is that fields like ErrorMessage, TenantId, RequestId and ProcessRef are normally set centrally by that class
     * and manual setting is discouraged (and even leads to a warning message)
     *
     * @param errorCode
     *            The error code to include in the response
     */
    public static ServiceResponse createServiceResponse(int errorCode, String errorDetails, ByteArray messageId, Long processRef) {
        ServiceResponse response = new ServiceResponse();
        if (errorCode > 99999999) {
            String errorMessage = T9tException.codeToString(errorCode);
            LOGGER.error("returning error code " + errorCode + " with details " + errorDetails + " for reason " + errorMessage);
            response.setErrorMessage(truncField(errorMessage, ServiceResponse.meta$$errorMessage.getLength()));
        } else {
            LOGGER.info("returning OK response of code " + errorCode + ((errorDetails != null) ? " with details " + errorDetails : ""));
        }
        response.setErrorDetails(truncErrorDetails(errorDetails));
        response.setReturnCode(errorCode);
        response.setMessageId(messageId);
        response.setProcessRef(processRef);
        return response;
    }

    public static ServiceResponse createServiceResponse(int errorCode, String errorDetails, ServiceRequestHeader hdr) {
        return createServiceResponse(errorCode, errorDetails, hdr == null ? null : hdr.getMessageId(), null);
    }

    public static ServiceResponse createError(int errorCode, ServiceRequestHeader hdr, Long processRef) {
        ServiceResponse resp = new ServiceResponse();
        resp.setReturnCode(errorCode);
        resp.setProcessRef(processRef);
        resp.setMessageId(hdr.getMessageId());
        return resp;
    }

    public static ServiceResponse createError(ApplicationException e, String tenantId, ByteArray messageId, Long processRef) {
        ServiceResponse resp = new ServiceResponse();
        resp.setReturnCode(e.getErrorCode());
        resp.setErrorMessage(truncField(e.getMessage(), ServiceResponse.meta$$errorMessage.getLength()));
        resp.setTenantId(tenantId);
        resp.setProcessRef(processRef);
        if (messageId != null && messageId.length() <= ServiceResponse.meta$$messageId.getLength())
            resp.setMessageId(messageId);
        return resp;
    }
}
