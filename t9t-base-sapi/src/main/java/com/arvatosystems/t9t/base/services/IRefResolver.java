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
package com.arvatosystems.t9t.base.services;

import java.util.List;

import de.jpaw.bonaparte.pojos.api.AbstractRef;
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.refs.BaseRefResolver;

/** API to noSQL backends (mini EntityManager) */
public interface IRefResolver<REF extends AbstractRef, DTO extends REF, TRACKING extends TrackingBase> extends BaseRefResolver<REF, DTO, TRACKING> {
    /**
     * Returns a new surrogate key value.
     */
    Long createNewPrimaryKey();

    /**
     * Returns the key for the provided unique index. Null-safe, returns null for a null parameter. Throws an exception if the reference does not exist.
     */
    Long getRef(REF refObject);

    /**
     * Returns the DTO for a given primary key. Null-safe, returns null for a null ref. Throws an exception if the key does not exist.
     * Performs permission check if it is allowed to read the returned DTO.
     */
    DTO getDTO(Long ref);

    /**
     * Returns a frozen copy of the tracking columns (to avoid tampering with them) for a given primary key.
     */
    TRACKING getTracking(Long ref);

    /**
     * Removes the record referenced by the key. Does nothing if key is null. Throws an exception if the key does not exist.
     */
    void remove(Long key);

    /** Returns a number of records for a query.
     * Throws UnsupportedOperationException in case the persistence provider does not support searches.
     */
    List<DataWithTrackingS<DTO, TRACKING>> query(int limit, int offset, SearchFilter filter, List<SortColumn> sortColumns);
}
