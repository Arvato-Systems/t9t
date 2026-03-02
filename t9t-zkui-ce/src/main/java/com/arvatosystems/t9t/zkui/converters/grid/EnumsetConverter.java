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

import jakarta.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.meta.AlphanumericEnumSetDataItem;
import de.jpaw.bonaparte.pojos.meta.EnumDefinition;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.NumericEnumSetDataItem;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

import com.arvatosystems.t9t.zkui.session.ApplicationSession;

@Singleton
@Named("enumset")
public class EnumsetConverter implements IItemConverter<Set<Enum<?>>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnumsetConverter.class);

    private record LocalEnumsetConverter(Map<String, String> enumXlation) implements IItemConverter<Set<Enum<?>>> {
        @Override
        public String getFormattedLabel(final Set<Enum<?>> value, final BonaPortable wholeDataObject, final String fieldName, final FieldDefinition meta) {
            // create a comma separated list of names
            final StringJoiner sj = new StringJoiner(",");

            for (final Enum<?> e: value) {
                final String instanceName = e.name();
                final String xlate = enumXlation.get(instanceName);
                sj.add(xlate != null ? xlate : instanceName);
            }
            return sj.toString();
        }
    }

    @Override
    @Nonnull
    public IItemConverter<Set<Enum<?>>> getInstance(@Nonnull final String fieldName, @Nonnull final FieldDefinition meta) {
        final ApplicationSession as = ApplicationSession.get();
        final EnumDefinition ed;
        if (meta instanceof NumericEnumSetDataItem nes) {
            ed = nes.getBaseEnumset().getBaseEnum();
        } else if (meta instanceof AlphanumericEnumSetDataItem aes) {
            ed = aes.getBaseEnumset().getBaseEnum();
        } else {
            ed = null;
            LOGGER.error("EnumsetConverter invoked for field {} of type {}", fieldName, meta.getClass().getCanonicalName());
            return this;
        }
        final Map<String, String> enumXlation = as.translateEnum(ed.getName());
        return new LocalEnumsetConverter(enumXlation);
    }
}
