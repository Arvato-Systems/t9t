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
package com.arvatosystems.t9t.base.be.impl;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.services.IDataChangeRequestFlow;
import com.arvatosystems.t9t.base.types.LongKey;
import com.arvatosystems.t9t.server.services.IAuthorize;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.crud.CrudAnyKeyRequest;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyRequest;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IRefResolver;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.Ref;
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
    protected final IDataChangeRequestFlow changeRequestFlow = Jdp.getRequired(IDataChangeRequestFlow.class);
    protected final IAuthorize authorizer = Jdp.getRequired(IAuthorize.class);

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
    protected void validateUpdate(final DTO current, final DTO intended) { }

    /**
     * Validates data before it is mapped or persisted. Hook to be overridden by
     * implementors. Can also be used to to adjust the incoming DTO.
     *
     * @param intended
     *            The new data set to be written to disk.
     * @throws ApplicationException
     *             if a plausibility check has been found to fail.
     */
    protected void validateCreate(final DTO intended) { }

    /**
     * Validates before deletion. Can also be used to perform additional
     * activity such as cascaded deletes or log entries.
     *
     * @param current
     *            The current data set.
     * @throws ApplicationException
     *             if a plausibility check has been found to fail.
     */
    protected void validateDelete(final DTO current) { }

    /** Called before a status is changed active / inactive.
     * Only called if there is a real change.
     * If the change happens as part of an update, it is called after validateUpdate. */
    protected void activationChange(final DTO current, final boolean newState) { }

    /**
     * Hook which allows to populate fields in the DTO which are not read from
     * disk directly, but also to hide data. Can be used by customizations. The
     * default implementation just returns the parameter unmodified.
     */
    protected void postRead(final DTO result) { }


    // execute function of the interface description, but additional parameters
    // required in order to work around type erasure
    protected CrudSurrogateKeyResponse<DTO, TRACKING> execute(final RequestContext ctx, final REQUEST crudRequest,
      final IRefResolver<REF, DTO, TRACKING> resolver) {

        // fields are set as required
        final CrudSurrogateKeyResponse<DTO, TRACKING> rs = new CrudSurrogateKeyResponse<>();

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
                final Long refFromCompositeKey = resolver.getRef(crudRequest.getNaturalKey());
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

        DTO crudData = crudRequest.getData();
        final OperationType crud = crudRequest.getCrud();

        // If changeRequestRef is not null, it means the request is for activation of a change request which was saved in the approval work flow.
        if (crudRequest.getChangeRequestRef() != null) {
            validateChangeRequest(ctx, crudRequest, getLongKey(crudRequest));
            // If the change request is valid, then just treat as a normal crud request to apply changes...
        } else {
            final DTO data = crudData != null ? crudData : resolver.getDTO(crudRequest.getKey());
            final String pqon = data.ret$PQON();
            // If the crudRequest requires approval, then create change request and then return the response.
            if (T9tConstants.OPERATION_TYPE_WRITE.contains(crud) && changeRequestFlow.requireApproval(pqon, crud)) {
                LOGGER.debug("Request requires approval, creating change request");
                requestForApproval(ctx, rs, crudRequest, pqon, getLongKey(crudRequest), data);
                rs.setKey(crudRequest.getKey());
                return rs;
            }
        }

        switch (crud) {
        case CREATE:
            validateCreate(crudData);    // plausibility check
            crudData.setObjectRef(resolver.createNewPrimaryKey());
            resolver.create(crudData);   // permissions checked by resolver
            rs.setKey(crudData.getObjectRef()); // just copy
            break;
        case READ:
            crudData = resolver.getDTO(crudRequest.getKey());
            rs.setKey(crudRequest.getKey()); // just copy
            break;
        case DELETE:
            crudData = resolver.getDTO(crudRequest.getKey());
            validateDelete(crudData);
            resolver.remove(crudRequest.getKey());   // permissions checked by resolver
            rs.setKey(crudRequest.getKey());
            break;
        case INACTIVATE:
        case ACTIVATE:
            final boolean newState = crudRequest.getCrud() == OperationType.ACTIVATE;
            crudData = resolver.getDTO(crudRequest.getKey());
            if (crudData.ret$Active() != newState) {
                // yes, this is a real state change
                activationChange(crudData, newState);
                crudData.put$Active(newState);
                resolver.update(crudData);
            }
            break;
        case UPDATE:
            final DTO old = resolver.getDTO(crudRequest.getKey());
            crudData.setObjectRef(crudRequest.getKey());
            validateUpdate(old, crudData); // plausibility
            resolver.update(crudData);
            rs.setKey(crudRequest.getKey());
            break;
        case MERGE:
            // If the key is passed in and result already exist then perform update.
            if (crudRequest.getKey() != null) {
                crudData.setObjectRef(crudRequest.getKey());         // required????
                validateUpdate(resolver.getDTO(crudRequest.getKey()), crudData); // plausibility
                resolver.update(crudData);
                rs.setKey(crudRequest.getKey());
            } else {
                validateCreate(crudData); // plausibility check
                resolver.create(crudData);
                rs.setKey(crudData.getObjectRef()); // just copy
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
            postRead(crudData);
            rs.setData(crudData); // populate result
            if (crudData != null) {
                rs.setTracking(resolver.getTracking(crudData.getObjectRef())); // populate result
            }
            // result
        }
        rs.setReturnCode(0);
        return rs;
    }

    /**
     * Create a data change request for the CRUD operation.
     *
     * @param ctx           the request context
     * @param response      the response object of the main CRUD request
     * @param crudRequest   the CRUD request
     * @param pqon          the PQON of the DTO
     * @param key           the key for which the data change request is created
     * @param dto           the data to be changed
     */
    @SuppressWarnings("unchecked")
    protected void requestForApproval(@Nonnull final RequestContext ctx, @Nonnull final CrudSurrogateKeyResponse<DTO, TRACKING> response,
        @Nonnull final REQUEST crudRequest, @Nonnull final String pqon, @Nullable final BonaPortable key, @Nonnull final DTO dto) {
        if (crudRequest.getChangeId() == null) {
            throw new T9tException(T9tException.MISSING_CHANGE_ID, "ChangeId is required for approval request");
        }
        if (OperationType.INACTIVATE == crudRequest.getCrud() || OperationType.ACTIVATE == crudRequest.getCrud()) {
            // modify the crud request as an UPDATE request for inactivate/activate
            final DTO data = (DTO) dto.ret$MutableClone(true, false);
            data.put$Active(OperationType.ACTIVATE == crudRequest.getCrud());
            crudRequest.setCrud(OperationType.UPDATE);
            crudRequest.setData(data);
        }
        final Long changeRequestRef = changeRequestFlow.createDataChangeRequest(ctx, pqon, crudRequest.getChangeId(), key, crudRequest,
                crudRequest.getChangeComment(), crudRequest.getSubmitChange());
        response.setChangeRequestRef(changeRequestRef);
        if (crudRequest.getCrud() != OperationType.DELETE) {
            // only send data without tracking fields
            response.setData(crudRequest.getData());
        }
    }

    /**
     * Validate the change request before applying it.
     *
     * @param ctx           the request context
     * @param crudRequest   the CRUD request
     * @param key           the key of the entity
     */
    protected void validateChangeRequest(@Nonnull final RequestContext ctx, @Nonnull final REQUEST crudRequest, @Nullable final BonaPortable key) {
        final Permissionset permissions = authorizer.getPermissions(ctx.internalHeaderParameters.getJwtInfo(), PermissionType.BACKEND, crudRequest.ret$PQON());
        if (!permissions.contains(OperationType.ACTIVATE)) {
            LOGGER.error("No permission to activate change request for {}. key:{}", crudRequest.ret$PQON(), key);
            throw new T9tException(T9tException.CHANGE_REQUEST_PERMISSION_ERROR, "No permission to activate change request for " + crudRequest.ret$PQON());
        }
        if (!changeRequestFlow.isChangeRequestValidToActivate(crudRequest.getChangeRequestRef(), key, crudRequest.getData())) {
            LOGGER.error("Change request is not valid! ref {}. key:{}", crudRequest.getChangeRequestRef(), key);
            throw new T9tException(T9tException.INVALID_CHANGE_REQUEST, "Change request not valid for ref: " + crudRequest.getChangeRequestRef());
        }
    }

    @Nullable
    protected LongKey getLongKey(@Nonnull final REQUEST crudRequest) {
        return crudRequest.getKey() != null ? new LongKey(crudRequest.getKey()) : null;
    }
}
