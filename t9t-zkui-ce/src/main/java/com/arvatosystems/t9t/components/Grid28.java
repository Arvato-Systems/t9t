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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Popup;
import org.zkoss.zul.event.PagingEvent;
import org.zkoss.zul.event.ZulEvents;

import com.arvatosystems.t9t.tfi.component.ComponentUtil;
import com.arvatosystems.t9t.tfi.services.ReturnCodeException;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.tfi.web.ZulUtils;
import com.arvatosystems.t9t.base.BooleanUtil;
import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.output.ExportParameters;
import com.arvatosystems.t9t.base.output.FoldableMediaType;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.search.SearchResponse;
import com.arvatosystems.t9t.base.search.SinkCreatedResponse;
import com.arvatosystems.t9t.base.uiprefs.UILeanGridPreferences;
import com.arvatosystems.t9t.component.ext.EventDataSelect28;
import com.arvatosystems.t9t.component.ext.IGridIdOwner;
import com.arvatosystems.t9t.component.ext.IPermissionOwner;
import com.arvatosystems.t9t.components.grid.IKeyFromDataProvider;
import com.arvatosystems.t9t.components.tools.ILeanGridConfigResolver;
import com.arvatosystems.t9t.components.tools.LeanGridConfigResolver;
import com.arvatosystems.t9t.components.tools.ListHeadRenderer28;
import com.arvatosystems.t9t.components.tools.ListItemRenderer28;
import com.arvatosystems.t9t.context.IGridContextMenu;
import com.arvatosystems.t9t.services.IT9TMessagingDAO;
import com.arvatosystems.t9t.services.T9TRemoteUtils;
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
    public static final String PREFIX_GRID28 = "com.grid";

    private final ApplicationSession session = ApplicationSession.get();
    protected Permissionset permissions = Permissionset.ofTokens();

    long                     totalSize     = 0;
    int                      activePage    = 0;
    int                      maxActivePage = 0;
    boolean initSearch = true;

    @Wire
    private Listbox lb;
    @Wire
    private Paging paging;
    @Wire
    private Button exportButton;
    @Wire("#info")
    private Image infoImage;

    private Context28 contextMenu;

    private String gridId;
    private String viewModelId;
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

    protected final T9TRemoteUtils remoteUtil = Jdp.getRequired(T9TRemoteUtils.class);
    protected final IT9TMessagingDAO messagingDAO = Jdp.getRequired(IT9TMessagingDAO.class);
    protected IKeyFromDataProvider keyFromDataProvider;

/*
    public void setNumRows(int numRows) {
        lb.setHeight(String.format("%dpx", LINE_HEIGHT * numRows + HEADER_HEIGHT));
    }*/

    // a parent to this is only assigned after the constructor is finished, therefore we cannot get the gridId of the outer element now
    public Grid28() {
        super();
        setVflex("1");
        LOGGER.debug("new Grid28() created");
        Executions.createComponents("/component/grid28.zul", this, null);
        Selectors.wireComponents(this, this, false);
        lb.addEventListener(Events.ON_SELECT, (SelectEvent ev) -> {
            final Set<DataWithTracking<BonaPortable,TrackingBase>> items = ev.getSelectedObjects();
            LOGGER.debug("SELECT event with {} entries", items.size());
            DataWithTracking<BonaPortable,TrackingBase> oneItem = null;
            for (DataWithTracking<BonaPortable,TrackingBase> p : items) {
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
        label.setValue(String.format("Grid ID: %s\nCurrent permissions: %s\nContext menu entry IDs: %s",
                gridId, permissions, contextEntries == null ? "NONE" : contextEntries));
        label.setParent(tooltip);
    }

    @Listen("onCreate")
    public void onCreate() {
        LOGGER.debug("Grid28.onCreate()");
        GridIdTools.enforceGridId(this);

        // provide the info tooltip
        String toolTipId = gridId + ".infoTooltip";
        buildInfoTooltip(toolTipId);
        if (infoImage != null)
            infoImage.setTooltip(toolTipId);
        else
            LOGGER.error("Cannot get infoImage");

        // export button only enabled if permission exists
        if (!permissions.contains(OperationType.EXPORT))
            exportButton.setDisabled(true);

        // if child of a tabpanel28, register myself for select events
        Component p = getParent();
        while (p != null) {
            if (p instanceof Tabpanel28) {
                LOGGER.debug("registering Grid28({}) as child of Tabpanel28({})", getId(), p.getId());
                ((Tabpanel28)p).setTargetGrid(this);
                break;
            } else if (p instanceof Direct28) {
                LOGGER.debug("registering Grid28({}) as child of Direct28({})", getId(), p.getId());
                ((Direct28)p).setTargetGrid(this);
                break;
            }
            p = p.getParent();
        }
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

        // create the grid config resolver
        leanGridConfigResolver = new LeanGridConfigResolver(gridId, session);
        defaultListItemRenderer = new ListItemRenderer28<>(crudViewModel.dtoClass, true);
        defaultListHeadRenderer = new ListHeadRenderer28(defaultListItemRenderer, leanGridConfigResolver, this, lb, permissions, listHeaders, crudViewModel.dtoClass);
        lb.setItemRenderer(defaultListItemRenderer);
        lb.setEmptyMessage(ZulUtils.translate("com","noDataFound"));

        defaultListHeadRenderer.createListhead(lb);
        defaultListHeadRenderer.redrawListbox();



        exportButton.addEventListener(Events.ON_CLICK, (MouseEvent ev) -> { exportData(); });
        exportButton.setDisabled(true);  // disable until actual data has arrived

        if (isHeaderConsistList())
            exportButton.setVisible(false);

        paging.addEventListener(ZulEvents.ON_PAGING, new EventListener<PagingEvent>() {
            @Override
            public void onEvent(PagingEvent event) throws Exception {
               activePage = event.getActivePage();  // set the current page
               if (activePage > maxActivePage) {
                   maxActivePage = activePage;
               }
               initSearch=false;
               search();
               initSearch=true;
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

        Component modalExportWindow = ModalWindows.runModal(
            "/component/export28p.zul", this,
            exportParameters, false,
            out -> {
                LOGGER.debug("Limit is now {}, or {}", exportParameters.getLimit(), out.getLimit());
                LOGGER.debug("Offset is now {}, or {}", exportParameters.getOffset(), out.getOffset());
                SearchCriteria clonedSearchRequest = lastSearchRequest.ret$MutableClone(false, false);
                OutputSessionParameters osp = new OutputSessionParameters();

                osp.setDataSinkId("UIExport");
                osp.setGridId(gridId);
                osp.setSmartMappingForDataWithTracking(Boolean.TRUE);
                osp.setCommunicationFormatType(MediaXType.forName(out.getCommunicationFormatType().name()));
                osp.setEnumOutputType(out.getEnumOutputType());
                clonedSearchRequest.setSearchOutputTarget(osp);
                clonedSearchRequest.setLimit(out.getLimit());
                clonedSearchRequest.setOffset(out.getOffset());
                clonedSearchRequest.setSearchOutputTarget(osp);
                clonedSearchRequest.setSortColumns(createSortDirective());
                clonedSearchRequest.validate();
                LOGGER.debug("Sending search request {}", clonedSearchRequest);
                try {
                    SinkCreatedResponse resp = remoteUtil.executeAndHandle(clonedSearchRequest, SinkCreatedResponse.class);
                    messagingDAO.downloadFileAndSave(resp.getSinkRef());
                } catch (ReturnCodeException e) {
                    LOGGER.error("Return code exception", e);
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
        search();
    }

    /** filter from Filter28 */
    public void setFilter1(SearchFilter filter) {
        filter1 = filter;
        solrFilter = null;
        search();
    }

    /** filter from selected row of other grid */
    public void setFilter2(SearchFilter filter) {
        filter2 = filter;
        search();
    }

    private List<DataWithTracking<BonaPortable, TrackingBase>> readData(SearchCriteria rq) {
        SearchResponse<BonaPortable, TrackingBase, DataWithTracking<BonaPortable, TrackingBase>> resp = remoteUtil.executeExpectOk(rq, SearchResponse.class);
        return resp.getDataList();
    }

    private void postSelectedEvent(DataWithTracking<BonaPortable, TrackingBase> dwt) {
        Events.postEvent(new Event(EventDataSelect28.ON_DATA_SELECT, this,
            new EventDataSelect28(dwt, -1, null))
        );
    }

    protected List<SortColumn> createSortDirective() {
        try {
            UILeanGridPreferences gridPreferences = leanGridConfigResolver.getGridPreferences();
            String sortColumnName = ComponentUtil.computeFieldForUnrolledListSorting(gridPreferences.getSortColumn());
            boolean isDescending = BooleanUtil.isTrue(gridPreferences.getSortDescending());
            if (sortColumnName != null)
                return Collections.singletonList(new SortColumn(sortColumnName, isDescending));
        } catch (IllegalArgumentException e) {
            LOGGER.debug("No enrichment of GenericSearchCriteria with default sorting");
        }
        return null;
    }

    public void search() {
        SearchCriteria rq = crudViewModel.searchClass.newInstance();
        rq.setSearchFilter(SearchFilters.and(filter1, filter2));
        rq.setExpression(solrFilter);

        // sort stuff
        rq.setSortColumns(createSortDirective());

        resetPaging();

        rq.setLimit(paging.getPageSize()+1);
        rq.setOffset(this.activePage * this.paging.getPageSize());
        LOGGER.debug("Reading data...");

        // only read the limit set in pageSize
        List<DataWithTracking<BonaPortable, TrackingBase>> dataListFromServer = readData(rq);
        List<DataWithTracking<BonaPortable, TrackingBase>> dwt = null;
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
        lb.setModel(new ListModelList<DataWithTracking<BonaPortable, TrackingBase>>(dwt));


        if (lb.getModel() != null && lb.getModel().getSize() > 0) {
            lb.setSelectedIndex(0);
        }


        calculateTotalSize(dataListFromServer.size());
        setPagingValues();


        LOGGER.debug("Done reading data, got {} entries", dwt.size());
        lastSearchRequest = rq;
        exportButton.setDisabled(dwt.isEmpty() || !permissions.contains(OperationType.EXPORT));
        postSelectedEvent(dwt.isEmpty() ? null : dwt.get(0));

    }

    private void setPagingValues() {
        paging.setPageSize(this.paging.getPageSize());
        paging.setTotalSize((int)this.totalSize);
        paging.setActivePage(this.activePage);
    }
    private void calculateTotalSize(int serverResultCount) {
        int resultSize = serverResultCount;
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
        final Map<String, IGridContextMenu> actions = new ConcurrentHashMap<String, IGridContextMenu>();
        final Map<String, Component> menuItems = new ConcurrentHashMap<String, Component>();

        for (Component c : contextMenu.getChildren()) {
            if (c instanceof Menuitem) {
                final String itemId = c.getId();
                final Permissionset perms = session.getPermissions(itemId);
                IGridContextMenu handler = Jdp.getRequired(IGridContextMenu.class, itemId);
                actions.put(itemId, handler);
                menuItems.put(itemId, c);
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
       return getListHeaders()!=null && !getListHeaders().isEmpty();
    }
    public boolean isItemSelected() {
       if (lb.getModel() != null && lb.getModel().getSize() > 0) {
           // Index is -1 if no item is selected
           return lb.getSelectedIndex() != -1;
       } else {
           return false;
       }
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
}
