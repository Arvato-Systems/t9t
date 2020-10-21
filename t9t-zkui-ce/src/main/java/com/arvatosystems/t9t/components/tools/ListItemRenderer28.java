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
package com.arvatosystems.t9t.components.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

import com.arvatosystems.t9t.base.FieldMappers;
import com.arvatosystems.t9t.itemConverter.AllItemConverters;
import com.arvatosystems.t9t.itemConverter.IItemConverter;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.core.DataAndMeta;
import de.jpaw.bonaparte.core.FoldingComposer;
import de.jpaw.bonaparte.core.ListMetaComposer;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.FoldingStrategy;
import de.jpaw.bonaparte.pojos.meta.Multiplicity;
import de.jpaw.dp.Jdp;

public class ListItemRenderer28<T extends BonaPortable> implements ListitemRenderer<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListItemRenderer28.class);

    protected final IItemConverter<Object> itemConverter = Jdp.getRequired(IItemConverter.class, "all");

    //protected List<String> visibleHeaders;
    protected List<String> visibleFieldnames;
    protected FoldingComposer<RuntimeException> foldingComposer;
    protected final ListMetaComposer metaComposer = new ListMetaComposer(false, true, true);
    protected final BonaPortableClass<T> bclass;
    protected final boolean haveTracking;

    protected String context;


    public ListItemRenderer28(BonaPortableClass<T> bclass, boolean haveTracking) {
        this.bclass = bclass;
        this.haveTracking = haveTracking;
        LOGGER.debug("Creating a new DefaultListItemRenderer for class {}", bclass.getPqon());
    }


    public void setGridPreferences(List<String> fields) {
        visibleFieldnames = new ArrayList<String>(fields.size());
        for (String fieldname : fields) {
            visibleFieldnames.add(haveTracking ? FieldMappers.addPrefix(fieldname) : fieldname);
        }
        foldingComposer = new FoldingComposer<>(metaComposer, Collections.singletonMap(BonaPortable.class, visibleFieldnames), FoldingStrategy.TRY_SUPERCLASS);
    }

    public void setContext(String context) {
        this.context = context;
    }

    private static final String GROUPED_EMPTY_LISTCELL_STYLE = "padding-left: 16px;";
    private static final String NUMERIC_LISTCELL_STYLE = "text-align:right;";

    @Override
    public void render(Listitem listitem, T data, int index) throws Exception {
//        if (listitem instanceof Listgroup) {
//            LOGGER.debug("Rendering row {} as group", index);
//            addListgroup(listitem, data, bclass.getBonaPortableClass());
//            return;
//        }
        LOGGER.trace("Rendering row {} (data)", index);
        listitem.setValue(data);
        listitem.setContext(context);

        metaComposer.reset();  // clear previous data
        foldingComposer.writeRecord(data);

        int numDescriptions = visibleFieldnames.size();  // just for sanity checks...
        int column = 0;
        List<DataAndMeta> row = metaComposer.getStorage();
        if (numDescriptions != row.size())
            LOGGER.error("column description count {} differs from element count {}", numDescriptions, row.size());
        for (DataAndMeta field : row) {
            if (column < numDescriptions) {// sanity check!
                addListcell(listitem, data, visibleFieldnames.get(column), bclass.getBonaPortableClass(), field.data, field.meta);
                ++column;
                if (isDynField(field.meta) && field.meta.getMultiplicity() == Multiplicity.LIST && column != row.size()) {
                    --column;  // for dyn fields, only do this for the last column
                }
            }
        }

        LOGGER.trace("Fields were {}", visibleFieldnames);
        LOGGER.trace("Data was {}", data);
    }

    private void addListcell(Listitem listitem, T data, String fieldName, Class<T> beanClass, Object value, FieldDefinition meta) {
        Listcell listcell = new Listcell();
        listcell.setParent(listitem);

        LOGGER.trace("Listcell for {} has value {}", fieldName, value);
        if (value != null) {
            // obtain a converter
            IItemConverter classConverter = AllItemConverters.getConverter(value, data, fieldName, meta);
//            LOGGER.debug("Converter for field {} of class {} is {}",
//                    fieldName, meta.getClass().getSimpleName(), classConverter == null ? "NULL" : classConverter.getClass().getSimpleName());
            if (classConverter == null || isDynField(meta)) {
                listcell.setLabel(value.toString());
            } else {
                // use the converter to convert
                String converted = classConverter.getFormattedLabel(value, data, fieldName, meta);
                listcell.setLabel(converted);

                if (classConverter.isRightAligned())
                    listcell.setStyle(NUMERIC_LISTCELL_STYLE);
            }
        }
    }

    public boolean isDynField(FieldDefinition meta) {
        final Map<String, String> props = meta.getProperties();
        return props != null && props.get("dynGrid") != null;
    }
}
