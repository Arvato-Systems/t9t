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
package com.arvatosystems.t9t.base;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.arvatosystems.t9t.base.cache.DataMap;

import de.jpaw.bonaparte.pojos.apiw.Ref;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Cache data block for 2 maps of type DataMap, once indexed by objectRef, once by primary ID,
 * which implements the default access methods.
 **/
public class DataCache<REF extends Ref, DTO extends REF, KEY extends REF, DESC extends REF> {
    private final Map<Long,   DataMap<REF, DTO, KEY, DESC>> dataByRef;
    private final Map<String, DataMap<REF, DTO, KEY, DESC>> dataById;

    private DataCache(final int capacity) {
        dataByRef = new ConcurrentHashMap<>(capacity);
        dataById = new ConcurrentHashMap<>(capacity);
    }

    public DataCache(@Nonnull final Collection<DataMap<REF, DTO, KEY, DESC>> data) {
        this(data.size());
        for (DataMap<REF, DTO, KEY, DESC> element: data) {
            dataByRef.put(element.getObjectRef(), element);
            dataById.put(element.getId(), element);
        }
    }

    @Nullable
    public DataMap<REF, DTO, KEY, DESC> getByRef(@Nonnull final Long ref) {
        return dataByRef.get(ref);
    }

    @Nonnull
    public DataMap<REF, DTO, KEY, DESC> getByRefOrFail(@Nonnull final Long ref) {
        final DataMap<REF, DTO, KEY, DESC> data = dataByRef.get(ref);
        if (data == null) {
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, getClass().getSimpleName());
        }
        return data;
    }

    @Nullable
    public DataMap<REF, DTO, KEY, DESC> getById(@Nonnull final String id) {
        return dataById.get(id);
    }

    @Nonnull
    public DataMap<REF, DTO, KEY, DESC> getByIdOrFail(@Nonnull final String id) {
        final DataMap<REF, DTO, KEY, DESC> data = dataById.get(id);
        if (data == null) {
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, getClass().getSimpleName());
        }
        return data;
    }

    @Nullable
    public DTO getDtoByRef(@Nonnull final Long ref) {
        final DataMap<REF, DTO, KEY, DESC> element = dataByRef.get(ref);
        return element == null ? null : element.getDto();
    }

    @Nullable
    public DESC getDescriptionByRef(@Nonnull final Long ref) {
        final DataMap<REF, DTO, KEY, DESC> element = dataByRef.get(ref);
        return element == null ? null : element.getDescription();
    }

    @Nullable
    public DTO getDtoById(@Nonnull final String id) {
        final DataMap<REF, DTO, KEY, DESC> element = dataById.get(id);
        return element == null ? null : element.getDto();
    }

    @Nullable
    public DESC getDescriptionById(@Nonnull final String id) {
        final DataMap<REF, DTO, KEY, DESC> element = dataById.get(id);
        return element == null ? null : element.getDescription();
    }

    @Nullable
    public KEY mapToKey(@Nullable final Long ref) {
        if (ref == null) {
            return null;
        }
        return getByRefOrFail(ref).getKey();
    }

    @Nullable
    public DTO mapToDto(@Nullable final Long ref) {
        if (ref == null) {
            return null;
        }
        return getByRefOrFail(ref).getDto();
    }

    @Nullable
    public DESC mapToDescription(@Nullable final Long ref) {
        if (ref == null) {
            return null;
        }
        return getByRefOrFail(ref).getDescription();
    }

    /** Returns the number of store data elements. */
    public int size() {
        return dataById.size();
    }

    /** Count elements which fulfill a certain condition without exposing the DataMap itself. */
    public int count(@Nonnull final Predicate<DataMap<REF, DTO, KEY, DESC>> processor) {
        int counter = 0;
        for (DataMap<REF, DTO, KEY, DESC> element: dataByRef.values()) {
            if (processor.test(element))
                ++counter;
        }
        return counter;
    }

    /** Iterate the elements of the data map without exposing the DataMap itself. */
    public void iterate(@Nonnull final Consumer<DataMap<REF, DTO, KEY, DESC>> processor) {
        for (DataMap<REF, DTO, KEY, DESC> element: dataByRef.values()) {
            processor.accept(element);
        }
    }
}
