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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.BindUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.A;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;

import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.ui.UIFilter;
import de.jpaw.bonaparte.util.ToStringHelper;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.FieldMappers;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.zkui.components.IGridIdOwner;
import com.arvatosystems.t9t.zkui.components.fields.FieldFactory;
import com.arvatosystems.t9t.zkui.components.fields.IField;
import com.arvatosystems.t9t.zkui.components.grid.ILeanGridConfigResolver;
import com.arvatosystems.t9t.zkui.components.grid.LeanGridConfigResolver;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.Constants;

/** Creates a grid with filters based on a grid definition.
 * Emits a search event when the search button has been pushed.
 * The event data is populated with the correct search request. */
public class Filter28 extends Grid implements IGridIdOwner {
    private static final Logger LOGGER = LoggerFactory.getLogger(Filter28.class);
    private static final long serialVersionUID = 234233457567L;

    private final ApplicationSession session = ApplicationSession.get();
    private Button28 resetButton;
    private Button28 searchButton;
    private A toggleButton;
    private final List<IField> filters = new ArrayList<>(15);

    private final int columnLength = 1;
    private String gridId;
    private String viewModelId;
    private ILeanGridConfigResolver leanGridConfigResolver;
    private CrudViewModel<BonaPortable, TrackingBase> crudViewModel;  // set when gridId is defined
    private boolean autoblurOnButtons = true;

    /**
     * Guard to avoid stealing focus repeatedly when this component is cached and shown multiple times.
     */
    private transient boolean pendingInitialFocusOnShow = false;

    public Filter28() {
        super();
        LOGGER.debug("new Filter28() created");
        setVflex("1");
        setSclass("filtergrid grid no-padding");
    }

    @Override
    public void setGridId(final String gridId) {
        LOGGER.debug("Filter28() assigned grid ID {}", gridId);
        this.gridId = gridId;
        setViewModelId(GridIdTools.getViewModelIdByGridId(gridId));
        addColumns();
        populateFilters(0); //use default during init
        resetButton.setAutoblur(autoblurOnButtons);
        searchButton.setAutoblur(autoblurOnButtons);
    }

    public void resetSearchFilters(final int gridPrefVariant) {
        super.removeChild(super.getRows());
        populateFilters(gridPrefVariant);
    }

    private void populateFilters(final int gridPrefVariant) {
        leanGridConfigResolver = new LeanGridConfigResolver(gridId, session, gridPrefVariant);
        addRows();
        resetButton.addEventListener(Events.ON_CLICK, (final MouseEvent ev) -> {
            performReset();
        });
        searchButton.addEventListener(Events.ON_CLICK, (final MouseEvent ev) -> {
            performSearch("onClick");
        });
        this.addEventListener(Events.ON_OK, (final KeyEvent ev) -> {
            performSearch("onOk");
        });
        if (toggleButton != null) {
            toggleButton.setSclass("toggleButton");
            toggleButton.setLabel(session.translate(Button28.PREFIX_BUTTON28, toggleButton.getId()));
            toggleButton.addEventListener(Events.ON_CLICK, (final MouseEvent ev) -> {
                Events.postEvent("onToSOLR", this, null);
            });
        }
        registerFields();

        // Apply global default focus: first filter with filterType 'W'
        // Post it so the components are attached and focusable.
        pendingInitialFocusOnShow = false;
        Events.postEvent(new Event("onSetInitialFocus", this, null));
    }
    // the purpose of the next method is unclear....
    void registerFields() {
        final Map<String, Object> args = new HashMap<>();
        args.put("filters", filters);
        BindUtils.postGlobalCommand(null, null, "registerFields", args);
    }

    private void performSearch(final String cause) {
        SearchFilter current = null;
        for (final IField f : filters) {
            final SearchFilter sf = f.getSearchFilter();
            if (sf != null) {
                current = SearchFilters.and(current, f.isNegated() ? SearchFilters.not(sf) : sf);
            }
        }
        LOGGER.debug("Filter triggered by {}, running {}", cause, ToStringHelper.toStringML(current));

        Events.postEvent("onSearch", this, current);
    }

    private void performReset() {
        for (final IField<?> e : filters) {
            e.clear();
        }
        Events.postEvent("onResetFilters", this, null);
    }


    private void addColumns() {
        final Columns cols = new Columns();
        for (int count = 0; count < columnLength; count++) {
            final Column col = new Column();
            col.setLabel("");
            col.setHflex("1");
            cols.appendChild(col);
        }
        super.appendChild(cols);
    }

    private void addRows() {
        filters.clear();
        final Rows rows = new Rows();

        // compose each component
        Row eachRow = new Row();
        final int index = 0;
        // calculate the cols span
        final int colsSpan = columnLength - (index % columnLength);

        // add buttons
        final Cell firstCell = new Cell();
        firstCell.setColspan(colsSpan);
        firstCell.setAlign("center");

        // add search button
        searchButton = new Button28();
        searchButton.setId("searchButton");
        //searchButton.setAutodisable("searchButton,newButton,saveButton,deleteButton,resetButton");
        firstCell.appendChild(searchButton);

        // add reset button
        resetButton = new Button28();
        resetButton.setId("resetButton");
        //resetButton.setAutodisable("searchButton,newButton,saveButton,deleteButton,resetButton");
        firstCell.appendChild(resetButton);

        // append row into rows
        eachRow.appendChild(firstCell);

        final FieldFactory factory = new FieldFactory(crudViewModel, gridId, session);

        rows.appendChild(eachRow);

        // determine default focus target while building filters
        Component focusCandidate = null;
        Component focusFallback = null;

        for (final UIFilter filter : leanGridConfigResolver.getFilters()) {
            final String fieldname = filter.getFieldName();
            final FieldDefinition fieldDef = FieldMappers.getFieldDefinitionForPath(fieldname, crudViewModel);
            final IField field = factory.createField(fieldname, filter, fieldDef);

            if (field != null) {
                filters.add(field);
                final List<Component> components = field.getComponents();
                // in case of range filters, there are two components (e.g. from - to), so we need to check both for focusability and wrap them in the same cell
                for (int i = 0; i < components.size(); i++) {
                    final Component c = components.get(i);
                    final Component focusable = resolveFocusableComponent(c);
                    // Prefer first non-dropdown / non-bandbox field; keep a fallback to the first focusable field.
                    if (focusFallback == null && focusable != null) {
                        focusFallback = focusable;
                    }
                    if (focusCandidate == null && focusable != null && !isDropdownOrBandbox(fieldDef)) {
                        focusCandidate = focusable;
                    }

                    eachRow = new Row();
                    final Cell eachCell = new Cell();
                    eachCell.appendChild(c);
                    eachRow.appendChild(eachCell);
                    eachCell.setSclass("filterCell");
                    if (T9tUtil.isTrue(filter.getNegate())) {
                        // check if the filter should be visually highlighted to be a negation
                        final String negateBegin = session.translate("filter.negate", "begin");
                        if (!T9tUtil.isBlank(negateBegin)) {
                            if (components.size() == 1) {
                                eachCell.insertBefore(new Label(negateBegin + " "), c);
                            } else if (i == 0) {
                                eachCell.insertBefore(new Label(negateBegin + " ( "), c);
                            } else if (i == components.size() - 1) {
                                eachCell.appendChild(new Label(" )"));
                            }
                        }
                    }
                    // append and create new row when it has reached the column length
                    rows.appendChild(eachRow);
                    eachRow.setVflex("min");
                }
            }
        }

        if (crudViewModel.searchClass.getProperty("isSolr") != null) {
            // is SOLR
            toggleButton = new A();
            toggleButton.setId("toggleButton");
            //resetButton.setAutodisable("searchButton,newButton,saveButton,deleteButton,resetButton");
            firstCell.appendChild(toggleButton);
        } else {
            toggleButton = null;
        }

        final Div div = new Div();
        div.setStyle("height:20px;display:block;");
        firstCell.appendChild(div);

        rows.appendChild(eachRow);

        super.appendChild(rows);

        // store candidate on component for the posted event handler
        final Component initialFocus = focusCandidate != null ? focusCandidate : focusFallback;
        if (initialFocus != null) {
            setAttribute("_initialFocus", initialFocus);
        } else {
            removeAttribute("_initialFocus");
        }
    }

    private boolean isDropdownOrBandbox(final FieldDefinition fieldDef) {
        if (fieldDef == null) {
            return false;
        }
        if ("enum".equals(fieldDef.getBonaparteType())) {
            return true;
        }
        if (fieldDef.getProperties() == null) {
            return false;
        }
        final Map<String, String> props = fieldDef.getProperties();
        return props.containsKey(Constants.UiFieldProperties.DROPDOWN)
            || props.containsKey(Constants.UiFieldProperties.MULTI_DROPDOWN)
            || props.containsKey(Constants.UiFieldProperties.BANDBOX);
    }

    /**
     * Try to find a component which can receive focus (some fields are wrapped in a div etc.).
     */
    private Component resolveFocusableComponent(final Component c) {
        if (c == null) {
            return null;
        }
        // Most input-like zul components implement org.zkoss.zul.impl.InputElement
        if (c instanceof org.zkoss.zul.impl.InputElement) {
            return c;
        }
        // Some complex fields wrap the input; pick the first input element child.
        for (final Component child : c.getChildren()) {
            final Component found = resolveFocusableComponent(child);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    @Listen("onSetInitialFocus")
    public void onSetInitialFocus() {
        final Object target = getAttribute("_initialFocus");
        if (target instanceof final org.zkoss.zul.impl.InputElement ie) {
            try {
                ie.focus();
            } catch (final Exception e) {
                LOGGER.debug("Could not set initial focus for grid {}", gridId, e);
            }
        }
    }

    @Listen("onInitModel")
    public void onInitModel() {
        // guard: gridId / crudViewModel must exist
        if (gridId == null || crudViewModel == null) {
            return;
        }

        // do whatever the event should initialize
        // examples:
        // - repopulate filters based on current pref variant
        // - re-register fields for databinding
        // - set focus
        if (!pendingInitialFocusOnShow) {
            pendingInitialFocusOnShow = true;
            Events.postEvent(new Event("onSetInitialFocus", this, null));
        }
    }

    @Override
    public String getGridId() {
        return gridId;
    }

    @Listen("onCreate")
    public void onCreate() {
        LOGGER.debug("Filter28.onCreate()");
        GridIdTools.enforceGridId(this);
    }

    @Override
    public CrudViewModel<BonaPortable, TrackingBase> getCrudViewModel() {
        GridIdTools.enforceGridId(this);
        return crudViewModel;
    }

    @Override
    public String getViewModelId() {
        return viewModelId;
    }

    @Override
    public void setViewModelId(final String viewModelId) {
        this.viewModelId = viewModelId;
        crudViewModel = GridIdTools.getViewModelByViewModelId(viewModelId);
    }

    @Override
    public ApplicationSession getSession() {
        return session;
    }

    /**
     * setting autoblur on all buttons in Filter28, setting false to disable the feature,
     * this is only required if this components are embedded in the popup based components
     **/
    public void setAutoblurOnButtons(final boolean autoblur) {
        autoblurOnButtons = autoblur;
    }
}
