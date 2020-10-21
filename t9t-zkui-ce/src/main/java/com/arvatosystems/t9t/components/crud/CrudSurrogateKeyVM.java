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
package com.arvatosystems.t9t.components.crud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.Init;

import com.arvatosystems.t9t.base.crud.CrudAnyKeyResponse;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyRequest;

import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.Ref;

@Init(superclass=true)
public class CrudSurrogateKeyVM<REF extends Ref, DTO extends REF, TRACKING extends TrackingBase>
extends AbstractCrudVM<Long, DTO, TRACKING, CrudSurrogateKeyRequest<REF, DTO, TRACKING>, CrudAnyKeyResponse<DTO,TRACKING>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrudSurrogateKeyVM.class);

    @Override
    protected CrudSurrogateKeyRequest<REF, DTO, TRACKING> createCrudWithKey() {
        if (crudViewModel == null || crudViewModel.crudClass == null) {
            LOGGER.error("Requested createCrudWithKey() in a model without Crud: DTO = {}, Search = {}",
                    crudViewModel == null || crudViewModel.dtoClass == null ? "NO DTO!" : crudViewModel.dtoClass.getPqon(),
                    crudViewModel == null || crudViewModel.searchClass == null ? "NO SearchRQ!" : crudViewModel.searchClass.getPqon());
            throw new RuntimeException("NO CRUD request!");
        }
        CrudSurrogateKeyRequest<REF, DTO, TRACKING> crudRq = (CrudSurrogateKeyRequest<REF, DTO, TRACKING>) crudViewModel.crudClass.newInstance();
        crudRq.setKey(data.getObjectRef());
        return crudRq;
    }

    @Override
    protected void clearKey() {
        data.setObjectRef(0L);
    }
}
