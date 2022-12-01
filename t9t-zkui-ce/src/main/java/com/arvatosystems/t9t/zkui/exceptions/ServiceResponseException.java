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
package com.arvatosystems.t9t.zkui.exceptions;

import com.arvatosystems.t9t.zkui.util.Constants;


public class ServiceResponseException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1105145029339557495L;
    private int returnCode =  Constants.ErrorCodes.RETURN_CODE_SUCCESS;
    private String returnMessage = null;
    private String errorDetails = null;

    public ServiceResponseException(int returnCode, String returnMessage, String errorDetails) {
        super("Returncode: " + returnCode + " -- " + returnMessage + " -- " + errorDetails);
        this.returnCode = returnCode;
        this.returnMessage = returnMessage;
        this.errorDetails = errorDetails;
    }

    /**
     * @return the returnCode
     */
    public int getReturnCode() {
        return returnCode;
    }


    public final String getReturnMessage() {
        return returnMessage;
    }

    public String getErrorDetails() {
        return errorDetails;
    }



    public final ServiceResponseException getReturnCodeMessage() {
        ServiceResponseException errorMessage = null;
        if (this.returnCode != Constants.ErrorCodes.RETURN_CODE_SUCCESS) {
            errorMessage = this;
        }
        return errorMessage;
    }
}
