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
package com.arvatosystems.t9t.zkui.components.grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.zkui.filters.IResultTextFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

import com.arvatosystems.t9t.base.FieldMappers;
import com.arvatosystems.t9t.zkui.converters.grid.AllItemConverters;
import com.arvatosystems.t9t.zkui.converters.grid.IItemConverter;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.core.DataAndMeta;
import de.jpaw.bonaparte.core.FoldingComposer;
import de.jpaw.bonaparte.core.ListMetaComposer;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.FoldingStrategy;
import de.jpaw.dp.Jdp;

public class ListItemRenderer28<T extends BonaPortable> implements ListitemRenderer<T> {
    public static final int LIMIT = 20;
    private static final Logger LOGGER = LoggerFactory.getLogger(ListItemRenderer28.class);

    protected final IItemConverter<Object> itemConverter = Jdp.getRequired(IItemConverter.class, "all");

    //protected List<String> visibleHeaders;
    protected List<String> visibleFieldnames;
    protected FoldingComposer<RuntimeException> foldingComposer;
    protected final ListMetaComposer metaComposer = new ListMetaComposer(false, true, true);
    protected final BonaPortableClass<T> bclass;
    protected final boolean haveTracking;
    protected final IGridRowCssSelector gridRowCssSelector;
    private IResultTextFilter<T> textFilterService;
    private Supplier<String> filterTextSource;
    protected String context;


    public ListItemRenderer28(final BonaPortableClass<T> bclass, final boolean haveTracking, final String rowCssQualifier) {
        this.bclass = bclass;
        this.haveTracking = haveTracking;
        this.gridRowCssSelector = Jdp.getOptional(IGridRowCssSelector.class, rowCssQualifier);
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
    public void render(final Listitem listitem, final T data, final int index) throws Exception {
//        if (listitem instanceof Listgroup) {
//            LOGGER.debug("Rendering row {} as group", index);
//            addListgroup(listitem, data, bclass.getBonaPortableClass());
//            return;
//        }
        LOGGER.trace("Rendering row {} (data)", index);
        listitem.setValue(data);
        listitem.setContext(context);

        if (gridRowCssSelector != null) {
            listitem.addSclass(gridRowCssSelector.getRowCssSelector(data));
        }
        final String filterText = filterTextSource != null ? filterTextSource.get() : null;
        if (textFilterService != null && T9tUtil.isNotBlank(filterText) && textFilterService.getFilter(filterText).test(data)) {
            listitem.addSclass("has-filter-text");
        }

        metaComposer.reset();  // clear previous data
        foldingComposer.writeRecord(data);

        int numDescriptions = visibleFieldnames.size();  // just for sanity checks...
        int column = 0;
        List<DataAndMeta> row = metaComposer.getStorage();
        if (numDescriptions != row.size()) {
            LOGGER.error("column description count {} differs from element count {}", numDescriptions, row.size());

            final Map<String, List<DataAndMeta>> dataByFieldName = new HashMap<>();
            for (final DataAndMeta field : row) {
                dataByFieldName.computeIfAbsent(field.meta.getName(), k -> new ArrayList<>())
                        .add(field);
            }

            for (final String fieldName : visibleFieldnames) {
                final String visibleFieldname = fieldName.substring(fieldName.lastIndexOf('.') + 1);
                final List<DataAndMeta> fields = dataByFieldName.get(visibleFieldname);
                if (fields != null && !fields.isEmpty()) {
                    // handling for dynamic fields
                    if (isDynField(fields.get(0).meta)) {
                        for (final DataAndMeta dynamicField : fields) {
                            addListcell(listitem, data, fieldName, bclass.getBonaPortableClass(), dynamicField.data, dynamicField.meta);
                        }
                        continue;
                    }
                    if (fields.size() == 1) {
                        addListcell(listitem, data, fieldName, bclass.getBonaPortableClass(), fields.get(0).data, fields.get(0).meta);
                    } else {
                        // handling for 1:n fields (list, set, map) with limit
                        final String joined = joinIntoSingleField(fields);
                        addListcell(listitem, data, fieldName, bclass.getBonaPortableClass(), joined, fields.get(0).meta);
                    }
                    continue;
                }
                // some field only exists in visibleFieldnames, thus no fields were added from "row"
                addListcell(listitem, data, fieldName, bclass.getBonaPortableClass(), null, null);
            }
        } else {
            // for simple grid, just add the cells for each field
            for (final DataAndMeta field : row) {
                addListcell(listitem, data, visibleFieldnames.get(column++), bclass.getBonaPortableClass(), field.data, field.meta);
            }
        }

        LOGGER.trace("Fields were {}", visibleFieldnames);
        LOGGER.trace("Data was {}", data);
    }

    private static String joinIntoSingleField(final List<DataAndMeta> dynamicFields) {
        final StringBuilder sb = new StringBuilder(60);
        int count = 0;
        for (final DataAndMeta field : dynamicFields) {
            if (count >= LIMIT) {
                sb.append(", ...");
                break;
            }
            final Object val = field.data;
            if (count > 0) {
                sb.append(", ");
            }
            sb.append(val == null ? "" : val.toString());
            count++;
        }
        return sb.toString();
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

    public void setTextFilter(final IResultTextFilter<T> filterService, final Supplier<String> textSource) {
        this.textFilterService = filterService;
        this.filterTextSource = textSource;
    }
}
