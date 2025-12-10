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

import java.util.Map;

import com.arvatosystems.t9t.zkui.util.Constants;
import com.arvatosystems.t9t.zkui.util.ZulUtils;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named("boolean")
@Singleton
public class BooleanConverter implements IItemConverter<Boolean> {

    private static final class BooleanTranslationConverter implements IItemConverter<Boolean> {
        private final String falseLabel = ZulUtils.readConfig("com.boolean.false.format");
        private final String trueLabel  = ZulUtils.readConfig("com.boolean.true.format");

        @Override
        public String getFormattedLabel(final Boolean value, final BonaPortable wholeDataObject, final String fieldName, final FieldDefinition meta) {
            return value ? trueLabel : falseLabel;
        }
    }

    private static final class BooleanIconConverter extends AbstractIconConverter<Boolean> implements IItemConverter<Boolean> {
        private static final String ICON_PATH_FALSE = Constants.UiFieldProperties.ICON_CORE_PATH + "FALSE.png";
        private static final String ICON_PATH_TRUE  = Constants.UiFieldProperties.ICON_CORE_PATH + "TRUE.png";

        @Override
        public String iconPath(final Boolean value, final BonaPortable wholeDataObject, final String fieldName, final FieldDefinition meta) {
            // Return icon path for TRUE or FALSE
            return value ? ICON_PATH_TRUE : ICON_PATH_FALSE;
        }
    }

    private static final BooleanIconConverter ICON_INSTANCE = new BooleanIconConverter();

    @Override
    public IItemConverter<Boolean> getInstance(final String fieldName, final FieldDefinition meta) {

        final Map<String, String> props = meta.getProperties();
        if (props == null || !props.containsKey(Constants.UiFieldProperties.ICON)) {
            // we have to create a new instance here, because the true/false labels differ per session language
            return new BooleanTranslationConverter();
        }
        return ICON_INSTANCE;
    }
}
