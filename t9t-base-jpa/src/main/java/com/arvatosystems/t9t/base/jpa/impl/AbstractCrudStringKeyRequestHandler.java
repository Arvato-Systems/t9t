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
package com.arvatosystems.t9t.base.jpa.impl;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.crud.CrudStringKeyRequest;
import com.arvatosystems.t9t.base.crud.CrudStringKeyResponse;
import com.arvatosystems.t9t.base.jpa.IEntityMapper;
import com.arvatosystems.t9t.base.jpa.IResolverStringKey;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.util.ApplicationException;
import jakarta.persistence.EntityManager;

public abstract class AbstractCrudStringKeyRequestHandler<
  DTO extends BonaPortable,
  TRACKING extends TrackingBase,
  REQUEST extends CrudStringKeyRequest<DTO, TRACKING>,
  ENTITY extends BonaPersistableKey<String> & BonaPersistableTracking<TRACKING>
> extends AbstractCrudAnyKeyRequestHandler<String, DTO, TRACKING, REQUEST, ENTITY> {

    // execute function of the interface description, but additional parameters
    // required in order to work around type erasure
    public CrudStringKeyResponse<DTO, TRACKING> execute(final RequestContext ctx, final IEntityMapper<String, DTO, TRACKING, ENTITY> mapper,
            final IResolverStringKey<TRACKING, ENTITY> resolver, final REQUEST crudRequest) {

        // fields are set as required
        validateParameters(crudRequest, crudRequest.getKey() == null);

        final CrudStringKeyResponse<DTO, TRACKING> rs = new CrudStringKeyResponse<>();
        rs.setReturnCode(0);
        ENTITY result;

        final EntityManager entityManager = jpaContextProvider.get().getEntityManager(); // copy it as we need it several times

        try {
            switch (crudRequest.getCrud()) {
            case CREATE:
                result = performCreate(mapper, resolver, crudRequest, entityManager);
                rs.setKey(result.ret$Key()); // just copy
                break;
            case READ:
                result = resolver.getEntityDataForKey(crudRequest.getKey());
                rs.setKey(crudRequest.getKey()); // just copy
                break;
            case DELETE:
                result = resolver.getEntityDataForKey(crudRequest.getKey());
                if (!resolver.writeAllowed(resolver.getTenantId(result))) {
                    throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
                }
                validateDelete(result);
                entityManager.remove(result);
                break;
            case INACTIVATE:
                result = resolver.getEntityDataForKey(crudRequest.getKey());
                if (!resolver.writeAllowed(resolver.getTenantId(result))) {
                    throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
                }
                result.put$Active(false);
                break;
            case ACTIVATE:
                result = resolver.getEntityDataForKey(crudRequest.getKey());
                if (!resolver.writeAllowed(resolver.getTenantId(result))) {
                    throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
                }
                result.put$Active(true);
                break;
            case UPDATE:
                result = performUpdateWithVersion(mapper, resolver, entityManager, crudRequest.getKey(), crudRequest, rs);
                break;
            case MERGE:
                // If the key is passed in and result already exist then perform update.
                if (crudRequest.getKey() != null) {
                    // at least attempt to read first
                    result = resolver.find(crudRequest.getKey());
                    if (result != null) {
                        result = performUpdateWithVersion(mapper, resolver, entityManager, crudRequest.getKey(), crudRequest, rs);
                        rs.setKey(result.ret$Key()); // just copy
                    } else {
                        // did not exist: perform create
                        result = performCreate(mapper, resolver, crudRequest, entityManager);
                        rs.setKey(result.ret$Key()); // just copy
                    }
                } else {
                    result = performCreate(mapper, resolver, crudRequest, entityManager);
                    rs.setKey(result.ret$Key()); // just copy
                }
                break;
            default:
                throw new T9tException(T9tException.INVALID_CRUD_COMMAND);
            }
            if (crudRequest.getCrud() != OperationType.DELETE) {
                rs.setTracking(result.ret$Tracking()); // populate result
                rs.setData(postRead(mapper.mapToDto(result), result)); // populate
                // result
            }
            return rs;
        } catch (final T9tException e) {
            // careful! Catching only ApplicationException masks standard T9tExceptions such as RECORD_INACTIVE or RECORD_NOT_FOUND!
            // We must return the original exception if we got a T9tException already!
            // Therefore this catch is essential!
            throw e;
        } catch (final ApplicationException e) {
            throw new T9tException(T9tException.ENTITY_DATA_MAPPING_EXCEPTION, "Tracking columns: " + e.toString());
        }
    }
}
