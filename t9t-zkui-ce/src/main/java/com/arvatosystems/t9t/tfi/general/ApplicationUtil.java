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
package com.arvatosystems.t9t.tfi.general;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.BindUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.KeyEvent;

import com.arvatosystems.t9t.tfi.general.Constants.Application.CachingType;
import com.arvatosystems.t9t.tfi.model.bean.Navi;
import com.arvatosystems.t9t.tfi.services.IApplicationDAO;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.dp.Jdp;


public class ApplicationUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationUtil.class);
    private static String version = null;
    private static Properties configuration = null;
    private static final int THIRTY_DAYS                           = 2592000;

    public static final String getVersion() {
        return version;
    }

    public static final String getVersionWithoutSnapshot() {
        if (version == null) {
            return version;
        } else {
            return version.replace("-SNAPSHOT", "");
        }
    }

    public static final void setVersion(String version) {
        ApplicationUtil.version = version;
    }

    public static final Properties getConfiguration() {
        return configuration;
    }

    public static final void setConfiguration(Properties configuration) {
        ApplicationUtil.configuration = configuration;
    }

    public static String getKeyCodeFromKeyEvent(KeyEvent keyEvent) {
        char keyCodeChar = ((char) keyEvent.getKeyCode());
        boolean isCtrlKey = keyEvent.isCtrlKey();
        boolean isAltKey = keyEvent.isAltKey();
        boolean isShiftlKey = keyEvent.isShiftKey();
        String keyCodeString = ""+(isCtrlKey ? Constants.KeyStrokes.CTRL_KEY : "")
                +(isAltKey ? Constants.KeyStrokes.ALT_KEY : "")
                +(isShiftlKey ? Constants.KeyStrokes.SHIFT_KEY : "")
                + "+" + keyCodeChar;


        LOGGER.trace("keyCodeString: " + keyCodeString);
        LOGGER.trace("ctekKeyCode: " + keyEvent);
        LOGGER.trace("getKeyCode: " + keyEvent.getKeyCode());
        LOGGER.trace("Char: " + keyCodeChar);
        LOGGER.trace("+Data: " + keyEvent.getData());
        LOGGER.trace("NAme: " + keyEvent.getName());
        LOGGER.trace("PAge: " + keyEvent.getPage());
        LOGGER.trace("Reference " + keyEvent.getReference());
        LOGGER.trace("Target: " + keyEvent.getTarget());
        LOGGER.trace("isAltKey: " + isAltKey);
        LOGGER.trace("isCtrlKey: " + isCtrlKey);
        LOGGER.trace("isPropagatable:" + keyEvent.isPropagatable());
        LOGGER.trace("isShiftKey:" + isShiftlKey);
        return keyCodeString;
    }

//    public static List<DataWithTracking<BonaPortable, FullTrackingWithActiveColumnAndVersion>> readConfigMultipleEntries(String configGroup,
//            boolean readGlobalTenant, Boolean isActive) throws ReturnCodeException {
//
//        IGenericCrudDAO<BonaPortable, FullTrackingWithActiveColumnAndVersion> genericCrudDAO =
//                Jdp.getRequired(IGenericCrudDAO.class);
//        String[] field = { "configGroup", "isActive" };
//        Object[] value = { configGroup, isActive };
//        Class<SearchCriteria> searchRequestClass = Generics.cast(ConfigSearchRequest.class);
//        GenericSearchCriteria genericSearchCriteria = fillGenericSearchCriteria(field, value);
//        List<DataWithTracking<BonaPortable, FullTrackingWithActiveColumnAndVersion>> dataWithTrackings = genericCrudDAO.read(genericSearchCriteria,
//                searchRequestClass).getDataList();
//
//        return dataWithTrackings;
//    }

    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

    /**
     * Reset a screen. Create a new screen (zul) and show it (with or without caching)
     *
     * @param naviLink
     */
    public static void navResetScreen(String naviLink) {
        navJumpToScreen(naviLink, new HashMap<String, Object>());
    }

    /**
     * Reset a screen. Create a new screen (zul) and show it (with or without caching)
     *
     * @param naviLink
     * @param params
     */
    public static void navResetScreen(String naviLink, Map<String, Object> params) {
        navJumpToScreen(naviLink, params);
    }

    /**
     * The same as {@link #navResetScreen(String)}, but used for solr tab reset. A parameter will be set <code>isTabAdvanced=Boolean.TRUE</code>
     *
     * @param naviLink
     */
    public static void navResetScreenTabAdvanced(String naviLink) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("isTabAdvanced", Boolean.TRUE);
        navJumpToScreen(naviLink, paramMap);
    }

    /**
     * This should be used for a real jump back. No actions can be triggered via this method. If you want to trigger something you have to use
     * {@link #navJumpToScreen(String, Map)}
     *
     * @param naviLink
     */
    public static void navBackToScreen(String naviLink) {
        navBackToScreen(naviLink,new HashMap<String, Object>());
        //navCreateLinkComponents(naviLink, CachingType.GET_CACHED, new HashMap<String, Object>());
    }

    /**
     * This should be used for a real jump back. No actions can be triggered via this method. If you want to trigger something you have to use
     * {@link #navJumpToScreen(String, Map)}
     *
     * @param naviLink
     * @param params
     */
    public static void navBackToScreen(String naviLink, Map<String, Object> params) {
        navCreateLinkComponents(naviLink, CachingType.GET_CACHED, params);
    }

    /**
     * @see navBackToScreen
     * An OnClick event will be fired on onCklickRefreshComoponent to trigger an refresh
     * @param naviLink
     * @param onCklickRefreshComoponent
     */
    public static void navBackToScreen(String naviLink,Component onCklickRefreshComoponent) {
        navBackToScreen(naviLink, new HashMap<String, Object>(), onCklickRefreshComoponent);
    }

    /**
     * @see navBackToScreen
     * An OnClick event will be fired on onCklickRefreshComoponent to trigger an refresh
     * @param naviLink
     * @param params
     * @param onCklickRefreshComoponent
     */
    public static void navBackToScreen(String naviLink, Map<String, Object> params,  Component onCklickRefreshComoponent) {
        if (onCklickRefreshComoponent == null)
            throw new IllegalArgumentException("onCklickRefreshComoponent cannot be null");
        Events.sendEvent(Events.ON_CLICK,  onCklickRefreshComoponent, null);
        navCreateLinkComponents(naviLink, CachingType.GET_CACHED, params);
    }

    /**
     * This method will determine the caching ({@link CachingType}) type by there self. how:<br/>
     * <ul>
     * <li>{@link CachingType#CREATE_AND_CACH} --> this will be set, if the passed 'naviLink' is configured as a main menu entry and is marked as visible</li>
     * <li>{@link CachingType#CREATE_WITHOUT_CACHING} --> this will be set, if the passed 'naviLink' is not configured as main menu entry or menu entry is
     * invisible (not visible)
     * </ul>
     *
     * @param naviLink
     * @param params
     */
    public static void navJumpToScreen(String naviLink, Map<String, Object> params) {
        CachingType cashingType = null;
        if (isLinkInMainmenuAndVisible(naviLink)) {
            cashingType = CachingType.CREATE_AND_CACH;
        } else {
            cashingType = CachingType.CREATE_WITHOUT_CACHING;
        }
        navCreateLinkComponents(naviLink, cashingType, params);
    }

    private static void navCreateLinkComponents(String naviLink, CachingType cacheingType, Map<String, Object> params) {
        Map<String, Object> args = new HashMap<>();
        args.put("naviLink", naviLink);
        args.put("params", params);
        args.put("CachingType", cacheingType);

        BindUtils.postGlobalCommand(null, null, "createLinkComponents", args);
    }

    private static boolean isLinkInMainmenuAndVisible(String naviLink) {
        IApplicationDAO applicationDAO = Jdp.getRequired(IApplicationDAO.class);
        Navi navi = applicationDAO.getNavigationByLink(ApplicationSession.get(), naviLink);
        if (navi == null)
            return false;
        else
            return navi.isMenuItemVisible();
    }

    public static Navi getNavigationByLink(String naviLink) {
        IApplicationDAO applicationDAO = Jdp.getRequired(IApplicationDAO.class);
        return applicationDAO.getNavigationByLink(ApplicationSession.get(), naviLink);
    }

    public static String getCookie(String name) {
        final Cookie [] cookies = ((HttpServletRequest)Executions.getCurrent().getNativeRequest()).getCookies();
        if (null != cookies) {
            for (Cookie cooky:cookies) {
                if (name.equals(cooky.getName())) {
                    return cooky.getValue();
                }
            }
        }
        return null;
    }

    public static void setCookie(String name, String value) {
        HttpServletResponse resp = (HttpServletResponse)Executions.getCurrent().getNativeResponse();
        Cookie cookie=new Cookie(name, value);
        cookie.setMaxAge(THIRTY_DAYS);
        cookie.setSecure(true);
        resp.addCookie(cookie);
    }
}
