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

import java.util.List;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.DataAndMeta;
import de.jpaw.bonaparte.core.FoldingComposer;
import de.jpaw.bonaparte.core.ListMetaComposer;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.ParsedFoldingComponent;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@Singleton
@Named("object")
public class BonaPortableConverter implements IItemConverter<BonaPortable> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nullable
    @Override
    public String getFormattedLabel(@Nonnull final BonaPortable data, @Nonnull final BonaPortable wholeDataObject, @Nonnull final String fieldName,
        @Nonnull final FieldDefinition d) {
        final String fname = extractKeyName(fieldName);
        final ParsedFoldingComponent pfc = FoldingComposer.createRecursiveFoldingComponent(fname);
        final ListMetaComposer metaComposer = new ListMetaComposer();
        metaComposer.reset();
        data.foldedOutput(metaComposer, pfc);
        final List<DataAndMeta> storage = metaComposer.getStorage();
        if (!storage.isEmpty()) {
            final DataAndMeta dataAndMeta = storage.get(0);
            if (dataAndMeta.data == null) {
                return null;
            }
            final IItemConverter valueConverter = ItemConverterRegistry.getConverter(fname, dataAndMeta.meta);
            return valueConverter.getFormattedLabel(dataAndMeta.data, wholeDataObject, fname, dataAndMeta.meta);
        }
        return data.toString();
    }

    @Nonnull
    private String extractKeyName(@Nonnull final String fieldName) {
        final int startPos = fieldName.lastIndexOf("[");
        if (startPos != -1) {
            final int endPos = fieldName.lastIndexOf("]");
            if (endPos != -1) {
                return fieldName.substring(startPos + 1, endPos);
            }
        }
        return fieldName;
    }
}
