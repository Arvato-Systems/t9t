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

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.crud.CrudAnyKeyRequest;
import com.arvatosystems.t9t.base.crud.CrudAnyKeyResponse;
import com.arvatosystems.t9t.base.entities.BucketTracking;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.IEntityMapper;
import com.arvatosystems.t9t.base.jpa.IResolverAnyKey;
import com.arvatosystems.t9t.base.jpa.ormspecific.IJpaCrudTechnicalExceptionMapper;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;

public abstract class AbstractCrudAnyKeyRequestHandler<
  KEY extends Serializable,
  DTO extends BonaPortable,
  TRACKING extends TrackingBase,
  REQUEST extends CrudAnyKeyRequest<DTO, TRACKING>,
  ENTITY extends BonaPersistableKey<KEY> & BonaPersistableTracking<TRACKING>
> extends AbstractRequestHandler<REQUEST> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCrudAnyKeyRequestHandler.class);

    protected final Provider<PersistenceProviderJPA> jpaContextProvider = Jdp.getProvider(PersistenceProviderJPA.class);
    protected final List<IJpaCrudTechnicalExceptionMapper> crudTechnicalExceptionMappers = Jdp.getAll(IJpaCrudTechnicalExceptionMapper.class);

    @Override
    public boolean isReadOnly(final REQUEST params) {
        return params.getCrud() == OperationType.READ || params.getCrud() == OperationType.LOOKUP || params.getCrud() == OperationType.VERIFY;
    }

    @Override
    public OperationType getAdditionalRequiredPermission(final REQUEST request) {
        return request.getCrud();       // must have permission for the crud operation
    }

    // plausi checks for parameters
    protected void validateParameters(final CrudAnyKeyRequest<DTO, TRACKING> rq, final boolean keyIsNull) {
        // check version, if required
        // check data: CREATE and MERGE need data! Anything else should not have it, but plausis deactivated because the UI sends data records
        switch (rq.getCrud()) {
        // for CREATE, the KEY is embedded in the data, no separate record
        // required / desired
        case CREATE:
            if (!keyIsNull) {
                throw new T9tException(T9tException.EXTRA_KEY_PARAMETER, rq.getCrud().toString());
            }
            // fall through
        case MERGE:
        case UPDATE:
            if (rq.getData() == null) {
                throw new T9tException(T9tException.MISSING_DATA_PARAMETER, rq.getCrud().toString());
            }
            break;
        case READ:
        case ACTIVATE:
        case INACTIVATE:
        case DELETE:
            if (keyIsNull) {
                throw new T9tException(T9tException.MISSING_KEY_PARAMETER, rq.getCrud().toString());
            }
            break;
        default:
        }
    }

    /**
     * Validates data before it is mapped or modified. Hook to be overridden by
     * implementors. Can also be used to to adjust the incoming DTO.
     *
     * @param current
     *            the currently existing set of data for UPDATE operations.
     * @param intended
     *            The new data set to be written to disk.
     * @throws T9tException
     *             if a plausibility check has been found to fail.
     */
    protected void validateUpdate(final ENTITY current, final DTO intended) {
        // no operation in the default implementation, provided as a hook for
        // customization
    }

//    /**
//     * Validates data after it has been mapped, but before any modified data is
//     * written to the database. Hook to be overridden by implementors.
//     *
//     * @param current
//     *            the currently existing set of data for UPDATE operations.
//     * @param intended
//     *            The new data set to be written to disk.
//     * @param dto
//     *            The original DTO (or modified one by the first hook), for
//     *            reference.
//     * @throws T9tException
//     *             if a plausibility check has been found to fail.
//     */
//    protected void validateUpdate(ENTITY current, ENTITY intended, DTO dto) {
//        // no operation in the default implementation, provided as a hook for
//        // customization
//    }

    /**
     * Validates data before it is mapped or persisted. Hook to be overridden by
     * implementors. Can also be used to to adjust the incoming DTO.
     *
     * @param intended
     *            The new data set to be written to disk.
     * @throws T9tException
     *             if a plausibility check has been found to fail.
     */
    protected void validateCreate(final DTO intended) {
        // no operation in the default implementation, provided as a hook for
        // customization
    }

    /**
     * Validates data after it has been mapped, but before any modified data is
     * written to the database. Hook to be overridden by implementors.
     *
     * @param intended
     *            The new data set to be written to disk.
     * @param dto
     *            The original DTO (or modified one by the first hook), for
     *            reference.
     * @throws T9tException
     *             if a plausibility check has been found to fail.
     */
    protected void validateCreate(final ENTITY intended, final DTO dto) {
        // no operation in the default implementation, provided as a hook for
        // customization
    }

    /**
     * Validates before deletion. Can also be used to perform additional
     * activity such as cascaded deletes or log entries.
     *
     * @param current
     *            The current data set.
     * @throws T9tException
     *             if a plausibility check has been found to fail.
     */
    protected void validateDelete(final ENTITY current) {
        // no operation in the default implementation, provided as a hook for
        // customization
    }

    /**
     * Hook which allows to populate fields in the DTO which are not read from
     * disk directly, but also to hide data. Can be used by customizations. The
     * default implementation just returns the parameter unmodified.
     */
    protected DTO postRead(final DTO result, final ENTITY data) {
        return result;
    }

    /**
     * Maps technical exception which occurred during entity crud operation to
     * t9t business exception. If mapping is not possible then original
     * technical exception is thrown further.
     *
     * @param technicalException
     *            caught exception that can be thrown during entity crud
     *            operation
     * @throws T9tException
     *             t9t specific exception.
     */
    protected void handleCrudTechnicalException(final PersistenceException technicalException) {
        if (crudTechnicalExceptionMappers != null) {
            for (final IJpaCrudTechnicalExceptionMapper exceptionMapper : crudTechnicalExceptionMappers) {
                if (exceptionMapper.handles(technicalException)) {
                    throw exceptionMapper.mapException(technicalException);
                }
            }
        }
        throw technicalException;
    }


    protected ENTITY performCreate(final IEntityMapper<KEY, DTO, TRACKING, ENTITY> mapper, final IResolverAnyKey<KEY, TRACKING, ENTITY> resolver,
            final REQUEST crudRequest, final EntityManager entityManager) {
        // check
        validateCreate(crudRequest.getData()); // plausibility check
        final ENTITY result = mapper.mapToEntity(crudRequest.getData());
        validateCreate(result, crudRequest.getData()); // plausibility

        try {
            resolver.save(result);
            entityManager.flush();
            entityManager.refresh(result); // update references to other entities
        } catch (final PersistenceException e) {
            handleCrudTechnicalException(e);
        }

        return result;
    }

    protected ENTITY performUpdate(final IEntityMapper<KEY, DTO, TRACKING, ENTITY> mapper, final IResolverAnyKey<KEY, TRACKING, ENTITY> resolver,
            final REQUEST crudRequest, final EntityManager entityManager, final KEY key, final ENTITY result) {
        final DTO dto = crudRequest.getData();
        if (result == null) {
            throw new T9tException(T9tException.WRITE_ACCESS_NOT_FOUND_PROBABLY_OTHER_TENANT,
              "key is " + key.toString() + " of class " + key.getClass().getSimpleName());
        }
        if (!resolver.writeAllowed(resolver.getTenantId(result))) {
            LOGGER.error("WRITE operation on {} for key {} rejected because other tenant", result.getClass().getSimpleName(), key);
            throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT, result.getClass().getSimpleName() + ": " + key);
        }
        validateUpdate(result, dto); // plausibility check
        mapper.checkNoUpdateFields(result, dto);
        // now the mapping must go to the resolved entity and not to some new object, because otherwise child entities are not available
        mapper.merge2Entity(result, dto);

        try {
            entityManager.flush();
            entityManager.refresh(result); // update references to other entities
        } catch (final PersistenceException e) {
            handleCrudTechnicalException(e);
        }

        return result;
    }

    protected ENTITY performUpdateWithVersion(final IEntityMapper<KEY, DTO, TRACKING, ENTITY> mapper, final IResolverAnyKey<KEY, TRACKING, ENTITY> resolver,
        final EntityManager entityManager, final KEY key, final REQUEST crudRequest, final CrudAnyKeyResponse<DTO, TRACKING> crudResponse) {
        final ENTITY entity = resolver.find(key);
        if (!isVersionValid(crudRequest, entity)) {
            crudResponse.setReturnCode(T9tException.UPDATE_DECLINED);
            crudResponse.setErrorMessage(T9tException.codeToString(T9tException.UPDATE_DECLINED));
            return entity;
        } else {
            return performUpdate(mapper, resolver, crudRequest, entityManager, key, entity);
        }
    }

    private boolean isVersionValid(final REQUEST crudRequest, final ENTITY entity) {
        return crudRequest.getVersion() == null // for force update
            || (entity.ret$Tracking() instanceof FullTrackingWithVersion fullTracking && crudRequest.getVersion() == fullTracking.getVersion())
            || (entity.ret$Tracking() instanceof BucketTracking bucketTracking && crudRequest.getVersion() == bucketTracking.getVersion());
    }
}
