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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Grid;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.zkui.components.IDataFactoryOwner;
import com.arvatosystems.t9t.zkui.components.IDataFieldFactory;
import com.arvatosystems.t9t.zkui.components.IViewModelOwner;
import com.arvatosystems.t9t.zkui.components.datafields.IDataField;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.viewmodel.AbstractCrudVM.CrudMode;
import com.arvatosystems.t9t.zkui.util.Constants;

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
