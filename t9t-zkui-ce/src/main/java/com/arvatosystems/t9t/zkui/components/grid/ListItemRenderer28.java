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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Image;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

import com.arvatosystems.t9t.base.FieldMappers;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.zkui.converters.grid.IItemConverter;
import com.arvatosystems.t9t.zkui.converters.grid.ItemConverterRegistry;
import com.arvatosystems.t9t.zkui.filters.IResultTextFilter;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.core.DataAndMeta;
import de.jpaw.bonaparte.core.FoldingComposer;
import de.jpaw.bonaparte.core.ListMetaComposer;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.FoldingStrategy;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;

public class ListItemRenderer28<T extends BonaPortable> implements ListitemRenderer<T> {
    public static final int LIMIT = 20;
    private static final Logger LOGGER = LoggerFactory.getLogger(ListItemRenderer28.class);

    protected List<String> visibleFieldnames;
    protected FoldingComposer<RuntimeException> foldingComposer;
    protected final ListMetaComposer metaComposer = new ListMetaComposer();
    protected final BonaPortableClass<T> bclass;  // currently unused
    protected final boolean haveTracking;
    protected final IGridRowCssSelector gridRowCssSelector;
    protected final Map<String, IItemConverter<?>> itemConverterMap = new ConcurrentHashMap<>(60);   // caches the item converter by field name (i.e. one per column)

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

    private static final String NUMERIC_LISTCELL_STYLE = "text-align:right;";

    @Override
    public void render(final Listitem listitem, final T data, final int index) throws Exception {
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
                            addListcell(listitem, data, fieldName, dynamicField.data, dynamicField.meta);
                        }
                        continue;
                    }
                    if (fields.size() == 1) {
                        addListcell(listitem, data, fieldName, fields.get(0).data, fields.get(0).meta);
                    } else {
                        // handling for 1:n fields (list, set, map) with limit
                        final String joined = joinIntoSingleField(fields);
                        addListcell(listitem, data, fieldName, joined, fields.get(0).meta);
                    }
                    continue;
                }
                // some field only exists in visibleFieldnames, thus no fields were added from "row"
                addListcell(listitem, data, fieldName, null, null);
            }
        } else {
            // for simple grid, just add the cells for each field
            for (final DataAndMeta field : row) {
                addListcell(listitem, data, visibleFieldnames.get(column++), field.data, field.meta);
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

    private <X> void addListcell(final Listitem listitem, final T data, final String fieldName, final X value, final FieldDefinition meta) {
        Listcell listcell = new Listcell();
        listcell.setParent(listitem);

        LOGGER.trace("Listcell for {} has value {}", fieldName, value);

        if (value == null) {
            return; // empty cell
        }
        if (isDynField(meta)) {
            // Display as text - no converter or dynamic field
            listcell.setLabel(value.toString());
            return;
        }
        // obtain a converter
        final IItemConverter<X> classConverter = (IItemConverter<X>) itemConverterMap.computeIfAbsent(fieldName, k -> ItemConverterRegistry.getConverter(fieldName, meta));

        // Check if converter provides an icon path
        if (classConverter.isIcon()) {
            final String iconPath = classConverter.iconPath(value, data, fieldName, meta);
            if (iconPath != null) {
                // Display as icon - path is already validated by the converter
                Image iconImage = new Image(iconPath);
                iconImage.setParent(listcell);
                // Set tooltip as fallback
                iconImage.setTooltip(value.toString());
                LOGGER.trace("Rendering icon for field {} with path {}", fieldName, iconPath);
            }
        } else {
            // Display as text using converter
            String converted = classConverter.getFormattedLabel(value, data, fieldName, meta);
            if (converted != null) {
                listcell.setLabel(converted);
                if (classConverter.isRightAligned()) {
                    listcell.setStyle(NUMERIC_LISTCELL_STYLE);
                }
            }
        }
    }

    public static boolean isDynField(@Nonnull final FieldDefinition meta) {
        final Map<String, String> props = meta.getProperties();
        return props != null && props.get("dynGrid") != null;
    }

    public void setTextFilter(final IResultTextFilter<T> filterService, final Supplier<String> textSource) {
        this.textFilterService = filterService;
        this.filterTextSource = textSource;
    }
}
