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
package com.arvatosystems.t9t.ssm.be.request;

import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.auth.ApiKeyDTO;
import com.arvatosystems.t9t.auth.PermissionsDTO;
import com.arvatosystems.t9t.auth.UserKey;
import com.arvatosystems.t9t.auth.request.ApiKeyCrudRequest;
import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.RandomNumberGenerators;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.be.impl.AbstractCrudSurrogateKeyBERequestHandler;
import com.arvatosystems.t9t.base.be.impl.CrossModuleRefResolver;
import com.arvatosystems.t9t.base.be.impl.SimpleCallOutExecutor;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.request.ExecuteOnAllNodesRequest;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.IForeignRequest;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.UplinkConfiguration;
import com.arvatosystems.t9t.core.CannedRequestDTO;
import com.arvatosystems.t9t.core.CannedRequestRef;
import com.arvatosystems.t9t.core.request.CannedRequestCrudRequest;
import com.arvatosystems.t9t.core.services.ICannedRequestResolver;
import com.arvatosystems.t9t.ssm.SchedulerSetupDTO;
import com.arvatosystems.t9t.ssm.SchedulerSetupRef;
import com.arvatosystems.t9t.ssm.event.SchedulerChangedEvent;
import com.arvatosystems.t9t.ssm.request.SchedulerSetupCrudRequest;
import com.arvatosystems.t9t.ssm.request.UpdateSchedulerDataRequest;
import com.arvatosystems.t9t.ssm.services.ISchedulerSetupResolver;
import com.google.common.base.Joiner;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;

public class SchedulerSetupCrudRequestHandler extends
  AbstractCrudSurrogateKeyBERequestHandler<SchedulerSetupRef, SchedulerSetupDTO, FullTrackingWithVersion, SchedulerSetupCrudRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerSetupCrudRequestHandler.class);
    private static final EnumSet<OperationType> OPERATION_TYPES_WITH_OLD_DTO = EnumSet.of(
        OperationType.DELETE, OperationType.UPDATE, OperationType.INACTIVATE, OperationType.ACTIVATE // OperationType.MERGE ???
    );
    private static final EnumSet<OperationType> OPERATION_TYPES_CRUPME = EnumSet.of(
        OperationType.CREATE, OperationType.UPDATE, OperationType.MERGE
    );
    private static final String PERMISSION_FOR_ALL_NODES = MessagingUtil.toPerm(new ExecuteOnAllNodesRequest());

    private final IExecutor executor = Jdp.getRequired(IExecutor.class);
    private final ISchedulerSetupResolver resolver = Jdp.getRequired(ISchedulerSetupResolver.class);
    private final CrossModuleRefResolver refResolver = Jdp.getRequired(CrossModuleRefResolver.class);
    private final ICannedRequestResolver rqResolver = Jdp.getRequired(ICannedRequestResolver.class);
    private final Provider<RequestContext> ctxProvider = Jdp.getProvider(RequestContext.class);
    private final String myServerId = ConfigProvider.getConfiguration().getServerIdSelf();

    @Override
    public void validateUpdate(final SchedulerSetupDTO current, final SchedulerSetupDTO intended) {
        if (intended.getApiKey() == null) {
            LOGGER.debug("UPDATE Scheduler: no API-Key provided, using old one");
            intended.setApiKey(current.getApiKey());
        } else {
            LOGGER.debug("UPDATE Scheduler with provided API-Key");
        }
    }

    @Override
    public void validateCreate(final SchedulerSetupDTO intended) {
        if (intended.getApiKey() == null) {
            LOGGER.debug("CREATE new Scheduler: no API-Key provided, creating one");
            this.createApiKey(intended);
        } else {
            LOGGER.debug("CREATE new Scheduler with provided API-Key");
        }
    }

    @Override
    public CrudSurrogateKeyResponse<SchedulerSetupDTO, FullTrackingWithVersion>
      execute(final RequestContext ctx, final SchedulerSetupCrudRequest crudRequest) throws Exception {

        // preprocessing
        final SchedulerSetupDTO oldSetup;
        if (OPERATION_TYPES_WITH_OLD_DTO.contains(crudRequest.getCrud())) {
            // any type with an old setup
            if (crudRequest.getKey() != null) {
                oldSetup = resolver.getDTO(crudRequest.getKey());
            } else if (crudRequest.getNaturalKey() != null) {
                oldSetup = resolver.getDTO(crudRequest.getNaturalKey());
            } else {
                oldSetup = null;
            }
        } else {
            oldSetup = null;
        }

        // check for "all nodes" and be so nice to add required permission, in case it is missing
        final SchedulerSetupDTO dto = crudRequest.getData();
        if (dto != null) {
            if (T9tConstants.SCHEDULER_RUN_ON_ALL_NODES.equals(dto.getRunOnNode())) {
                if (dto.getAdditionalPermissions() == null) {
                    dto.setAdditionalPermissions(PERMISSION_FOR_ALL_NODES);
                } else if (!dto.getAdditionalPermissions().contains(PERMISSION_FOR_ALL_NODES)) {
                    dto.setAdditionalPermissions(dto.getAdditionalPermissions() + "," + PERMISSION_FOR_ALL_NODES);
                }
            }
        }

        if (OPERATION_TYPES_CRUPME.contains(crudRequest.getCrud())) {
            final CannedRequestRef dataRequestRef = dto.getRequest();

            if (crudRequest.getCrud() == OperationType.UPDATE) {
                if (oldSetup != null && dataRequestRef instanceof CannedRequestDTO cannedRequestDTO
                  && oldSetup.getRequest() instanceof CannedRequestDTO oldDto) {
                    if (cannedRequestDTO.getRequestId().equals(oldDto.getRequestId())) {
                        // In case the crudRequest.data.request is an instance of CannedRequestDTO with the same request Id,
                        // update the oldSetup.request for field comparison.
                        LOGGER.debug("dataRequest is an instance of CannedRequestDTO, update oldSetup's request for comparison");
                        oldSetup.setRequest(dataRequestRef);
                    }
                }
            }

            if (!(dataRequestRef instanceof CannedRequestDTO)) {
                // replace any reference by full DTO of the canned request
                dto.setRequest(refResolver.getData(new CannedRequestCrudRequest(), dataRequestRef));
            }
        }

        // perform the regular CRUD
        final CrudSurrogateKeyResponse<SchedulerSetupDTO, FullTrackingWithVersion> response = execute(ctx, crudRequest, resolver);

        if (response.getReturnCode() == 0) {
            final SchedulerSetupDTO setup = response.getData();
            if (crudRequest.getCrud() != null) {
                switch (crudRequest.getCrud()) {
                case ACTIVATE:
                    // send an activate request to the target machine
                    sendToTargetServer(ctx, OperationType.CREATE, setup.getSchedulerId(), setup.getSchedulerEnvironment(), setup);
                    break;
                case CREATE:
                    if (setup.getIsActive()) {
                        sendToTargetServer(ctx, OperationType.CREATE, setup.getSchedulerId(), setup.getSchedulerEnvironment(), setup);
                    }
                    break;
                case DELETE:
                    if (oldSetup.getIsActive()) {
                        sendToTargetServer(ctx, OperationType.DELETE, oldSetup.getSchedulerId(), oldSetup.getSchedulerEnvironment(), null);
                    }
                    break;
                case INACTIVATE:
                    sendToTargetServer(ctx, OperationType.DELETE, setup.getSchedulerId(), setup.getSchedulerEnvironment(), null);
                    break;
                case UPDATE:
                    // this case is more complex, because next to changes of active flags, we also have to consider migration of the job to another server type
                    // also on the same server, there are 2 types of requests, some which change the job map, and some which do not.
                    // This have to be transmitted via different operation type settings
                    if (oldSetup.getIsActive()
                      && (!setup.getIsActive() || !Objects.equals(oldSetup.getSchedulerEnvironment(), setup.getSchedulerEnvironment()))) {
                        // removal of old server (and no job should run on that node in the future)
                        sendToTargetServer(ctx, OperationType.DELETE, oldSetup.getSchedulerId(), oldSetup.getSchedulerEnvironment(), null);
                    }
                    if (setup.getIsActive()
                      && (!oldSetup.getIsActive() || !Objects.equals(oldSetup.getSchedulerEnvironment(), setup.getSchedulerEnvironment()))) {
                        // creation of a new server (and no job running on that node currently)
                        sendToTargetServer(ctx, OperationType.CREATE, setup.getSchedulerId(), setup.getSchedulerEnvironment(), setup);
                    }
                    if (oldSetup.getIsActive() && setup.getIsActive() && Objects.equals(oldSetup.getSchedulerEnvironment(), setup.getSchedulerEnvironment())) {
                        // recreation of the same job on the same node
                        // find out if we need an UPDATE or a MERGE operation
                        final OperationType updateOpType = needsNewJobMap(oldSetup, setup) ? OperationType.UPDATE : OperationType.MERGE;
                        sendToTargetServer(ctx, updateOpType, setup.getSchedulerId(), setup.getSchedulerEnvironment(), setup);
                    }
                    break;
                default:
                    break;
                }
            }
        }

        // check for suppression of request object
        if (Boolean.TRUE.equals(crudRequest.getSuppressResponseParameters())) {
            if (response.getData() != null && response.getData().getRequest() instanceof CannedRequestDTO cannedDto) {
                cannedDto.setRequest(null);
            }
        }
        return response;
    }

    private boolean needsNewJobMap(final SchedulerSetupDTO oldSetup, final SchedulerSetupDTO setup) {
        if (!oldSetup.getRequest().equals(setup.getRequest())) {
            LOGGER.debug("Request has changed - replacing job");
            return true;
        }
        if (!Objects.equals(oldSetup.getApiKey(), setup.getApiKey())) {
            LOGGER.debug("API-Keys have changed - replacing job");
            return true;
        }
        if (!Objects.equals(oldSetup.getLanguageCode(), setup.getLanguageCode())) {
            LOGGER.debug("Language has changed - replacing job");
            return true;
        }
        return false;
    }

    private void sendToTargetServer(final RequestContext ctx, final OperationType op, final String schedulerId, final String server,
      final SchedulerSetupDTO dto) {
        final UpdateSchedulerDataRequest usdr = new UpdateSchedulerDataRequest();
        usdr.setOperationType(op);
        usdr.setSchedulerId(schedulerId);
        usdr.setSetup(dto);
        if (server == null || server.equals(myServerId)) {
            // perform changes on local node
            executor.executeSynchronousAndCheckResult(ctx, usdr, ServiceResponse.class);
            if (Boolean.TRUE.equals(ConfigProvider.getConfiguration().getRunInCluster())) {
                // also inform the other nodes
                final SchedulerChangedEvent event = new SchedulerChangedEvent();
                event.setSenderJvmId(RandomNumberGenerators.THIS_JVM_ID);
                event.setOperationType(op);
                event.setSchedulerId(schedulerId);
                event.setSetup(dto);
                executor.publishEvent(ctx, event);
            }
        } else {
            final UplinkConfiguration uplink = ConfigProvider.getUplinkOrThrow(server);
            final IForeignRequest remoteExecutor = SimpleCallOutExecutor.createCachedExecutor(server, uplink.getUrl());
            remoteExecutor.executeSynchronousAndCheckResult(ctx, usdr, ServiceResponse.class);
        }
    }

    private void createApiKey(final SchedulerSetupDTO dto) {
        final RequestContext ctx = ctxProvider.get();
        final JwtInfo jwt = ctx.internalHeaderParameters.getJwtInfo();

        if (dto != null && dto.getApiKey() == null) {
            // merge default permission and additional permissions
            final CannedRequestRef rqRef = dto.getRequest();
            final CannedRequestDTO requestDTO = rqRef instanceof CannedRequestDTO cannedRequestDTO ? cannedRequestDTO : rqResolver.getDTO(rqRef);
            String permissions = MessagingUtil.toPerm(requestDTO.getJobRequestObjectName());
            if (dto.getAdditionalPermissions() != null) {
                permissions = Joiner.on(",").join(permissions, dto.getAdditionalPermissions());
            }
            final PermissionsDTO permissionsDTO = new PermissionsDTO();
            permissionsDTO.setMinPermissions(jwt.getPermissionsMin());
            permissionsDTO.setMaxPermissions(jwt.getPermissionsMax());
            permissionsDTO.setLogLevel(jwt.getLogLevel());
            permissionsDTO.setLogLevelErrors(jwt.getLogLevelErrors());
            permissionsDTO.setResourceIsWildcard(Boolean.valueOf(true));
            permissionsDTO.setResourceRestriction(permissions);

            final ApiKeyDTO apiKeyDTO = new ApiKeyDTO();
            apiKeyDTO.setUserRef(new UserKey(dto.getUserId()));
            apiKeyDTO.setApiKey(UUID.randomUUID());
            apiKeyDTO.setIsActive(true);
            apiKeyDTO.setName("automatically created by SSM for scheduled task");
            apiKeyDTO.setPermissions(permissionsDTO);

            final ApiKeyCrudRequest rq = new ApiKeyCrudRequest();
            rq.setCrud(OperationType.CREATE);
            rq.setData(apiKeyDTO);
            dto.setApiKey(apiKeyDTO.getApiKey());
            try {
                executor.executeSynchronousAndCheckResult(ctx, rq, CrudSurrogateKeyResponse.class);
            } catch (final T9tException e) {
                if (e.getErrorCode()  != T9tException.RECORD_ALREADY_EXISTS) {
                    throw e;
                }
            }
        }
    }
}
