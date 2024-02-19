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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.auth.PermissionEntry;
import com.arvatosystems.t9t.zkui.azure.ad.AadAuthUtil;
import com.arvatosystems.t9t.zkui.azure.ad.AadConstants;
import com.arvatosystems.t9t.zkui.azure.ad.IdentityContextData;
import com.arvatosystems.t9t.zkui.exceptions.ReturnCodeException;
import com.arvatosystems.t9t.zkui.services.IAuthenticationService;
import com.arvatosystems.t9t.zkui.services.IUserDAO;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.ApplicationUtil;
import com.arvatosystems.t9t.zkui.util.Constants;
import com.arvatosystems.t9t.zkui.util.ZulUtils;
import com.arvatosystems.t9t.zkui.viewmodel.beans.ComboBoxItem;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Strings;
import com.microsoft.aad.msal4j.AuthorizationRequestUrlParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.Prompt;
import com.microsoft.aad.msal4j.ResponseMode;

import de.jpaw.dp.Jdp;
import jakarta.servlet.http.HttpServletRequest;



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
    protected final IAuthenticationService authenticationService = Jdp.getRequired(IAuthenticationService.class);
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

    private static final Cache<String, UserInfo> USER_INFO_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES).build();
    private List<ComboBoxItem> languageListModel = new ArrayList<ComboBoxItem>();
    private ComboBoxItem selected;

    private String lastScreen;
    private TimeZone lastZone;
    private String lastRealTz;
    private boolean microsoftAuthEnabled = false;
    private boolean showLoginErrorMessage = false;

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
        final StringBuilder lastScreenBuilder = new StringBuilder("desktop: ").append(evt.getDesktopWidth()).append("*").append(evt.getDesktopHeight())
                .append(", screen: ").append(evt.getScreenWidth()).append("*").append(evt.getScreenHeight());

        lastZone = evt.getTimeZone();
        if (Executions.getCurrent() != null) {
            String ua = Executions.getCurrent().getUserAgent();
            LOGGER.debug("ZK user agent is {}", ua);
            lastScreenBuilder.append(", agent: ").append(ua);
        }
        lastScreen = lastScreenBuilder.toString();
    }

    @NotifyChange("showLoginErrorMessage")
    @Command("login")
    public void onLogin(@BindingParam("username") String username, @BindingParam("password") String password, @BindingParam("rememberMe") boolean rememberMe) {
        setShowLoginErrorMessage(false); // Reset error message flag on every login attempt

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

        // Attempt to login
        try {
            authenticationService.login(username, password);
        } catch (final T9tException e) {
            setShowLoginErrorMessage(true);
        }
    }

    @NotifyChange("showLoginErrorMessage")
    @Command("msLogin")
    public void onMsLogin() {
        LOGGER.debug("submit login for microsoft");
        try {
            final ApplicationSession applicationSession = ApplicationSession.get();
            IdentityContextData aadContextData = (IdentityContextData) applicationSession.getSessionValue(AadConstants.SESSION_PARAM);
            if (aadContextData == null) {
                aadContextData = new IdentityContextData();
            }

            final String state = UUID.randomUUID().toString();
            final String nonce = UUID.randomUUID().toString();
            aadContextData.setState(state);
            aadContextData.setNonce(nonce);
            applicationSession.setSessionValue(AadConstants.SESSION_PARAM, aadContextData);

            final ConfidentialClientApplication client = AadAuthUtil.getConfidentialClientInstance();
            final AuthorizationRequestUrlParameters parameters = AuthorizationRequestUrlParameters
                .builder(AadConstants.REDIRECT_URI, Collections.singleton(AadConstants.SCOPES)).responseMode(ResponseMode.QUERY).prompt(Prompt.SELECT_ACCOUNT)
                .state(state).nonce(nonce).build();

            final String authorizeUrl = client.getAuthorizationRequestUrl(parameters).toString();
            LOGGER.debug("redirecting user to microsoft login...");
            Executions.sendRedirect(authorizeUrl);

        } catch (final T9tException | MalformedURLException ex) {
            LOGGER.error("Error occured while login with microsoft. {}", ex);
            setShowLoginErrorMessage(true);
        }
    }

    public static UserInfo getUserInfo(final String username) {
        return USER_INFO_CACHE.getIfPresent(username);
    }

    @Init
    @NotifyChange({ "selected", "showLoginErrorMessage", "microsoftAuth" })
    public void init(@BindingParam("isInitialLogin") Boolean isInitialLogin) {
        microsoftAuthEnabled = AadConstants.MICROSOFT_AUTH_ENABLED;
        setLanguageFromCooky(isInitialLogin);
        setListbox();
        //FT-1127        UI login does not work if already logged in
        final ApplicationSession applicationSession = ApplicationSession.get();
        if (applicationSession.isAuthenticated() && isInitialLogin == null) {

            if (applicationSession.getTenantId() != null) {
                Executions.sendRedirect(Constants.ZulFiles.HOME);
            } else {
                Executions.sendRedirect(Constants.ZulFiles.LOGIN_TENANT_SELECTION);
            }
        }
        String isLoginFail = Executions.getCurrent().getParameter("loginFail");
        showLoginErrorMessage = isLoginFail != null && isLoginFail.equalsIgnoreCase("true");
    }

    @Command("onLanguageChanged")
    public void onLanguageChanged(@BindingParam("localeValue") String localeValue) {
        Locale preferLocale = null;
        if (localeValue != null && localeValue.length() == 5) {
            preferLocale = new Locale(localeValue.substring(0, 2), localeValue.substring(3, 5));
        } else {
            preferLocale = new Locale(localeValue);
        }
        Sessions.getCurrent().setAttribute(org.zkoss.web.Attributes.PREFERRED_LOCALE, preferLocale);
        this.switchLanguage();
        Executions.sendRedirect("");
    }

    @Command("switchLanguage")
    public void switchLanguage() {
        setListbox();

        final ApplicationSession applicationSession = ApplicationSession.get();
        if (applicationSession.isAuthenticated()) {
            try {
                // This code is also called after logged in.  This causes an extra JWT to be generated.
                // It should be improved to avoid that, unless required, in order to reduce the number of session log entries.
                userDAO.switchLanguage(selected.getValue());
                // needs to reload permissions as well
                List<PermissionEntry> userPermissionForThisTenant = userDAO.getPermissions();
                applicationSession.storePermissions(userPermissionForThisTenant);
            } catch (ReturnCodeException e) {
                throw new T9tException(T9tException.USER_NOT_FOUND, "Login failed");
            }
        }
    }

    @Command("switchLanguageOnLogin")
    public void onLanguageChange() {
        String localeValue = selected.getValue();
        Locale preferLocale = null;
        if (localeValue != null && localeValue.length() == 5) {
            preferLocale = new Locale(localeValue.substring(0, 2), localeValue.substring(3, 5));
        } else {
            preferLocale = new Locale(localeValue);
        }
        Sessions.getCurrent().setAttribute(org.zkoss.web.Attributes.PREFERRED_LOCALE, preferLocale);
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
                Locale userLocale = cookieLanguage.length() == 2 ? new Locale(cookieLanguage)
                        : new Locale(cookieLanguage.substring(0, 2), cookieLanguage.substring(3, 5));
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

    public boolean isShowLoginErrorMessage() {
        return showLoginErrorMessage;
    }

    public void setShowLoginErrorMessage(final boolean showLoginErrorMessage) {
        this.showLoginErrorMessage = showLoginErrorMessage;
    }

    public boolean isMicrosoftAuthEnabled() {
        return microsoftAuthEnabled;
    }
}
