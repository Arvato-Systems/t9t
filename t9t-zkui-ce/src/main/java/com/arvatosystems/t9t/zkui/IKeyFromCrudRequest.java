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
package com.arvatosystems.t9t.zkui;

import com.arvatosystems.t9t.base.crud.CrudAnyKeyRequest;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.TrackingBase;

public interface IKeyFromCrudRequest<DTO extends BonaPortable, TRACKING extends TrackingBase, REQ extends CrudAnyKeyRequest<DTO, TRACKING>> {

    /**
     * Create a search filter for the key based on the given {@link REQ} request.
     * @param {@link REQ} crud request
     * @return {@link SearchFilter} for the key
     */
    SearchFilter getFilterForKey(REQ crudRequest);
}
