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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.zkui.components.dropdown28.factories.IDropdown28DbFactory;
import com.arvatosystems.t9t.zkui.util.Constants;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("ascii")
@Named("unicode")
public class StringConverter implements IItemConverter<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringConverter.class);

    private static final class StringIconConverter extends AbstractIconConverter<String> implements IItemConverter<String> {
        private static final Logger LOGGER = LoggerFactory.getLogger(StringIconConverter.class);
        private static final String VALID_VALUE_REGEXP = "^[a-z0-9_\\-]+$";
        private final String prefix;
        private final String empty;

        private StringIconConverter(final String prefix, final String empty) {
            this.prefix = prefix;
            if (empty == null) {
                this.empty = null;
            } else if (empty.isEmpty()) {
                this.empty = Constants.UiFieldProperties.ICON_CORE_PATH + "EMPTY.png";
            } else if (empty.matches(VALID_VALUE_REGEXP)) {
                this.empty = prefix + empty + ".png";
            } else {
                LOGGER.error("Invalid icon empty value provided: '{}', using default EMPTY icon", empty);
                this.empty = Constants.UiFieldProperties.ICON_CORE_PATH + "BADEMPTY.png";
            }
        }

        @Override
        public String iconPath(final String value, final BonaPortable wholeDataObject, final String fieldName, final FieldDefinition meta) {
            final String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                return empty;
            }
            if (trimmed.matches(VALID_VALUE_REGEXP)) {
                return prefix + trimmed + ".png";
            }
            LOGGER.warn("Invalid icon value for field {}: value={}", fieldName, value);
            return Constants.UiFieldProperties.ICON_CORE_PATH + "BADVALUE.png";
        }
    }

    @Override
    public IItemConverter<String> getInstance(final String fieldName, final FieldDefinition meta) {
        if (meta.getProperties() != null) {
            final String dropdown = meta.getProperties().get(Constants.UiFieldProperties.DROPDOWN);
            if (dropdown != null) {
                final IDropdown28DbFactory<?> dropDownFactory = Jdp.getOptional(IDropdown28DbFactory.class, dropdown);
                if (dropDownFactory == null) {
                    LOGGER.error("No dropdown factory found for dropdown '{}' (field {})", dropdown, fieldName);
                    return this;
                }
                return new DropdownConverter(dropdown, dropDownFactory);
            }
            final String iconPathProperty = meta.getProperties().get(Constants.UiFieldProperties.ICON);
            if (iconPathProperty != null) {
                // Validate and construct the icon path
                if (!isValidIconPath(iconPathProperty)) {
                    LOGGER.error("Bad path provided for icon: '{}' (field {})", iconPathProperty, fieldName);
                    return this;
                }
                final String iconPathPrefix = "icon/" + iconPathProperty.toLowerCase().replace('.', '/') + "/";
                // now check for icon for empty values
                final String emptyIcon = meta.getProperties().get(Constants.UiFieldProperties.ICON_EMPTY);
                return new StringIconConverter(iconPathPrefix, emptyIcon);
            }
        }
        return this;
    }

    @Override
    public String getFormattedLabel(String value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
        return value;
    }

    /**
     * Validates that the icon path only contains safe characters to prevent path traversal attacks.
     * Allows alphanumeric characters, hyphens, underscores, dots (for package names), and forward slashes for subdirectories.
     */
    private boolean isValidIconPath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        // Reject paths containing parent directory references or backslashes
        if (path.contains("..") || path.contains("\\")) {
            return false;
        }
        // Reject absolute paths or paths starting with slash
        if (path.startsWith("/")) {
            return false;
        }
        // Only allow alphanumeric, hyphens, underscores, dots, and forward slashes
        return path.matches("^[a-zA-Z0-9_\\-/.]+$");
    }
}
