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
package com.arvatosystems.t9t.tfi.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UnknownAccountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.annotation.ToClientCommand;
import org.zkoss.bind.annotation.ToServerCommand;
import org.zkoss.web.Attributes;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.ClientInfoEvent;
import org.zkoss.zk.ui.util.Clients;

import com.arvatosystems.t9t.base.auth.PermissionEntry;
import com.arvatosystems.t9t.tfi.general.ApplicationUtil;
import com.arvatosystems.t9t.tfi.general.Constants;
import com.arvatosystems.t9t.tfi.model.bean.ComboBoxItem;
import com.arvatosystems.t9t.tfi.services.IUserDAO;
import com.arvatosystems.t9t.tfi.services.ReturnCodeException;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.tfi.web.ZulUtils;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.jpaw.dp.Jdp;



/**
 * Login VModel.
 * @author INCI02
 *
 */
@ToServerCommand("realTimezone")
@ToClientCommand("realTimezone")
public class LoginViewModel {
    private static final String LANGUAGE_COOKIE                     = "LANGUAGE_COOKIE";
    private static final String USERNAME_COOKIE                     = "USERNAME_COOKIE";
    private static final Logger LOGGER                     = LoggerFactory.getLogger(LoginViewModel.class);
    protected final IUserDAO userDAO = Jdp.getRequired(IUserDAO.class);
//    private static final ConcurrentMap<String, String>   screen = new ConcurrentHashMap<String, String>(100);
//    private static final ConcurrentMap<String, TimeZone> zones = new ConcurrentHashMap<String, TimeZone>(100);
//    private static final ConcurrentMap<String, Locale> locales = new ConcurrentHashMap<String, Locale>(100);
//    private static final ConcurrentMap<String, String> realTz = new ConcurrentHashMap<String, String>(100);
//
//    public static String getScreen(String username) {
//        return screen.get(username);
//    }
//    public static TimeZone getTimeZone(String username) {
//        return zones.get(username);
//    }
//    public static Locale getLocale(String username) {
//        return locales.get(username);
//    }
    public static class UserInfo {
        public final String screenInfo;
        public final String browserTz;
        public final Locale locale;
        public final TimeZone zkTz;
        public UserInfo(String screenInfo, String browserTz, Locale locale, TimeZone zkTz) {
            super();
            this.screenInfo = screenInfo;
            this.browserTz = browserTz;
            this.locale = locale;
            this.zkTz = zkTz;
        }
    }

    private static final Cache<String, UserInfo> USER_INFO_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES).build();
    private List<ComboBoxItem> languageListModel = new ArrayList<ComboBoxItem>();
    private ComboBoxItem selected;

    private String lastScreen;
    private TimeZone lastZone;
    private String lastRealTz;

    @Command("realTimezone")
    public void realTimezone(@BindingParam("tzid") String zoneId) {
        LOGGER.debug("received time zone ID {}", zoneId);
        lastRealTz = zoneId;
    }

    @Command("onClientInfo")
    public void onClientInfo(@BindingParam("eventData") ClientInfoEvent evt) {
        LOGGER.debug("received info event: tz={}, display={}, desktop size={}*{}, screen size={}*{}",
                evt.getTimeZone(), evt.getTimeZone().getDisplayName(),
                evt.getDesktopWidth(), evt.getDesktopHeight(),
                evt.getScreenWidth(), evt.getScreenHeight());
        lastScreen = String.format("desktop: %d*%d, screen: %d*%d",
                evt.getDesktopWidth(), evt.getDesktopHeight(),
                evt.getScreenWidth(), evt.getScreenHeight());
        lastZone = evt.getTimeZone();
        if (Executions.getCurrent() != null) {
            String ua = Executions.getCurrent().getUserAgent();
            LOGGER.debug("ZK user agent is {}", ua);
            lastScreen = lastScreen + ", agent: " +ua;
        }
    }

    @Command("login")
    public void onLogin(@BindingParam("username") String username, @BindingParam("rememberMe") boolean rememberMe) {
        LOGGER.debug("submit login for user name {}, last screen is {}, lang is {}", username, lastScreen,
                Sessions.getCurrent().getAttribute(org.zkoss.web.Attributes.PREFERRED_LOCALE));
        if (username != null) {

            if (rememberMe) {
                ApplicationUtil.setCookie(USERNAME_COOKIE, username);
            } else {
                ApplicationUtil.setCookie(USERNAME_COOKIE, null);
            }

            ApplicationUtil.setCookie(LANGUAGE_COOKIE, selected.getValue());
            USER_INFO_CACHE.put(username, new UserInfo(lastScreen, lastRealTz,
                    (Locale)(Sessions.getCurrent().getAttribute(org.zkoss.web.Attributes.PREFERRED_LOCALE)),
                    lastZone));
        }
        Clients.submitForm("f");
    }

    public static UserInfo getUserInfo(String username) {
        return USER_INFO_CACHE.getIfPresent(username);
    }

    @Init
    @NotifyChange({ "selected" })
    public void init(@BindingParam("isInitialLogin") Boolean isInitialLogin) {
        setLanguageFromCooky(isInitialLogin);
        setListbox();
        //FT-1127        UI login does not work if already logged in
        if ((SecurityUtils.getSubject() != null) && (SecurityUtils.getSubject().isAuthenticated() == true) && (isInitialLogin == null)) {

            if ((ApplicationSession.get().getTenantId() != null)) {
                Executions.sendRedirect(Constants.ZulFiles.HOME);
            } else {
                Executions.sendRedirect(Constants.ZulFiles.LOGIN_TENANT_SELECTION);
            }
        }
    }

    @Command("onLanguageChanged")
    public void onLanguageChanged(@BindingParam("localeValue") String localeValue) {
        // String localeValue = ((com.arvatosystems.t9t.tfi.model.bean.ComboBoxItem) self.getSelectedItem().getValue()).getValue();
        Locale prefer_locale = null;
        if(localeValue!=null && localeValue.length()==5){
            prefer_locale = new Locale(localeValue.substring(0,2),localeValue.substring(3,5));
        }else{
            prefer_locale = new Locale(localeValue);
        }
        Sessions.getCurrent().setAttribute(org.zkoss.web.Attributes.PREFERRED_LOCALE, prefer_locale);
        this.switchLanguage();
        Executions.sendRedirect("");
    }

    @Command("switchLanguage")
    public void switchLanguage() {
        setListbox();

        if ((SecurityUtils.getSubject() != null) && (SecurityUtils.getSubject().isAuthenticated() == true)) {
            try {
                // This code is also called after logged in.  This causes an extra JWT to be generated.
                // It should be improved to avoid that, unless required, in order to reduce the number of session log entries.
                userDAO.switchLanguage(selected.getValue());
                // needs to reload permissions as well
                List<PermissionEntry> userPermissionForThisTenant = userDAO.getPermissions();
                ApplicationSession.get().storePermissions(userPermissionForThisTenant);
            } catch (ReturnCodeException e) {
                throw new UnknownAccountException("Login failed");
            }
        }
    }

    @Command("switchLanguageOnLogin")
    public void onLanguageChange() {
        String localeValue = selected.getValue();
        Locale prefer_locale = null;
        if(localeValue!=null && localeValue.length()==5){
         prefer_locale = new Locale(localeValue.substring(0,2),localeValue.substring(3,5));
        }else{
         prefer_locale = new Locale(localeValue);
        }
        Sessions.getCurrent().setAttribute(org.zkoss.web.Attributes.PREFERRED_LOCALE, prefer_locale);
    }

    private Locale getLocale() {
        // first, try manually selected locale
        Locale selectedLocale = (Locale) Sessions.getCurrent().getAttribute(Attributes.PREFERRED_LOCALE);
        if (selectedLocale != null) {
            return selectedLocale;
        }
        Locale defaultLocale = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getLocale();
        Sessions.getCurrent().setAttribute(org.zkoss.web.Attributes.PREFERRED_LOCALE, defaultLocale);
        return defaultLocale;
    }

    private void setListbox() {
         languageListModel.clear();
        Map<String, ComboBoxItem> items = new HashMap<String, ComboBoxItem>();

        String name = null;
        String value = null;
        StringTokenizer st = new StringTokenizer(ZulUtils.translate("login", "languagebox"), ",");
        //Library.setProperty(Attributes.PREFERRED_TIME_ZONE, "GMT+02:00");
        Locale l = getLocale();
        String localeString = l.toString();
        boolean foundLocale = false;

        while (st.hasMoreTokens()) {
            name = st.nextToken();
            value = st.nextToken();
            if (!Strings.isNullOrEmpty(value)) {
                ComboBoxItem comboBoxItem = new ComboBoxItem(name, value);
                languageListModel.add(comboBoxItem);
                items.put(value, comboBoxItem);
                if (localeString.equals(value)) {
                    foundLocale = true;
                    setSelected(comboBoxItem);
                }
            }
        }
        // find the best fit, unless perfect match was found...
        if (!foundLocale) {
            localeString = l.getLanguage();
            ComboBoxItem langFit = items.get(localeString);
            if (langFit == null) {
                langFit = items.get("en");
                if (langFit == null) {
                    LOGGER.error("Not even en found in languages???");
                    throw new RuntimeException("Missing language en in language selection");
                }
            }
            setSelected(langFit);
            Sessions.getCurrent().setAttribute(org.zkoss.web.Attributes.PREFERRED_LOCALE, Locale.forLanguageTag(localeString));
        }
    }

    void setLanguageFromCooky(Boolean isInitialLogin) {
        if (null == isInitialLogin || isInitialLogin) {
            String cookieLanguage  =       ApplicationUtil.getCookie(LANGUAGE_COOKIE);
            if (null != cookieLanguage) {
                Locale  userLocale = cookieLanguage.length() == 2 ? new Locale(cookieLanguage) :
                    new Locale(cookieLanguage.substring(0, 2), cookieLanguage.substring(3, 5));
                Sessions.getCurrent().setAttribute(org.zkoss.web.Attributes.PREFERRED_LOCALE, userLocale);
            }
        }
    }
    public final List<ComboBoxItem> getLanguageListModel() {
        return languageListModel;
    }

    public final void setLanguageListModel(List<ComboBoxItem> languageListModel) {
        this.languageListModel = languageListModel;
    }

    public final ComboBoxItem getSelected() {
        return selected;
    }

    public final void setSelected(ComboBoxItem selected) {
        this.selected = selected;
    }

    public String getUsername() {
        return ApplicationUtil.getCookie(USERNAME_COOKIE);
    }
}
