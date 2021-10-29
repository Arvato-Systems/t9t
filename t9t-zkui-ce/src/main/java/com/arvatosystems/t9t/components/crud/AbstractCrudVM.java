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
package com.arvatosystems.t9t.components.crud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;

import com.arvatosystems.t9t.base.crud.CrudAnyKeyRequest;
import com.arvatosystems.t9t.base.crud.CrudAnyKeyResponse;
import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.base.search.ResolveAnyRefRequest;
import com.arvatosystems.t9t.base.search.ResolveAnyRefResponse;
import com.arvatosystems.t9t.tfi.web.T9TConfigConstants;
import com.arvatosystems.t9t.tfi.web.ZulUtils;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.bonaparte.util.ToStringHelper;

@Init(superclass = true)
public abstract class AbstractCrudVM<
    KEY,
    DTO extends BonaPortable,
    TRACKING extends TrackingBase,
    CRUDRQ extends CrudAnyKeyRequest<DTO, TRACKING>,
    CRUDRS extends CrudAnyKeyResponse<DTO, TRACKING>
  > extends AbstractViewOnlyVM<DTO, TRACKING> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCrudVM.class);
    protected ICrudNotifier hardLink = null;
    protected boolean useProtectedView;

    public void setUseProtectedView(final boolean useProtectedView) {
        this.useProtectedView = useProtectedView;
        LOGGER.debug("useProtectedView set to {}", useProtectedView);
    }

    public enum CrudMode {
        NONE,           // no current record, not yet pushed "NEW"
        UNSAVED_NEW,    // editing new record, not yet saved
        CURRENT,        // editing some existing record
        CURRENT_RO,      // showing some existing record, no edit allowed (no permissions),
        CURRENT_PROTECTED_VIEW    // this is a protected view mode to avoid unintentional modification
    }

    protected CrudMode currentMode = CrudMode.NONE;
    protected static final String COMMON = "com";
    protected static final String ENTITY = "entity";
    protected static final String DELETE_CONFIRMATION = "deleteConfirmation";
    protected static final String DELETE_CONFIRMATION_MESSAGE = "deleteConfirmationMessage";
    protected static final String DELETE_CONFIRMATION_DETAIL = "deleteConfirmationDetail";

    public void setHardLink(final ICrudNotifier notifier) {
        // link to the crud component to avoid boilerplate caused by @Notifier
        this.hardLink = notifier;
    }

    // to be overridden in case arrays need to be initialized
    @Override
    protected void clearData() {   // TODO: init child objects if exist, do it via injected class, qualifier to be passed to @Init
        super.clearData();
        if (data != null)
            data.put$Active(true);  // if the DTO has an active field, create it as active by default
    }

    @Command
    public void commandSave() {
        saveHook();
        if (currentMode == CrudMode.UNSAVED_NEW) {
            final CrudAnyKeyRequest<DTO, TRACKING> crudRq = crudViewModel.crudClass.newInstance();
            crudRq.setCrud(OperationType.CREATE);
            crudRq.setData(data);
            runCrud(crudRq, Boolean.FALSE);
            setCurrentMode(useProtectedView ? CrudMode.CURRENT_PROTECTED_VIEW : CrudMode.CURRENT);
        } else {
            final CrudAnyKeyRequest<DTO, TRACKING> crudRq = createCrudWithKey();
            crudRq.setCrud(OperationType.UPDATE);
            crudRq.setData(data);
            runCrud(crudRq, Boolean.TRUE);
        }
        final boolean showMessage = ZulUtils.readBooleanConfig(T9TConfigConstants.CRUD_SHOW_MESSAGE);
        if (showMessage) {
            Messagebox.show(session.translate(COMMON, "saved"), session.translate(COMMON, "info"), Messagebox.OK,
                    Messagebox.INFORMATION);
        }
    }
    @NotifyChange("*")
    @Command
    public void commandNew() {
        clearData();
        setCurrentMode(CrudMode.UNSAVED_NEW);
    }
    @NotifyChange("*")
    @Command
    public void commandCopy() {
        // we work on a detached object already, there should be no need to duplicate it again
        // data = (DTO) data.ret$MutableClone(true, true);
        tenantRef = session.getTenantRef();
        // clearKey();  // disabled, clearKey() lead to confusion that some fields would be omitted.
        setCurrentMode(CrudMode.UNSAVED_NEW);
    }
    @Command
    public void commandDelete() {
        showDeleteConfirmationDialog(new EventListener<Event>() {

            @Override
            public void onEvent(final Event event) throws Exception {
                if (event.getName().equals(Messagebox.ON_YES)) {
                    final CrudAnyKeyRequest<DTO, TRACKING> crudRq = createCrudWithKey();
                    crudRq.setCrud(OperationType.DELETE);
                    runCrud(crudRq, Boolean.FALSE);
                }
            }
        });
    }
    @NotifyChange("data.isActive")
    @Command
    public void commandActivate() {
        activateDeactivate(true);
    }
    @NotifyChange("data.isActive")
    @Command
    public void commandDeactivate() {
        activateDeactivate(false);
    }
    @NotifyChange("*")
    @Command
    public void commandEdit() {
        setCurrentMode(CrudMode.CURRENT);
    }

    protected abstract CrudAnyKeyRequest<DTO, TRACKING> createCrudWithKey();
    protected abstract void clearKey();  // required before some CREATE

    protected void activateDeactivate(final boolean newActive) {
        final CrudAnyKeyRequest<DTO, TRACKING> crudRq = createCrudWithKey();
        crudRq.setCrud(newActive ? OperationType.ACTIVATE : OperationType.INACTIVATE);
        runCrud(crudRq, Boolean.TRUE);  // performs Grid28.refreshCurrentItem(); via event
    }

    protected void runCrud(final CrudAnyKeyRequest<DTO, TRACKING> crudRq, final Object eventData) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("runCrud {} with key = {}, data = {}",
                crudRq.getCrud(), crudRq.getData() == null ? "NULL" : ToStringHelper.toStringML(crudRq.getData()));
        }
        final CrudAnyKeyResponse<BonaPortable, TrackingBase> crudRs = remoteUtil.executeExpectOk(crudRq, CrudAnyKeyResponse.class);
        if (eventData != null) {
            setRefresher(eventData);  // Component to issue an event
        }
    }

    // to be overriden in case data needs to be collected before saving
    protected void saveHook() {
    }

    @Command
    @NotifyChange("*")
    public void setSelectionData(@BindingParam("dwt") final DataWithTracking<DTO, TRACKING> dwt) {
        if (dwt != null) {
            LOGGER.debug("setSelectionData(some data)");
            loadData(dwt);
            setCurrentMode(tenantAccessCheck());
        } else {
            LOGGER.debug("setSelectionData(null)");
            clearData();
            setCurrentMode(CrudMode.NONE);
        }
    }

    public CrudMode getCurrentMode() {
        return currentMode;
    }
    public void setCurrentMode(final CrudMode currentMode) {
        this.currentMode = currentMode;
        LOGGER.debug("Updating button states to new currentMode {} in viewmodel", currentMode);
        modeNotifier();
    }
    public void setRefresher(final Object refresher) {
        LOGGER.debug("Setting refresher to {}", refresher);
        if (hardLink != null)
            hardLink.setRefresher(refresher);
    }

    @NotifyChange("currentMode")
    @Command
    public void modeNotifier() {
//      if (hardLink != null)
//          hardLink.setCurrentMode(currentMode);
    }

    private CrudMode tenantAccessCheck() {

        final CrudMode currentCrudMode = useProtectedView ? CrudMode.CURRENT_PROTECTED_VIEW : CrudMode.CURRENT;

        if (tenantId != null) {
            /*
             *  Check tenantId first because tenantId is not initialized, if it is not null
             *  means it is from data with DataWithTrackingW
             */
            return tenantId.equals(this.session.getTenantId()) ? currentCrudMode : CrudMode.CURRENT_RO;
        } else if (tenantRef != null) {
            /*
             * TenantRef has being initialized as session.getTenantId during class initial,
             * so if the data is not null and it is DataWithTrackingS, use this to validate
             */
            return tenantRef.equals(this.session.getTenantRef()) ? currentCrudMode : CrudMode.CURRENT_RO;
        }

        //No tenant restriction
        return currentCrudMode;
    }

    protected void showDeleteConfirmationDialog(final EventListener<Event> eventListener) {

        if (data instanceof Ref) {
            final ResolveAnyRefRequest rq = new ResolveAnyRefRequest(((Ref) data).getObjectRef());
            final ResolveAnyRefResponse res = remoteUtil.executeExpectOk(rq, ResolveAnyRefResponse.class);
            if (res.getEntityClass() != null || res.getDescription() != null) {
                final Description desc = res.getDescription();
                final String translatedEntityName = session.translate(ENTITY, res.getEntityClass());
                final String formattedMessage = session.translate(COMMON, DELETE_CONFIRMATION_DETAIL, translatedEntityName, desc.getId(), desc.getName());

                Messagebox.show(formattedMessage,
                        session.translate(COMMON, DELETE_CONFIRMATION), Messagebox.YES | Messagebox.CANCEL,
                        Messagebox.EXCLAMATION, eventListener);
                return;
            }
        }

      Messagebox.show(session.translate(COMMON, DELETE_CONFIRMATION_MESSAGE),
              session.translate(COMMON, DELETE_CONFIRMATION), Messagebox.YES | Messagebox.CANCEL,
              Messagebox.EXCLAMATION, eventListener);
  }

}
