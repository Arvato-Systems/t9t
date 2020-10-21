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
package com.arvatosystems.t9t.tfi.viewmodel;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.UiException;

import com.arvatosystems.t9t.tfi.general.Constants;
import com.arvatosystems.t9t.tfi.model.bean.ErrorPopupEntity;
import com.arvatosystems.t9t.tfi.services.ReturnCodeException;
import com.arvatosystems.t9t.tfi.services.ServiceResponseException;
import com.arvatosystems.t9t.tfi.web.ZulUtils;

public class ErrorViewModel {

    ErrorPopupEntity paramGeneralErrorMessage = null;

    public ErrorViewModel() {
        Exception exception = (Exception) Executions.getCurrent().getAttribute("javax.servlet.error.exception");

        if (exception instanceof UiException) {
            UiException e = (UiException) exception;
            if (e.getCause() instanceof ReturnCodeException) {
                paramGeneralErrorMessage = ZulUtils.getErrorPopupInfo((ReturnCodeException) e.getCause());
            } else {
                String msg = "";
                if (e.getCause() != null)
                    msg = e.getCause().getMessage();
                else
                    msg = e.getMessage();
                ReturnCodeException rce = new ReturnCodeException(Constants.ErrorCodes.GENERAL_EXCEPTION, msg, null);
                paramGeneralErrorMessage = ZulUtils.getErrorPopupInfo(rce);

            }
        } else {
            ReturnCodeException rce = null;
            if (exception instanceof ServiceResponseException) {
                ServiceResponseException sre = (ServiceResponseException) exception;
                rce = new ReturnCodeException(sre.getReturnCode(), sre.getReturnMessage(), sre.getErrorDetails());
            } else {
                rce = new ReturnCodeException(Constants.ErrorCodes.GENERAL_EXCEPTION, exception.getMessage(), null);
            }
            paramGeneralErrorMessage = ZulUtils.getErrorPopupInfo(rce);

        }
    }

    public ErrorPopupEntity getParamGeneralErrorMessage() {
        return paramGeneralErrorMessage;
    }

    public void setParamGeneralErrorMessage(ErrorPopupEntity paramGeneralErrorMessage) {
        this.paramGeneralErrorMessage = paramGeneralErrorMessage;
    }

}
