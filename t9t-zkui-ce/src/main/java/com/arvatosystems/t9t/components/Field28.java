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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.annotation.ComponentAnnotation;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.impl.InputElement;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.FieldMappers;
import com.arvatosystems.t9t.component.datafields.DataFieldParameters;
import com.arvatosystems.t9t.component.datafields.DecimalDataField;
import com.arvatosystems.t9t.component.datafields.GroupedDropdownDataField;
import com.arvatosystems.t9t.component.datafields.IDataField;
import com.arvatosystems.t9t.component.ext.IDataFieldFactory;
import com.arvatosystems.t9t.component.ext.IViewModelOwner;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.util.FieldGetter;
import de.jpaw.dp.Jdp;

/** A field in a cell. Used in modal dialogs or grid detail views.
 * The superclass must set a view model which references a data object.
 *  */
@ComponentAnnotation("value:@ZKBIND(ACCESS=both,SAVE_EVENT=onChange)")
public class Field28 extends Cell {
    private static final long serialVersionUID = -77019355145361940L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Field28.class);

    protected ApplicationSession as;
    protected IViewModelOwner vmOwner;
    protected String viewModelId;
    protected final IDataFieldFactory ff = Jdp.getRequired(IDataFieldFactory.class);
    protected CrudViewModel<BonaPortable, TrackingBase> crudViewModel;
    protected IDataField idf;
    private Object deferredValue;
    private int rows1 = 1;
    private boolean readonly1 = false;
    private Boolean required1 = null;
    private String enums1 = null;
    private boolean disabled1= false;
    private String decimals1 = null;
    protected Form28 form = null;

    public void setValue(Object t) {
        LOGGER.debug("{}.setValue({}) called", getId(), t);
        deferredValue = t;
        if (idf != null)
            idf.setValue(t);
    }
    public Object getValue() {
        LOGGER.debug("{}.getValue() called", getId());
        return idf.getValue();
    }

    @Listen("onCreate")
    public void onCreate() {
        String myId = getId();

        vmOwner = GridIdTools.getAnchestorOfType(this, IViewModelOwner.class);
        viewModelId = GridIdTools.enforceViewModelId(vmOwner);
        crudViewModel = vmOwner.getCrudViewModel();
        as = vmOwner.getSession();
        String strippedFieldname = FieldMappers.stripIndexes(myId);
        FieldDefinition f = FieldGetter.getFieldDefinitionForPathname(crudViewModel.dtoClass.getMetaData(), strippedFieldname);

        idf = ff.createField(new DataFieldParameters(f, myId, required1, as, enums1, decimals1), crudViewModel);

        if (deferredValue != null && idf != null)
            idf.setValue(deferredValue);

        Component dataField = idf != null ? idf.getComponent() : null;
        if (dataField != null) {
            dataField.setParent(this);
            dataField.setVisible(this.isVisible());
            // also forward the onChange event to allow saving of changed data
            dataField.addEventListener(Events.ON_CHANGE, (ev) -> {
                LOGGER.debug("onChange caught for {}, current value is {}", getId(), getValue());
                    Events.postEvent(new Event(Events.ON_CHANGE, this, null));
            });

            if ((dataField instanceof Textbox) && rows1 > 1) {
                Textbox tb = (Textbox)dataField;
                tb.setRows(rows1);
                tb.setMultiline(true);
            }

            if (dataField != null ) {
                if (dataField instanceof InputElement) {
                    ((InputElement) dataField).setDisabled(disabled1);
                }
                if (dataField instanceof Checkbox) {
                    ((Checkbox) dataField).setDisabled(disabled1);
                }
            }
        }

        // check if we are within a form. This is done in order to register the fields for automatic enabling / disabling
        form = GridIdTools.findAnchestorOfType(this, Form28.class);
        if (form != null)
            form.register(idf);
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
            if (c != null && c instanceof InputElement) {
                ((InputElement)c).setReadonly(readonly1);
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

    public boolean isDisabled1() {
        return disabled1;
    }

    public void setDisabled1(boolean disabled1) {
        this.disabled1 = disabled1;
    }

    public String getDecimals1() {
        return decimals1;
    }

    public void setDecimals1(String decimals1) {
        this.decimals1 = decimals1;  // set initial number of decimals
        if (idf != null && idf instanceof DecimalDataField)
            ((DecimalDataField)idf).setDecimals(decimals1);  // updates after creation
        else
            LOGGER.warn("Setting decimals1 property for a field which is not a Decimal");
    }

    public void setGroup(Object ref) {
        if (idf instanceof GroupedDropdownDataField) {
            GroupedDropdownDataField field = (GroupedDropdownDataField) idf;
            field.setGroup(ref);
        }
    }

    @Override
    public boolean setVisible(boolean visible) {
        if (idf != null && idf.getComponent() != null) {
            boolean old = idf.getComponent().isVisible();
            idf.getComponent().setVisible(visible);
            return old;
        } else {
            return super.setVisible(visible);
        }
    }
}
