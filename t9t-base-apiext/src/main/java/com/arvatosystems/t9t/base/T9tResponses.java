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

import com.arvatosystems.t9t.base.api.ServiceResponse;

import de.jpaw.util.ApplicationException;

/** A utility class to provide response messages. */
public class T9tResponses {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tResponses.class);

    /** Create a ServiceResponse, using a provided error code (or OK). */
    public static ServiceResponse createOk(int returnCode) {
        ServiceResponse response = new ServiceResponse();
        response.setReturnCode(returnCode);
        return response;
    }

    /** Create a ServiceResponse, using a provided error code (or OK). */
    public static ServiceResponse createServiceResponse(int errorCode, String errorDetails) {
        ServiceResponse response = new ServiceResponse();
        if (errorCode > T9tConstants.MAX_OK_RETURN_CODE) {
            String errorMessage = T9tException.codeToString(errorCode);
            LOGGER.error("returning error code " + errorCode + " with details " + errorDetails + " for reason " + errorMessage);
            response.setErrorMessage(MessagingUtil.truncErrorMessage(errorMessage));
        } else {
            LOGGER.info("returning OK response of code " + errorCode + ((errorDetails != null) ? " with details " + errorDetails : ""));
        }
        response.setErrorDetails(MessagingUtil.truncErrorDetails(errorDetails));
        response.setReturnCode(errorCode);
        return response;
    }

    /** Defines the error message contents based on some exception. */
    public static String errorMessage(Exception e) {
        if (e instanceof ApplicationException) {
            // application exception
            return MessagingUtil.truncErrorMessage(((ApplicationException)e).getLocalizedStandardDescription());
        } else {
            // general exception
            // TBD: hide details or return as much information as possible?  By security, we have to hide internal details! So we log it instead.
            LOGGER.error("General exception returned. Details are {}", e.getLocalizedMessage());
            // provide full stack trace to the log
            LOGGER.error("General error cause is: ", e);
            return MessagingUtil.truncErrorMessage(ApplicationException.codeToString(T9tException.GENERAL_EXCEPTION));
        }
    }
}
