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

import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.fixedpoint.FixedPointBase;
import jakarta.annotation.Nonnull;

/**
 * A converter for FixedPointBase values that formats them as decimal numbers
 * for display in UI grids. This converter creates appropriate converter instances
 * with fixed decimal places based on field metadata configuration.
 *
 * <p>The converter is registered as a named dependency injection component with
 * the name "fixedpoint" and follows the singleton pattern.</p>
 *
 * <p>The converter checks field metadata for a "decimals" property to determine
 * the number of decimal places to display. If found, it creates a FixedDecimalsConverter
 * with the specified decimal places. The decimal format pattern is retrieved from
 * the label "com.decimal.format" with a default fallback of "0.00".</p>
 */
@Singleton
@Named("fixedpoint")
public class FixedPointConverter implements IItemConverter<FixedPointBase<?>> {

    /**
     * Returns an appropriate converter instance for the specified field.
     *
     * <p>This method checks if the field metadata contains a custom decimal places configuration
     * via the <b>"decimals"</b> property specified in the Bonaparte (BON) metadata for the field.
     * If a single-digit decimal places value is found, it creates a FixedDecimalsConverter with that
     * specific formatting. If no valid decimals property is found, it creates a FixedDecimalsConverter
     * that uses the value's scale for the number of decimal places.</p>
     *
     * <p>
     * <b>Example BON field definition with decimals property:</b>
     * <pre>
     *   required amount  amount properties decimals="2";
     * </pre>
     * In this example, the field <code>amount</code> will be formatted with 2 decimal places.
     * </p>
     *
     * @param fieldName the name of the field to create a converter for
     * @param meta the metadata definition for the field
     * @return a new FixedDecimalsConverter instance with either fixed decimal places if configured,
     *         or dynamic decimal places based on the value's scale
     */
    @Override
    public IItemConverter<FixedPointBase<?>> getInstance(@Nonnull final String fieldName, @Nonnull final FieldDefinition meta) {
        return new NumberConverter<FixedPointBase<?>>(meta, "0.00", (df, value) -> df.setMinimumFractionDigits(value.scale()));
    }
}
