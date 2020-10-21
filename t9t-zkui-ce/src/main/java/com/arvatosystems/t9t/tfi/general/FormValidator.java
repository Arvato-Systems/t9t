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

import org.apache.commons.lang3.StringUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.SimpleConstraint;

public class FormValidator {

    public FormValidator() {
    }

    public boolean simpleConstraintValidator(ValidationContext ctx) {
        Clients.clearWrongValue(ctx.getBindContext().getComponent());
        boolean errorOcured = false;

        Boolean isValidationRequiered = (Boolean) ctx.getBindContext().getValidatorArg("isValidationRequiered");
        if (((isValidationRequiered != null) && !isValidationRequiered)) {
            return errorOcured;
        }
        String constraintString = null;
        Object validationArg = ctx.getBindContext().getValidatorArg("constraint");
        if (null == validationArg) {
            return false;
        }
        if (validationArg instanceof String[]) {
            String[] constraintStringArray = (String[]) validationArg;
            constraintString = StringUtils.join(constraintStringArray, ',');
        } else {
            constraintString = String.valueOf(validationArg);
        }
        Constraint constraint = SimpleConstraint.getInstance(constraintString);
        try {
            constraint.validate(ctx.getBindContext().getComponent(), ctx.getProperty().getValue());
        } catch (WrongValueException e) {
            errorOcured = true;
            Clients.wrongValue(ctx.getBindContext().getComponent(), e.getMessage());
        }
        return errorOcured;
    }

    public boolean isValidationRequiered(ValidationContext ctx) {
        Boolean isValidationRequiered = (Boolean) ctx.getBindContext().getValidatorArg("isValidationRequiered");
        return isValidationRequiered == null ? true : isValidationRequiered;
    }

    protected boolean isCustomValidationRequiered(ValidationContext ctx) {
        return true;
    }

    public Validator getCustomValidator() {
        return new AbstractValidator() {
            @Override
            public void validate(ValidationContext ctx) {
                if (!isValidationRequiered(ctx)) {
                    return;
                }
                if (!isCustomValidationRequiered(ctx)) {
                    return;
                }
                boolean errorOcured = simpleConstraintValidator(ctx);
                if (errorOcured) {
                    addInvalidMessage(ctx, "SimpleConstraintError");
                }
            }
        };
    }
}
