/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.jpa.updater;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.crud.CrudAnyKeyRequest;
import com.arvatosystems.t9t.base.crud.CrudCompositeKeyRequest;
import com.arvatosystems.t9t.base.crud.CrudLongKeyRequest;
import com.arvatosystems.t9t.base.crud.CrudModuleCfgRequest;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyRequest;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.CompositeKeyRef;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;

public abstract class AbstractDataRequestHandler<T extends RequestParameters>
  extends AbstractRequestHandler<T> {
    private static final Map<String, CrudViewModel> INDEXED_VIEWMODELS = new ConcurrentHashMap<>(100);

    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);

    public AbstractDataRequestHandler() {
        if (INDEXED_VIEWMODELS.isEmpty()) {
            for (CrudViewModel cvm: IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.values()) {
                if (cvm.dtoClass != null && cvm.crudClass != null) {
                    INDEXED_VIEWMODELS.put(cvm.dtoClass.getBonaPortableClass().getCanonicalName(), cvm);
                }
            }
        }
    }

    @Override
    public OperationType getAdditionalRequiredPermission(final T request) {
        return OperationType.ADMIN;       // must have permission for the general CRUD operation
    }

    @Nonnull
    protected CrudViewModel getCrudViewModel(final String dtoClassName) {
        final CrudViewModel cvm = INDEXED_VIEWMODELS.get(dtoClassName);
        if (cvm == null) {
            throw new T9tException(T9tException.UPDATER_NO_CRUDVIEWMODEL_FOR_CLASS, dtoClassName);
        }
        return cvm;
    }

    protected CrudAnyKeyRequest getCrudRequest(final String dtoClassName, final BonaPortable key) {
        final CrudAnyKeyRequest crudRq = (CrudAnyKeyRequest)(getCrudViewModel(dtoClassName).crudClass.newInstance());
        if (crudRq instanceof CrudSurrogateKeyRequest crudSurrRq) {
            if (key instanceof Ref ref) {
                if (ref.getObjectRef() != null) {
                    // primary key provided
                    crudSurrRq.setKey(ref.getObjectRef());
                } else {
                    // natural key provided
                    crudSurrRq.setNaturalKey(ref);
                }
            } else {
                throw new T9tException(T9tException.UPDATER_KEY_CLASS_MISMATCH, key.ret$PQON());
            }
        } else if (crudRq instanceof CrudModuleCfgRequest crudModuleCfgRq) {
            int i = 0;    // nothing to do
        } else if (crudRq instanceof CrudCompositeKeyRequest crudCompositeKeyRq) {
            crudCompositeKeyRq.setKey((CompositeKeyRef)key);
        } else if (crudRq instanceof CrudLongKeyRequest crudLongKeyRq && key instanceof Ref ref) {
            crudLongKeyRq.setKey(ref.getObjectRef());
        } else {
            throw new T9tException(T9tException.UPDATER_UNSUPPORTED_CRUD_TYPE, crudRq.ret$PQON());
        }
        return crudRq;
    }
}
