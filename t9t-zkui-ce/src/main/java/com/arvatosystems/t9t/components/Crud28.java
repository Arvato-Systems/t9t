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
package com.arvatosystems.t9t.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.Binder;
import org.zkoss.bind.impl.BinderUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Vlayout;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.component.ext.EventDataSelect28;
import com.arvatosystems.t9t.component.ext.IDataSelectReceiver;
import com.arvatosystems.t9t.component.ext.IViewModelOwner;
import com.arvatosystems.t9t.components.crud.AbstractCrudVM;
import com.arvatosystems.t9t.components.crud.AbstractCrudVM.CrudMode;
import com.arvatosystems.t9t.components.crud.ICrudNotifier;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.google.common.collect.ImmutableMap;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;

/** The Crud28 component also serves as the ViewModel for crud screens. The actual data is the BonaPortable field.
 * Screens which need special functionality should inherit from this component and add for example the commands of additional buttons.
 */
public class Crud28 extends Vlayout implements IViewModelOwner, IDataSelectReceiver, ICrudNotifier, IdSpace {
    private static final long serialVersionUID = -82034057035680582L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Crud28.class);

    @Wire("#crudButtons")      protected Hlayout  crudButtons;
    @Wire("#saveButton")       protected Button28 saveButton;
    @Wire("#newButton")        protected Button28 newButton;
    @Wire("#copyButton")       protected Button28 copyButton;
    @Wire("#deleteButton")     protected Button28 deleteButton;
    @Wire("#activateButton")   protected Button28 activateButton;
    @Wire("#deactivateButton") protected Button28 deactivateButton;

    protected IDataSelectReceiver detailsSection;  // the data form, which may be a tabbbox with separate panels
    protected Permissionset perms;  // available after onCreate
    protected CrudMode currentMode = CrudMode.NONE;

    protected String viewModelId;
    protected CrudViewModel<BonaPortable, TrackingBase> crudViewModel;  // set when gridId is defined
    protected final ApplicationSession session = ApplicationSession.get();
    protected final List<Form28> childForms = new ArrayList<Form28>(8);
    protected String cachesDropdown = null;  // if set to non null, a DELETE or SAVE will invalidate the cache for this ID

    public Crud28() {
        super();
        LOGGER.debug("new Crud28() created");
        setVflex("1");
        Executions.createComponents("/component/crud28.zul", this, null);
        Selectors.wireComponents(this, this, false);
    }

    private void updateCrudButtonStates() {
        LOGGER.debug("Update button states to new mode {}, have {} forms", currentMode, childForms.size());
        switch (currentMode) {
        case NONE:
        case UNSAVED_NEW:
            saveButton.setDisabled      (currentMode == CrudMode.NONE);
            newButton.setDisabled       (currentMode == CrudMode.UNSAVED_NEW);
            copyButton.setDisabled      (true);
            deleteButton.setDisabled    (true);
            activateButton.setDisabled  (true);
            deactivateButton.setDisabled(true);
            break;
        case CURRENT:
        case CURRENT_RO:
            saveButton.setDisabled      (currentMode == CrudMode.CURRENT_RO);
            newButton.setDisabled       (false);
            copyButton.setDisabled      (false);
            deleteButton.setDisabled    (currentMode == CrudMode.CURRENT_RO);
            activateButton.setDisabled  (currentMode == CrudMode.CURRENT_RO);
            deactivateButton.setDisabled(currentMode == CrudMode.CURRENT_RO);
            break;
        }
        // update child forms
        for (Form28 form : childForms)
            form.updateState(currentMode);
    }

    @Listen("onCreate")
    public void onCreate() {
        LOGGER.debug("Crud28.onCreate()");
        GridIdTools.enforceViewModelId(this);

        perms = GridIdTools.getPermissionFromAnchestor(this);
        LOGGER.info("Update all button to visible");
        if (!perms.contains(OperationType.DELETE))
            deleteButton.setVisible(false);
        if (!perms.contains(OperationType.CREATE)) {
            copyButton.setVisible(false);
            newButton.setVisible(false);
        }
        if (!perms.contains(OperationType.UPDATE))  // saveButton is needed for NEW / COPY as well, to persist!
            saveButton.setVisible(false);
        if (!perms.contains(OperationType.ACTIVATE))
            activateButton.setVisible(false);
        if (!perms.contains(OperationType.INACTIVATE))
            deactivateButton.setVisible(false);

        updateCrudButtonStates();

        final Binder binder = BinderUtil.getBinder(this);
        if (binder != null) {
            AbstractCrudVM viewModelInstance = (AbstractCrudVM)binder.getViewModel();
            LOGGER.debug("viewmodel is of class {}", viewModelInstance.getClass().getCanonicalName());
            viewModelInstance.setHardLink(this);
        }
        // move contents and wire events: A row selected event has to be forwarded to the contents of this component
        List<Component> children = ComponentTools28.moveChilds(this, crudButtons, null);
        if (children != null && !children.isEmpty()) {
            // move it up to be the first child
            for (Component child : children) {
                insertBefore(child, crudButtons);
                if (child instanceof IDataSelectReceiver) {
                    detailsSection = (IDataSelectReceiver) child;
                    // wire events
                    // avoid NPE
                    DataWithTracking<BonaPortable, TrackingBase> initialDwt = new DataWithTracking<BonaPortable, TrackingBase>();
                    initialDwt.setData(crudViewModel.dtoClass.newInstance());
                    EventDataSelect28 initial = new EventDataSelect28(initialDwt, 0, null);
                    detailsSection.setSelectionData(initial);
                    }
                // throw new RuntimeException("Child of Crud28 must be a IDataSelectReceiver,
                // but is " + child.getClass().getSimpleName());
            }
        }

        // wire events to commands
        final Map<String, Object> NO_ARGS = ImmutableMap.<String, Object>of();
        saveButton      .addEventListener(Events.ON_CLICK, ev -> { binder.sendCommand("commandSave", NO_ARGS); invalidateCache(); });
        newButton       .addEventListener(Events.ON_CLICK, ev -> binder.sendCommand("commandNew", NO_ARGS));
        copyButton      .addEventListener(Events.ON_CLICK, ev -> binder.sendCommand("commandCopy", NO_ARGS));
        deleteButton    .addEventListener(Events.ON_CLICK, ev -> { binder.sendCommand("commandDelete", NO_ARGS); invalidateCache(); });
        activateButton  .addEventListener(Events.ON_CLICK, ev -> binder.sendCommand("commandActivate", NO_ARGS));
        deactivateButton.addEventListener(Events.ON_CLICK, ev -> binder.sendCommand("commandDeactivate", NO_ARGS));
    }

    protected void invalidateCache() {
        if (cachesDropdown != null)
            session.invalidateCachedDropDownData(cachesDropdown);
    }

    @Override
    public void setSelectionData(EventDataSelect28 eventData) {
        LOGGER.debug("received event data {}", eventData);
        if (detailsSection != null) {
            detailsSection.setSelectionData(eventData);
        }

        Binder binder = BinderUtil.getBinder(this);
        // AbstractCrudVM viewModelInstance = (AbstractCrudVM)binder.getViewModel();
        binder.sendCommand("setSelectionData", Collections.singletonMap("dwt", eventData.getDwt()));
    }

    @Override
    public CrudViewModel<BonaPortable, TrackingBase> getCrudViewModel() {
        GridIdTools.enforceViewModelId(this);
        return crudViewModel;
    }

    @Override
    public String getViewModelId() {
        return viewModelId;
    }

    protected boolean hasColumn(BonaPortableClass<?> cls, String fieldname) {
        BonaPortableClass<?> parent = cls.getParent();
        boolean parentHasColumn = parent != null && hasColumn(parent, fieldname);
        if (parentHasColumn)
            return true;
        for (FieldDefinition f: cls.getMetaData().getFields())
            if (f.getName().equals(fieldname))
                return true;
        return false;
    }

    @Override
    public void setViewModelId(String viewModelId) {
        LOGGER.debug("Setting view model ID to {}", viewModelId);
        this.viewModelId = viewModelId;
        crudViewModel = GridIdTools.getViewModelByViewModelId(viewModelId);
        // test if the data part has an active column; if not then hide the activate / deactivate buttons.
        // need to check superclasses as well
        if (!hasColumn(crudViewModel.dtoClass, "isActive")) {
            LOGGER.debug("viewModel {} has no isActive column, hiding activateButton / deactivateButton", viewModelId);
            activateButton.setVisible(false);
            deactivateButton.setVisible(false);
        }
    }

    public CrudMode getCurrentMode() {
        return currentMode;
    }

    @Override
    public void setCurrentMode(CrudMode currentMode) {
        this.currentMode = currentMode;
        LOGGER.debug("Updating button states to {}", currentMode);
        updateCrudButtonStates();
    }

    // setter on pseudo-field with the sole purpose to post an event to the grid
    @Override
    public void setRefresher(Object eventData) {
        LOGGER.debug("Updating refresher to {}", eventData);
        if (eventData != null)
            Events.postEvent(new Event("onCrudUpdate", this, eventData));
    }

    @Override
    public ApplicationSession getSession() {
        return session;
    }

    public void registerForm(Form28 form) {
        childForms.add(form);
    }

    public void setHide(String buttonNames) {
        if (buttonNames != null) {
            String [] buttonsToHide = buttonNames.split(",");
            if (buttonsToHide != null) {
                for (String buttonToHide : buttonsToHide) {
                    String normalizedName = buttonToHide.trim().toUpperCase();
                    switch (normalizedName) {
                    case "SAVE":
                        saveButton.setVisible(false);
                        break;
                    case "NEW":
                        newButton.setVisible(false);
                        break;
                    case "COPY":
                        copyButton.setVisible(false);
                        break;
                    case "DELETE":
                        deleteButton.setVisible(false);
                        break;
                    case "ACTIVATE":
                        activateButton.setVisible(false);
                        break;
                    case "DEACTIVATE":
                        deactivateButton.setVisible(false);
                        break;
                    default:
                        throw new RuntimeException("invalid button name to hide: " + normalizedName);
                    }
                }
            }
        }
    }

    public String getCachesDropdown() {
        return cachesDropdown;
    }

    public void setCachesDropdown(String cachesDropdown) {
        this.cachesDropdown = cachesDropdown;
    }
}
