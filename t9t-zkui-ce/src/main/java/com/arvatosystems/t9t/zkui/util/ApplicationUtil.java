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
package com.arvatosystems.t9t.zkui.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.BindUtils;
import org.zkoss.text.DateFormats;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.KeyEvent;

import com.arvatosystems.t9t.zkui.services.IApplicationDAO;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.Constants.Application.CachingType;
import com.arvatosystems.t9t.zkui.viewmodel.beans.Navi;

import de.jpaw.dp.Jdp;


public final class ApplicationUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationUtil.class);
    private static String version = null;
    private static Properties configuration = null;
    private static final int THIRTY_DAYS                           = 2592000;

    private ApplicationUtil() { }

    public static String getVersion() {
        return version;
    }

    public static String getVersionWithoutSnapshot() {
        if (version == null) {
            return version;
        } else {
            return version.replace("-SNAPSHOT", "");
        }
    }

    public static void setVersion(String version) {
        ApplicationUtil.version = version;
    }

    public static Properties getConfiguration() {
        return configuration;
    }

    public static void setConfiguration(Properties configuration) {
        ApplicationUtil.configuration = configuration;
    }

    public static String getKeyCodeFromKeyEvent(KeyEvent keyEvent) {
        char keyCodeChar = ((char) keyEvent.getKeyCode());
        boolean isCtrlKey = keyEvent.isCtrlKey();
        boolean isAltKey = keyEvent.isAltKey();
        boolean isShiftKey = keyEvent.isShiftKey();
        String keyCodeString = "" + (isCtrlKey ? Constants.KeyStrokes.CTRL_KEY : "") + (isAltKey ? Constants.KeyStrokes.ALT_KEY : "")
                + (isShiftKey ? Constants.KeyStrokes.SHIFT_KEY : "") + "+" + keyCodeChar;

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("keyCodeString: " + keyCodeString);
            LOGGER.trace("keyEvent: " + keyEvent);
            LOGGER.trace("getKeyCode: " + keyEvent.getKeyCode());
            LOGGER.trace("Char: " + keyCodeChar);
            LOGGER.trace("+Data: " + keyEvent.getData());
            LOGGER.trace("Name: " + keyEvent.getName());
            LOGGER.trace("Page: " + keyEvent.getPage());
            LOGGER.trace("Reference " + keyEvent.getReference());
            LOGGER.trace("Target: " + keyEvent.getTarget());
            LOGGER.trace("isAltKey: " + isAltKey);
            LOGGER.trace("isCtrlKey: " + isCtrlKey);
            LOGGER.trace("isPropagatable:" + keyEvent.isPropagatable());
            LOGGER.trace("isShiftKey:" + isShiftKey);
        }
        return keyCodeString;
    }

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
        navBackToScreen(naviLink, new HashMap<String, Object>());
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
    public static void navBackToScreen(String naviLink, Component onCklickRefreshComoponent) {
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
        final Cookie[] cookies = ((HttpServletRequest)Executions.getCurrent().getNativeRequest()).getCookies();
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
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(THIRTY_DAYS);
        cookie.setSecure(true);
        resp.addCookie(cookie);
    }

    /**
     * Returns the date time format for the given locale. If locale is null, the default locale is used.
     * The format is modified to replace Narrow No-Break Spaces (NNBSP) with regular spaces.
     * This is a workaround for a bug in Java 21. See https://bugs.openjdk.org/browse/JDK-8304925
     *
     * @param locale the locale to use, or null for the default locale
     * @return the date time format string
     */
    @Nonnull
    public static String getDateTimeFormat(@Nullable final Locale locale) {
        return replaceNnbsp(DateFormats.getDateTimeFormat(DateFormat.MEDIUM, DateFormat.MEDIUM, locale, null));
    }

    /**
     * Returns the time format for the given locale. If locale is null, the default locale is used.
     * The format is modified to replace Narrow No-Break Spaces (NNBSP) with regular spaces.
     * This is a workaround for a bug in Java 21. See https://bugs.openjdk.org/browse/JDK-8304925
     *
     * @param locale the locale to use, or null for the default locale
     * @return the time format string
     */
    @Nonnull
    public static String getTimeFormat(@Nullable final Locale locale) {
        return replaceNnbsp(DateFormats.getTimeFormat(DateFormat.MEDIUM, locale, null));
    }

    /**
     * Returns the date format for the given locale. If locale is null, the default locale is used.
     * The format is modified to replace Narrow No-Break Spaces (NNBSP) with regular spaces.
     * This is a workaround for a bug in Java 21. See https://bugs.openjdk.org/browse/JDK-8304925
     *
     * @param locale the locale to use, or null for the default locale
     * @return the date format string
     */
    @Nonnull
    public static String getDateFormat(@Nullable final Locale locale) {
        return replaceNnbsp(DateFormats.getDateFormat(DateFormat.MEDIUM, locale, null));
    }

    @Nonnull
    private static String replaceNnbsp(@Nonnull final String s) {
        return s.replace("\u202F", " ");
    }

    /**
     * Creates a {@link DecimalFormat} instance using the current user's locale and applies the given pattern.
     *
     * @param format the decimal format pattern to apply, expressed in {@link java.text.DecimalFormat} pattern syntax
     * @return a {@link DecimalFormat} configured for the current user's locale with the given pattern applied
     */
    @Nonnull
    public static DecimalFormat getLocalizedDecimalFormat(@Nonnull final String format) {
        final Locale userLocale = ApplicationSession.get().getUserLocale();
        final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(userLocale);
        df.applyPattern(format);
        return df;
    }
}
