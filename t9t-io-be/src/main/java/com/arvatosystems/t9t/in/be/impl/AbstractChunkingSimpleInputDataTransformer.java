/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.in.be.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.in.services.IInputSession;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;
import jakarta.annotation.Nonnull;

/**
 * Chunking transformer, simple variant, without transformation.
 *
 * @param <T> the input type, which is also the type passed into the request
 */
public abstract class AbstractChunkingSimpleInputDataTransformer<T extends BonaPortable> extends AbstractInputDataTransformer<T> {

    private static final int DEFAULT_CHUNK_SIZE = 100;

    protected int chunkSize = DEFAULT_CHUNK_SIZE;
    protected List<T> pendingData = Collections.emptyList();

    @Override
    public void open(final IInputSession newInputSession, final Map<String, Object> newParams, final BonaPortableClass<?> newBaseBClass) {
        super.open(newInputSession, newParams, newBaseBClass);

        if (importDataSinkDTO.getChunkSize() != null && importDataSinkDTO.getChunkSize() > 0) {
            chunkSize = importDataSinkDTO.getChunkSize();
        }
        pendingData = new ArrayList<>(chunkSize);
    }

    /** Check if data should be imported. */
    protected boolean isTransferrable(final T dto) {
        // unless filtered by subclass, everything will be passed into the request
        return true;
    }

    /** Given there is at least one data record in pendingData, convert it to a load request. */
    protected abstract @Nonnull RequestParameters toRequest();

    @Override
    public final RequestParameters transform(final T dto) {
        if (!isTransferrable(dto)) {
            // should be skipped
            return null;  // no change
        }
        // add it to the list of pending data
        pendingData.add(dto);
        if (pendingData.size() >= chunkSize) {
            // flush it!
            final RequestParameters rq = toRequest();
            pendingData = new ArrayList<>(chunkSize);  // not clear(), because it could clear a reference in rq as well
            return rq;
        }
        return null; // capacity not yet reached
    }

    /** Returns any request which was postponed for buffering purposes before. */
    @Override
    public RequestParameters getPending() {
        if (pendingData.isEmpty()) {
            return null;
        }
        final RequestParameters rq = toRequest();
        pendingData = new ArrayList<>(chunkSize);  // not clear(), because it could clear a reference in rq as well
        return rq;
    }
}
