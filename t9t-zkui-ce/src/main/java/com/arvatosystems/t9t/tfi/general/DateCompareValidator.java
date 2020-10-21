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

import java.util.Date;

import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.zk.ui.util.Clients;

import com.arvatosystems.t9t.tfi.web.ZulUtils;

public class DateCompareValidator extends AbstractValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DateCompareValidator.class);
    public static final Object CHECK_TYPE_MIN = "MIN"; // Occures an error if date1 is lower than Date2
    public static final Object CHECK_TYPE_MAX = "MAX"; // Occures an error if date1 is greater than Date2
    //TODO is not optimized may some addons are requiered
    @Override
    public void validate(ValidationContext ctx) {
        FormValidator formValidator = new FormValidator();

        if (!formValidator.isValidationRequiered(ctx)) {
            return;
        }

        Clients.clearWrongValue(ctx.getBindContext().getComponent());

        Boolean isValidationRequiered = formValidator.isValidationRequiered(ctx);
        if (!isValidationRequiered) {
            return;
        }

        //Simple Constraint Validator
        boolean errorOcured = formValidator.simpleConstraintValidator(ctx);
        if (errorOcured) {
            addInvalidMessage(ctx, "SimpleConstraintError");
            return;
        }
        //ctx.getBindContext().getComponent()

        Object dateOneValue = ctx.getProperty().getValue();
        Object dateTwoValue = ctx.getBindContext().getValidatorArg("dateToCompare");
        Object checkType = ctx.getBindContext().getValidatorArg("checkType");
        if ((dateOneValue == null) || (dateTwoValue == null)) {
            return;
        }



        long dateOneInmilliseconds = getDateInMiliseconds(dateOneValue);
        long dateTwoInmilliseconds = getDateInMiliseconds(dateTwoValue);
        Object[] arguments = new Object[1];

        //OCUR ERROR IF
        if ((checkType == CHECK_TYPE_MIN) && (dateOneInmilliseconds <= dateTwoInmilliseconds)) {
            addInvalidMessage(ctx, "COMPARE_DATE");
            arguments[0] = dateTwoValue;
            Clients.wrongValue(ctx.getBindContext().getComponent(), ZulUtils.translate("err","dateComapareMin", arguments));
            return;
        }
        if ((checkType == CHECK_TYPE_MAX) && (dateOneInmilliseconds >= dateTwoInmilliseconds)) {
            addInvalidMessage(ctx, "COMPARE_DATE");
            arguments[0] = dateOneValue;
            Clients.wrongValue(ctx.getBindContext().getComponent(), ZulUtils.translate("err","dateComapareMax", arguments));
            return;
        }


    }

    long getDateInMiliseconds(Object dateValue) {
        if (dateValue instanceof LocalDateTime) {
            return ((LocalDateTime) dateValue).toDate().getTime();
        } else if (dateValue instanceof LocalDate) {
            return ((LocalDate) dateValue).toDate().getTime();
        } else if (dateValue instanceof Instant) {
            return ((Instant) dateValue).toDate().getTime();
        } else if (dateValue instanceof Date) {
            return ((Date) dateValue).getTime();
        }
        return 0;
    }

}
