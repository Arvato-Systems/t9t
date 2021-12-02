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
/**
 *
 */
package com.arvatosystems.t9t.zkui.viewmodel.support;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Clients;

import com.arvatosystems.t9t.zkui.exceptions.ReturnCodeException;
import com.arvatosystems.t9t.zkui.util.Constants;
import com.arvatosystems.t9t.zkui.util.ZulUtils;

/**
 * @author kamp13
 */
public abstract class AbstractViewModel extends FormValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractViewModel.class);

    protected ReturnCodeException errorMessage;
    protected String                               confirmMessage;
    protected String                               confirmWithCancelMessage;

    // //////////////////////////////////////////////////////////////////
    // Validators
    // //////////////////////////////////////////////////////////////////

    public Validator getNullValidator() {
        return new AbstractValidator() {
            @Override
            public void validate(final ValidationContext ctx) {
                final Boolean isValidationRequired = (Boolean) ctx.getBindContext().getValidatorArg("isValidationRequired");
                //boolean isComponentVisible = ctx.getBindContext().getComponent().isVisible();  --> the simple visibility
                final boolean isComponentVisible = isComponentVisible(ctx.getBindContext().getComponent());
                final String logMessage = String.format("Component:%s - visible:%s - isValidationRequired-argument-set:%s", ctx.getBindContext().getComponent(),
                        isComponentVisible, isValidationRequired);
                if (((isValidationRequired == null) || isValidationRequired) && isComponentVisible) {
                    if ((null == ctx.getProperty().getValue()) || "".equals(ctx.getProperty().getValue())) {
                        LOGGER.debug("FIELD isEMPTY: {}", logMessage);
                        addInvalidMessage(ctx, "EMPTY");
                        Clients.wrongValue(ctx.getBindContext().getComponent(), ZulUtils.translate("err", "fieldNotEmpty"));
                    }
                } else {
                    if (StringUtils.isNotBlank(ctx.getBindContext().getComponent().getId())) {
                        LOGGER.debug("NullV SKIPPED: {}", logMessage);
                    }
                }
            }
        };
    }

    private int              visibilityDepth      = 0;
    private static final int VISIBILITY_MAX_DEPTH = 5;

    public boolean isComponentVisible(final Component component) {
        boolean isVisible = true;
        visibilityDepth++;
        if (visibilityDepth >= VISIBILITY_MAX_DEPTH) {
            return true;  // we reach the max depth, so we assume the component is visible
        }
        if ((component != null) && component.isVisible()) {
            isVisible = isComponentVisible(component.getParent());
            visibilityDepth = 0;
            return isVisible;
        } else {
            visibilityDepth = 0;
            return false; // component is NOT visible
        }

    }

    // //////////////////////////////////////////////////////////////////
    // ERROR MESSAGE
    // //////////////////////////////////////////////////////////////////

    /**
     * Clear the message if ok button is pressed on confirmation pop up.
     */
    @NotifyChange("errorMessage")
    @Command
    public final void okErrorMessage() {
        // clear the message
        errorMessage = null;
    }

    /**
     * Get the error message and decide if an error occurred and show the pop up message.
     *
     * @return ReturnCodeException error message.
     */
    public final ReturnCodeException getErrorMessage() {
        return errorMessage;
    }

    public boolean isError() {
        return errorMessage != null;
    }

    //    public void setErrorMessage(ReturnCodeException e) {
    //        this.errorMessage = e.getReturnCodeMessage();
    //    }

    public void setErrorMessage(final ReturnCodeException message) {
        this.errorMessage = message;
    }

    /**
     * Creates an new ReturnCodeException with Constants.ErrorCodes.GENERAL_EXCEPTION and set it to
     * errorMessage.
     * @param msg translated message String
     */
    public void setErrorMessage(final String msg) {
        errorMessage = new ReturnCodeException(Constants.ErrorCodes.GENERAL_EXCEPTION, msg, null);
    }
    // //////////////////////////////////////////////////////////////////
    // CONFIRM MESSAGE
    // //////////////////////////////////////////////////////////////////

    public final String getConfirmMessage() {
        return confirmMessage;
    }

    /**
     * Clear the message if ok button is pressed on confirmation pop up.
     */
    @NotifyChange("confirmMessage")
    @Command
    public void okConfirmMessage() {
        // clear the message
        confirmMessage = null;
    }

    @NotifyChange({ "confirmMessage" })
    public final void setSuccessMessage(final String message) {
        this.confirmMessage = message;
    }

    public final void setSuccessMessageTransaction() {
        setSuccessMessage(ZulUtils.translate("com", "success.transaction"));
    }

    // //////////////////////////////////////////////////////////////////
    // CONFIRM WITH CANCEL - MESSAGE
    // //////////////////////////////////////////////////////////////////

    public String getConfirmWithCancelMessage() {
        return confirmWithCancelMessage;
    }

    /**
     * @param confirmWithCancelMessage the confirmWithCancelMessage to set
     */
    @NotifyChange("confirmWithCancelMessage")
    public void setConfirmWithCancelMessage(final String confirmWithCancelMessage) {
        this.confirmWithCancelMessage = confirmWithCancelMessage;
    }

    /**
     * This command must be overwritten. Because for this case something (an action like execute something) must be done.<br/>
     * Please keep in mind to call on the overwritten method
     *
     * <pre>
     * @NotifyChange("confirmWithCancelMessage")
     * </pre>
     */
    @NotifyChange("confirmWithCancelMessage")
    @Command
    public void okConfirmWithCancelMessage() {
        // clear the message
        confirmWithCancelMessage = null;
    }

    /**
     * Clear the message if OK-button is pressed on confirmation pop up.
     */
    @NotifyChange("confirmWithCancelMessage")
    @Command
    public void cancelConfirmWithCancelMessage() {
        // clear the message
        confirmWithCancelMessage = null;
    }
}
