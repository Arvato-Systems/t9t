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

import java.time.Instant;

import com.arvatosystems.t9t.zkui.session.ApplicationSession;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.TemporalElementaryDataItem;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("java.time.Instant")
public class JavaInstantConverter implements IItemConverter<Instant> {

    /**
     * Returns a formatted string.
     */
    @Override
    public String getFormattedLabel(final Instant value, final BonaPortable wholeDataObject, final String fieldName, final FieldDefinition meta) {
        if (meta instanceof TemporalElementaryDataItem temporalElementaryDataItem) {
            return ApplicationSession.get().format(value, temporalElementaryDataItem.getFractionalSeconds());
        }
        return ApplicationSession.get().format(value);
    }
}
