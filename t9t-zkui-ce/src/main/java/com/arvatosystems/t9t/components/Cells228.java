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
import org.zkoss.zul.Cell;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.impl.InputElement;

import com.arvatosystems.t9t.base.FieldMappers;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.entities.InternalTenantRef42;
import com.arvatosystems.t9t.component.datafields.DecimalDataField;
import com.arvatosystems.t9t.component.datafields.IDataField;

import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.util.FieldGetter;

// a short form for the below explicit code:
// Example: Instead of
// <cells228 id="dataSinkId" value="@bind(vm.data.dataSinkId)" id2="outputEncoding" value2="@bind(vm.data.outputEncoding)"/>
// you can also use
// <row>
//     <label28 id="dataSinkId"/>     <field28 id="dataSinkId"     value="@bind(vm.data.dataSinkId)"/>
//     <label28 id="outputEncoding"/> <field28 id="outputEncoding" value="@bind(vm.data.outputEncoding)"/>
// </row>
// if more flexibility is required
@ComponentAnnotation({
    "value:@ZKBIND(ACCESS=both,SAVE_EVENT=onChange)",
    "value2:@ZKBIND(ACCESS=both,SAVE_EVENT=onChange)"
})
public class Cells228 extends Cells28 {
    private static final long serialVersionUID = -770193551161940L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Cells228.class);

    private IDataField idf2;
    private Label label2;
    private Cell cell2a;
    private Cell cell2b;
    private String dataFieldId2;
    private Object deferredValue2;
    private int colspan2 = 1;
    private int rows2 = 1;
    private boolean readonly2 = false;
    private Boolean required2 = null;
    private Boolean disabled2 = false;
    private String enums2 = null;
    private String decimals2 = null;
    private String type2 = null;

    public Cells228() {
        super();
        cell2a = new Cell();
        cell2a.setParent(this);
        cell2b = new Cell();
        cell2b.setParent(this);
        label2 = new Label();
        label2.setParent(cell2a);
        if (cellHeight != null) {
            cell2a.setHeight(cellHeight);
            cell2b.setHeight(cellHeight);
        }
    }

    public void setId2(String id) {
        // forward the id to the data field only
        dataFieldId2 = id;
    }

    public void setValue2(Object t) {
        if (LOGGER.isTraceEnabled()) {
            // mask value as ***** if type1 equals "password", for everything else log the value
            LOGGER.trace("{}.setValue2({}) called (class {})", dataFieldId2, (getType2() != null && getType2().equals("password")) ? "*****" : t, t == null ? "N/A" : t.getClass().getSimpleName());
        }
        deferredValue2 = t;
        if (idf2 != null) {
            if (t == null && idf2.getComponent() instanceof InputElement) {
                InputElement tb = (InputElement) idf2.getComponent();
                tb.setRawValue(null);
            } else {
                idf2.setValue(t);
            }
        }
    }
    public Object getValue2() {
        final Object res = idf2 != null ? idf2.getValue() : null;
        if (LOGGER.isTraceEnabled()) {
            // mask value as ***** if type1 equals "password", for everything else log the value
            LOGGER.trace("{}.getValue2() called, returns {}", dataFieldId2, (getType2() != null && getType2().equals("password")) ? "*****" : res);
        }
        return res;
    }

//    @Listen("onCreate")
    @Override
    protected void myOnCreate() {
        // LOGGER.debug("cells 228 onCreate");
        super.myOnCreate();
        String strippedFieldname2 = FieldMappers.stripIndexes(dataFieldId2);
        FieldDefinition f = dataFieldId2.endsWith(T9tConstants.TENANT_REF_FIELD_NAME42) ?
                InternalTenantRef42.meta$$tenantRef :
                FieldGetter.getFieldDefinitionForPathname(crudViewModel.dtoClass.getMetaData(), strippedFieldname2);

        // provide the label text and create the data field
        idf2 = setIdAndValueAndCreateField(label2, f, required2, dataFieldId2, enums2, decimals2);
        if (deferredValue2 != null)
            setValue2(deferredValue2);

        Component dataField2 = idf2.getComponent();
        if (dataField2 != null) {
            if (dataField2 instanceof InputElement) {
                InputElement iE = (InputElement) dataField2;
                iE.setReadonly(readonly2);
                iE.setDisabled(getDisabled2());
                LOGGER.debug("InputElement2 {} space owner is {}, dataField space owner {}", dataFieldId2, iE.getSpaceOwner(), dataField2.getSpaceOwner());
            }
            dataField2.setId(dataFieldId2 + ".c");
            dataField2.setParent(cell2b);

            // also forward the onChange event to allow saving of changed data
            dataField2.addEventListener(Events.ON_CHANGE, (ev) -> {
                if (LOGGER.isTraceEnabled()) {
                    // mask value as ***** if type1 equals "password", for everything else log the value
                    LOGGER.trace("onChange caught for {}, current value is {}", dataFieldId2, (getType2() != null && getType2().equals("password")) ? "*****" : getValue());
                }
                Events.postEvent(new Event(Events.ON_CHANGE, this, null));
            });

            if (dataField2 instanceof Textbox) {
                Textbox tb = (Textbox)dataField2;
                if (rows2 > 1) {
                     tb.setRows(rows2);
                     tb.setMultiline(true);
                }

                if (type2 != null) {
                    tb.setType(type2);
                }
            }
        }
        if (form != null)
            form.register(idf2);
    }

    public int getColspan2() {
        return colspan2;
    }

    public void setColspan2(int colspan2) {
        this.colspan2 = colspan2;
        cell2b.setColspan(colspan2);
    }

    public int getRows2() {
        return rows2;
    }

    public void setRows2(int rows2) {
        this.rows2 = rows2;
    }

    public boolean isReadonly2() {
        return readonly2;
    }

    public void setReadonly2(boolean readonly2) {
        this.readonly2 = readonly2;
        if (idf2 != null) {
            // post creation
            Component c = idf2.getComponent();
            if (c != null && c instanceof InputElement) {
                ((InputElement)c).setReadonly(readonly2);
            }
        }
    }

    public Boolean getRequired2() {
        return required2;
    }

    public void setRequired2(Boolean required1) {
        this.required2 = required1;
    }

    public String getEnums2() {
        return enums2;
    }

    public void setEnums2(String enums2) {
        this.enums2 = enums2;
    }

    public Boolean getDisabled2() {
        return disabled2;
    }

    public void setDisabled2(Boolean disabled2) {
        this.disabled2 = disabled2;
    }

    public String getDecimals2() {
        return decimals2;
    }

    public void setDecimals2(String decimals2) {
        this.decimals2 = decimals2;  // set initial number of decimals
        if (idf2 != null && idf2 instanceof DecimalDataField)
            ((DecimalDataField)idf2).setDecimals(decimals2);  // updates after creation
        else
            LOGGER.warn("Setting decimals2 property for a field which is not a Decimal ({})", dataFieldId2);
    }

    public String getType2() {
        return type2;
    }

    public void setType2(String type2) {
        this.type2 = type2;
    }

    public void setGroup2(Object group2) {
        setGroup(idf2, group2);
    }
}
