/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceRequest;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.PermissionType;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableFactory;
import de.jpaw.bonaparte.core.DataConverter;
import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;
import de.jpaw.util.ApplicationException;
import jakarta.annotation.Nullable;

/**
 * Utility class in charge of providing common utility functionality to be used in the scope of the overall message processing.
 */
public final class MessagingUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingUtil.class);

    public static final boolean IS_MS_WINDOWS             = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
    public static final String HOSTNAME                   = stripHostname(System.getenv(IS_MS_WINDOWS ? "COMPUTERNAME" : "HOSTNAME"));
    public static final String JPAW_PACKAGE_PREFIX        = "de.jpaw";                          // all jpaw classes (some are needed for JSOn)
    public static final String BONAPARTE_PACKAGE_PREFIX   = "de.jpaw.bonaparte";                // just the bonaparte libraries
    public static final String TWENTYEIGHT_PACKAGE_PREFIX = "com.arvatosystems.t9t";            // prefix for t9t and a28

    private static final String DEFAULT_LANGUAGE = "en";

    public static final String[] PACKAGES_TO_SCAN_FOR_XENUMS = {
        JPAW_PACKAGE_PREFIX,
        TWENTYEIGHT_PACKAGE_PREFIX
    };

    private MessagingUtil() {
    }

    /**
     * Initialization method to install a suitable package prefix resolution for the Bonaparte parsers.
     *
     */
    public static void initializeBonaparteParsers() {
        BonaPortableFactory.addToPackagePrefixMap("t9t", TWENTYEIGHT_PACKAGE_PREFIX);               // and of course everything else starting with "t9t"
    }

    /** Get the list of languages to examine, with fallbacks. */
    public static String[] getLanguagesWithFallback(final String language) {
        return getLanguagesWithFallback(language, DEFAULT_LANGUAGE);
    }

    /** Get the list of languages to examine, with fallbacks. */
    public static String[] getLanguagesWithFallback(final String language, final String defaultLanguage) {
        if (language == null) {
            return new String[] { defaultLanguage };
        }
        final boolean isDefault = language.startsWith(defaultLanguage);

        if (language.length() <= 2) {
            return isDefault
                ? new String[] { defaultLanguage }
                : new String[] { language, defaultLanguage };
        } else if (language.length() <= 5) {
            return isDefault
                ? new String[] { language, language.substring(0, 2) }
                : new String[] { language, language.substring(0, 2), defaultLanguage };
        } else {
            return isDefault
                ? new String[] { language, language.substring(0, 5), language.substring(0, 2) }
                : new String[] { language, language.substring(0, 5), language.substring(0, 2), defaultLanguage };
        }
    }

    /** converts a request class PQON to the corresponding resource ID. */
    public static String toPerm(final String requestClassPqon) {
        return PermissionType.BACKEND.getToken() + "."
            + (requestClassPqon.endsWith("Request") ? requestClassPqon.substring(0, requestClassPqon.length() - 7) : requestClassPqon);
    }

    public static String truncField(final Object text, final int maxLength) {
        if (text == null)
            return null;
        return truncField(text.toString(), maxLength);
    }

    public static String truncField(final String text, final int maxLength) {
        return text == null || text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
    public static String truncErrorDetails(final String text) {
        return truncField(text, ServiceResponse.meta$$errorDetails.getLength());
    }
    public static String truncErrorMessage(final String text) {
        return truncField(text, ServiceResponse.meta$$errorMessage.getLength());
    }

    /** Normalize the line end characters (\r\n or \r to \n) so they can be compared in unit tests cross platform. */
    public static String normalizeEOLs(final String in) {
        return in.replaceAll("\\r\\n?", "\n");
    }

    /**
     * Method acts as object factory by creating new {@link ServiceRequest} instances based on the given input parameters and fills in default values.
     *
     * @param requestParameters
     *            The request parameters the service request shall include
     * @return The new {@link ServiceRequest} instance
     */
    public static ServiceRequest createServiceRequest(final RequestParameters requestParameters) {
        // Create and fill the service request
        final ServiceRequest srq = new ServiceRequest();
        srq.setRequestParameters(requestParameters);
        return srq;
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
    public static ServiceResponse createServiceResponse(final int errorCode, final String errorDetails, final UUID messageId, final String tenantId,
      final Long processRef) {
        final ServiceResponse response = createServiceResponse(errorCode, errorDetails);
        response.setMessageId(messageId);
        response.setTenantId(tenantId);
        response.setProcessRef(processRef);
        return response;
    }

    /** Create a ServiceResponse, using a provided error code (or OK). */
    public static ServiceResponse createServiceResponse(final int errorCode, final String errorDetails) {
        final ServiceResponse response = new ServiceResponse();
        if (errorCode > T9tConstants.MAX_OK_RETURN_CODE) {
            final String errorMessage = ApplicationException.codeToString(errorCode);
            LOGGER.error("returning error code " + errorCode + " with details " + errorDetails + " for reason " + errorMessage);
            response.setErrorMessage(truncErrorMessage(errorMessage));
        } else {
            LOGGER.info("returning OK response of code " + errorCode + ((errorDetails != null) ? " with details " + errorDetails : ""));
        }
        response.setErrorDetails(truncErrorDetails(errorDetails));
        response.setReturnCode(errorCode);
        return response;
    }

    /** Create a ServiceResponse, using a provided error code (or OK). */
    public static ServiceResponse createOk(final int returnCode) {
        final ServiceResponse response = new ServiceResponse();
        response.setReturnCode(returnCode);
        return response;
    }

    private static final DataConverter<String, AlphanumericElementaryDataItem> STRING_TRIMMER = new StringTrimmer();

    /** Strips any leading or trailing spaces. */
    public static void stringTrimmer(final BonaPortable data) {
        data.treeWalkString(STRING_TRIMMER, true);
    }

    /** Truncates a hostname (or K8s pod name) to at most 16 characters. */
    @Nullable
    public static String stripHostname(@Nullable final String hostnameIn) {
        if (hostnameIn == null || hostnameIn.length() <= 16) {
            return hostnameIn;
        }
        // must truncate: If this is a FQDN, strip the domain
        final int dotPosition = hostnameIn.indexOf('.');
        if (dotPosition > 0) {
            // return the unqualified hostname, or, in case that is also too long, the trailing chars of it (assumed to be more interesting than a fixed prefix)
            return dotPosition <= 16 ? hostnameIn.substring(0, dotPosition) : hostnameIn.substring(dotPosition - 16, dotPosition);
        } else {
            return hostnameIn.substring(hostnameIn.length() - 16, hostnameIn.length());
        }
    }
}
