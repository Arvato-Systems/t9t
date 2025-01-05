/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.arvatosystems.t9t.zkui.filters.IResultTextFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.FieldComparator;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.event.PagingEvent;
import org.zkoss.zul.event.ZulEvents;

import com.arvatosystems.t9t.all.request.ExportAndEmailResultRequest;
import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.output.ExportParameters;
import com.arvatosystems.t9t.base.output.FoldableMediaType;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.request.ExecuteAsyncRequest;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.search.SearchResponse;
import com.arvatosystems.t9t.base.search.SinkCreatedResponse;
import com.arvatosystems.t9t.base.uiprefs.UILeanGridPreferences;
import com.arvatosystems.t9t.zkui.IKeyFromDataProvider;
import com.arvatosystems.t9t.zkui.components.EventDataSelect28;
import com.arvatosystems.t9t.zkui.components.IGridIdOwner;
import com.arvatosystems.t9t.zkui.components.IPermissionOwner;
import com.arvatosystems.t9t.zkui.components.grid.ILeanGridConfigResolver;
import com.arvatosystems.t9t.zkui.components.grid.LeanGridConfigResolver;
import com.arvatosystems.t9t.zkui.components.grid.ListHeadRenderer28;
import com.arvatosystems.t9t.zkui.components.grid.ListItemRenderer28;
import com.arvatosystems.t9t.zkui.context.IGridContextMenu;
import com.arvatosystems.t9t.zkui.exceptions.ReturnCodeException;
import com.arvatosystems.t9t.zkui.services.IT9tMessagingDAO;
import com.arvatosystems.t9t.zkui.services.IT9tRemoteUtils;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.GridConfigUtil;
import com.arvatosystems.t9t.zkui.util.T9tConfigConstants;
import com.arvatosystems.t9t.zkui.util.ZulUtils;
import com.google.common.base.Strings;

import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.bonaparte.pojos.api.media.EnumOutputType;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;
import de.jpaw.dp.Jdp;

/** Class which creates a listbox with a configuration grid and a paging bar and export button.
 * The widget emits the select events of the listbox.
 * The expected parameter from the UI is the grid ID and an optional context list. */
public class Grid28 extends Div implements IGridIdOwner, IPermissionOwner {
    private static final long serialVersionUID = -8203405703032080582L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Grid28.class);
    private static final Set<String> TRACKING_FIELDS = GridConfigUtil.getTrackingFieldNames();
    public static final String PREFIX_GRID28 = "com.grid";
    public static final String ON_SEARCH_COMPLETED = "onSearchCompleted";

    private final ApplicationSession session = ApplicationSession.get();
    protected Permissionset permissions = Permissionset.ofTokens();

    private long                     totalSize     = 0;
    private int                      activePage    = 0;
    private int                      maxActivePage = 0;
    private boolean                  initSearch    = true;

    @Wire
    private Listbox lb;
    @Wire
    private Paging paging;
    @Wire
    private Button28 exportButton;
    @Wire
    private Textbox textFilterField;
    @Wire("#info")
    private Image infoImage;

    private Context28 contextMenu;

    private String gridId;
    private String viewModelId;
    private String gridRowCssQualifier;
    private CrudViewModel<BonaPortable, TrackingBase> crudViewModel;  // set when gridId is defined
    private ILeanGridConfigResolver    leanGridConfigResolver;        // set when gridId is defined
    private ListHeadRenderer28         defaultListHeadRenderer;
    private ListItemRenderer28<?>      defaultListItemRenderer;
    private List<String>               listHeaders = new ArrayList<>(0);  // extra headers for Lists

    private String solrFilter;                  // at least one of solrFilter and filter1 is null
    private SearchFilter filter1;               // filter as defined by Filter28
    private SearchFilter filter2;               // filter as defined by selected row of other grid
    private SearchCriteria lastSearchRequest;    // combined search request with sorting

    private String contextEntries = null;
    protected final IT9tRemoteUtils remoteUtil = Jdp.getRequired(IT9tRemoteUtils.class);
    protected final IT9tMessagingDAO messagingDAO = Jdp.getRequired(IT9tMessagingDAO.class);
    protected IKeyFromDataProvider keyFromDataProvider;
    protected boolean dynamicColumnSize;
    protected boolean searchAfterInit;
    protected boolean countTotal;
    protected boolean columnAggregationAllowed;
    protected boolean multiSelect;
    private boolean clientSidePaging = false;
    private List<DataWithTracking<BonaPortable, TrackingBase>> dataList;
    private IResultTextFilter textFilterService;

    // a parent to this is only assigned after the constructor is finished, therefore we cannot get the gridId of the outer element now
    public Grid28() {
        super();
        setVflex("1");
        boolean gridLineWrapConfig = ZulUtils.readBooleanConfig(T9tConfigConstants.GRID_LINE_WRAP);
        dynamicColumnSize      = ZulUtils.readBooleanConfig(T9tConfigConstants.GRID_DYNAMIC_COL_SIZE);
        if (gridLineWrapConfig) {
            setSclass("gridLineWrap");
        }
        LOGGER.debug("new Grid28() created");
        Executions.createComponents("/component/grid28.zul", this, null);
        Selectors.wireComponents(this, this, false);
        lb.addEventListener(Events.ON_SELECT, (SelectEvent ev) -> {
            final Set<DataWithTracking<BonaPortable, TrackingBase>> items = ev.getSelectedObjects();
            LOGGER.debug("SELECT event with {} entries", items.size());
            DataWithTracking<BonaPortable, TrackingBase> oneItem = null;
            for (DataWithTracking<BonaPortable, TrackingBase> p : items) {
                oneItem = p;
                // LOGGER.debug("    Item is {}", p);
            }
            Events.postEvent(new Event(EventDataSelect28.ON_DATA_SELECT, this,
                new EventDataSelect28(oneItem, ev.getKeys(), null))
            );
        });
    }

    protected void buildInfoTooltip(String id) {
        Popup tooltip = new Popup();
        tooltip.setParent(this);
        tooltip.setId(id);
        Label label = new Label();
        label.setPre(true);
        label.setMultiline(true);
        label.setValue(new StringBuilder("Grid ID: ").append(gridId).append("\nCurrent permissions: ").append(permissions).append("\nContext menu entry IDs: ")
                .append(contextEntries == null ? "NONE" : contextEntries).toString());

        label.setParent(tooltip);
    }

    @Listen("onCreate")
    public void onCreate() {
        LOGGER.debug("Grid28.onCreate()");
        GridIdTools.enforceGridId(this);
        initializeGrid();

        // export button only enabled if permission exists
        if (!permissions.contains(OperationType.EXPORT))
            exportButton.setDisabled(true);

        // if child of a tabpanel28, register myself for select events
        Component p = getParent();
        while (p != null) {
            if (p instanceof Tabpanel28 tabpanel) {
                LOGGER.debug("registering Grid28({}) as child of Tabpanel28({})", getId(), p.getId());
                tabpanel.setTargetGrid(this);
                if (tabpanel.getDetailMapper() != null) {
                    // set client side paging
                    lb.setMold("paging");
                    clientSidePaging = true;
                    // remove export button because it can't be used without search request.
                    exportButton.setVisible(false);
                }
                break;
            } else if (p instanceof Direct28) {
                LOGGER.debug("registering Grid28({}) as child of Direct28({})", getId(), p.getId());
                ((Direct28)p).setTargetGrid(this);
                break;
            }
            p = p.getParent();
        }

        if (searchAfterInit)
            search();
    }

    @Override
    public void setGridId(String gridId) {
        this.gridId = gridId;
        LOGGER.debug("Grid28({}) assigned grid ID {}", getId(), gridId);
        if (Strings.isNullOrEmpty(getId())) {
            // also use it for the id
            LOGGER.debug("    *** also assigning {} as id", gridId);
            setId(gridId);
        }
        permissions = session.getPermissions(gridId);
        LOGGER.debug("Grid ID {} has permissions {}", gridId, permissions);
        setViewModelId(GridIdTools.getViewModelIdByGridId(gridId));
        // provide the info tooltip
        String toolTipId = gridId + ".infoTooltip";
        buildInfoTooltip(toolTipId);
        if (infoImage != null)
            infoImage.setTooltip(toolTipId);
        else
            LOGGER.error("Cannot get infoImage");
    }

    private void initializeGrid() {
     // create the grid config resolver
        leanGridConfigResolver = new LeanGridConfigResolver(gridId, session);
        defaultListItemRenderer = new ListItemRenderer28<>(crudViewModel.dtoClass, true, gridRowCssQualifier);
        if (textFilterService != null) {
            defaultListItemRenderer.setTextFilter(textFilterService, () -> textFilterField.getValue());
        }
        defaultListHeadRenderer = new ListHeadRenderer28(defaultListItemRenderer, leanGridConfigResolver, this, lb, permissions, listHeaders,
                crudViewModel.dtoClass, dynamicColumnSize);
        lb.setItemRenderer(defaultListItemRenderer);
        lb.setEmptyMessage(ZulUtils.translate("com", "noDataFound"));

        defaultListHeadRenderer.createListhead(lb);
        defaultListHeadRenderer.redrawListbox();



        exportButton.addEventListener(Events.ON_CLICK, (MouseEvent ev) -> {
            exportData();
        });
        exportButton.setDisabled(true);  // disable until actual data has arrived

        if (isHeaderConsistList())
            exportButton.setVisible(false);

        paging.addEventListener(ZulEvents.ON_PAGING, new EventListener<PagingEvent>() {
            @Override
            public void onEvent(PagingEvent event) throws Exception {
                if (!clientSidePaging) {
                    activePage = event.getActivePage(); // set the current page
                    if (activePage > maxActivePage) {
                        maxActivePage = activePage;
                    }
                    initSearch = false;
                    search();
                    initSearch = true; // TODO: why is this done?
                }
            }
        });
    }


    private void exportData() {
        if (lastSearchRequest == null) {
            LOGGER.error("No recent search request - cannot export");
            return;
        }

        ExportParameters exportParameters = new ExportParameters();
        exportParameters.setCommunicationFormatType(FoldableMediaType.XLSX);
        exportParameters.setEnumOutputType(EnumOutputType.NAME);
        exportParameters.setLimit(100);
        exportParameters.setOffset(0);
        exportParameters.setAsynchronousByEmail(Boolean.FALSE);
        final String configInString = ZulUtils.readConfig(T9tConfigConstants.EXPORT_DEFAULT_LIMIT);
        if (configInString != null) {
            exportParameters.setLimit(Integer.parseInt(configInString));
        }


        ModalWindows.runModal(
            "/component/export28p.zul", this,
            exportParameters, false,
            out -> {
                LOGGER.debug("Limit is now {}, or {}", exportParameters.getLimit(), out.getLimit());
                LOGGER.debug("Offset is now {}, or {}", exportParameters.getOffset(), out.getOffset());
                SearchCriteria clonedSearchRequest = lastSearchRequest.ret$MutableClone(false, false);
                OutputSessionParameters osp = new OutputSessionParameters();

                osp.setDataSinkId(T9tConstants.DATA_SINK_ID_UI_EXPORT);
                osp.setGridId(gridId);
                osp.setSmartMappingForDataWithTracking(Boolean.TRUE);
                osp.setCommunicationFormatType(MediaXType.forName(out.getCommunicationFormatType().name()));
                osp.setEnumOutputType(out.getEnumOutputType());
                osp.setAsOf(Instant.now());  // required in case of asynchronous requests
                clonedSearchRequest.setSearchOutputTarget(osp);
                clonedSearchRequest.setLimit(out.getLimit());
                clonedSearchRequest.setOffset(out.getOffset());
                clonedSearchRequest.setSearchOutputTarget(osp);
                clonedSearchRequest.setSortColumns(createSortDirective());
                clonedSearchRequest.validate();
                LOGGER.debug("Sending search request {}", clonedSearchRequest);
                if (Boolean.TRUE.equals(exportParameters.getAsynchronousByEmail())) {
                    // just trigger the async export / do not expect any result, assume OK
                    final ExportAndEmailResultRequest emailRq = new ExportAndEmailResultRequest();
                    emailRq.setTargetEmailAddress(exportParameters.getTargetEmailAddress());
                    emailRq.setDocumentTemplateId(T9tConstants.DOCUMENT_ID_UI_EXPORT);
                    emailRq.setSearchRequest(clonedSearchRequest);
                    // wrap it into some async
                    final ExecuteAsyncRequest asyncRq = new ExecuteAsyncRequest();
                    asyncRq.setAsyncRequest(emailRq);
                    remoteUtil.executeExpectOk(asyncRq);
                } else {
                    // synchronous request, as before
                    try {
                        SinkCreatedResponse resp = remoteUtil.executeAndHandle(clonedSearchRequest, SinkCreatedResponse.class);
                        messagingDAO.downloadFileAndSave(resp.getSinkRef());
                    } catch (ReturnCodeException e) {
                        LOGGER.error("Return code exception", e);
                        Messagebox.show(ZulUtils.translate("err", "unableToExport"), ZulUtils.translate("err", "title"), Messagebox.OK, Messagebox.ERROR);
                    }
                }
            }
            // close the window
            //Events.postEvent(new Event(Events.ON_CLOSE, modalExportWindow, null));
        );
    }

    /** filter from Filter28 */
    public void setFilter1(String filter) {
        solrFilter = filter;
        filter1 = null;
        searchAfterInit = true;
    }

    /** filter from Filter28 */
    public void setFilter1(SearchFilter filter) {
        filter1 = filter;
        solrFilter = null;
        searchAfterInit = true;
    }

    /** filter from selected row of other grid */
    public void setFilter2(SearchFilter filter) {
        filter2 = filter;
        searchAfterInit = true;
    }

    private List<DataWithTracking<BonaPortable, TrackingBase>> readData(SearchCriteria rq) {
        SearchResponse<BonaPortable, TrackingBase, DataWithTracking<BonaPortable, TrackingBase>> resp = remoteUtil.executeExpectOk(rq, SearchResponse.class);
        calculateTotalSize(resp.getDataList().size(), resp.getNumResults());
        return resp.getDataList();
    }

    private void postSelectedEvent(DataWithTracking<BonaPortable, TrackingBase> dwt) {
        Events.postEvent(new Event(EventDataSelect28.ON_DATA_SELECT, this,
            new EventDataSelect28(dwt, -1, null))
        );
    }

    private void postSearchResultEvent() {
        Events.postEvent(new Event(ON_SEARCH_COMPLETED, this, this.totalSize));
    }

    protected List<SortColumn> createSortDirective() {
        try {
            UILeanGridPreferences gridPreferences = leanGridConfigResolver.getGridPreferences();
            String sortColumnName = computeFieldForUnrolledListSorting(gridPreferences.getSortColumn());
            boolean isDescending = T9tUtil.isTrue(gridPreferences.getSortDescending());
            if (sortColumnName != null)
                return Collections.singletonList(new SortColumn(sortColumnName, isDescending));
        } catch (IllegalArgumentException e) {
            LOGGER.debug("No enrichment of GenericSearchCriteria with default sorting");
        }
        return null;
    }

    private static String computeFieldForUnrolledListSorting(String fieldName) {
        if (fieldName == null) {
            return null;
        }
        if (fieldName.indexOf('[') < 0)
            return fieldName;  // shortcut: not index fields
        String[] tokens = fieldName.split("\\[|\\]"); // split by "[" and "]"
        StringBuilder preparedFieldName = new StringBuilder();
        for (String token : tokens) {
            if (NumberUtils.isDigits(token)) {
                token = StringUtils.leftPad(String.valueOf(Integer.parseInt(token) + 1), 2, "0"); // fill with leading 0 size two digits
            }
            preparedFieldName.append(token);
        }

        return preparedFieldName.toString();
    }

    public void search() {
        List<DataWithTracking<BonaPortable, TrackingBase>> dwt = null;
        if (dataList != null) {
            handleInternalDataModel();
            dwt = dataList;
        } else if (crudViewModel.searchClass != null) {
            final SearchCriteria rq = crudViewModel.searchClass.newInstance();
            rq.setSearchFilter(SearchFilters.and(filter1, filter2));
            rq.setExpression(solrFilter);

            if (initSearch && countTotal) {
                rq.setCountTotals(true);
            }

            // sort stuff
            rq.setSortColumns(createSortDirective());

            if (columnAggregationAllowed) {
                final UILeanGridPreferences gridPreferences = leanGridConfigResolver.getGridPreferences();
                rq.setGroupByColumns(gridPreferences.getGroupByColumns());
                rq.setAggregateColumns(gridPreferences.getAggregateColumns());
            }

            resetPaging();

            rq.setLimit(paging.getPageSize() + 1);
            rq.setOffset(this.activePage * this.paging.getPageSize());
            LOGGER.debug("Reading data...");

            // only read the limit set in pageSize
            final List<DataWithTracking<BonaPortable, TrackingBase>> dataListFromServer = readData(rq);
            if (paging.getPageSize() != 0) {
                dwt = new ArrayList<>();
                int innerLooper = 0;
                // use loop to avoid index OOB
                while (innerLooper < dataListFromServer.size() && innerLooper < paging.getPageSize()) {
                    dwt.add(dataListFromServer.get(innerLooper));
                    innerLooper += 1;
                }
            } else {
                dwt = readData(rq);
            }
            final ListModelList<DataWithTracking<BonaPortable, TrackingBase>> model = new ListModelList<DataWithTracking<BonaPortable, TrackingBase>>(dwt);
            model.setMultiple(multiSelect);
            lb.setModel(model);

            if (lb.getModel() != null && lb.getModel().getSize() > 0) {
                lb.setSelectedIndex(0);
            }

            setPagingValues();

            LOGGER.debug("Done reading data, got {} entries", dwt.size());
            lastSearchRequest = rq;
        }
        exportButton.setDisabled(T9tUtil.isEmpty(dwt) || !permissions.contains(OperationType.EXPORT));
        postSelectedEvent(T9tUtil.isEmpty(dwt) ? null : dwt.get(0));
        postSearchResultEvent();

    }

    public void setInternalDataModel(final List<DataWithTracking<BonaPortable, TrackingBase>> dwt) {
        this.dataList = dwt;
        handleInternalDataModel();
    }

    @SuppressWarnings("unchecked")
    private void handleInternalDataModel() {
        if (dataList != null) {
            final ListModelList<DataWithTracking<BonaPortable, TrackingBase>> model = new ListModelList<DataWithTracking<BonaPortable, TrackingBase>>(dataList);
            model.setMultiple(multiSelect);
            lb.setModel(model);
            if (lb.getModel() != null && lb.getModel().getSize() > 0) {
                lb.setSelectedIndex(0);
            }
            final List<SortColumn> sortColumns = createSortDirective();
            if (T9tUtil.isNotEmpty(sortColumns)) {
                // sort data model
                final SortColumn sortColumn = sortColumns.get(0); // always have one column
                final String fieldName = TRACKING_FIELDS.contains(sortColumn.getFieldName())
                    ? DataWithTracking.meta$$tracking.getName() + "." + sortColumn.getFieldName()
                    : DataWithTracking.meta$$data.getName() + "." + sortColumn.getFieldName();
                final FieldComparator fieldComparator = new FieldComparator(fieldName, !sortColumn.getDescending());
                model.sort(fieldComparator);
            }
        }
    }

    private void setPagingValues() {
        paging.setPageSize(this.paging.getPageSize());
        paging.setTotalSize((int)this.totalSize);
        paging.setActivePage(this.activePage);
    }
    private void calculateTotalSize(int pageResultSize, Long numResultsFromBE) {

        if (countTotal) {
            if (numResultsFromBE != null) {
                // no calculation required on init search
                totalSize = numResultsFromBE;
                return;
            }

            if (paging.getActivePage() == paging.getPageCount() - 1 && pageResultSize > paging.getPageSize()) {
                // last page and a new page required
                totalSize = totalSize + paging.getPageSize(); // + 1 more page
            } else if (paging.getActivePage() > 0 && pageResultSize > 0 && pageResultSize < paging.getPageSize() + 1) {
                // if no next page detected
                totalSize = paging.getPageSize() * paging.getActivePage() + pageResultSize;
            }

        } else {
            int resultSize = pageResultSize;
            if (resultSize > 0) {
                // always retrieve pageSize + 1 to find if next paging is required to fetch
                if (resultSize < paging.getPageSize() + 1) {
                    if (maxActivePage == 0) {
                        this.totalSize = resultSize; // first page
                    } else {
                        this.totalSize = (paging.getPageSize() * maxActivePage) + resultSize;  // last page
                    }
                } else { // if pageSize == resultSize
                    if ((activePage == maxActivePage) && (activePage > 0 || maxActivePage == 0)) { // if activePage == 0 jump back to first (0) page
                        this.totalSize = (paging.getPageSize() * maxActivePage) + resultSize;  // in between
                    }
                }
                //            [ 231 - 240 / 241 ]
            }
        }
    }

    private void resetPaging() {
        if (initSearch) {
            this.activePage = 0; // reset possible paging to first page
            this.maxActivePage = 0;
            this.totalSize = 0;
        }
    }

    public void setPageSize(int size) {
        paging.setPageSize(size);
    }

    public int getPageSize() {
        return paging.getPageSize();
    }

    @Override
    public String getGridId() {
        return gridId;
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
    public void setViewModelId(String viewModelId) {
        this.viewModelId = viewModelId;
        crudViewModel = GridIdTools.getViewModelByViewModelId(viewModelId);
        // also set the key for data provider
        keyFromDataProvider = Jdp.getRequired(IKeyFromDataProvider.class, viewModelId);
    }

    public void setGridContext(String entries) {
        setGridContext(entries, getGridId());
    }

    public void setGridContext(String entries, String baseId) {
        if (Strings.isNullOrEmpty(baseId)) {
            LOGGER.error("Cannot create context menu corresponding grid has no id yet - using UNKNOWN for now");
            baseId = "UNKNOWN";
        }
        boolean createContext = permissions != null && permissions.contains(OperationType.CONTEXT);
        contextEntries = entries;
        String ctxId = baseId + ".ctx";
        LOGGER.debug("Creating {}context menu for ID {}, grid ID = {}, permissions = {}",
                createContext ? "" : "NO ",
                ctxId, gridId, permissions);

        // this did not work, the permissions are not yet determined in all cases at this point, despite the id was set (probably not the grid ID)
//        if (!createContext)
//            return;

        contextMenu = new Context28();
        contextMenu.setParent(this);
        contextMenu.setId(ctxId);
        contextMenu.setContextOptions(entries);
        lb.setContext(ctxId);

        // load and register the handlers
        for (Component c : contextMenu.getChildren()) {
            if (c instanceof Menuitem) {
                final String itemId = c.getId();
                final Permissionset perms = session.getPermissions(itemId);
                IGridContextMenu handler = Jdp.getRequired(IGridContextMenu.class, itemId);
                c.addEventListener(Events.ON_CLICK, ev -> {
                    // find the correct record
                    Listitem li = lb.getSelectedItem();
                    if (li != null) {
                        LOGGER.debug("Now starting context menu onClick handler for {} / {}", ctxId, itemId);
                        DataWithTracking<BonaPortable, TrackingBase> dwt = li.getValue();
                        handler.selected(this, dwt, perms);
                    }
                });
            }
        }

        // register event listener
        contextMenu.addEventListener(Events.ON_OPEN, (OpenEvent ev) -> {
            if (ev.isOpen()) {
                LOGGER.debug("Context menu OPEN on {}", ctxId);
                // decide which are enabled and which not
                Listitem li = lb.getSelectedItem();
                if (li != null) {
                    DataWithTracking<BonaPortable, TrackingBase> dwt = li.getValue();
                    for (Component c : contextMenu.getChildren()) {
                        if (c instanceof Menuitem) {
                            String itemId = c.getId();
                            IGridContextMenu handler = Jdp.getRequired(IGridContextMenu.class, itemId);
                            final Permissionset perms = session.getPermissions(itemId);
                            ((Menuitem) c).setDisabled(!handler.isEnabled(dwt, perms));
                        }
                    }
                }
            }
        });
    }


    // called after context menu activity
    public void refreshCurrentItem() {
        // get the key for the currently selected item.
        int currentIndex = lb.getSelectedIndex();
        if (currentIndex == -1)
            return;
        ListModel x = lb.getModel();
        DataWithTracking<BonaPortable, TrackingBase> dwt = (DataWithTracking<BonaPortable, TrackingBase>) x.getElementAt(currentIndex);

        SearchCriteria srq2 = crudViewModel.searchClass.newInstance();
        srq2.setLimit(2);  // we need 1, and the second just as a plausi check
        srq2.setSearchFilter(keyFromDataProvider.getFilterForKey(dwt));
        List<DataWithTracking<BonaPortable, TrackingBase>> dwt2List = readData(srq2);
        if (dwt2List.size() != 1) {
            LOGGER.error("Expected result set of size 1 for {}, got {}", srq2, dwt2List.size());
            return;
        }

        DataWithTracking<BonaPortable, TrackingBase> dwt2 = dwt2List.get(0);
        // update current (we have the reference)
        dwt.setData(dwt2.getData());
        dwt.setTracking(dwt2.getTracking());
        // update the row
        Listitem currentItem = lb.getSelectedItem();
        if (currentItem != null)
            currentItem.setValue(dwt2);  // does this redraw?
        // trigger selected event again
        postSelectedEvent(dwt2);
        // force refresh (again)
        lb.setModel(lb.getModel());
        lb.setSelectedIndex(currentIndex);
    }

    @Override
    public Permissionset getPermissions() {
        return permissions;
    }

    @Override
    public ApplicationSession getSession() {
        return session;
    }

    public List<String> getListHeaders() {
        return listHeaders;
    }

    public void setListHeaders(List<String> listHeaders) {
        this.listHeaders = listHeaders == null ? new ArrayList<>(0) : listHeaders;
        if (defaultListHeadRenderer != null)
            defaultListHeadRenderer.setListHeaders(this.listHeaders);
    }

    public boolean isHeaderConsistList() {
        return getListHeaders() != null && !getListHeaders().isEmpty();
    }
    public boolean isItemSelected() {
       if (lb.getModel() != null && lb.getModel().getSize() > 0) {
           // Index is -1 if no item is selected
           return lb.getSelectedIndex() != -1;
       }
       return false;
    }
    public void selectNextItem() {
        // check, if operation is applicable
        int currentIndex = lb.getSelectedIndex();
        if (currentIndex == -1 || currentIndex >= lb.getItemCount() - 1) {
            return;
        }
        lb.selectItem(lb.getItemAtIndex(currentIndex + 1));
        DataWithTracking<BonaPortable, TrackingBase> dwt = (DataWithTracking<BonaPortable, TrackingBase>) lb
                .getModel().getElementAt(currentIndex + 1);
        postSelectedEvent(dwt);

        lb.renderAll();
    }

    /**
     * Override the grid setting to have dynamic column width depends on the content's length.
     * Enable this will disable the width setting defined in the grid configuration.
     *
     * To apply globally, set grid.dynamicColumnSize at the configuration properties file.
     *
     * @param dynamicColumnSize
     */
    public void setDynamicColumnSize(boolean dynamicColumnSize) {
        this.dynamicColumnSize = dynamicColumnSize;
    }

    /**
     * setting autoblur on all buttons in Filter28, setting false to disable the feature,
     * this is only required if this components are embedded in the popup based components
     **/
    public void setAutoblurOnButtons(final boolean autoblur) {
        exportButton.setAutoblur(autoblur);
    }

    public void setCountTotal(boolean countTotal) {
        this.countTotal = countTotal;
    }

    public void setMultiSelect(final boolean multiSelect) {
        this.multiSelect = multiSelect;
    }

    public void setCheckmark(final boolean checkmark) {
        lb.setCheckmark(checkmark);
    }

    public void setGridRowCssQualifier(final String gridRowCssQualifier) {
        this.gridRowCssQualifier = gridRowCssQualifier;
    }

    public void setTextFilterQualifier(final String qualifier) {
        textFilterService = Jdp.getRequired(IResultTextFilter.class, qualifier);
        textFilterField.setVisible(textFilterService != null);
    }

    public void clearTextFilterField() {
        textFilterField.setValue(null);
    }

    public void setColumnAggregationAllowed(final boolean columnAggregationAllowed) {
        this.columnAggregationAllowed = columnAggregationAllowed;
    }

    public boolean isColumnAggregationAllowed() {
        return columnAggregationAllowed;
    }
}
