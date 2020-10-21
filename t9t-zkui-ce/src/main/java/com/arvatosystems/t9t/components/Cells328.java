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
// <cells328
//       id="dataSinkId"      value="@bind(vm.data.dataSinkId)"
//       id2="outputEncoding" value2="@bind(vm.data.outputEncoding)"
//       id3="timeoutValue"   value3="@bind(vm.data.timeoutValue)"/>
// you can also use
// <row>
//     <label28 id="dataSinkId"/>     <field28 id="dataSinkId"     value="@bind(vm.data.dataSinkId)"/>
//     <label28 id="outputEncoding"/> <field28 id="outputEncoding" value="@bind(vm.data.outputEncoding)"/>
// </row>
// if more flexibility is required
@ComponentAnnotation({
    "value:@ZKBIND(ACCESS=both,SAVE_EVENT=onChange)",
    "value2:@ZKBIND(ACCESS=both,SAVE_EVENT=onChange)",
    "value3:@ZKBIND(ACCESS=both,SAVE_EVENT=onChange)"
})
public class Cells328 extends Cells228 {
    private static final long serialVersionUID = -770193551161940L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Cells328.class);

    private IDataField idf3;
    private Label label3;
    private Cell cell3a;
    private Cell cell3b;
    private String dataFieldId3;
    private Object deferredValue3;
    private int colspan3 = 1;
    private int rows3 = 1;
    private boolean readonly3 = false;
    private Boolean required3 = null;
    private Boolean disabled3 = false;
    private String enums3 = null;
    private String decimals3 = null;
    private String type3 = null;

    public Cells328() {
        super();
        cell3a = new Cell();
        cell3a.setParent(this);
        cell3b = new Cell();
        cell3b.setParent(this);
        label3 = new Label();
        label3.setParent(cell3a);
        if (cellHeight != null) {
            cell3a.setHeight(cellHeight);
            cell3b.setHeight(cellHeight);
        }
    }

    public void setId3(String id) {
        // forward the id to the data field only
        dataFieldId3 = id;
    }

    public void setValue3(Object t) {
        if (LOGGER.isTraceEnabled()) {
            // mask value as ***** if type1 equals "password", for everything else log the value
            LOGGER.trace("{}.setValue3({}) called (class {})", dataFieldId3, (getType3() != null && getType3().equals("password")) ? "*****" : t, t == null ? "N/A" : t.getClass().getSimpleName());
        }
        deferredValue3 = t;
        if (idf3 != null) {
            if (t == null && idf3.getComponent() instanceof InputElement) {
                InputElement tb = (InputElement) idf3.getComponent();
                tb.setRawValue(null);
            } else {
                idf3.setValue(t);
            }
        }
    }
    public Object getValue3() {
        final Object res = idf3 != null ? idf3.getValue() : null;
        if (LOGGER.isTraceEnabled()) {
            // mask value as ***** if type1 equals "password", for everything else log the value
            LOGGER.trace("{}.getValue3() called, returns {}", dataFieldId3, (getType3() != null && getType3().equals("password")) ? "*****" : res);
        }
        return res;
    }

//    @Listen("onCreate")
    @Override
    protected void myOnCreate() {
        // LOGGER.debug("cells 328 onCreate");
        super.myOnCreate();
        String strippedFieldname3 = FieldMappers.stripIndexes(dataFieldId3);
        FieldDefinition f = dataFieldId3.endsWith(T9tConstants.TENANT_REF_FIELD_NAME42) ?
                InternalTenantRef42.meta$$tenantRef :
                FieldGetter.getFieldDefinitionForPathname(crudViewModel.dtoClass.getMetaData(), strippedFieldname3);

        // provide the label text and create the data field
        idf3 = setIdAndValueAndCreateField(label3, f, required3, dataFieldId3, enums3, decimals3);
        if (deferredValue3 != null)
            setValue3(deferredValue3);

        Component dataField3 = idf3.getComponent();
        if (dataField3 != null) {
            if (dataField3 instanceof InputElement) {
                InputElement iE = (InputElement) dataField3;
                iE.setReadonly(readonly3);
                iE.setDisabled(getDisabled3());
                LOGGER.debug("InputElement3 {} space owner is {}, dataField space owner {}", dataFieldId3, iE.getSpaceOwner(), dataField3.getSpaceOwner());
            }
            dataField3.setId(dataFieldId3 + ".c");
            dataField3.setParent(cell3b);

            // also forward the onChange event to allow saving of changed data
            dataField3.addEventListener(Events.ON_CHANGE, (ev) -> {
                if (LOGGER.isTraceEnabled()) {
                    // mask value as ***** if type1 equals "password", for everything else log the value
                    LOGGER.trace("onChange caught for {}, current value is {}", dataFieldId3, (getType3() != null && getType3().equals("password")) ? "*****" : getValue());
                }
                Events.postEvent(new Event(Events.ON_CHANGE, this, null));
            });

            if (dataField3 instanceof Textbox) {
                Textbox tb = (Textbox)dataField3;
                if (rows3 > 1) {
                     tb.setRows(rows3);
                     tb.setMultiline(true);
                }

                if (type3 != null) {
                    tb.setType(type3);
                }
            }
        }
        if (form != null)
            form.register(idf3);
    }

    public int getColspan3() {
        return colspan3;
    }

    public void setColspan3(int colspan3) {
        this.colspan3 = colspan3;
        cell3b.setColspan(colspan3);
    }

    public int getRows3() {
        return rows3;
    }

    public void setRows3(int rows3) {
        this.rows3 = rows3;
    }

    public boolean isReadonly3() {
        return readonly3;
    }

    public void setReadonly3(boolean readonly3) {
        this.readonly3 = readonly3;
        if (idf3 != null) {
            // post creation
            Component c = idf3.getComponent();
            if (c != null && c instanceof InputElement) {
                ((InputElement)c).setReadonly(readonly3);
            }
        }
    }

    public Boolean getRequired3() {
        return required3;
    }

    public void setRequired3(Boolean required1) {
        this.required3 = required1;
    }

    public String getEnums3() {
        return enums3;
    }

    public void setEnums3(String enums3) {
        this.enums3 = enums3;
    }

    public Boolean getDisabled3() {
        return disabled3;
    }

    public void setDisabled3(Boolean disabled3) {
        this.disabled3 = disabled3;
    }

    public String getDecimals3() {
        return decimals3;
    }

    public void setDecimals3(String decimals3) {
        this.decimals3 = decimals3;  // set initial number of decimals
        if (idf3 != null && idf3 instanceof DecimalDataField)
            ((DecimalDataField)idf3).setDecimals(decimals3);  // updates after creation
        else
            LOGGER.warn("Setting decimals3 property for a field which is not a Decimal ({})", dataFieldId3);
    }

    public String getType3() {
        return type3;
    }

    public void setType3(String type3) {
        this.type3 = type3;
    }

    public void setGroup3(Object group3) {
        setGroup(idf3, group3);
    }
}
