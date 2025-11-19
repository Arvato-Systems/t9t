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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.annotation.ComponentAnnotation;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.impl.InputElement;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.FieldMappers;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.entities.InternalTenantId;
import com.arvatosystems.t9t.zkui.components.IDataFieldFactory;
import com.arvatosystems.t9t.zkui.components.IViewModelOwner;
import com.arvatosystems.t9t.zkui.components.datafields.DataFieldParameters;
import com.arvatosystems.t9t.zkui.components.datafields.DecimalDataField;
import com.arvatosystems.t9t.zkui.components.datafields.GroupedDropdownDataField;
import com.arvatosystems.t9t.zkui.components.datafields.IDataField;
import com.arvatosystems.t9t.zkui.components.dropdown28.nodb.Dropdown28ComboBoxItem;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.Constants;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.util.FieldGetter;
import de.jpaw.dp.Jdp;

/**
 * A row with 2 cells, a label and a field. The row has a generic getValue() method and therefore we can use any object type.
 * The class is inherited by Cells228 for 4 cells and Cells328 for 6 cells.
 */
// the @ComponentAnnotation annotation informs ZK to invoke getValue() and
// update the viewmodel after the onChange event
// see
// https://www.zkoss.org/wiki/ZK_Developer's_Reference/MVVM/Advanced/Binding_Annotation_for_a_Custom_Component
@ComponentAnnotation("value:@ZKBIND(ACCESS=both,SAVE_EVENT=onChange)")
public class Cells28 extends Row {
    private static final long serialVersionUID = -770193551161940L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Cells28.class);

    protected ApplicationSession as;
    protected String viewModelId;
    protected CrudViewModel<BonaPortable, TrackingBase> crudViewModel;
    protected final IDataFieldFactory ff = Jdp.getRequired(IDataFieldFactory.class);
    protected Form28 form = null;
    protected String cellHeight = "32px";

    private IDataField idf;
    private Label label;
    private Cell cell1a;
    private Cell cell1b;
    private String dataFieldId;
    private Object deferredValue;
    private int colspan1 = 1;
    private int rows1 = 1;
    private boolean readonly1 = false;
    private Boolean required1 = null;
    private Boolean disabled1 = false;
    private String enums1 = null;
    private String decimals1 = null;
    private String type1 = null;
    private boolean formRegister1 = true; // if true then only this component will register with the parent Form28

    public Cells28() {
        super();
        cell1a = new Cell();
        cell1a.setParent(this);
        cell1b = new Cell();
        cell1b.setParent(this);
        label = new Label();
        label.setParent(cell1a);
        if (cellHeight != null) {
            cell1a.setHeight(cellHeight);
            cell1b.setHeight(cellHeight);
        }

        addEventListener(Events.ON_CREATE, (ev) -> myOnCreate());
    }

    @Override
    public void setId(String id) {
        // forward the id to the data field only
        super.setId(id);
        dataFieldId = id;
    }

    protected IDataField setIdAndValueAndCreateField(Label l, FieldDefinition f, Boolean override, String fieldId, String enumZulRestrictions,
            String decimals) {
        IDataField df = ff.createField(new DataFieldParameters(f, fieldId, override, as, enumZulRestrictions, decimals), crudViewModel);
        if (df == null) {
            LOGGER.error("****  FATAL: cannot create data field {} inside viewModel {}", fieldId, viewModelId);
            // throw, error logs are sometimes not noticed...
            throw new RuntimeException("****  FATAL: cannot create data field " + fieldId + " inside viewModel " + viewModelId);
        }
        String requiredMarker = df.getIsRequired() ? "*" : "";
        l.setId(fieldId + ".l");
        l.setValue(as.translate(viewModelId, fieldId) + requiredMarker + ":");
        return df;
    }

    @Override
    public void setValue(Object t) {
        if (LOGGER.isTraceEnabled()) {
            // mask value as ***** if type1 equals "password", for everything else log the value
            LOGGER.trace("{}.setValue({}) called (class {})", dataFieldId, (getType1() != null && getType1().equals("password")) ? "*****" : t,
                    t == null ? "N/A" : t.getClass().getSimpleName());
        }
        deferredValue = t;
        if (idf != null) {
            if (t == null && idf.getComponent() instanceof InputElement iE) {
                if (idf.getComponent() instanceof Dropdown28ComboBoxItem) {
                    // Update selected item to null
                    idf.setValue(null);
                }

                // Update value to null
                iE.setRawValue(null);
            } else {
                idf.setValue(t);
            }
        }
    }

    @Override
    public Object getValue() {
        final Object res = idf != null ? idf.getValue() : null;
        if (LOGGER.isTraceEnabled()) {
            // mask value as ***** if type1 equals "password", for everything else log the value
            LOGGER.trace("{}.getValue() called, returns {}", dataFieldId, (getType1() != null && getType1().equals("password")) ? "*****" : res);
        }
        return res;
    }

    protected void myOnCreate() {
        IViewModelOwner vmOwner = GridIdTools.getAnchestorOfType(this, IViewModelOwner.class);
        if (vmOwner == null) {
            LOGGER.error("****  FATAL: unable to create cells28 inside viewModel {}, vmOwner is null.", viewModelId);
            throw new RuntimeException("Unable to create cells28 inside viewModel " + viewModelId);
        }
        LOGGER.debug("vmOwner is {}", vmOwner.getClass().getSimpleName() + ":" + vmOwner.getViewModelId());
        viewModelId = GridIdTools.enforceViewModelId(vmOwner);
        crudViewModel = vmOwner.getCrudViewModel();
        as = vmOwner.getSession();
        String strippedFieldname = FieldMappers.stripIndexes(dataFieldId);
        FieldDefinition f = (dataFieldId.endsWith(T9tConstants.TENANT_ID_FIELD_NAME) && !viewModelId.equals(Constants.VM_ID_TENANT))
                ? InternalTenantId.meta$$tenantId
                : FieldGetter.getFieldDefinitionForPathname(crudViewModel.dtoClass.getMetaData(), strippedFieldname);

        // check if we are within a form. This is done in order to register the fields for automatic enabling / disabling
        form = GridIdTools.findAnchestorOfType(this, Form28.class);

        // provide the label text and create the data field
        idf = setIdAndValueAndCreateField(label, f, required1, dataFieldId, enums1, decimals1);
        if (deferredValue != null)
            setValue(deferredValue);

        Component dataField = idf.getComponent();

        if (dataField != null) {
            if (dataField instanceof InputElement iE) {
                iE.setReadonly(readonly1);
                iE.setDisabled(getDisabled1());
                LOGGER.debug("InputElement {} space owner is {}, dataField space owner {}", dataFieldId, iE.getSpaceOwner(), dataField.getSpaceOwner());
            }
            dataField.setId(dataFieldId + ".c");
            dataField.setParent(cell1b);

            // also forward the onChange event to allow saving of changed data
            dataField.addEventListener(Events.ON_CHANGE, (ev) -> {
                if (LOGGER.isTraceEnabled()) {
                    // mask value as ***** if type1 equals "password", for everything else log the value
                    LOGGER.trace("onChange caught for {}, current value is {}", getId(),
                            (getType1() != null && getType1().equals("password")) ? "*****" : getValue());
                }
                Events.postEvent(new Event(Events.ON_CHANGE, this, null));
            });

            if (dataField instanceof Textbox tb) {
                if (rows1 > 1) {
                     tb.setRows(rows1);
                     tb.setMultiline(true);
                }

                if (type1 != null) {
                    tb.setType(type1);
                }
            }
        }
        if (form != null && formRegister1)
            form.register(idf);
    }

    public int getColspan1() {
        return colspan1;
    }

    public void setColspan1(int colspan1) {
        this.colspan1 = colspan1;
        cell1b.setColspan(colspan1);
    }

    public int getRows1() {
        return rows1;
    }

    public void setRows1(int rows1) {
        this.rows1 = rows1;
    }

    public boolean isReadonly1() {
        return readonly1;
    }

    public void setReadonly1(boolean readonly1) {
        this.readonly1 = readonly1;
        if (idf != null) {
            // post creation
            Component c = idf.getComponent();
            if (c instanceof InputElement iE) {
                iE.setReadonly(readonly1);
            }
        }
    }

    public Boolean getRequired1() {
        return required1;
    }

    public void setRequired1(Boolean required1) {
        this.required1 = required1;
    }

    public String getEnums1() {
        return enums1;
    }

    public void setEnums1(String enums1) {
        this.enums1 = enums1;
    }

    public Boolean getDisabled1() {
        return disabled1;
    }

    public void setDisabled1(Boolean disabled1) {
        this.disabled1 = disabled1;
        if (idf != null) {
            idf.setDisabled(disabled1);
        }
    }

    public String getDecimals1() {
        return decimals1;
    }

    public void setDecimals1(String decimals1) {
        this.decimals1 = decimals1;  // set initial number of decimals
        if (idf != null && idf instanceof DecimalDataField decimalField)
            decimalField.setDecimals(decimals1);  // updates after creation
        else
            LOGGER.warn("Setting decimals1 property for a field which is not a Decimal ({})", dataFieldId);
    }

    public String getType1() {
        return type1;
    }

    public void setType1(String type1) {
        this.type1 = type1;
    }

    public void setGroup1(Object group1) {
        setGroup(idf, group1);
    }

    protected void setGroup(IDataField df, Object object) {
        if (df instanceof GroupedDropdownDataField groupedDropdownDF) {
            groupedDropdownDF.setGroup(object);
        }
    }

    public boolean getFormRegister1() {
        return formRegister1;
    }

    public void setFormRegister1(boolean formRegister1) {
        this.formRegister1 = formRegister1;
    }
}
