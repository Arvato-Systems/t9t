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

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.Constants;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.enums.BonaEnum;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.XEnumDataItem;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.enums.XEnum;

@Singleton
@Named("xenum")
public class XenumTranslationConverter implements IItemConverter<XEnum<?>> {

    private static final class XenumIconConverter extends AbstractIconConverter<XEnum<?>> implements IItemConverter<XEnum<?>> {
        private final String prefix;

        private XenumIconConverter(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String iconPath(XEnum<?> value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
            return prefix + value.name() + ".png";
        }
    }

    @Override
    public IItemConverter<XEnum<?>> getInstance(final String fieldName, final FieldDefinition meta) {
        if (meta instanceof XEnumDataItem xedi) {
            if (T9tUtil.getFieldProperty(meta, Constants.UiFieldProperties.ICON) != null) {
                final String iconPathPrefix = "icon/" + xedi.getBaseXEnum().getName().replace('.', '/') + "/";
                return new XenumIconConverter(iconPathPrefix);
            }
        }
        return this;
    }

    @Override
    public String getFormattedLabel(XEnum<?> value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
        return ApplicationSession.get().translateEnum((BonaEnum)value.getBaseEnum());
    }
}
