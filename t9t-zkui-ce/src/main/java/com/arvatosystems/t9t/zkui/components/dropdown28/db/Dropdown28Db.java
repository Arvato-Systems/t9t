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
package com.arvatosystems.t9t.zkui.components.dropdown28.db;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.lang.Objects;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.ListModels;

import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.base.search.LeanSearchRequest;
import com.arvatosystems.t9t.zkui.components.dropdown28.ComboBoxEntry;
import com.arvatosystems.t9t.zkui.components.dropdown28.ComboBoxEntryComboitemRenderer;
import com.arvatosystems.t9t.zkui.components.dropdown28.factories.IDropdown28DbFactory;
import com.arvatosystems.t9t.zkui.components.dropdown28.nodb.Dropdown28Registry;
import com.arvatosystems.t9t.zkui.fixedFilters.IFixedFilter;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.T9tConfigConstants;
import com.arvatosystems.t9t.zkui.util.UiConfigurationProvider;
import com.arvatosystems.t9t.zkui.util.ZulUtils;

import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.dp.Jdp;

public class Dropdown28Db<REF extends BonaPortable> extends Combobox {
    private static final Logger LOGGER = LoggerFactory.getLogger(Dropdown28Db.class);
    private static final long serialVersionUID = 3911446278727438869L;
    private static final String DEFAULT_DISPLAY_FORMAT = "{0} - {1}";
    private static final int MAX_ROWS = getMaxItems();
    private static final Comparator STRING_COMPARATOR = (key, value) -> {
        final String idx = Objects.toString(key);
        return idx != null && value != null && !idx.isEmpty() && Objects.toString(value).toLowerCase().startsWith(idx.toLowerCase()) ? 0 : 1;
    };

    // instance fields and methods

    private final ApplicationSession                session = ApplicationSession.get();
    private final IDropdown28DbFactory<REF>         factory;
    private List<Description>                       entries;      // entries is not final because they can be reloaded
    private final ListModelList<ComboBoxEntry>      allIds;
    private final Map<String, Description>          lookupById;    // lookup with formatted id and description/name of DTO
    private final Map<String, Description>          lookupByKey;   // lookup with DTO id only
    private final Map<Long,   Description>          lookupByRef;
    protected IFixedFilter                          fixedFilter = null;        // a fixed search filter provider
    protected IDescriptionFilter                    descriptionFilter = null;  // a filter on results
    protected SearchFilter                          additionalFilter  = null;   // primary runtime filter (e.g., from dropdown filter field)
    protected SearchFilter                          additionalFilter2 = null;   // secondary runtime filter (e.g., from dropdown filter field)
    protected final String                          dropdownDisplayFormat;
    protected final MessageFormat                   messageFormat;

    public Dropdown28Db(IDropdown28DbFactory<REF> myFactory, String dropdownDisplayFormat) {
        super();
        this.setAutocomplete(true);
        this.setAutodrop(true);
        this.setHflex("1");
        this.setMaxlength(120);
        this.setSclass("dropdown");
        this.dropdownDisplayFormat = dropdownDisplayFormat;

        factory           = myFactory;
        entries           = session.getDropDownData(factory.getDropdownId(), factory.getSearchRequest());
        lookupByKey       = new ConcurrentHashMap<>(entries.size());
        lookupById        = new ConcurrentHashMap<>(entries.size());
        lookupByRef       = new ConcurrentHashMap<>(entries.size());
        allIds            = new ListModelList<>(entries.size());
        LOGGER.debug("Dropdown DB {} instantiated, got {} entries", factory.getDropdownId(), entries.size());
        messageFormat = new MessageFormat(getDisplayFormat());
        getDropDownData();

        addEventListener(Events.ON_CHANGE, (event) -> doChangeEvent());
        setItemRenderer(new ComboBoxEntryComboitemRenderer());
    }

    protected void doChangeEvent() {
        final String currentValue = getValue();
        if (!lookupById.containsKey(currentValue.toLowerCase())) {
            setRawValue("");  // clearing raw data is not always what we want, it kills the ability to search with LIKE criteria...
            setDropdownModel();
        }
    }

    public Dropdown28Db(IDropdown28DbFactory<REF> myFactory) {
        this(myFactory, null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Dropdown28Db(String dropdownId) {
        this((IDropdown28DbFactory) Dropdown28Registry.requireFactoryById(dropdownId));
    }

    public Dropdown28Db(String dropdownId, ComboitemRenderer<?> renderer) {
        this(dropdownId);
        setItemRenderer(renderer);
    }

    public Description lookupByKey(String id) {
        return lookupByKey.get(id.toLowerCase());
    }

    public Description lookupById(String id) {
        return lookupById.get(id.toLowerCase());
    }

    public Description lookupByRef(Long ref) {
        return lookupByRef.get(ref);
    }

    public String getLabelByRef(Long ref) {
        final Description desc = lookupByRef.get(ref);
        return desc == null ? null : getFormattedLabel(desc);
    }

    public IDropdown28DbFactory<REF> getFactory() {
        return factory;
    }

    /** Sets the fixed filter by qualifier. */
    public void setFixedFilter(String filterName) {
        fixedFilter = Jdp.getRequired(IFixedFilter.class, filterName);
        reloadDropDownData();
    }

    /** Sets the result based filter by qualifier. */
    public void setDescriptionFilter(String filterName) {
        descriptionFilter = Jdp.getRequired(IDescriptionFilter.class, filterName);
        reloadDropDownData();
    }

    /** Sets an additional runtime filter (e.g., from dropdown filter field). */
    public void setAdditionalFilter(SearchFilter filter) {
        additionalFilter = filter;
        reloadDropDownData();
    }

    /** Sets an additional runtime filter (e.g., from dropdown filter field). */
    public void setAdditionalFilter2(SearchFilter filter) {
        additionalFilter2 = filter;
        reloadDropDownData();
    }

    // query the backend, no caching (reloads are done only if caching does not apply)
    public void reloadDropDownData() {
        final LeanSearchRequest srq = factory.getSearchRequest();
        final SearchFilter adds = SearchFilters.and(additionalFilter, additionalFilter2);
        srq.setSearchFilter(fixedFilter == null ? adds : SearchFilters.and(fixedFilter.get(), adds));
        entries = session.getDropDownData(srq);  // read uncached....
        if (descriptionFilter != null) {
            entries = descriptionFilter.filter(entries);
        }
        LOGGER.debug("Reloaded dropdown DB {} (uncached), got {} entries", factory.getDropdownId(), entries.size());
        getDropDownData();
    }

    // reloads the data based on updated filter requirements - common subroutine for
    // constructor and reloadDropDownData() (must be final because called by constructor)
    protected void getDropDownData() {
        lookupById.clear();
        lookupByKey.clear();
        lookupByRef.clear();
        allIds.clear();
        for (final Description d : entries) {
            final String id = d.getId();
            final String label = getFormattedLabel(d);
            lookupByKey.put(id.toLowerCase(), d);
            lookupById.put(label.toLowerCase(), d);
            lookupByRef.put(d.getObjectRef(), d);
            final ComboBoxEntry comboBoxEntry = new ComboBoxEntry(id, label, "", null);
            allIds.add(comboBoxEntry);
        }
        allIds.sort(Comparator.comparing(ComboBoxEntry::getValue));
        setDropdownModel();
    }

    protected String getDisplayFormat() {
        String format = ZulUtils.readConfig(T9tConfigConstants.DROPDOWN_DISPLAY_FORMAT + "." + factory.getDropdownId());
        if (format != null) {
            return format;
        }
        format = ZulUtils.readConfig(T9tConfigConstants.DROPDOWN_DISPLAY_FORMAT);
        return format != null ? format : dropdownDisplayFormat != null ? dropdownDisplayFormat : DEFAULT_DISPLAY_FORMAT;
    }

    public String getFormattedLabel(Description desc) {
        if (desc.getName() == null || desc.getName().isBlank()) {
            return desc.getId(); // shortcut
        }
        final Object[] args = {desc.getId(), desc.getName()};
        return messageFormat.format(args);
    }

    private void setDropdownModel() {
        if (allIds.getSize() > MAX_ROWS) {
            setModel(ListModels.toListSubModel(allIds, STRING_COMPARATOR, MAX_ROWS));
        } else {
            setModel(new ListModelList<>(allIds));
        }
    }

    private static int getMaxItems() {
        final String configKey = "dropdown.item.max";
        final String valueByPropertyFile = UiConfigurationProvider.getProperty(configKey);
        final String configValue = valueByPropertyFile == null ? ZulUtils.readConfig(configKey) : valueByPropertyFile;
        return configValue == null ? 50 : Integer.parseInt(configValue);
    }
}
