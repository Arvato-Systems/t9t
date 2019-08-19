/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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

import com.arvatosystems.t9t.base.crud.CrudAnyKeyRequest;
import com.arvatosystems.t9t.base.search.SearchCriteria;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.pojos.api.TrackingBase;

/** A container to hold the common references for a standard UI CRUD screen. */
public class CrudViewModel<DTO extends BonaPortable, TRACKING extends TrackingBase> {
    public final BonaPortableClass<DTO> dtoClass;
    public final BonaPortableClass<TRACKING> trackingClass;
    public final BonaPortableClass<? extends SearchCriteria> searchClass;
    public final BonaPortableClass<? extends CrudAnyKeyRequest<DTO, TRACKING>> crudClass;

    public CrudViewModel(
            BonaPortableClass<DTO> dtoClass,
            BonaPortableClass<TRACKING> trackingClass,
            BonaPortableClass<? extends SearchCriteria> searchClass,
            BonaPortableClass<? extends CrudAnyKeyRequest<DTO, TRACKING>> crudClass) {
        this.dtoClass = dtoClass;
        this.trackingClass = trackingClass;
        this.searchClass = searchClass;
        this.crudClass = crudClass;
    }
}
