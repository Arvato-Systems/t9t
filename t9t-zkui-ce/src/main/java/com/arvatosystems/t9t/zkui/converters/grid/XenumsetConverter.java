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
import java.util.Set;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.zkui.session.ApplicationSession;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.XEnumDefinition;
import de.jpaw.bonaparte.pojos.meta.XEnumSetDataItem;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.enums.XEnum;
import jakarta.annotation.Nonnull;

@Singleton
@Named("xenumset")
public class XenumsetConverter implements IItemConverter<Set<XEnum<?>>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(XenumsetConverter.class);

    private record LocalXEnumsetConverter(Map<String, String> enumXlation) implements IItemConverter<Set<XEnum<?>>> {
        @Override
        public String getFormattedLabel(final Set<XEnum<?>> value, final BonaPortable wholeDataObject, final String fieldName, final FieldDefinition meta) {
            // create a comma separated list of names
            final StringJoiner sj = new StringJoiner(",");

            for (final XEnum<?> e: value) {
                final String instanceName = e.name();
                final String xlate = enumXlation.get(instanceName);
                sj.add(xlate != null ? xlate : instanceName);
            }
            return sj.toString();
        }
    }

    @Override
    @Nonnull
    public IItemConverter<Set<XEnum<?>>> getInstance(@Nonnull final String fieldName, @Nonnull final FieldDefinition meta) {
        final ApplicationSession as = ApplicationSession.get();
        if (!(meta instanceof XEnumSetDataItem xes)) {
            LOGGER.error("XenumsetConverter invoked for field {} of type {}", fieldName, meta.getClass().getCanonicalName());
            return this;
        }
        final XEnumDefinition ed = xes.getBaseXEnumset().getBaseXEnum();
        final Map<String, String> enumXlation = as.translateEnum(ed.getName());
        return new LocalXEnumsetConverter(enumXlation);
    }
}
