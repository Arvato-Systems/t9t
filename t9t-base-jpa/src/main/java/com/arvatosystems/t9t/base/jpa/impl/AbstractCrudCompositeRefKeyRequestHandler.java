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
package com.arvatosystems.t9t.base.jpa.impl;

import jakarta.persistence.EntityManager;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.crud.CrudCompositeKeyRequest;
import com.arvatosystems.t9t.base.crud.CrudCompositeKeyResponse;
import com.arvatosystems.t9t.base.jpa.IEntityMapper;
import com.arvatosystems.t9t.base.jpa.IResolverCompositeKey;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.CompositeKeyBase;
import de.jpaw.bonaparte.pojos.api.CompositeKeyRef;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.util.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCrudCompositeRefKeyRequestHandler<
  REF extends CompositeKeyRef,
  KEY extends REF,
  DTO extends CompositeKeyBase,
  TRACKING extends TrackingBase,
  REQUEST extends CrudCompositeKeyRequest<REF, DTO, TRACKING>,
  ENTITY extends BonaPersistableKey<KEY> & BonaPersistableTracking<TRACKING>
> extends AbstractCrudAnyKeyRequestHandler<KEY, DTO, TRACKING, REQUEST, ENTITY> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCrudCompositeRefKeyRequestHandler.class);

    // execute function of the interface description, but additional parameters
    // required in order to work around type erasure
    public CrudCompositeKeyResponse<KEY, DTO, TRACKING> execute(final RequestContext ctx, final IEntityMapper<KEY, DTO, TRACKING, ENTITY> mapper,
            final IResolverCompositeKey<REF, KEY, TRACKING, ENTITY> resolver, final REQUEST crudRequest) {

        // convert any REF key type to KEY, if provided
        final KEY key = resolver.refToKey(crudRequest.getKey());

        // fields are set as required
        validateParameters(crudRequest, key == null);

        final CrudCompositeKeyResponse<KEY, DTO, TRACKING> rs = new CrudCompositeKeyResponse<>();
        rs.setReturnCode(0);
        ENTITY result;

        // If changeRequestRef is not null, it means the request is for activation of a change request which was saved in the approval work flow.
        if (crudRequest.getChangeRequestRef() != null) {
            validateChangeRequest(ctx, crudRequest, crudRequest.getKey());
            // If the change request is valid, then just treat as a normal crud request to apply changes...
        } else {
            final DTO dto = getRequestDTO(crudRequest, () -> key != null ? mapper.mapToDto(key) : null);
            final String pqon = dto.ret$PQON();
            // If the crudRequest requires approval, then create change request and then return the response.
            if (requestRequiresApproval(crudRequest.getCrud(), pqon)) {
                LOGGER.debug("Request requires approval, creating change request");
                requestForApproval(ctx, rs, crudRequest, pqon, crudRequest.getKey(), dto);
                rs.setKey(key);
                return rs;
            }
        }

        final EntityManager entityManager = jpaContextProvider.get().getEntityManager(); // copy it as we need it several times

        try {
            switch (crudRequest.getCrud()) {
            case CREATE:
                result = performCreate(mapper, resolver, crudRequest, entityManager);
                rs.setKey(result.ret$Key()); // just copy
                break;
            case READ:
                result = resolver.getEntityDataForKey(key);
                rs.setKey(key); // just copy
                break;
            case DELETE:
                result = resolver.getEntityDataForKey(key);
                if (!resolver.writeAllowed(resolver.getTenantId(result))) {
                    throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
                }
                validateDelete(result);
                entityManager.remove(result);
                break;
            case INACTIVATE:
                result = resolver.getEntityDataForKey(key);
                if (!resolver.writeAllowed(resolver.getTenantId(result))) {
                    throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
                }
                result.put$Active(false);
                break;
            case ACTIVATE:
                result = resolver.getEntityDataForKey(key);
                if (!resolver.writeAllowed(resolver.getTenantId(result))) {
                    throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
                }
                result.put$Active(true);
                break;
            case UPDATE:
                result = performUpdateWithVersion(mapper, resolver, entityManager, key, crudRequest, rs);
                break;
            case MERGE:
                // If the key is passed in and result already exist then perform update.
                if (recordExists(key, resolver)) {
                    result = performUpdateWithVersion(mapper, resolver, entityManager, key, crudRequest, rs);
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
            throw new T9tException(T9tException.ENTITY_DATA_MAPPING_EXCEPTION, "Tracking columns: "
                    + e.toString());
        }
    }

    /** recordExists is used for the MERGE operation and next to existence of the record also ensures that the record
     * is of the current tenant. This implies that MERGE operations only work for the current tenant, which is OK
     * for all entities except the Tenant entity itself. The tenant however is not of key type "composite", but "surrogate".
     * @param key
     * @param resolver
     * @return
     */
    protected boolean recordExists(final KEY key, final IResolverCompositeKey<REF, KEY, TRACKING, ENTITY> resolver) {
        try {
            final ENTITY entity = resolver.find(key);
            return entity != null && resolver.isOfMatchingTenant(entity);
        } catch (final T9tException e) {
            return false;
        }
    }
}
