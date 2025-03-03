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
import com.arvatosystems.t9t.base.crud.CrudModuleCfgRequest;
import com.arvatosystems.t9t.base.crud.CrudModuleCfgResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.IEntityMapper;
import com.arvatosystems.t9t.base.jpa.IResolverStringKey;
import com.arvatosystems.t9t.base.moduleCfg.ModuleConfigDTO;
import com.arvatosystems.t9t.base.moduleCfg.ModuleConfigKey;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// the request handler assumes a resolver which works with a Long key for historical reasons
public abstract class AbstractCrudModuleCfgRequestHandler<
  DTO extends ModuleConfigDTO,
  REQUEST extends CrudModuleCfgRequest<DTO>,
  ENTITY extends BonaPersistableKey<String> & BonaPersistableTracking<FullTrackingWithVersion>
> extends AbstractCrudAnyKeyRequestHandler<String, DTO, FullTrackingWithVersion, REQUEST, ENTITY> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCrudModuleCfgRequestHandler.class);
    private static final ModuleConfigKey FIXED_KEY = new ModuleConfigKey();
    static {
        FIXED_KEY.freeze();
    }

    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);

    // execute function of the interface description, but additional parameters
    // required in order to work around type erasure
    public CrudModuleCfgResponse<DTO> execute(final RequestContext ctx, final IEntityMapper<String, DTO, FullTrackingWithVersion, ENTITY> mapper,
            final IResolverStringKey<FullTrackingWithVersion, ENTITY> resolver, final REQUEST crudRequest) {

        // as the key is empty, provide it if it was not provided by the caller, for convenience
        if (crudRequest.getCrud() != OperationType.CREATE && crudRequest.getKey() == null)
            crudRequest.setKey(FIXED_KEY);

        // fields are set as required
        validateParameters(crudRequest, crudRequest.getKey() == null);

        final CrudModuleCfgResponse<DTO> rs = new CrudModuleCfgResponse<>();
        rs.setReturnCode(0);
        ENTITY result;

        // If changeRequestRef is not null, it means the request is for activation of a change request which was saved in the approval work flow.
        if (crudRequest.getChangeRequestRef() != null) {
            validateChangeRequest(ctx, crudRequest, FIXED_KEY);
            // If the change request is valid, then just treat as a normal crud request to apply changes...
        } else {
            final DTO dto = getRequestDTO(crudRequest, () -> mapper.mapToDto(ctx.tenantId));
            final String pqon = dto.ret$PQON();
            // If the crudRequest requires approval, then create change request and then return the response.
            if (requestRequiresApproval(crudRequest.getCrud(), pqon)) {
                LOGGER.debug("Request requires approval, creating change request");
                requestForApproval(ctx, rs, crudRequest, pqon, FIXED_KEY, dto);
                rs.setKey(FIXED_KEY);
                return rs;
            }
        }

        final EntityManager entityManager = jpaContextProvider.get().getEntityManager(); // copy it as we need it several times

        try {
            switch (crudRequest.getCrud()) {
            case CREATE:
                result = performCreate(mapper, resolver, crudRequest, entityManager);
                rs.setKey(FIXED_KEY);
                break;
            case READ:
                result = resolver.getEntityDataForKey(ctx.tenantId);
                rs.setKey(crudRequest.getKey()); // just copy
                break;
            case DELETE:
                result = resolver.getEntityDataForKey(ctx.tenantId);
                if (!resolver.writeAllowed(resolver.getTenantId(result))) {
                    throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
                }
                validateDelete(result);
                entityManager.remove(result);
                break;
            case INACTIVATE:
                result = resolver.getEntityDataForKey(ctx.tenantId);
                if (!resolver.writeAllowed(resolver.getTenantId(result))) {
                    throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
                }
                result.put$Active(false);
                break;
            case ACTIVATE:
                result = resolver.getEntityDataForKey(ctx.tenantId);
                if (!resolver.writeAllowed(resolver.getTenantId(result))) {
                    throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
                }
                result.put$Active(true);
                break;
            case UPDATE:
                result = performUpdateWithVersion(mapper, resolver, entityManager, ctx.tenantId, crudRequest, rs);
                break;
            case MERGE:
                //If the key is passed in and result already exist then perform update.
                if (isExists(ctx.tenantId, resolver)) {
                    result = performUpdateWithVersion(mapper, resolver, entityManager, ctx.tenantId, crudRequest, rs);
                } else {
                    result = performCreate(mapper, resolver, crudRequest, entityManager);
                    rs.setKey(FIXED_KEY);
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

            // if in cluster mode, send a cache invalidation event
            executor.clearCache(resolver.getBaseJpaEntityClass().getSimpleName(), null);
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


    private boolean isExists(final String tenantId, final IResolverStringKey<FullTrackingWithVersion, ENTITY> resolver) {
        final ENTITY entity = resolver.find(tenantId);
        return entity != null;
    }
}
