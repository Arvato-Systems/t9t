/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.crud.CrudAnyKeyRequest;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyRequest;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IRefGenerator;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.bonaparte.refsw.RefResolver;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;

/**
 * Generic superclass for backend CRUD request handlers for classes with a surrogate key.
 *
 * The execute method will call validateUpdate() for UPDATE and validateCreate() for CREATE.
 * In case of the MERGE operation, either validateUpdate() or validateCreate() will be called,
 * depending on whether the record did already exist or not.
 */
public abstract class AbstractCrudSurrogateKeyBERequestHandler<REF extends Ref, DTO extends REF, TRACKING extends TrackingBase,
    REQUEST extends CrudSurrogateKeyRequest<REF, DTO, TRACKING>> extends AbstractRequestHandler<REQUEST> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCrudSurrogateKeyBERequestHandler.class);

    @Override
    public boolean isReadOnly(REQUEST params) {
        return params.getCrud() == OperationType.READ || params.getCrud() == OperationType.LOOKUP || params.getCrud() == OperationType.VERIFY;
    }

    @Override
    public OperationType getAdditionalRequiredPermission(REQUEST request) {
        return request.getCrud();       // must have permission for the crud operation
    }

    protected final IRefGenerator genericRefGenerator = Jdp.getRequired(IRefGenerator.class);

    protected void checkActive(DTO result, boolean onlyActive) {
        if (onlyActive && !result.ret$Active())
            throw new T9tException(T9tException.RECORD_INACTIVE, result.getObjectRef().toString());
    }

    // plausi checks for parameters
    protected void validateParameters(CrudAnyKeyRequest<DTO, TRACKING> rq, boolean keyIsNull) {
        // check version if required
        // check key
        switch (rq.getCrud()) {
        case CREATE:
            // for CREATE, the KEY is embedded in the data, no separate record
            // required / desired
            if (!keyIsNull) {
                throw new T9tException(T9tException.EXTRA_KEY_PARAMETER, rq.getCrud().toString());
            }
            break;
        default:
            if (keyIsNull) {
                throw new T9tException(T9tException.MISSING_KEY_PARAMETER, rq.getCrud().toString());
            }
        }

        // check data: CREATE and MERGE need data! Anything else should not have it, but plausis deactivated because the UI sends data records
        switch (rq.getCrud()) {
        case CREATE:
        case MERGE:
            if (rq.getData() == null) {
                throw new T9tException(T9tException.MISSING_DATA_PARAMETER, rq.getCrud().toString());
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
     * @throws ApplicationException
     *             if a plausibility check has been found to fail.
     */
    protected void validateUpdate(DTO current, DTO intended) { }

    /**
     * Validates data before it is mapped or persisted. Hook to be overridden by
     * implementors. Can also be used to to adjust the incoming DTO.
     *
     * @param intended
     *            The new data set to be written to disk.
     * @throws ApplicationException
     *             if a plausibility check has been found to fail.
     */
    protected void validateCreate(DTO intended) { }

    /**
     * Validates before deletion. Can also be used to perform additional
     * activity such as cascaded deletes or log entries.
     *
     * @param current
     *            The current data set.
     * @throws ApplicationException
     *             if a plausibility check has been found to fail.
     */
    protected void validateDelete(DTO current) { }

    /** Called before a status is changed active / inactive.
     * Only called if there is a real change.
     * If the change happens as part of an update, it is called after validateUpdate. */
    protected void activationChange(DTO current, boolean newState) { }

    /**
     * Hook which allows to populate fields in the DTO which are not read from
     * disk directly, but also to hide data. Can be used by customizations. The
     * default implementation just returns the parameter unmodified.
     */
    protected void postRead(DTO result) { }


    // execute function of the interface description, but additional parameters
    // required in order to work around type erasure
    protected CrudSurrogateKeyResponse<DTO, TRACKING> execute(RequestContext ctx, REQUEST crudRequest, RefResolver<REF, DTO, TRACKING> resolver) {

        // fields are set as required
        CrudSurrogateKeyResponse<DTO, TRACKING> rs = new CrudSurrogateKeyResponse<DTO, TRACKING>();

        LOGGER.trace("{}: {} called for surrogate key = {}, natural key = {}",
                this.getClass().getSimpleName(),
                crudRequest.getCrud().name(),
                crudRequest.getKey() == null ? "null" : crudRequest.getKey(),
                crudRequest.getNaturalKey() == null ? "null" : crudRequest.getNaturalKey()
                );
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

//        EntityManager entityManager = jpaContextProvider.get().getEntityManager(); // copy it as we need it several times

        // step 1: possible resolution of the natural key
        if (crudRequest.getNaturalKey() != null) {
            try {
                Long refFromCompositeKey = resolver.getRef(crudRequest.getNaturalKey());
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
            } catch (T9tException e) {
                if (e.getErrorCode() != T9tException.RECORD_DOES_NOT_EXIST) {
                    throw e; // we are not interested in this one
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

        DTO dto = crudRequest.getData();

        switch (crudRequest.getCrud()) {
        case CREATE:
            validateCreate(dto); // plausibility check
            dto.setObjectRef(genericRefGenerator.generateRef(dto.ret$rtti()));
            resolver.create(dto);
            rs.setKey(dto.getObjectRef()); // just copy
            break;
        case READ:
            dto = resolver.getDTO(crudRequest.getKey());
            checkActive(dto, crudRequest.getOnlyActive());
            rs.setKey(crudRequest.getKey()); // just copy
            break;
        case DELETE:
            dto = resolver.getDTO(crudRequest.getKey());
            checkActive(dto, crudRequest.getOnlyActive());
//            if (!resolver.writeAllowed(resolver.getTenantId(result))) {
//                throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
//            }
            validateDelete(dto);
            resolver.remove(crudRequest.getKey());
            rs.setKey(crudRequest.getKey());
            break;
        case INACTIVATE:
        case ACTIVATE:
            boolean newState = crudRequest.getCrud() == OperationType.ACTIVATE;
            dto = resolver.getDTO(crudRequest.getKey());
            checkActive(dto, crudRequest.getOnlyActive());
//            if (!resolver.writeAllowed(resolver.getTenantId(result))) {
//                throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
//            }
            if (dto.ret$Active() != newState) {
                // yes, this is a real state change
                activationChange(dto, newState);
                dto.put$Active(newState);
                resolver.update(dto);
            }
            break;
        case UPDATE:
            DTO old = resolver.getDTO(crudRequest.getKey());
            checkActive(old, crudRequest.getOnlyActive());
//          if (!resolver.writeAllowed(resolver.getTenantId(result))) {
//              throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
//          }
            validateUpdate(old, dto); // plausibility
//          mapper.checkNoUpdateFields(result, intended);
//          validateUpdate(result, intended, dto); // plausibility
//          result.mergeFrom(intended);
//            result = performUpdate(mapper, resolver, crudRequest, entityManager, crudRequest.getKey());
            resolver.update(dto);
            rs.setKey(crudRequest.getKey());
            break;
        case MERGE:
            // If the key is passed in and result already exist then perform update.
            if (crudRequest.getKey() != null) {
//                result = performUpdate(mapper, resolver, crudRequest, entityManager, crudRequest.getKey());
                dto.setObjectRef(crudRequest.getKey());         // required????
                validateUpdate(resolver.getDTO(crudRequest.getKey()), dto); // plausibility
                resolver.update(dto);
                rs.setKey(crudRequest.getKey());
            } else {
                validateCreate(dto); // plausibility check
                resolver.create(dto);
                rs.setKey(dto.getObjectRef()); // just copy
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
            postRead(dto);
            rs.setData(dto); // populate result
            if (dto != null) {
                rs.setTracking(resolver.getTracking(dto.getObjectRef())); // populate result
            }
            // result
        }
        rs.setReturnCode(0);
        return rs;
    }
}
