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

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import jakarta.annotation.Nonnull;

/**
 * Converter interface for formatting grid cell values.
 * The qualifier of the implementation is the bonaparte type name of the field, plus optionally a colon and the field name.
 * In case the bonaparte type name would be "ref" (an object reference), then the simple name of the Java class which is the lower bound is used instead.
 * Both notations cannot overlap, because all bonaparte type names are lowercase, and Java class names start with uppercase letters.
 *
 * The lookup is done first including the field name, then without.
 *
 * @param <T> the type of the field to be converted
 */
public interface IItemConverter<T> {

    /**
     * Returns an instance of this converter, which is possibly optimized.
     *
     * @param fieldName the path to the field from the root object
     * @param d the field definition (meta data)
     */
    default IItemConverter<T> getInstance(@Nonnull String fieldName, @Nonnull FieldDefinition d) {
        return this;
    }

    /** Returns the alignment of this converter. Used for numeric fields. */
    default boolean isRightAligned() {
        return false;
    }

    /** Returns if the converter produces icons or text. */
    default boolean isIcon() {
        return false;
    }

    /**
     * Returns the validated icon path for this field if it should be displayed as an icon.
     * The path is already security-checked and ready to use.
     *
     * @param value The value itself
     * @param wholeDataObject the whole Data Object
     * @param fieldName the property name of the object
     * @param d the field definition
     * @return the icon path if the field should be displayed as icon, null otherwise
     */
    default String iconPath(@Nonnull T value, @Nonnull BonaPortable wholeDataObject, @Nonnull String fieldName, @Nonnull FieldDefinition d) {
        return null;
    }

    /**
     * Prints the label in the correct way.<br>
     * e.g.:
     * <ul>
     * <li>Object like: Person</li>
     * <li>With properties: gender (Integer) 0=male / 1=female</li>
     * <li>the following value would be passed</li>
     * <ul>
     * <li>value: Integer(1)</li>
     * <li>wholeDataObject: Person-object</li>
     * <li>fieldName: gender</li>
     * </ul>
     * <li>Return: the string "Female"</li>
     * </ul>
     * @param value The value itself that should be formatted
     * @param wholeDataObject the whole Data Object will be passed for cases if you need additional values from other fields you want to combine
     * @param fieldName the property name of the object
     * @return your formatted label as String
     */
    default String getFormattedLabel(@Nonnull T value, @Nonnull BonaPortable wholeDataObject, @Nonnull String fieldName, @Nonnull FieldDefinition d) {
        return value.toString();
    }
}
