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
package com.arvatosystems.t9t.zkui.components.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Grid;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.zkui.components.IDataFactoryOwner;
import com.arvatosystems.t9t.zkui.components.IDataFieldFactory;
import com.arvatosystems.t9t.zkui.components.IViewModelOwner;
import com.arvatosystems.t9t.zkui.components.datafields.AbstractDropdownDataField;
import com.arvatosystems.t9t.zkui.components.datafields.IDataField;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.Constants;
import com.arvatosystems.t9t.zkui.viewmodel.AbstractCrudVM.CrudMode;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Jdp;

/** A form which is part of a window (crud detail, modal popup, tabbed dialog tab).
 * It provides either a single or double column display of fields with label, arranges as rows.
 */
public class Form28 extends Grid implements IDataFactoryOwner, IViewModelOwner {
    private static final long serialVersionUID = -6036011203878228983L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Form28.class);
    private final ApplicationSession as = ApplicationSession.get();
    private final IDataFieldFactory dataFieldFactory = Jdp.getRequired(IDataFieldFactory.class);
    private CrudViewModel<BonaPortable, TrackingBase> crudViewModel;
    private String viewModelId;
    private int numColumns = 1;
    private String aspectText = "1"; // relative size of label column
    private String aspect = "3";     // relative size of entry field column (1:1 / 1:2 / 1:3 as you like)
    private final Columns cols;
    private Crud28 inCrud = null;
    private final List<IDataField> myFields = new ArrayList<IDataField>(32);

    public Form28() {
        super();
        LOGGER.debug("new Form28() created");
        setVflex("1");
        this.setHflex("4");
        this.setClass("grid");
        cols = new Columns();
        cols.setParent(this);
    }

    public void setAspect(String aspect) {
        final int index = aspect.indexOf(':');
        if (index >= 1 && aspect.length() > index) {
            // delimited, and we have text before and after the colon
            this.aspectText = aspect.substring(0, index);
            this.aspect = aspect.substring(index + 1);
        } else {
            // just relative size of entryfields
            this.aspect = aspect;
        }
    }

    @Override
    public void setViewModelId(String viewModelId) {
        this.viewModelId = viewModelId;
        crudViewModel = GridIdTools.getViewModelByViewModelId(viewModelId);
    }

    public void register(IDataField field) {
        myFields.add(field);
    }

    @Listen("onCreate")
    public void onCreate() {
        GridIdTools.enforceViewModelId(this);
        if (crudViewModel == null) {
            LOGGER.error("Form28 without viewModelId");
            throw new RuntimeException("Form28 without viewModelId");
        }

        LOGGER.debug("Form28 onCreate: doing columns...");
        for (int i = 0; i < numColumns; ++i) {
            Column labelColumn = new Column();
            labelColumn.setHflex(aspectText);
            labelColumn.setParent(cols);
            Column dataColumn = new Column();
            dataColumn.setHflex(aspect);
            dataColumn.setParent(cols);
        }

        // if this form is inside some Crud28 control, register for activation / deactivation functionality
        inCrud = GridIdTools.findAnchestorOfType(this, Crud28.class);
        if (inCrud != null) {
            inCrud.registerForm(this);
        }
        LOGGER.debug("I'm {}inside a Crud28, {} fields have been registered",
                inCrud == null ? "NOT " : "", myFields.size());

        // Automatically wire up dropdown filters based on dropdownFilterField property
        setupAutoFilters();
    }

    /**
     * Automatically sets up filter event wiring for dropdown fields that have a dropdownFilterField property.
     * This method is called after all fields have been registered in onCreate().
     */
    private void setupAutoFilters() {
        // Find all dropdown fields that have a filter field specified
        for (IDataField dropdownField : myFields) {
            if (dropdownField instanceof AbstractDropdownDataField addf) {
                setupAutoFilter(addf, addf.getFilterFieldName(), true);
                setupAutoFilter(addf, addf.getFilterFieldName2(), false);
            }
        }
    }

    private void setupAutoFilter(final AbstractDropdownDataField addf, final String filterFieldName, final boolean primary) {
        if (filterFieldName == null) {
            return; // no filter: nothing to do
        }
        final IDataField sourceField = findFieldByName(filterFieldName);
        if (sourceField != null) {
            final Consumer<Event> setter = primary ? e -> addf.setFilterValue(sourceField.getValue()) : e -> addf.setFilterValue2(sourceField.getValue());
            // First: Initial filter setup based on current value
            setter.accept(null);  // contents of the event is unused, therefore null is fine

            // Then: Listen for changes in the source field and update the dropdown filter
            if (sourceField.getComponent() != null) {
                sourceField.getComponent().addEventListener(Events.ON_CHANGE, e -> setter.accept(e));
                LOGGER.debug("Auto filter setup for dropdown field {} based on source field {}", addf.getFieldName(), sourceField.getFieldName());
            }
        } else {
            LOGGER.warn("Cannot autowire dropdown filter for field {}: filter field {} not found", addf.getFieldName(), filterFieldName);
        }
    }

    /**
     * Finds a field by its field name.
     * @param fieldName the field name to search for
     * @return the field with the given name, or null if not found
     */
    private IDataField findFieldByName(final String fieldName) {
        for (IDataField field : myFields) {
            if (fieldName.equals(field.getFieldName())) {
                return field;
            }
        }
        return null;
    }

    /** Called by Crud28 parent, in order to enable / disable fields on this form. */
    public void updateState(CrudMode mode) {
        LOGGER.debug("Updating {} fields to {}", myFields.size(), mode);
        switch (mode) {
        case CURRENT:
            for (IDataField field: myFields) {
                if (field.getFieldName().equals(T9tConstants.TENANT_ID_FIELD_NAME)) {
                    field.setDisabled(true);
                } else {
                    Map<String, String> props = field.getFieldDefintion().getProperties();
                    if (props != null && props.get("notupdatable") != null)
                        field.setDisabled(true);
                    else
                        field.setDisabled(false);
                }
            }
            break;
        case CURRENT_PROTECTED_VIEW:
        case CURRENT_RO:
        case NONE:
            // disable all
            for (IDataField field: myFields) {
                field.setDisabled(true);
            }
            break;
        case UNSAVED_NEW:
            // enable all except a possible tenantId field, unless in tenantCategory A and current tenant @
            final boolean isTenantVM = Constants.VM_ID_TENANT.equals(viewModelId);
            for (IDataField field: myFields) {
                field.setDisabled(field.getFieldName().equals(T9tConstants.TENANT_ID_FIELD_NAME) && !isTenantVM);
            }
            break;
        }
    }

    @Override
    public CrudViewModel<BonaPortable, TrackingBase> getCrudViewModel() {
        return crudViewModel;
    }

    @Override
    public IDataFieldFactory getDataFactory() {
        return dataFieldFactory;
    }

    @Override
    public String getViewModelId() {
        return viewModelId;
    }

    public int getNumColumns() {
        return numColumns;
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
    }

    @Override
    public ApplicationSession getSession() {
        return as;
    }
}
