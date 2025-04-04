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
import com.arvatosystems.t9t.base.crud.CrudSuperclassKeyRequest;
import com.arvatosystems.t9t.base.crud.CrudSuperclassKeyResponse;
import com.arvatosystems.t9t.base.jpa.IEntityMapper;
import com.arvatosystems.t9t.base.jpa.IResolverSuperclassKey;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.util.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCrudSuperclassKeyRequestHandler<
  REF extends BonaPortable,
  KEY extends REF,
  DTO extends KEY,
  TRACKING extends TrackingBase,
  REQUEST extends CrudSuperclassKeyRequest<REF, KEY, DTO, TRACKING>,
  ENTITY extends BonaPersistableKey<KEY> & BonaPersistableTracking<TRACKING>
> extends AbstractCrudAnyKeyRequestHandler<KEY, DTO, TRACKING, REQUEST, ENTITY> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCrudSuperclassKeyRequestHandler.class);

    // execute function of the interface description, but additional parameters
    // required in order to work around type erasure
    public CrudSuperclassKeyResponse<KEY, DTO, TRACKING> execute(
            final RequestContext ctx, final IEntityMapper<KEY, DTO, TRACKING, ENTITY> mapper,
            final IResolverSuperclassKey<REF, KEY, TRACKING, ENTITY> resolver,
            final REQUEST crudRequest) {

        // fields are set as required
        final CrudSuperclassKeyResponse<KEY, DTO, TRACKING> rs = new CrudSuperclassKeyResponse<>();
        rs.setReturnCode(0);
        ENTITY result;

        // If changeRequestRef is not null, it means the request is for activation of a change request which was saved in the approval work flow.
        if (crudRequest.getChangeRequestRef() != null) {
            validateChangeRequest(ctx, crudRequest, crudRequest.getKey());
            // If the change request is valid, then just treat as a normal crud request to apply changes...
        } else {
            final DTO dto = getRequestDTO(crudRequest, () -> crudRequest.getKey() != null ? mapper.mapToDto(crudRequest.getKey()) : null);
            final String pqon = dto.ret$PQON();
            // If the crudRequest requires approval, then create change request and then return the response.
            if (requestRequiresApproval(crudRequest.getCrud(), pqon)) {
                LOGGER.debug("Request requires approval, creating change request");
                requestForApproval(ctx, rs, crudRequest, pqon, crudRequest.getKey(), dto);
                rs.setKey(crudRequest.getKey());
                return rs;
            }
        }

        // check natural key.
        switch (crudRequest.getCrud()) {
        case MERGE:
        case VERIFY:
        case LOOKUP:
            if (crudRequest.getNaturalKey() == null) {
                throw new T9tException(T9tException.CRUD_NATURAL_KEY_MISSING, crudRequest.getCrud());
            }
            break;
        default:
        }

        final EntityManager entityManager = jpaContextProvider.get().getEntityManager(); // copy it as we need it several times

        // step 1: possible resolution of the natural key
        if (crudRequest.getNaturalKey() != null) {
            try {
                final KEY refFromCompositeKey = resolver.getEntityData(crudRequest.getNaturalKey()).ret$Key();
                // provide it into the response
                rs.setKey(refFromCompositeKey);

                if (crudRequest.getKey() == null) {
                    // use the obtained reference
                    crudRequest.setKey(refFromCompositeKey);
                } else {
                    // both exists, compare them. If they are not equal, fail in any case
                    if (!crudRequest.getKey().equals(refFromCompositeKey)) {
                        throw new T9tException(T9tException.CRUD_BOTH_KEYS_MISMATCH, crudRequest.getKey().toString() + " <> " + refFromCompositeKey.toString());
                    }
                }
            } catch (final T9tException e) {
                if (e.getErrorCode() != T9tException.RECORD_DOES_NOT_EXIST) {
                    throw e; // we are not interested int his one
                    // deal with non-existing records. In some cases, this is acceptable
                }
                // natural key has been specified, but record does not exist.
                // we throw an exception as well, unless it is LOOKUP (in which case we return null) or MERGE (in which case we perform a CREATE)
                switch (crudRequest.getCrud()) {
                case LOOKUP:
                    // missing data is OK, we return null
                    rs.setReturnCode(0);
                    return rs;
                case MERGE:
                    // missing data is OK, this means we perform a CREATE
                    crudRequest.setCrud(OperationType.CREATE);
                    break;
                default:
                    // any other case is a problem
                    throw e;
                }
            }
        }

        // This was not there before, but should be present to avoid NPEs!
        validateParameters(crudRequest, crudRequest.getKey() == null);

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
                rs.setKey(crudRequest.getKey());
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
                rs.setKey(crudRequest.getKey());
                break;
            case MERGE:
                // If the key is passed in and result already exist then perform update.
                if (crudRequest.getKey() != null) {
                    result = performUpdateWithVersion(mapper, resolver, entityManager, crudRequest.getKey(), crudRequest, rs);
                } else {
                    result = performCreate(mapper, resolver, crudRequest, entityManager);
                    rs.setKey(result.ret$Key()); // just copy
                }
                rs.setKey(crudRequest.getKey());
                break;
            case VERIFY:
                rs.setReturnCode(0);
                return rs; // return here as we have no data (cannot fill tracking)
            case LOOKUP:
                // data was found, due to parameter verification
                rs.setReturnCode(0);
                return rs; // return here as we have no data (cannot fill tracking)
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
