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
package com.arvatosystems.t9t.zkui.viewmodel.support;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.arvatosystems.t9t.zkui.services.IAuthenticationService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.image.AImage;
import org.zkoss.lang.Generics;
import org.zkoss.xel.fn.CommonFns;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Div;
import org.zkoss.zul.GroupComparator;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Panelchildren;
import org.zkoss.zul.Style;
import org.zkoss.zul.Window;

import com.arvatosystems.t9t.auth.request.GetDefaultScreenRequest;
import com.arvatosystems.t9t.auth.request.GetDefaultScreenResponse;
import com.arvatosystems.t9t.authc.api.TenantDescription;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.request.QueryConfigRequest;
import com.arvatosystems.t9t.base.request.QueryConfigResponse;
import com.arvatosystems.t9t.zkui.components.fields.IField;
import com.arvatosystems.t9t.zkui.exceptions.ServiceResponseException;
import com.arvatosystems.t9t.zkui.services.IApplicationDAO;
import com.arvatosystems.t9t.zkui.services.INavBarCreator;
import com.arvatosystems.t9t.zkui.services.IT9tRemoteUtils;
import com.arvatosystems.t9t.zkui.services.ITitleBarSearch;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.ApplicationUtil;
import com.arvatosystems.t9t.zkui.util.Constants;
import com.arvatosystems.t9t.zkui.util.Constants.NaviConfig;
import com.arvatosystems.t9t.zkui.util.JumpTool;
import com.arvatosystems.t9t.zkui.util.ZulUtils;
import com.arvatosystems.t9t.zkui.viewmodel.beans.Navi;
import com.google.common.collect.ImmutableMap;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.ObjectReference;
import de.jpaw.dp.Jdp;
/**
 * index View Model build the whole application.
 *
 * @author INCI02
 *
 */
public class ApplicationViewModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationViewModel.class);

    private String userInfo;
    private NaviGroupingViewModel naviGroupingViewModel = null;
    private Object selected;
    private Map<String, Panelchildren> naviContentMap = new HashMap<String, Panelchildren>();
    private boolean previouslyCachingTypeWasCreateWithoutCaching = false;
    private String previousNaviKey;

    private String whenLastLoggedIn;
    private Long pwdExpiresInDays = null;
    private Integer numberOfIncorrectAttempts;
    private String selectedTenantId;
    private final ApplicationSession as = ApplicationSession.get();
    private final IApplicationDAO applicationDAO = Jdp.getRequired(IApplicationDAO.class);
    private final INavBarCreator navbarCreator = Jdp.getRequired(INavBarCreator.class);
    private final ITitleBarSearch titleSearch = Jdp.getRequired(ITitleBarSearch.class);
    private final IT9tRemoteUtils t9tRemoteUtils = Jdp.getRequired(IT9tRemoteUtils.class);
    private final IAuthenticationService authenticationService = Jdp.getRequired(IAuthenticationService.class);

    private String userName;
    private String userId;
    @SuppressWarnings("rawtypes")
    private List<IField> filters;
    private List<HtmlBasedComponent> htmlBasedFieldComponents;
    private boolean jumpBackVisible = false;
    private static final int MAX_NUMBER_SUBMENU_ITEMS_PER_COLUMN = 13;
    private static final long MILLISECONDS_PER_DAY = 24L * 60L * 60L * 1000L;

    @Wire("#navbarContainer") private Component navbar;
    @Wire("#mainHome") private Window mainHome;
    @Wire("#reverse")  private Style  reverse;
    @Wire("#panel")    private Panel  panel;
    @Wire("#environmentIdentifier") private Div environmentIdentifier;
    private static String ctrlKeys;

    private static class NaviComparator implements Comparator<Navi>, GroupComparator<Navi>, Serializable {
        private static final long serialVersionUID = -5442923541968897269L;

        @Override
        public final int compare(Navi o1, Navi o2) {
            return o1.getPrefixCategoryId().compareTo(o2.getPrefixCategoryId().toString());
        }

        @Override
        public final int compareGroup(Navi o1, Navi o2) {
            if (o1.getPrefixCategoryId().equals(o2.getPrefixCategoryId())) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    private static class CtrlKeyHandler {
        private static final Logger LOGGER = LoggerFactory.getLogger(CtrlKeyHandler.class);

        public void ctrlKeyClick(KeyEvent keyEvent, List<HtmlBasedComponent> htmlBasedComponents) {

            String keyCodeString = ApplicationUtil.getKeyCodeFromKeyEvent(keyEvent);
            LOGGER.debug("keyCodeString: " + keyCodeString);

            if (null != htmlBasedComponents && !htmlBasedComponents.isEmpty()) {
                if (keyCodeString.equals("C+E")) {
                    htmlBasedComponents.get(0).setFocus(true);
                }
                for (int i = 0; i < htmlBasedComponents.size(); i++) {
                    if (keyCodeString.equals("A+" + i) && htmlBasedComponents.size() >= (i + 2)) {
                        htmlBasedComponents.get(i + 1).setFocus(true);
                    }
                    if (keyCodeString.equals("C+M")) {
                        if (i < 11) {
                            if (i == 0) {
                                Clients.showNotification("C+E", null, htmlBasedComponents.get(i), "top_center", 2000);
                            } else {
                                Clients.showNotification("A+" + (i - 1), null, htmlBasedComponents.get(i), "top_center", 2000);
                            }
                        }
                    }
                }
            }
        }
    }

    public ApplicationViewModel() {

        if (as.getTenantId() == null) {
            Executions.sendRedirect(Constants.ZulFiles.LOGOUT);
        } else {
            selectedTenantId = as.getTenantId();
            final ApplicationSession applicationSession = ApplicationSession.get();

            if (!applicationSession.isAuthenticated()) {
                Executions.getCurrent().sendRedirect(Constants.ZulFiles.LOGIN);
            }

            final String currentUserId = applicationSession.getJwtInfo().getUserId();
            setUserInfo(currentUserId);

            userId = as.getUserId();
            userName = as.getJwtInfo().getName();
            if (userName == null)
                userName = "?";

            numberOfIncorrectAttempts = as.getNumberOfIncorrectAttempts();
            if (numberOfIncorrectAttempts == null)
                numberOfIncorrectAttempts = Integer.valueOf(0);
            final Instant lastLoggedIn = as.getLastLoggedIn();
            if (lastLoggedIn != null) {
                whenLastLoggedIn = CommonFns.formatDate(Date.from(lastLoggedIn), ZulUtils.readConfig("com.datetime.format"));
            }
            final Instant passwordExpires = as.getPasswordExpires();
            if (passwordExpires != null) {
                pwdExpiresInDays = (passwordExpires.toEpochMilli() - System.currentTimeMillis()) / MILLISECONDS_PER_DAY;
                if (pwdExpiresInDays < 0L)
                    pwdExpiresInDays = 0L;
            }

            LOGGER.info("New ApplicationViewModel created for user {}, now reading menu...", userId);
            as.readMenu();

            //Reset all screens in hash for each new reloading the menus
            /*FT-808*/  naviContentMap = new HashMap<String, Panelchildren>();
            //          paramMap = new HashMap<String, Object>();
            ctrlKeys = ZulUtils.readConfig("keys.ctrlKeys.ctrlKeys");
        }
    }

    @Command
    public void changeTenant() {
        List<TenantDescription> allowedTenants = as.getAllowedTenants();

        if (allowedTenants.size() > 1) {
            Map<String, Object> args = new HashMap<>();
            args.put("isCancelClose", true);
            args.put("isPopup", true);
            final Window win = (Window) Executions.createComponents(Constants.ZulFiles.LOGIN_TENANT_SELECTION, null, args);
            win.setClosable(true);
            win.setMode(Window.MODAL);
            win.doModal();
            win.setSclass("embeddedTenantSelection");
        }
    }

    @AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, false);

        boolean isDefaultOrder = ZulUtils.readBooleanConfig("isDefaultOrder");
        mainHome.setSclass(isDefaultOrder ? "" : "reverse");
        reverse.setSrc(!isDefaultOrder ? "/css/reverse.css" : "");
        if (!isDefaultOrder) {
            Clients.evalJavaScript("enableRTL();");
        }
        navbarCreator.createNavBar(this, navbar, getNaviGroupingViewModel());

        // redirect screen
        String link = Executions.getCurrent().getParameter("link");
        if (link != null) {
            Navi navi = ApplicationUtil.getNavigationByLink(link);
            if (navi == null)  {
                Messagebox.show(ZulUtils.translate("redirect", "pageNotFound"), ZulUtils.translate("err", "title"), Messagebox.OK, Messagebox.ERROR);
            } else {
                setSelectedFromJump(navi, null);
            }
        } else {
            final GetDefaultScreenResponse response;
            try {
                response = t9tRemoteUtils.executeExpectOk(new GetDefaultScreenRequest(), GetDefaultScreenResponse.class);
            } catch (ServiceResponseException e) {
                LOGGER.error("Unable to get default screen. " + e);
                Messagebox.show(ZulUtils.translate("err", "unableToGetDefaultScreen") + " - " + e.getReturnCodeMessage(), ZulUtils.translate("err", "title"),
                    Messagebox.OK, Messagebox.ERROR);
                return;
            }
            if (response.getDefaultScreenId() != null) {
                Optional<Navi> navi = as.getAllNavigations().stream().filter(i -> i.getNaviId().equals(response.getDefaultScreenId())).findFirst();
                if (navi.isPresent()) {
                    // the configured screen exists
                    setNaviSelection(navi.get());
                } else {
                    // this is a valid case because a previously existing screen could be unavailable due to a mistake in realease upgrade,
                    // or due to manual editing
                    LOGGER.error("Configured default screen {} does not exist", response.getDefaultScreenId());
                }
            }
        }

        final List<String> queryConfigs = new ArrayList<>();
        queryConfigs.add(T9tConstants.CFG_FILE_KEY_ENVIRONMENT_TEXT);
        queryConfigs.add(T9tConstants.CFG_FILE_KEY_ENVIRONMENT_CSS);
        final QueryConfigRequest queryConfigRequest = new QueryConfigRequest(queryConfigs);
        final QueryConfigResponse queryConfigResponse;
        try {
            queryConfigResponse = t9tRemoteUtils.executeExpectOk(queryConfigRequest, QueryConfigResponse.class);
        } catch (ServiceResponseException e) {
            LOGGER.error("Unable to query config. " + e);
            Messagebox.show(ZulUtils.translate("err", "unableToQueryConfig") + " - " + e.getReturnCodeMessage(), ZulUtils.translate("err", "title"),
                Messagebox.OK, Messagebox.ERROR);
            return;
        }
        final Map<String, String> configs = queryConfigResponse.getKeyValuePairs();
        if (configs != null && !configs.isEmpty()) {
           String envText = configs.get(T9tConstants.CFG_FILE_KEY_ENVIRONMENT_TEXT);
           if (envText != null) {
               Label label = (Label) environmentIdentifier.getFirstChild();
               label.setValue(envText);

               String envCssClass = configs.get(T9tConstants.CFG_FILE_KEY_ENVIRONMENT_CSS);
               if (envCssClass != null) {
                   environmentIdentifier.addSclass(envCssClass);
               }
           }
        }
    }

    /**
     * @return the pwdExpiresInDays
     */
    public Long getPwdExpiresInDays() {
        return pwdExpiresInDays;
    }
    /**
     * @param pwdExpiresInDays
     *            the pwdExpiresInDays to set
     */
    public void setPwdExpiresInDays(Long pwdExpiresInDays) {
        this.pwdExpiresInDays = pwdExpiresInDays;
    }
    /**
     * @return the whenLastLoggedIn
     */
    public final String getWhenLastLoggedIn() {
        return whenLastLoggedIn;
    }
    /**
     * @param whenLastLoggedIn
     *            the whenLastLoggedIn to set
     */
    public final void setWhenLastLoggedIn(String whenLastLoggedIn) {
        this.whenLastLoggedIn = whenLastLoggedIn;
    }
    /**
     *
     * @param hierarchy
     *            if not null than get only the given hierarchy
     */
    public final void setNavigation(Integer hierarchy) {

        if (hierarchy == null) {
            naviGroupingViewModel = new NaviGroupingViewModel(as.getAllNavigations(), new NaviComparator(), false);
        } else {
            naviGroupingViewModel = new NaviGroupingViewModel(as.getAllNavigations(), new NaviComparator(), false);
        }
    }

    /**
     * @return the userInfo
     */
    public final String getUserInfo() {
        return userInfo;
    }

    /**
     * @param userInfo
     *            the userInfo to set
     */
    public final void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }


    /**
     * @return the naviGroupingViewModel
     */
    public final NaviGroupingViewModel getNaviGroupingViewModel() {
        if (naviGroupingViewModel == null) {
            setNavigation(0);
        }
        return naviGroupingViewModel;
    }

    /**
     * @param naviGroupingViewModel
     *            the naviGroupingViewModel to set
     */
    public final void setNaviGroupingViewModel(NaviGroupingViewModel naviGroupingViewModel) {
        this.naviGroupingViewModel = naviGroupingViewModel;
    }

    /**
     * @return the selected
     */
    public final Object getSelected() {
        return selected;
    }

    /**
     * @param selected
     *            the selected to set
     */
    public final void setSelected(Object selected) {
        if (selected instanceof String) {
            setNaviGroup(String.valueOf(selected), true);
        } else {
            createComponents((Navi) selected);
            this.selected = selected;
            setNaviGroup(((Navi)selected).getCategory(), false);
        }
    }
    private static final Map<String, Object> NO_PARAMS = ImmutableMap.of();

    @GlobalCommand(JumpTool.SELECTED_PARAM_2)
    @NotifyChange("jumpBackVisible")
    public final void setSelectedFromJump(
      @BindingParam(JumpTool.SELECTED_PARAM_1) final Object xselected,
      @BindingParam(JumpTool.BACK_LINK_1) final String backNaviLink
    ) {
        if (xselected instanceof String xstr) {
            setNaviGroup(xstr, true);
        } else if (xselected instanceof Navi navi) {
            if (!naviContentMap.containsKey(navi.getNaviId())) {
                //createComponents(navi);
                createComponents(navi, backNaviLink == null ? NO_PARAMS : Collections.singletonMap(JumpTool.BACK_LINK_2, backNaviLink),
                        Constants.Application.CachingType.GET_CACHED);
                this.selected = xselected;
                setNaviGroup((navi).getCategory(), false);
            } else {
                String targetZul = navi.getLink();
                ApplicationUtil.navJumpToScreen(targetZul,
                        backNaviLink == null ? NO_PARAMS : Collections.singletonMap(JumpTool.BACK_LINK_2, backNaviLink));
            }
            jumpBackVisible = true;
        }
    }
    private void setNaviGroup(String category, boolean isClosePermitted) {
        Integer groupIndex = applicationDAO.getGroupIndexByCategory(category, naviGroupingViewModel);
        if (groupIndex != null) {
            if (!naviGroupingViewModel.isGroupOpened(groupIndex)) {
                naviGroupingViewModel.addOpenGroup(groupIndex.intValue());
            } else if (isClosePermitted && naviGroupingViewModel.isGroupOpened(groupIndex)) {
                naviGroupingViewModel.removeOpenGroup(groupIndex.intValue());
            }
        }
    }

    @Command
    @NotifyChange({"selected", "screenTitle"})
    public final void setNaviSelection(@BindingParam("navi") Navi navi) {
        setSelected(navi);
    }

    public final String getSubMenuClass(int childCount) {

        if (childCount > MAX_NUMBER_SUBMENU_ITEMS_PER_COLUMN) {
            int i = childCount / MAX_NUMBER_SUBMENU_ITEMS_PER_COLUMN;
            if (i > 1)
                return "header-nav-submenu-" + i + "c";
        }

        return "";
    }

    /**
     *
     * @param navi
     *            Navigates to the selected menu item.
     */
    public final void createComponents(Navi navi) {
        createComponents(navi, null, Constants.Application.CachingType.GET_CACHED);
    }

    public final void createComponents(Navi navi, Map<String, Object> params, /*boolean isCached*/Constants.Application.CachingType cachingType) {

        panel.getChildren().clear();

        if (previouslyCachingTypeWasCreateWithoutCaching && previousNaviKey != null) {
            naviContentMap.remove(previousNaviKey);
        }

        as.setRequestParams(params);

        Map<String, String> map = new HashMap<String, String>();
        map.put(NaviConfig.PERMISSION, navi.getPermission());
        map.put(NaviConfig.LINK, navi.getLink());

        String key = navi.getNaviId();

        if (previouslyCachingTypeWasCreateWithoutCaching) {
            panel.getChildren().clear();
            previouslyCachingTypeWasCreateWithoutCaching = false;
        }

        if (!naviContentMap.containsKey(key) || (cachingType == Constants.Application.CachingType.CREATE_AND_CACH)
                || (cachingType == Constants.Application.CachingType.CREATE_WITHOUT_CACHING)) {

            if (cachingType == Constants.Application.CachingType.CREATE_WITHOUT_CACHING) {
                naviContentMap.remove(key); // clear the content map
            }

            Component content = Executions.createComponents(navi.getLink(), null,  map);
            Panelchildren panelChildren = new Panelchildren();
            if (cachingType == Constants.Application.CachingType.CREATE_WITHOUT_CACHING) {
                content.setId(key + "_" + Constants.Application.CachingType.CREATE_WITHOUT_CACHING);
                panelChildren.setId(key + "_" + Constants.Application.CachingType.CREATE_WITHOUT_CACHING);
            } else {
                content.setId(key);
                panelChildren.setId(key);
            }

            panelChildren.appendChild(content);
            panel.appendChild(panelChildren);

            if (cachingType == Constants.Application.CachingType.CREATE_WITHOUT_CACHING) {
                previouslyCachingTypeWasCreateWithoutCaching = true;
            } else {
                naviContentMap.put(key, panelChildren);
            }

        } else {
            panel.getChildren().clear();
            panel.appendChild(naviContentMap.get(key));
            panel.setFocus(true);
        }

        previousNaviKey = key;

        LOGGER.debug("Childs are {} {} {}", "x", panel.getLastChild().getId(), panel.getLastChild().hashCode());

        //set screen title
        String command = String.format("setAppCurrentPageTitle('%s');", navi.getName());
        Clients.evalJavaScript(command);
    }
    /*
     * @GlobalCommand("createLinkComponents") public final void
     * createLinkComponents(@BindingParam("naviLink") String naviLink,
     * @BindingParam("params") String params) {
     * with pre-populated/ pre-selected data Navi navi=
     * applicationDAO.getNavigationByLink(naviLink); //
     * this.contentDiv.getChildren().clear(); Map<String,String> map= new
     * HashMap<String,String>(); map.put("permission", navi.getPermission());
     * map.put("params", params);
     *
     * Integer groupIndex=applicationDAO.getGroupIndex(naviLink,
     * naviGroupingViewModel); if (groupIndex!=null) {
     * if (!naviGroupingViewModel.isGroupOpened(groupIndex)) {
     * naviGroupingViewModel.addOpenGroup(groupIndex.intValue()); } else
     * if (naviGroupingViewModel.isGroupOpened(groupIndex)) {
     * naviGroupingViewModel.removeOpenGroup(groupIndex.intValue());
     * naviGroupingViewModel.addOpenGroup(groupIndex.intValue()); } }
     *
     * this.selected = navi; Executions.createComponents(navi.getLink(),
     * contentCard, map); }
     */
    @GlobalCommand("createLinkComponents")
    @NotifyChange({"selected"})
    public final void createLinkComponents(
        @BindingParam("naviLink") final String naviLink,
        @BindingParam("params")   final Map<String, Object> params,
        @BindingParam("CachingType") final Constants.Application.CachingType cachingType) {

        final Navi navi = applicationDAO.getNavigationByLink(as, naviLink);

        if (navi == null) {
            throw new IllegalArgumentException("The navigation link " + naviLink + " is not configured in the resource properties. "
                    + "Please check section:\n "
                    + "\tmenu.base= {\n"
                    + "\t\t...\n"
                    + "\t\tnavi_id, position, category, name, " + naviLink + ", hierarchy, permission, "
                    + "closeGroup, AuthenticationType availability (*=all), menuItemVisible,item-image\n"
                    + "\t\t...\n" + "\t}");
        }
        // Map<String,String> map= new HashMap<String,String>();
        // map.put("permission", navi.getPermission());
        // map.put("params", params);
        if (cachingType == null) {
            throw new IllegalArgumentException("Wrong CachingType (null)");
        }

        // logging
        final String paramsToDisplay;
        if (params instanceof Map) {
            final Map<Object, Object> toStringMap = Generics.cast(params);
            paramsToDisplay = mapToString(toStringMap);
        } else {
            paramsToDisplay = params != null ? String.valueOf(params) : null;
        }
        LOGGER.debug("Jump->to:{}:{} with:params={}", naviLink, cachingType, paramsToDisplay);

        // menu focus
        if (navi.isMenuItemVisible()) {
            setNaviSelection(navi);
        }

        createComponents(navi, params, cachingType);

        /*
         * Integer groupIndex=applicationDAO.getGroupIndex(naviLink,
         * naviGroupingViewModel); if (groupIndex!=null) {
         * if (!naviGroupingViewModel.isGroupOpened(groupIndex)) {
         * naviGroupingViewModel.addOpenGroup(groupIndex.intValue()); } else
         * if (naviGroupingViewModel.isGroupOpened(groupIndex)) {
         * naviGroupingViewModel.removeOpenGroup(groupIndex.intValue());
         * naviGroupingViewModel.addOpenGroup(groupIndex.intValue()); } }
         *
         * this.selected = navi; Executions.createComponents(navi.getLink(),
         * contentCard, map);
         */
    }


    /**
     *
     * @param keyEvent
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    @Command
    public void ctrlKeyClick(@org.zkoss.bind.annotation.BindingParam("item") KeyEvent keyEvent) {
        CtrlKeyHandler ctrlKeyHandler = new CtrlKeyHandler();
        //ctrlKeyHandler.ctrlKeyClick(keyEvent,  applicationDAO);
        ctrlKeyHandler.ctrlKeyClick(keyEvent,  this.htmlBasedFieldComponents);
    }
    @SuppressWarnings("rawtypes")
    @GlobalCommand
    @NotifyChange({ "filters"})
    public void registerFields(@BindingParam("filters") List<IField> xfilters) {
        this.filters = xfilters;
        registerFieldComponents(this.filters);
    }

    @SuppressWarnings("rawtypes")
    public void registerFieldComponents(List<IField> xfilters) {
        if (xfilters.isEmpty()) {
            this.htmlBasedFieldComponents = null;
        } else {
            htmlBasedFieldComponents = new ArrayList<HtmlBasedComponent>();
            for (IField filter : xfilters) {
                for (Object component:filter.getComponents()) {
                    htmlBasedFieldComponents.add((HtmlBasedComponent) component);
                }
            }
        }
    }

    @Command("changePwd")
    @NotifyChange({"selected", "screenTitle"})
    public void navigateToChangePasswordPage() {
        //Create a dummy navi because it will be excluded from the menu configuration
        Navi navi = new Navi();
        navi.setNaviId("Session-Change Password");
        navi.setCategory("Session");
        navi.setName("Change Password");
        navi.setLink("screens/session/change_pwd.zul");
        navi.setPermission("changePasswordScreen");

        setNaviSelection(navi);
    }

//    /**
//     * @param contentDiv
//     *            the contentDiv to set
//     */
//    @Command
//    public final void setPanel(
//            @BindingParam("panel") Panel panel) {
//        this.panel = panel;
//
//        if (this.selected == null) {
//            // initial set selected to the first item
//            // setSelected(naviGroupingViewModel.getChild(0, 0));
//            initDefaultScreen();
//        }
//    }
    /**
     * @return the selectedTenantId
     */
    public final String getSelectedTenantId() {
        return selectedTenantId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getNumberOfIncorrectAttempts() {
        return numberOfIncorrectAttempts;
    }

    public void setNumberOfIncorrectAttempts(Integer numberOfIncorrectAttempts) {
        this.numberOfIncorrectAttempts = numberOfIncorrectAttempts;
    }

    public AImage getTenantLogo() {
        return as.getTenantLogo();
    }

    public String getTenantResource(String resource) {
        return as.getTenantResource(resource);
    }

    public boolean getJumpBackVisible() {
        return jumpBackVisible;
    }

    @Command("search")
    public void searchTitle(@BindingParam("self_value") String searchText) {
        titleSearch.search(searchText);
    }

    @Command("jumpBack")
    @NotifyChange("jumpBackVisible")
    public void jumpBack() {
        Map<String, Object> params = as.getRequestParams();
        Object backLink = params.get(JumpTool.BACK_LINK_2);
        if (backLink != null && backLink instanceof String backNaviLink) {
            final Navi navi = ApplicationUtil.getNavigationByLink(backNaviLink);
            createComponents(navi);
        }
        jumpBackVisible = false;
    }

    /**
     * To check if subtitle should be display based on the menus iteration
     * if there is a new subtitle compared to the previous one, return true
     *
     * @param index
     * @param childIndex
     * @return
     */
    public boolean subtitleShouldDisplay(int index, int childIndex) {

        if (childIndex != 0) {
            return naviGroupingViewModel.getChild(index, childIndex).getSubcategory() != null && naviGroupingViewModel.getChild(index, childIndex)
                    .getSubcategory() != naviGroupingViewModel.getChild(index, childIndex - 1).getSubcategory();
        } else {
            return naviGroupingViewModel.getChild(index, childIndex).getSubcategory() != null;
        }
    }

    /**
     * This method is called from javascript to show the warning when the session is about to expired.
     */
    @Command
    public void showSessionExpirationWarning() {
        Messagebox.show(as.translate("session", "timeoutMessage"), as.translate("session", "timeout"),
                new Messagebox.Button[] { Messagebox.Button.NO, Messagebox.Button.OK },
                new String[] { as.translate("session", "exit"), as.translate("session", "continue") },
                null, Messagebox.Button.OK, new EventListener<ClickEvent>() {
                    @Override
                    public void onEvent(ClickEvent evt) throws Exception {
                        if (evt.getName().equals(Messagebox.ON_NO)) {
                            logout();
                        } else if (evt.getName().equals(Messagebox.ON_OK)) {
                            Clients.evalJavaScript("keepSessionAlive();");
                        }
                    }
                });
    }

    /**
     * This method is called from javascript to logout the user when the session is expired.
     */
    @Command
    public void logout() {
        authenticationService.logout();
    }

    private static String mapToString(Map<Object, Object> map) {
        if (map == null) {
            return "null";
        }
        final int maxWidth = 80;
        Set<Entry<Object, Object>> entrySet = map.entrySet();
        Iterator<Entry<Object, Object>> i = entrySet.iterator();
        if (!i.hasNext()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;) {
            Entry<?, ?> e = i.next();
            Object key = e.getKey();
            Object value = e.getValue();
            sb.append(key == map ? "(this Map)" : key);
            sb.append('=');
            if (value instanceof Map) {
                Map<Object, Object> innerMap = Generics.cast(value);
                sb.append(mapToString(innerMap));
            } else if (value instanceof BonaPortable) {

                sb.append("((");
                BonaPortable bonaPortable = Generics.cast(value);
                List<FieldDefinition> fieldDefinitions = bonaPortable != null ? bonaPortable.ret$MetaData().getFields()
                        : Collections.<FieldDefinition>emptyList();
                for (FieldDefinition fieldDefinition : fieldDefinitions) {
                    if (fieldDefinition instanceof ObjectReference) {
                        try {
                            Field field = bonaPortable.getClass().getDeclaredField(fieldDefinition.getName());
                            field.setAccessible(true);
                            sb.append(StringUtils.abbreviate(String.valueOf(field.get(bonaPortable)), maxWidth));
                        } catch (Exception e1) {
                            sb.append(String.valueOf(e1.getMessage()));
                        }
                        sb.append(',').append(' ');
                    }
                }
                sb.append("))");

            } else {
                sb.append(StringUtils.abbreviate((value == map ? "(this Map)" : String.valueOf(value)), maxWidth));
            }
            if (!i.hasNext()) {
                return sb.append('}').toString();
            } else {
                sb.append(',').append(' ');
            }
        }
    }
}
