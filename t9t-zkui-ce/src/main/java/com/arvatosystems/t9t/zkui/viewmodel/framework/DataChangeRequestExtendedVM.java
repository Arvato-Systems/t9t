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
package com.arvatosystems.t9t.zkui.viewmodel.framework;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.crud.CrudAnyKeyRequest;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.types.LongKey;
import com.arvatosystems.t9t.base.types.NoKey;
import com.arvatosystems.t9t.base.types.StringKey;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowConfigDTO;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowStatus;
import com.arvatosystems.t9t.changeRequest.DataChangeRequestDTO;
import com.arvatosystems.t9t.changeRequest.DataChangeRequestExtendedDTO;
import com.arvatosystems.t9t.changeRequest.DataChangeRequestRef;
import com.arvatosystems.t9t.changeRequest.request.DeleteDataChangeRequestRecordRequest;
import com.arvatosystems.t9t.changeRequest.request.ModifyDataChangeRequestDataRequest;
import com.arvatosystems.t9t.changeRequest.request.UpdateDataChangeRequestStatusRequest;
import com.arvatosystems.t9t.zkui.components.basic.Grid28;
import com.arvatosystems.t9t.zkui.components.basic.View28;
import com.arvatosystems.t9t.zkui.services.IChangeWorkFlowConfigDAO;
import com.arvatosystems.t9t.zkui.util.ApplicationUtil;
import com.arvatosystems.t9t.zkui.util.JumpTool;
import com.arvatosystems.t9t.zkui.viewmodel.ViewOnlyVM;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.JsonComposerPrettyPrint;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Messagebox;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Init(superclass = true)
public class DataChangeRequestExtendedVM extends ViewOnlyVM<DataChangeRequestExtendedDTO, FullTrackingWithVersion> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataChangeRequestExtendedVM.class);
    protected static final EnumSet<ChangeWorkFlowStatus> TO_REVIEW_DISABLE_STATUS = EnumSet.of(ChangeWorkFlowStatus.APPROVED, ChangeWorkFlowStatus.REJECTED,
        ChangeWorkFlowStatus.CONFLICT, ChangeWorkFlowStatus.TO_REVIEW);

    protected final IChangeWorkFlowConfigDAO changeWorkFlowConfigDAO = Jdp.getRequired(IChangeWorkFlowConfigDAO.class);

    private static final String EVENT_CRUD_UPDATE = "onCrudUpdate";

    private DataChangeRequestDTO changeDto;
    private CrudAnyKeyRequest<BonaPortable, TrackingBase> crudRequest;
    private boolean separateActivation;
    private String screenLocation;
    private String dataJsonStr;
    private String keyJsonStr;
    private String operationType;
    private View28 view28;
    private Grid28 grid28;
    private String comment;

    private boolean disableDelete = true;
    private boolean disableEdit = true;
    private boolean disableSubmitReview = true;
    private boolean disableReject = true;
    private boolean disableApprove = true;
    private boolean disableActivate = true;
    private boolean disableComment = true;

    @AfterCompose
    public void afterCompose() {
        view28 = (View28) Selectors.find("view28").get(0);
        grid28 = (Grid28) Selectors.find("grid28").get(0);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void loadData(final DataWithTracking<DataChangeRequestExtendedDTO, FullTrackingWithVersion> dwt) {
        super.loadData(dwt);
        this.comment = null;
        changeDto = dwt.getData().getChange();
        crudRequest = (CrudAnyKeyRequest<BonaPortable, TrackingBase>) changeDto.getCrudRequest();
        final ChangeWorkFlowConfigDTO config = changeWorkFlowConfigDAO.getChangeWorkFlowConfigByPqon(changeDto.getPqon());
        separateActivation = config != null && config.getSeparateActivation();
        screenLocation = config != null ? config.getScreenLocation() : null;
        dataJsonStr = crudRequest.getData() != null ? JsonComposerPrettyPrint.toJsonString(crudRequest.getData()) : null;
        operationType = session.translate("t9t.OperationType", crudRequest.getCrud().name());
        final BonaPortable key = changeDto.getKey();
        if (key instanceof NoKey) {
            keyJsonStr = null;
        } else if (key instanceof LongKey longKey) {
            keyJsonStr = longKey.getKey().toString();
        } else if (key instanceof StringKey stringKey) {
            keyJsonStr = stringKey.getKey();
        } else {
            keyJsonStr = JsonComposerPrettyPrint.toJsonString(changeDto.getKey());
        }

        updateButtonAndCommentState(dwt.getData().getPermissions());
    }

    @Override
    @NotifyChange({ "disableDelete", "disableEdit", "disableSubmitReview", "disableReject", "disableApprove", "disableActivate", "disableComment" })
    protected void clearData() {
        disableDelete = true;
        disableEdit = true;
        disableSubmitReview = true;
        disableReject = true;
        disableApprove = true;
        disableActivate = true;
        disableComment = true;
    }

    @Command
    public void deleteDataChangeRequest() {
        if (changeDto != null) {
            confirmAction("deleteConfirmation", "deleteConfirmationMessage", it -> {
                final DeleteDataChangeRequestRecordRequest request = new DeleteDataChangeRequestRecordRequest();
                request.setDataChangeRequestRef(new DataChangeRequestRef(changeDto.getObjectRef()));
                remoteUtil.executeExpectOk(request);
                Events.postEvent(new Event(EVENT_CRUD_UPDATE, view28, Boolean.FALSE));
            });
        }
    }

    @Command
    public void editDataChangeRequest() {
        if (T9tUtil.isNotBlank(screenLocation)) {
            // setup params and jump to the relevant CRUD screen
            final Map<String, Object> jumpParams = new HashMap<>(3);
            jumpParams.put(JumpTool.DATA, crudRequest.getData());
            jumpParams.put(JumpTool.SAVE_HANDLER, getModifyDataChangesHandler());
            jumpParams.put(JumpTool.BACK_LINK_2, "screens/data_admin/dataChangeRequestExtended28.zul");
            jumpParams.put(JumpTool.CACHE_SUFFIX, "-DCR" + data.getChange().getObjectRef().toString());
            ApplicationUtil.navJumpToScreen(screenLocation, jumpParams);
        }
    }

    @Command
    public void reviewDataChangeRequest() {
        if (changeDto != null) {
            final UpdateDataChangeRequestStatusRequest request = new UpdateDataChangeRequestStatusRequest();
            request.setDataChangeRequestRef(new DataChangeRequestRef(changeDto.getObjectRef()));
            request.setNewStatus(ChangeWorkFlowStatus.TO_REVIEW);
            request.setComment(T9tUtil.isBlank(comment) ? null : comment);
            remoteUtil.executeExpectOk(request);
            Events.postEvent(new Event(EVENT_CRUD_UPDATE, view28, Boolean.TRUE));
        }
    }

    @Command
    public void rejectDataChangeRequest() {
        if (changeDto != null) {
            confirmAction("rejectConfirmation", "rejectConfirmationMessage", it -> {
                final UpdateDataChangeRequestStatusRequest request = new UpdateDataChangeRequestStatusRequest();
                request.setDataChangeRequestRef(new DataChangeRequestRef(changeDto.getObjectRef()));
                request.setNewStatus(ChangeWorkFlowStatus.REJECTED);
                request.setComment(T9tUtil.isBlank(comment) ? null : comment);
                remoteUtil.executeExpectOk(request);
                // if user has UPDATE or DELETE permission then reload the row only else reload the whole grid.
                final Boolean reloadRowOnly = data.getPermissions().contains(OperationType.UPDATE) || data.getPermissions().contains(OperationType.DELETE);
                Events.postEvent(new Event(EVENT_CRUD_UPDATE, view28, reloadRowOnly));
            });
        }
    }

    @Command
    public void approveDataChangeRequest() {
        if (changeDto != null) {
            if (separateActivation) {
                invokeApproveDataChangeRequest(true);
            } else {
                // do as activate
                // if user has UPDATE or DELETE permission then reload the row only else reload the whole grid.
                final Boolean reloadRowOnly = data.getPermissions().contains(OperationType.UPDATE) || data.getPermissions().contains(OperationType.DELETE);
                confirmAction("activateConfirmation", "activateConfirmationMessage", it -> {
                    invokeApproveDataChangeRequest(reloadRowOnly);
                });
            }
        }
    }

    private void invokeApproveDataChangeRequest(final Boolean reloadRowOnly) {
        final UpdateDataChangeRequestStatusRequest request = new UpdateDataChangeRequestStatusRequest();
        request.setDataChangeRequestRef(new DataChangeRequestRef(changeDto.getObjectRef()));
        request.setNewStatus(ChangeWorkFlowStatus.APPROVED);
        request.setComment(T9tUtil.isBlank(comment) ? null : comment);
        remoteUtil.executeExpectOk(request);
        Events.postEvent(new Event(EVENT_CRUD_UPDATE, view28, reloadRowOnly));
    }

    @Command
    public void activateDataChangeRequest() {
        if (changeDto != null) {
            confirmAction("activateConfirmation", "activateConfirmationMessage", it -> {
                final UpdateDataChangeRequestStatusRequest request = new UpdateDataChangeRequestStatusRequest();
                request.setDataChangeRequestRef(new DataChangeRequestRef(changeDto.getObjectRef()));
                request.setNewStatus(ChangeWorkFlowStatus.ACTIVATED);
                request.setComment(T9tUtil.isBlank(comment) ? null : comment);
                remoteUtil.executeExpectOk(request);
                // if user has UPDATE or DELETE permission then reload the row only else reload the whole grid.
                Boolean reloadRowOnly = data.getPermissions().contains(OperationType.UPDATE) || data.getPermissions().contains(OperationType.DELETE);
                Events.postEvent(new Event(EVENT_CRUD_UPDATE, view28, reloadRowOnly));
            });
        }
    }

    public DataChangeRequestDTO getChangeDto() {
        return changeDto;
    }

    public String getCrudRequestJsonStr() {
        return dataJsonStr;
    }

    public String getKeyJsonStr() {
        return keyJsonStr;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public boolean isDisableDelete() {
        return disableDelete;
    }

    public boolean isDisableEdit() {
        return disableEdit;
    }

    public boolean isDisableSubmitReview() {
        return disableSubmitReview;
    }

    public boolean isDisableReject() {
        return disableReject;
    }

    public boolean isDisableApprove() {
        return disableApprove;
    }

    public boolean isDisableActivate() {
        return disableActivate;
    }

    public boolean isDisableComment() {
        return disableComment;
    }

    private void confirmAction(final String titleLabel, final String messageLabel, final Consumer<Void> action) {
        final String title = session.translate("changeRequest", titleLabel);
        final String message = session.translate("changeRequest", messageLabel, changeDto.getChangeId(), changeDto.getPqon());
        Messagebox.show(message, title, Messagebox.YES | Messagebox.CANCEL, Messagebox.EXCLAMATION, event -> {
            if (event.getName().equals(Messagebox.ON_YES)) {
                action.accept(null);
            }
        });
    }

    /**
     * Handler will get the updated data (BonaPortable) from the relevant CRUD screen (extensions of AbstractCrudVM).
     * Execute ModifyDataChangeRequestDataRequest request to modify the data part only.
     * Update the Grid28 relevant entry and also update the detail part.
     */
    private Consumer<BonaPortable> getModifyDataChangesHandler() {
        return dataUpdate -> {
            final ModifyDataChangeRequestDataRequest request = new ModifyDataChangeRequestDataRequest();
            request.setDataChangeRequestRef(new DataChangeRequestRef(changeDto.getObjectRef()));
            request.setData(dataUpdate);
            remoteUtil.executeExpectOk(request);
            DataWithTracking<BonaPortable, TrackingBase> dwt = grid28.refreshCurrentItem();
            if (dwt != null) {
                final DataChangeRequestExtendedDTO data = (DataChangeRequestExtendedDTO) dwt.getData();
                this.data = data;
                this.changeDto = data.getChange();
                this.tracking = (FullTrackingWithVersion) dwt.getTracking();
                if (this.changeDto.getCrudRequest() instanceof CrudAnyKeyRequest<?, ?> crudAnyKeyReq) {
                    this.crudRequest.setData(crudAnyKeyReq.getData());
                    this.dataJsonStr = JsonComposerPrettyPrint.toJsonString(this.crudRequest.getData());
                }
                updateButtonAndCommentState(data.getPermissions());
            } else {
                LOGGER.error("Unable to get data after refresh of current item in Grid28");
            }
        };
    }

    @NotifyChange({ "disableDelete", "disableEdit", "disableSubmitReview", "disableReject", "disableApprove", "disableActivate", "disableComment" })
    private void updateButtonAndCommentState(@Nonnull final Permissionset permissionSet) {
        disableDelete = !permissionSet.contains(OperationType.DELETE);
        disableEdit = !permissionSet.contains(OperationType.UPDATE) || T9tUtil.isBlank(screenLocation) || crudRequest.getData() == null;
        disableSubmitReview = !permissionSet.contains(OperationType.UPDATE) || TO_REVIEW_DISABLE_STATUS.contains(changeDto.getStatus());
        disableReject = !permissionSet.contains(OperationType.REJECT);
        disableApprove = !permissionSet.contains(OperationType.APPROVE);
        disableActivate = !permissionSet.contains(OperationType.ACTIVATE) || !separateActivation;
        disableComment = disableSubmitReview && disableReject && disableApprove;
    }
}
