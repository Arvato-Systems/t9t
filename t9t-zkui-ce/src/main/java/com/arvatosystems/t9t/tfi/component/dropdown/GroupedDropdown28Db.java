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
package com.arvatosystems.t9t.tfi.component.dropdown;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;

import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.base.search.DescriptionList;
import com.arvatosystems.t9t.base.search.LeanGroupedSearchRequest;
import com.arvatosystems.t9t.base.search.LeanSearchRequest;
import com.arvatosystems.t9t.component.fields.fixedfilters.IDescriptionFilter;
import com.arvatosystems.t9t.component.fields.fixedfilters.IFixedFilter;
import com.arvatosystems.t9t.tfi.component.SimpleListModelExt;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.dp.Jdp;

public class GroupedDropdown28Db<REF extends Ref> extends Combobox {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupedDropdown28Db.class);

    // instance fields and methods

    private final ApplicationSession                session = ApplicationSession.get();
    private final IGroupedDropdown28DbFactory<REF>  factory;
    private DescriptionList                         entries;      // entries is not final because they can be reloaded
    private final List<String>                      allIds;
    private final Map<String, Description>          lookupById;   // this is a case insensitive lookup
    private final Map<Long,   Description>          lookupByRef;
    protected IFixedFilter                          fixedFilter = null;        // a fixed search filter provider
    protected IDescriptionFilter                    descriptionFilter = null;  // a filter on results
    protected Long                                  group;        //key of the entries


    public GroupedDropdown28Db(IGroupedDropdown28DbFactory<REF> myFactory) {
        super();
        this.setAutocomplete(true);
        this.setAutodrop(true);
        this.setHflex("1");
        this.setMaxlength(36);  // role has 32 chars length, defaultExternalId 36 (= max)
        this.setSclass("dropdown");

        factory = myFactory;
        entries = session.getGroupedDropdownData(factory.getDropdownId(), factory.getGroup(), factory.getSearchRequest(), false);
        lookupById        = new ConcurrentHashMap<String, Description>(entries.getDescriptions().size() * 2);
        lookupByRef       = new ConcurrentHashMap<Long, Description>(entries.getDescriptions().size() * 2);
        allIds            = new ArrayList<String>(entries.getDescriptions().size());
        LOGGER.debug("Dropdown DB {} instantiated, got {} entries", factory.getDropdownId(), entries.getDescriptions().size());
        getDropDownData();

        this.addEventListener(Events.ON_CHANGE, (event) -> doChangeEvent());
    }

    protected void doChangeEvent() {
        if (!allIds.contains(getValue())) {
            setRawValue("");  // clearing raw data is not always what we want, it kills the ability to search with LIKE criteria...
            setModel(new SimpleListModelExt<String>(allIds));
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public GroupedDropdown28Db(String dropdownId) {
        this( (IGroupedDropdown28DbFactory) Dropdown28Registry.requireFactoryById(dropdownId));
    }

    public Description lookupById(String id) {
        return lookupById.get(id.toLowerCase());
    }
    public Description lookupByRef(Long ref) {
        return lookupByRef.get(ref);
    }
    public IGroupedDropdown28DbFactory<REF> getFactory() {
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

 // query the backend, no caching (reloads are done only if caching does not apply)
    public void reloadDropDownData() {
        LeanGroupedSearchRequest srq = factory.getSearchRequest();
        if (fixedFilter != null)
            srq.setSearchFilter(fixedFilter.get());
        entries = session.getGroupedDropdownData(factory.getDropdownId(), group, factory.getSearchRequest(), false);  // read uncached....
        if (descriptionFilter != null)
            entries = new DescriptionList(descriptionFilter.filter(entries.getDescriptions()));
        LOGGER.debug("Reloaded grouped dropdown DB {} group {} (uncached), got {} entries", factory.getDropdownId(), group, entries.getDescriptions().size());
        getDropDownData();
    }

    // reloads the data based on updated filter requirements - common subroutine for constructor and reloadDropDownData() (must be final because called by constructor)
    private final void getDropDownData() {
        lookupById.clear();
        lookupByRef.clear();
        allIds.clear();
        for (Description d : entries.getDescriptions()) {
            String keyInLowercase = d.getId().toLowerCase();
            lookupById.put(keyInLowercase, d);
            lookupByRef.put(d.getObjectRef(), d);
            allIds.add(d.getId());
        }
        setModel(new SimpleListModelExt<String>(allIds));
    }

    public Long getGroup() {
        return group;
    }

    public void setGroup(Long group) {
        this.group = group;
    }

}
