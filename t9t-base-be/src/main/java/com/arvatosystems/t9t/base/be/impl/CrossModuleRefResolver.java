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
package com.arvatosystems.t9t.base.be.impl;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyRequest;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.crud.RefResolverRequest;
import com.arvatosystems.t9t.base.crud.RefResolverResponse;
import com.arvatosystems.t9t.base.services.ICrossModuleRefResolver;
import com.arvatosystems.t9t.base.services.IExecutor;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

/**
 * Default implementation for {@linkplain ICrossModuleRefResolver} interface.
 *
 */
@Singleton
public class CrossModuleRefResolver implements ICrossModuleRefResolver {
    // private static final Logger LOGGER = LoggerFactory.getLogger(CrossModuleRefResolver.class);

    protected final IExecutor messaging = Jdp.getRequired(IExecutor.class);

    @Override
    public <REF extends Ref, REQ extends RefResolverRequest<REF>> Long getRef(REQ req, REF ref) {
        if (ref == null) {
            return null; // play null-safe
        }
        // shortcut! If we have a long already, there's no need to perform the cross module call (and any DB I/O)
        if (ref.getObjectRef() != null) {
            return ref.getObjectRef();
        }

        if (req == null) {
            // programming problem! The request parameter is essential due to Java's class erasure
            throw new T9tException(T9tException.REF_RESOLVER_REQUEST_PARAMETER);
        }
        req.setRef(ref);
        RefResolverResponse serviceResponse = messaging.executeSynchronousAndCheckResult(req, RefResolverResponse.class);
        return serviceResponse.getKey();
    }

    @Override
    public <REF extends Ref, DTO extends REF, TRACKING extends TrackingBase, REQ extends CrudSurrogateKeyRequest<REF, DTO, TRACKING>> DTO getData(REQ req, Long objectRef, boolean onlyActive)
            {
        if (objectRef == null) {
            return null; // play null-safe
        }
        if (req == null) {
            // programming problem! The request parameter is essential due to Java's class erasure
            throw new T9tException(T9tException.REF_RESOLVER_REQUEST_PARAMETER);
        }
        req.setCrud(OperationType.READ);
        req.setKey(objectRef);
        req.setOnlyActive(onlyActive);
        CrudSurrogateKeyResponse<DTO, TRACKING> serviceResponse = messaging.executeSynchronousAndCheckResult(req, CrudSurrogateKeyResponse.class);
        return serviceResponse.getData();
    }

    @Override
    public <REF extends Ref, DTO extends REF, TRACKING extends TrackingBase, REQ extends CrudSurrogateKeyRequest<REF, DTO, TRACKING>> DTO getData(REQ req, REF ref, boolean onlyActive) {
        if (ref == null) {
            return null; // play null-safe
        }
        // shortcut! If we have a long already, there's no need to perform the cross module call (and any DB I/O)
        if (ref.getObjectRef() != null) {
            return getData(req, ref.getObjectRef(), onlyActive);
        }

        if (req == null) {
            // programming problem! The request parameter is essential due to Java's class erasure
            throw new T9tException(T9tException.REF_RESOLVER_REQUEST_PARAMETER);
        }
        req.setCrud(OperationType.READ);
        req.setNaturalKey(ref);
        req.setOnlyActive(onlyActive);
        CrudSurrogateKeyResponse<DTO, TRACKING> serviceResponse = messaging.executeSynchronousAndCheckResult(req, CrudSurrogateKeyResponse.class);
        return serviceResponse.getData();
    }

    @Override
    public <REF extends Ref, DTO extends REF, TRACKING extends TrackingBase, REQ extends CrudSurrogateKeyRequest<REF, DTO, TRACKING>> DTO getData(REQ req, Long objectRef) {
        return getData(req, objectRef, false);
    }

    @Override
    public <REF extends Ref, DTO extends REF, TRACKING extends TrackingBase, REQ extends CrudSurrogateKeyRequest<REF, DTO, TRACKING>> DTO getData(REQ req, REF ref) {
        return getData(req, ref, false);
    }
}
