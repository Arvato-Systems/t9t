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
package com.arvatosystems.t9t.tfi.general;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.lang.Generics;
import org.zkoss.zk.ui.util.Clients;

public class ModuloValidator extends AbstractValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuloValidator.class);

    @Override
    public void validate(ValidationContext ctx) {
        if (!isValidationRequiered(ctx)) {
            return;
        }

        Clients.clearWrongValue(ctx.getBindContext().getComponent());

        if (ctx.getBindContext().getValidatorArg("message") == null) {
            throw new IllegalArgumentException(
                    "The validator argument 'message' (error message in case: entered value is not 'mod % <<entered_value>> == 0') is not set. Check your implementation");
        }
        long modulo;
        try {
            modulo = Generics.cast(ctx.getBindContext().getValidatorArg("modulo"));
        } catch (Exception e) {
            throw new IllegalArgumentException("The validator argument 'modulo' (modulo value) is not set or not numeric. Check your implementation: "
                    + e.getMessage());
        }

        String message = Generics.cast(ctx.getBindContext().getValidatorArg("message"));
        if (message == null) {
            throw new IllegalArgumentException(
                    "The validator argument 'message' (error message in case: entered value is not 'mod % <<entered_value>> == 0') is not set. Check your implementation");
        }

        Object propertyValue = ctx.getProperty().getValue();
        if (propertyValue == null) {
            // skip:
            return;
        }
        try {
            long toBeCheckedValue = Long.parseLong(String.valueOf((propertyValue)));
            LOGGER.debug("Modulo valirator ({} % {} != 0) ==> {}", modulo, toBeCheckedValue, (modulo % toBeCheckedValue) != 0);
            if ((modulo % toBeCheckedValue) != 0) {
                displayError(ctx, message);
            }
        } catch (Exception e) {
            LOGGER.debug("Modulo violation ({} % {} != 0) ==> {}", modulo, propertyValue, e.getMessage());
            displayError(ctx, message);
        }

    }

    public boolean isValidationRequiered(ValidationContext ctx) {
        Boolean isValidationRequiered = (Boolean) ctx.getBindContext().getValidatorArg("isValidationRequiered");
        return isValidationRequiered == null ? true : isValidationRequiered;
    }

    private void displayError(ValidationContext ctx, String message) {
        Clients.wrongValue(ctx.getBindContext().getComponent(), message);
        addInvalidMessage(ctx, "ModuloConstraintError");
    }

}
