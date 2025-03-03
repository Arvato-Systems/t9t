package com.arvatosystems.t9t.changeRequest.service.impl;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.crud.CrudAnyKeyRequest;
import com.arvatosystems.t9t.base.services.IDataChangeRequestFlow;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowConfigDTO;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowStatus;
import com.arvatosystems.t9t.changeRequest.DataChangeRequestDTO;
import com.arvatosystems.t9t.changeRequest.jpa.entities.DataChangeRequestEntity;
import com.arvatosystems.t9t.changeRequest.jpa.mapping.IChangeWorkFlowConfigDTOMapper;
import com.arvatosystems.t9t.changeRequest.jpa.mapping.IDataChangeRequestDTOMapper;
import com.arvatosystems.t9t.changeRequest.jpa.persistence.IChangeWorkFlowConfigEntityResolver;
import com.arvatosystems.t9t.changeRequest.jpa.persistence.IDataChangeRequestEntityResolver;
import com.arvatosystems.t9t.changeRequest.services.IChangeWorkFlowConfigCache;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Singleton
public class DataChangeRequestFlow implements IDataChangeRequestFlow {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataChangeRequestFlow.class);

    protected final IChangeWorkFlowConfigCache configCache = Jdp.getRequired(IChangeWorkFlowConfigCache.class);
    protected final IChangeWorkFlowConfigEntityResolver configResolver = Jdp.getRequired(IChangeWorkFlowConfigEntityResolver.class);
    protected final IDataChangeRequestEntityResolver resolver = Jdp.getRequired(IDataChangeRequestEntityResolver.class);
    protected final IDataChangeRequestDTOMapper mapper = Jdp.getRequired(IDataChangeRequestDTOMapper.class);
    protected final IChangeWorkFlowConfigDTOMapper configMapper = Jdp.getRequired(IChangeWorkFlowConfigDTOMapper.class);

    @Override
    public boolean requireApproval(@Nonnull final String pqon, @Nonnull final OperationType operationType) {
        final ChangeWorkFlowConfigDTO config;
        if (pqon.equals(ChangeWorkFlowConfigDTO.my$PQON)) {
            // special case: if the checking is for ChangeWorkFlowConfigDTO, then we need to fetch the config from the database to load it with read/write mode.
            // Cache will load the entity as read-only.
            config = configMapper.mapToDto(configResolver.findByPqon(true, pqon));
        } else {
            config = configCache.getOrNull(pqon);
        }
        if (config == null) {
            return false;
        }
        return switch (operationType) {
            case CREATE -> config.getApprovalRequiredForCreate();
            case DELETE -> config.getApprovalRequiredForDelete();
            case UPDATE -> config.getApprovalRequiredForUpdate();
            case MERGE -> config.getApprovalRequiredForCreate() || config.getApprovalRequiredForUpdate();
            case INACTIVATE -> config.getApprovalRequiredForDeactivation();
            case ACTIVATE -> config.getApprovalRequiredForActivation();
            default -> false;
        };
    }

    @Override
    public Long createDataChangeRequest(@Nonnull final RequestContext ctx, @Nonnull String pqon, @Nonnull final String changeId,
        @Nullable final BonaPortable key, @Nonnull final RequestParameters crudRequest, @Nullable final String changeComment,
        @Nullable final Boolean submitChange) {
        DataChangeRequestEntity entity = new DataChangeRequestEntity();
        entity.setObjectRef(resolver.createNewPrimaryKey());
        entity.setPqon(pqon);
        entity.setChangeId(changeId);
        entity.setKey(key == null ? T9tConstants.NO_KEY : key);
        entity.setCrudRequest(crudRequest);
        entity.setUserIdCreated(ctx.userId);
        entity.setWhenCreated(ctx.executionStart);
        entity.setUserIdModified(ctx.userId);
        entity.setWhenLastModified(ctx.executionStart);
        if (T9tUtil.isTrue(submitChange)) {
            entity.setUserIdSubmitted(ctx.userId);
            entity.setWhenSubmitted(ctx.executionStart);
            entity.setTextSubmitted(changeComment);
            entity.setStatus(ChangeWorkFlowStatus.TO_REVIEW);
        } else {
            entity.setStatus(ChangeWorkFlowStatus.WORK_IN_PROGRESS);
        }
        resolver.save(entity);
        LOGGER.debug("Creating data change request for changeId:{}, key:{}, submitChange:{}", changeId, key, submitChange);
        return entity.getObjectRef();
    }

    @Override
    public boolean isChangeRequestValidToActivate(@Nonnull final Long objectRef, @Nullable BonaPortable key, @Nonnull BonaPortable data) {
        final DataChangeRequestEntity entity = resolver.find(objectRef);
        if (entity == null) {
            return false;
        }
        final DataChangeRequestDTO dto = mapper.mapToDto(entity);
        // Key and data should match and the change request should be ready to activate
        final BonaPortable crudData = ((CrudAnyKeyRequest) dto.getCrudRequest()).getData();
        return Objects.equals(dto.getKey(), key) && Objects.equals(crudData, data) && ChangeWorkFlowStatus.ACTIVATED == dto.getStatus();
    }
}
