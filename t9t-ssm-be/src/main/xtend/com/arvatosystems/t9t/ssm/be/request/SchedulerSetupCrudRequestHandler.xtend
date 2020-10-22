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
package com.arvatosystems.t9t.ssm.be.request

import com.arvatosystems.t9t.auth.ApiKeyDTO
import com.arvatosystems.t9t.auth.PermissionsDTO
import com.arvatosystems.t9t.auth.UserKey
import com.arvatosystems.t9t.auth.request.ApiKeyCrudRequest
import com.arvatosystems.t9t.base.MessagingUtil
import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.be.impl.AbstractCrudSurrogateKeyBERequestHandler
import com.arvatosystems.t9t.base.be.impl.CrossModuleRefResolver
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion
import com.arvatosystems.t9t.base.services.IExecutor
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.core.CannedRequestDTO
import com.arvatosystems.t9t.core.services.ICannedRequestResolver
import com.arvatosystems.t9t.ssm.SchedulerSetupDTO
import com.arvatosystems.t9t.ssm.SchedulerSetupRef
import com.arvatosystems.t9t.ssm.be.impl.Workarounds
import com.arvatosystems.t9t.ssm.request.SchedulerSetupCrudRequest
import com.arvatosystems.t9t.ssm.services.ISchedulerService
import com.arvatosystems.t9t.ssm.services.ISchedulerSetupResolver
import com.google.common.base.Joiner
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.dp.Inject
import de.jpaw.dp.Provider
import java.util.UUID

@AddLogger
class SchedulerSetupCrudRequestHandler extends AbstractCrudSurrogateKeyBERequestHandler<SchedulerSetupRef, SchedulerSetupDTO, FullTrackingWithVersion, SchedulerSetupCrudRequest> {

    @Inject IExecutor               executor
    @Inject ISchedulerSetupResolver resolver
    @Inject ISchedulerService       schedulerService
    @Inject CrossModuleRefResolver  refResolver
    @Inject ICannedRequestResolver  rqResolver
    @Inject Provider<RequestContext> ctxProvider

    override void validateUpdate(SchedulerSetupDTO current, SchedulerSetupDTO intended) {
        if (intended.apiKey === null) {
            LOGGER.debug("UPDATE Scheduler: no API-Key provided, using old one")
            intended.apiKey = current.apiKey
        } else {
            LOGGER.debug("UPDATE Scheduler with provided API-Key")
           }
    }

    override void validateCreate(SchedulerSetupDTO intended) {
        if (intended.apiKey === null) {
            LOGGER.debug("CREATE new Scheduler: no API-Key provided, creating one")
            createApiKey(intended)
        } else {
            LOGGER.debug("CREATE new Scheduler with provided API-Key")
           }
    }

    override CrudSurrogateKeyResponse<SchedulerSetupDTO, FullTrackingWithVersion> execute(RequestContext ctx, SchedulerSetupCrudRequest crudRequest) throws Exception {
        val oldSetup = if (crudRequest.crud == OperationType.DELETE || crudRequest.crud == OperationType.UPDATE ) {
            if (crudRequest.key !== null)
                resolver.getDTO(crudRequest.key)
            else if (crudRequest.naturalKey !== null)
                resolver.getDTO(crudRequest.naturalKey);
        }

        if (crudRequest.crud == OperationType.CREATE || crudRequest.crud == OperationType.UPDATE) {
            crudRequest.data.request = Workarounds.getData(refResolver, crudRequest.data.request)   // refResolver.getData(new CannedRequestCrudRequest, crudRequest.data.request)
        }

        // perform the regular CRUD
        val response = execute(ctx, crudRequest, resolver);

        if (response.returnCode == 0) {
            val setup = response.data
            switch (crudRequest.crud) {
            case ACTIVATE:
                schedulerService.createScheduledJob(setup)
            case CREATE:
                if (setup.getIsActive())
                    schedulerService.createScheduledJob(setup)
            case DELETE:
                if (oldSetup.getIsActive())
                    schedulerService.removeScheduledJob(oldSetup.schedulerId)
            case INACTIVATE:
                schedulerService.removeScheduledJob(setup.schedulerId)
            case UPDATE:
                updateSchedulerEntry(oldSetup, setup)
            default:
                {}
            }
        }
        // check for suppression of request object
        if (Boolean.TRUE == crudRequest.suppressResponseParameters) {
            if (response.data?.request !== null) {
                (response.data.request as CannedRequestDTO).request = null
            }
        }
        return response;
    }

    def void createApiKey(SchedulerSetupDTO dto) {
        val ctx = ctxProvider.get
        val jwt = ctx.internalHeaderParameters.jwtInfo

        if (dto !== null && dto.apiKey === null) {
            // merge default permission and additional permissions
            val rqRef = dto.getRequest();
            val requestDTO = if (rqRef instanceof CannedRequestDTO) rqRef else rqResolver.getDTO(rqRef);
            var permissions = MessagingUtil.toPerm(requestDTO.jobRequestObjectName);
            if (dto.additionalPermissions !== null) {
                 permissions = Joiner.on(",").join(permissions, dto.additionalPermissions)
            }

            // create an API key. Use the current user's effective permissions
            val apiKeyDTO = new ApiKeyDTO => [
                userRef         = new UserKey(dto.userId)
                apiKey          = UUID.randomUUID
                isActive        = true
                name            = 'automatically created by SSM for scheduled task' //+ dto.requestId
                permissions             = new PermissionsDTO => [
                    minPermissions      = jwt.permissionsMin
                    maxPermissions      = jwt.permissionsMax
                    logLevel            = jwt.logLevel
                    logLevelErrors      = jwt.logLevelErrors
                    resourceIsWildcard  = true
                ]
            ]
            apiKeyDTO.permissions.resourceRestriction = permissions

            val rq      = new ApiKeyCrudRequest
            rq.crud     = OperationType.CREATE
            rq.data     = apiKeyDTO
            dto.apiKey  = apiKeyDTO.apiKey      // expect a good response
            try {
                executor.executeSynchronousAndCheckResult(ctx, rq, CrudSurrogateKeyResponse)
            } catch (T9tException e) {
                if (e.errorCode != T9tException.RECORD_ALREADY_EXISTS)
                    throw e;  // else ignore: API-Key already created, do not change!
            }
        }
    }

    def private void updateSchedulerEntry(SchedulerSetupDTO oldSetup, SchedulerSetupDTO setup) {
        if (setup.getIsActive()) {
            // the new job is active
            if (oldSetup.getIsActive()) {
                // It was active before, now still active => update the other data
                schedulerService.updateScheduledJob(oldSetup, setup);
            } else {
                // was not active before: create it now!
                schedulerService.createScheduledJob(setup);
            }
        } else {
            // the new job is not active. Delete any old one
            if (oldSetup.getIsActive()) {
                schedulerService.removeScheduledJob(oldSetup.getSchedulerId());
            }
        }
    }

}
