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
package com.arvatosystems.t9t.out.be.impl.formatgenerator;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.io.OutputHeading;
import com.arvatosystems.t9t.out.be.impl.output.EnumTranslatorComposerFilter;
import com.arvatosystems.t9t.out.be.impl.output.VariantComposerFilter;

import de.jpaw.bonaparte.core.BonaCustom;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.EnumAsTokenComposerFilter;
import de.jpaw.bonaparte.core.FoldingComposer;
import de.jpaw.bonaparte.core.MessageComposer;
import de.jpaw.bonaparte.pojos.meta.FoldingStrategy;
import jakarta.annotation.Nonnull;

public abstract class FoldableFormatGenerator<E extends Exception> extends AbstractFormatGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(FoldableFormatGenerator.class);

    protected MessageComposer<E> foldingComposer;
    protected Map<Class<? extends BonaCustom>, List<String>> map = null;

    /** Retrieves the base composer (not the folding one, and not an enum converting one). */
    @Nonnull
    protected abstract MessageComposer<E> getMessageComposer();


    @Override
    protected void openHook() throws IOException {
        MessageComposer<E> baseComposer = getMessageComposer();
        if (foldableParams == null || T9tUtil.isEmpty(foldableParams.getSelectedFields())) {
            map = null;
        } else {
            map = new HashMap<>();
            map.put(BonaPortable.class, foldableParams.getSelectedFields());
            if (foldableParams.getRelevantEnumType() != null) {
                switch (foldableParams.getRelevantEnumType()) {
                case NAME:
                    baseComposer = new EnumAsTokenComposerFilter<>(baseComposer);
                    break;
                case DESCRIPTION:
                    baseComposer = new EnumTranslatorComposerFilter<>(baseComposer, foldableParams.getEnumTranslator());
                    break;
                default:
                    // intentionally no activity
                }
            }
            if (foldableParams.isApplyVariantFilter()) {
                // insert a mapper from the Variant to one of its member types
                baseComposer = new VariantComposerFilter<>(baseComposer);
            }
        }
        foldingComposer = map == null ? baseComposer : new FoldingComposer<>(baseComposer, map, FoldingStrategy.TRY_SUPERCLASS);
    }

    /** Write specific header data. Header data consists of a row of text fields, of the same number of fields / columns as the data rows. */
    protected void writeTitles() {
        if (!Boolean.TRUE.equals(sinkCfg.getWriteHeaderRow())) {
            return;  // no header row requested
        }
        if (foldableParams == null || T9tUtil.isEmpty(foldableParams.getHeaders())) {
            return;
        }
        try {
            getMessageComposer().writeRecord(new OutputHeading(foldableParams.getHeaders()));
        } catch (final Exception e) {
            LOGGER.error("Failed to write header to output.", e);
        }
    }
}
