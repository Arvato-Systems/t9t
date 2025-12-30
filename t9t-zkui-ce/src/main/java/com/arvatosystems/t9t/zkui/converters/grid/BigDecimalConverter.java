/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.converters.grid;

import java.math.BigDecimal;

import java.text.DecimalFormat;
import com.arvatosystems.t9t.zkui.util.ApplicationUtil;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.util.BigDecimalTools;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import org.zkoss.util.resource.Labels;

@Singleton
@Named("decimal")  // java.math.BigDecimal
public class BigDecimalConverter implements IItemConverter<BigDecimal> {
    private static final String DEFAULT_FORMAT = "###,##0.00";

    private final String format;

    public BigDecimalConverter() {
        this.format = Labels.getLabel("com.decimal.format", DEFAULT_FORMAT);
    }

    private record DecimalFormatConverter(@Nonnull String format, @Nonnull String path) implements IItemConverter<BigDecimal> {
        private static final Logger LOGGER = LoggerFactory.getLogger(DecimalFormatConverter.class);

        @Nonnull
        @Override
        public String getFormattedLabel(@Nonnull final BigDecimal value, @Nonnull final BonaPortable wholeDataObject, @Nonnull final String fieldName,
            @Nonnull final FieldDefinition meta) {
            final BonaPortable root = getObjectWithoutDataOrTracking(wholeDataObject);
            BigDecimal scaledValue = BigDecimalTools.retrieveScaled(root, path);
            if (scaledValue == null) {
                LOGGER.warn("Can't find BigDecimal value in {}#{}. Please check decimal property in bon file.", root.ret$PQON(), path);
                scaledValue = setDefaultMinScale(value);
            }
            final DecimalFormat df = ApplicationUtil.getLocalizedDecimalFormat(format);
            df.setMinimumFractionDigits(scaledValue.scale());
            return df.format(scaledValue);
        }

        @Nonnull
        private BonaPortable getObjectWithoutDataOrTracking(@Nonnull final BonaPortable wholeDataObject) {
            if (wholeDataObject instanceof DataWithTracking<?, ?> dwt) {
                return dwt.getData();
            }
            return wholeDataObject;
        }

        @Nonnull
        private BigDecimal setDefaultMinScale(@Nonnull final BigDecimal value) {
            final BigDecimal tmp = value.stripTrailingZeros();
            if (tmp.scale() < 0) {
                return tmp.setScale(0);
            }
            return tmp;
        }

        @Override
        public boolean isRightAligned() {
            return true;
        }
    }

    @Nonnull
    @Override
    public IItemConverter<BigDecimal> getInstance(@Nonnull final String fieldName, @Nonnull final FieldDefinition d) {
        return new DecimalFormatConverter(format, getPathWithoutDataOrTracking(fieldName));
    }

    @Nonnull
    private String getPathWithoutDataOrTracking(@Nonnull final String fullPath) {
        // check if fullPath starts with "data.", this means we use DataWithTracking
        // -> remove the data.
        return fullPath.startsWith("data.") || fullPath.startsWith("tracking.")
                ? StringUtils.substringAfter(fullPath, ".") : fullPath;
    }
}
