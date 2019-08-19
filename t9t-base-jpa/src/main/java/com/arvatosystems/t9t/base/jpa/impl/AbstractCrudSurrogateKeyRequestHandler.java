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
package com.arvatosystems.t9t.base.jpa.impl;

import javax.persistence.EntityManager;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyRequest;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.jpa.IEntityMapper;
import com.arvatosystems.t9t.base.jpa.IResolverSurrogateKey;
import com.arvatosystems.t9t.base.services.IRefGenerator;

import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;

public abstract class AbstractCrudSurrogateKeyRequestHandler<
    REF extends Ref,
    DTO extends REF,
    TRACKING extends TrackingBase,
    REQUEST extends CrudSurrogateKeyRequest<REF, DTO, TRACKING>,
    ENTITY extends BonaPersistableKey<Long> & BonaPersistableTracking<TRACKING>
> extends AbstractCrudAnyKeyRequestHandler<Long, DTO, TRACKING, REQUEST, ENTITY> {

    protected final IRefGenerator genericRefGenerator = Jdp.getRequired(IRefGenerator.class);

    // execute function of the interface description, but additional parameters
    // required in order to work around type erasure
    public CrudSurrogateKeyResponse<DTO, TRACKING> execute(IEntityMapper<Long, DTO, TRACKING, ENTITY> mapper,
            IResolverSurrogateKey<REF, TRACKING, ENTITY> resolver, REQUEST crudRequest) {

        // fields are set as required
        CrudSurrogateKeyResponse<DTO, TRACKING> rs = new CrudSurrogateKeyResponse<DTO, TRACKING>();
        ENTITY result;

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

        EntityManager entityManager = jpaContextProvider.get().getEntityManager(); // copy it as we need it several times

        // step 1: possible resolution of the natural key
        if (crudRequest.getNaturalKey() != null) {
            try {
                ENTITY entityFoundByNaturalKeyQuery = resolver.getEntityData(crudRequest.getNaturalKey(), false);
                boolean entityFoundByNaturalKeyQueryIsOfOtherTenant = !resolver.getSharedTenantId().equals(resolver.getTenantId(entityFoundByNaturalKeyQuery));
                Long refFromCompositeKey = entityFoundByNaturalKeyQuery.ret$Key();
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
                if (crudRequest.getCrud() == OperationType.MERGE && entityFoundByNaturalKeyQueryIsOfOtherTenant) {
                    // FT-2875: cannot use this one! It would create an access violation. By ordering of result set, we know there is no entry for the current tenant.
                    rs.setKey(null);
                    crudRequest.setKey(null);
                    crudRequest.setCrud(OperationType.CREATE);
                }
            } catch (T9tException e) {
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
                // HACK: for CREATE, must provide the new artificial key to the mapper, just in case the entity
                // has child entities which reference this. The only way to pass it into the mapper is via the DTO...
                Long newKey = resolver.createNewPrimaryKey();
                crudRequest.getData().setObjectRef(newKey);
                result = performCreate(mapper, resolver, crudRequest, entityManager);
                rs.setKey(result.ret$Key()); // just copy
                break;
            case READ:
                result = resolver.findActive(crudRequest.getKey(), crudRequest.getOnlyActive());
                rs.setKey(crudRequest.getKey()); // just copy
                break;
            case DELETE:
                result = resolver.findActive(crudRequest.getKey(), crudRequest.getOnlyActive());
                if (!resolver.writeAllowed(resolver.getTenantId(result))) {
                    throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
                }
                validateDelete(result);
                entityManager.remove(result);
                rs.setKey(crudRequest.getKey());
                break;
            case INACTIVATE:
                result = resolver.findActive(crudRequest.getKey(), crudRequest.getOnlyActive());
                if (!resolver.writeAllowed(resolver.getTenantId(result))) {
                    throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
                }
                result.put$Active(false);
                break;
            case ACTIVATE:
                result = resolver.findActive(crudRequest.getKey(), crudRequest.getOnlyActive());
                if (!resolver.writeAllowed(resolver.getTenantId(result))) {
                    throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
                }
                result.put$Active(true);
                break;
            case UPDATE:
                // HACK
                crudRequest.getData().setObjectRef(crudRequest.getKey());
                result = performUpdate(mapper, resolver, crudRequest, entityManager, crudRequest.getKey());
                rs.setKey(crudRequest.getKey());
                break;
            case MERGE:
                //If the key is passed in and result already exist then perform update.
                if (crudRequest.getKey() != null) {
                    // HACK
                    crudRequest.getData().setObjectRef(crudRequest.getKey());
                    rs.setKey(crudRequest.getKey());
                    result = performUpdate(mapper, resolver, crudRequest, entityManager, crudRequest.getKey());
                } else {
                    Long newKey1 = resolver.createNewPrimaryKey();
                    crudRequest.getData().setObjectRef(newKey1);
                    result = performCreate(mapper, resolver, crudRequest, entityManager);
                    rs.setKey(result.ret$Key()); // just copy
                }
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
            rs.setReturnCode(0);
            return rs;
        } catch (T9tException e) {
            // careful! Catching only ApplicationException masks standard T9tExceptions such as RECORD_INACTIVE or RECORD_NOT_FOUND!
            // We must return the original exception if we got a T9tException already!
            // Therefore this catch is essential!
            throw e;
        } catch (ApplicationException e) {
            throw new T9tException(T9tException.ENTITY_DATA_MAPPING_EXCEPTION, "Tracking columns: " + e.toString());
        }
    }

}
