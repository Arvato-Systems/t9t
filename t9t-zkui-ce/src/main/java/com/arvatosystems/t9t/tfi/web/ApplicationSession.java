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
package com.arvatosystems.t9t.tfi.web;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.session.Session;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.image.AImage;
import org.zkoss.zk.ui.Sessions;

import com.arvatosystems.t9t.tfi.general.MenuUtil;
import com.arvatosystems.t9t.tfi.model.bean.Navi;
import com.arvatosystems.t9t.tfi.services.ReturnCodeException;
import com.arvatosystems.t9t.authc.api.GetTenantLogoRequest;
import com.arvatosystems.t9t.authc.api.GetTenantLogoResponse;
import com.arvatosystems.t9t.authc.api.TenantDescription;
import com.arvatosystems.t9t.base.auth.PermissionEntry;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.base.search.DescriptionList;
import com.arvatosystems.t9t.base.search.LeanGroupedSearchRequest;
import com.arvatosystems.t9t.base.search.LeanGroupedSearchResponse;
import com.arvatosystems.t9t.base.search.LeanSearchRequest;
import com.arvatosystems.t9t.base.search.LeanSearchResponse;
import com.arvatosystems.t9t.services.T9TRemoteUtils;
import com.arvatosystems.t9t.translation.services.ITranslationProvider;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;

import de.jpaw.bonaparte.api.auth.JwtConverter;
import de.jpaw.bonaparte.core.MessageParserException;
import de.jpaw.bonaparte.enums.BonaEnum;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ExceptionUtil;

/**
 * The overall Session handler.
 * @author INCI02
 */
public final class ApplicationSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationSession.class);
    private static final Permissionset NO_PERMISSIONS = Permissionset.ofTokens();

    private static Object                    lock                          = new Object();
    private static final String              SESSION_ATTRIBUTE_APPLICATIOM = "applicationSession";

    private final ITranslationProvider translationProvider = Jdp.getRequired(ITranslationProvider.class);

    private String                           jwt;  // the JWT of the current session, or null is the user has not successfully authenticated
    private String                           authorizationHeader;  // the JWT of the current session, or null is the user has not successfully authenticated
    private JwtInfo                          jwtInfo;
    private List<TenantDescription>          allowedTenants;
    private Map<Long, TenantDescription>     tenantsByRef;
    private Instant                          lastLoggedIn;
    private Instant                          passwordExpires;
    private Integer                          numberOfIncorrentAttempts;
    private Cache<String, Map<String, String>> enumTranslationCache = CacheBuilder.newBuilder().build();
    private Cache<String, List<Description>> dropdownDataCache =
            CacheBuilder.newBuilder().expireAfterWrite(15L, TimeUnit.MINUTES).build();
    private Cache<String, Map<Long, DescriptionList>> groupedDropdownDataCache =
            CacheBuilder.newBuilder().expireAfterWrite(15L, TimeUnit.MINUTES).build();
    private final ConcurrentMap<String, Permissionset> permissionCache = new ConcurrentHashMap<>(100);
    private final List<Navi>                 navis = new ArrayList<Navi>();
    private Long                             primaryTenant;

    public final List<Navi> getAllNavigations() {
        return navis;
    }

    // Session params
    private Map<String, Object> requestParams = null;

    // Map<language, Map<fieldName, translatedFieldNAme>>
    private Map<String, Map<String, String>> gridTranslations              = new HashMap<>();

    private ApplicationSession() {
    }

    /**
     * @return ApplicationSession
     */
    public static ApplicationSession get() {
        Session session = SecurityUtils.getSubject().getSession();
        ApplicationSession applicationSession = (ApplicationSession) session.getAttribute(SESSION_ATTRIBUTE_APPLICATIOM);
        if (applicationSession == null) {
            synchronized (lock) {
                applicationSession = (ApplicationSession) session.getAttribute(SESSION_ATTRIBUTE_APPLICATIOM);
                if (applicationSession == null) {
                    LOGGER.debug("Creating new ApplicationSession for Shiro session with id: {}", session.getId());
                    session.setAttribute(SESSION_ATTRIBUTE_APPLICATIOM, new ApplicationSession());
                    applicationSession = (ApplicationSession) session.getAttribute(SESSION_ATTRIBUTE_APPLICATIOM);
                }
            }
        }
        return applicationSession;
    }

    // read the menu
    public void readMenu() {
        MenuUtil.readMenuConfiguration(this, navis);
    }

    /**
     * @return is the session is valid - this means if a Security manager can be retrieved
     */
    public static boolean isSessionValid() {
        try {
            SecurityUtils.getSecurityManager();
        } catch (UnavailableSecurityManagerException e) {
            return false;
        }
        return true;
    }

    /**
     * ==================================================Request params RELATED ========================================================
     */
    /**
     * @return the requestParams
     */
    public Map<String, Object> getRequestParams() {
        if (requestParams == null) {
            return new HashMap<String, Object>();
        }
        return requestParams;
    }

    /**
     * getRequestParamsRequired
     * @return the requestParams
     * @throws IllegalArgumentException if requestParams is null --> The 'requestParams' are NULL. Please check the caller (invocation point) why NULL was passed.
     */
    public Map<String, Object> getRequestParamsRequired() {
        if (requestParams == null) {
            throw new IllegalArgumentException("The 'requestParams' are NULL. Please check the caller (invocation point) why NULL was passed. createLinkComponents(params->null)");
        }
        return requestParams;
    }

    /**
     * @param requestParams the requestParams to set
     */
    public void setRequestParams(Map<String, Object> requestParams) {
        this.requestParams = requestParams;
    }

    /**
     * ==================================================Request params RELATED ========================================================
     */


    /**
     * ================================================== GRID RELATED ========================================================
     */

    public Map<String, String> getGridTranslations(String tenantId, String language, String gridId) {
        return gridTranslations.get(tenantId + "_" + language + "_" + gridId);
    }

    public void setGridTranslations(String tenantId, String language, String gridId, Map<String, String> gridTranslations) {
        this.gridTranslations.put(tenantId + "_" + language + "_" + gridId, gridTranslations);
    }


    /**
     * ================================================== GRID RELATED ========================================================
     */

    /** Invalidates cached data for a dropdown for the current user. This allows to create new objects and then use them in a new screen.
     * Please note that dropdowns created before will not be refreshed, because the whole UI elements are cached and not created new
     * every time a screen is invoked. To get updated data for those, the user has to change language or click the tenant name.
     * @param dropdownId
     */
    public void invalidateCachedDropDownData(String dropdownId) {
        dropdownDataCache.invalidate(dropdownId);
    }

    public void invalidateCachedGroupedDropDownData(String dropdownId) {
        groupedDropdownDataCache.invalidate(dropdownId);
    }

    /** Retrieves (possibly cached) data for a dropdown. Queries the backend if the data
     * is too old or was not queried before in this session.
     * @param dropdownId
     * @param rq
     * @return
     */
    public List<Description> getDropDownData(String dropdownId, LeanSearchRequest rq) {
        List<Description> cachedData = dropdownDataCache.getIfPresent(dropdownId);
        if (cachedData != null)
            return cachedData;
        // not present, query backend
        LOGGER.info("No valid dropdown data for {} in cache, querying backend...", dropdownId);
        List<Description> resp = getDropDownData(rq);
        dropdownDataCache.put(dropdownId, resp);  // store for subsequent queries
        return resp;
    }

    /** Retrieves (possibly cached) data for a grouped dropdown. Queries the backend if the data
     * is too old or was not queried before in this session.
     * @param dropdownId
     * @param rq
     * @return
     */
    public DescriptionList getGroupedDropdownData(String dropdownId, Long group, LeanGroupedSearchRequest rq, boolean clearCached) {
        if (!clearCached) {
            Map<Long, DescriptionList> cachedData = groupedDropdownDataCache.getIfPresent(dropdownId);
            if (cachedData != null) {
                DescriptionList list =  cachedData.get(group);
                if (list == null)
                    return new DescriptionList(Collections.<Description>emptyList());
                return list;
            }
        }

        // not present, query backend
        LOGGER.info("No valid grouped dropdown data for dropdownId {} group {} in cache, querying backend...", dropdownId, group);
        Map<Long, DescriptionList> resp = getGroupedDropDownData(rq);
        groupedDropdownDataCache.put(dropdownId, resp);
        DescriptionList list = resp.get(group);
        if (list == null)
            return new DescriptionList(Collections.<Description>emptyList());
        return list;
    }

    /** Retrieves data for a dropdown without caching.
     * @param rq
     * @return
     */
    public List<Description> getDropDownData(LeanSearchRequest rq) {
        try {
            // certain dropdowns (retail stores) can have lots of entries (> 1000 at least)
            rq.setLimit(5000);
            LeanSearchResponse dropdowndataResponse = Jdp.getRequired(T9TRemoteUtils.class).executeAndHandle(rq, LeanSearchResponse.class);
            if (dropdowndataResponse.getReturnCode() != 0)
                return Collections.<Description>emptyList();
            return dropdowndataResponse.getDescriptions();
        } catch (ReturnCodeException e) {
            LOGGER.error("could not query DB for search request {}", rq.ret$PQON());
            return Collections.<Description>emptyList();
        }
    }

    /** Retrieves data for a grouped dropdown without caching.
     * @param rq
     * @return
     */
    public Map<Long, DescriptionList> getGroupedDropDownData(LeanGroupedSearchRequest rq) {
        try {
            // certain grouped dropdowns (retail stores) can have lots of entries (> 1000 at least)
            rq.setLimit(5000);
            LeanGroupedSearchResponse dropdowndataResponse = Jdp.getRequired(T9TRemoteUtils.class).executeAndHandle(rq, LeanGroupedSearchResponse.class);
            if (dropdowndataResponse.getReturnCode() != 0)
                return new HashMap<Long, DescriptionList>();
            return dropdowndataResponse.getResults();
        } catch (ReturnCodeException e) {
            LOGGER.error("could not query DB for search request {}", rq.ret$PQON());
            return new HashMap<Long, DescriptionList>();
        }
    }



    // enum restrictions - stored for language "en" only
    private final String [] STD_LANGS = new String [] { "en" };
    private final String ENUM_RESTRICTIONS_FIELD_NAME = "$enums";

    // converts a comma separated value list to a set. If the input starts with a hash, it is used as a key.
    private Set<String> csv2Set(String csv) {
        return csv == null ? null : new HashSet<String>(Arrays.asList(csv.trim().split(",")));
    }

    // direct lookup for a non-null key, such as PQON, for derived key after indirection
    private Set<String> lookupEnumsForKey(String key) {
        String setting = translationProvider.getTranslation(getTenantId(), STD_LANGS, key, ENUM_RESTRICTIONS_FIELD_NAME);
        return csv2Set(setting);
    }

    // lookup for possibly null key, or one with indirection (parameter to bon file or in zul)
    private Set<String> lookupEnums(String key) {
        if (key == null)
            return null;
        if (key.charAt(0) == '#') { // indirection:
            key = translationProvider.getTranslation(getTenantId(), STD_LANGS, key.substring(1), ENUM_RESTRICTIONS_FIELD_NAME);
            if (key == null)
                return null;
        }
        return csv2Set(key);
    }

    // intersect 2 sets, but with null mapping to the full set. Modifies s1
    private Set<String> intersect(Set<String> s1, Set<String> s2) {
        if (s1 == null)
            return s2;
        if (s2 == null)
            return s1;
        s1.retainAll(s2);
        return s1;
    }

    /** Computes the intersection of 3 restrictions, where null means no restriction, but the empty set means no possible instance. */
    public Set<String> enumRestrictions(String enumPqon, String dtoRestrictions, String zulRestrictions) {
        final Set<String> s1 = lookupEnumsForKey(enumPqon);
        final Set<String> s2 = lookupEnums(dtoRestrictions);
        final Set<String> s3 = lookupEnums(zulRestrictions);
        return intersect(intersect(s1, s2), s3);
    }

    private String userLanguage = "en";  // never null
    private String [] userLanguages = new String [] { "en" };  // languages with fallbacks
    private Locale userLocale = Locale.ENGLISH;
    private DateTimeZone userJodaTimeZone;
    private TimeZone userTimeZone;
    private ZoneId userZoneId;
    protected DateTimeFormatter dayFormat;            // day without time (Joda)
    protected DateTimeFormatter timeFormat;           // time on second precision (Joda)
    protected DateTimeFormatter timestampFormat;      // day and time on second precision (Joda)
    protected static final Map<String, String> NO_ENUM_TRANSLATIONS = ImmutableMap.<String, String>of();

    public Locale getUserLocale() {
        return userLocale;
    }

    public String translate(String path, String fieldname, Object... args) {
        String translatedText = translate(path, fieldname);
        MessageFormat format = new MessageFormat(translatedText);
        return format.format(args);
    }

    public String translate(String path, String fieldname) {
        String x = translationProvider.getTranslation(getTenantId(), userLanguages, path, fieldname);
        return x == null ? "${" + (path == null ? fieldname : path + ":" + fieldname) + "}" : x;
    }
    public String translateWithDefault(String path, String fieldname, String defaultValue) {
        String x = translationProvider.getTranslation(getTenantId(), userLanguages, path, fieldname);
        return x != null ? x : defaultValue;
    }

    // translate a single enum instance. Uses the cached whole enum translation as a subroutine.
    public String translateEnum(final BonaEnum e) {
        Map<String, String> allTranslations = translateEnum(e.ret$PQON());
        String xlation = allTranslations.get(e.name());
        LOGGER.debug("enum translation for {} and {} is {}", e.ret$PQON(), e.name(), xlation);
        return xlation == null ? e.name() : xlation;
    }

    public Map<String, String> translateEnum(final String pqon) {
        try {
            return enumTranslationCache.get(pqon, () -> translationProvider.getEnumTranslation(getTenantId(), pqon, userLanguage, true));
        } catch (ExecutionException e) {
            LOGGER.error("Problem during enum translation: {}", ExceptionUtil.causeChain(e));
            // return an empty map
            return NO_ENUM_TRANSLATIONS;
        }
    }

    protected DateTimeFormatter doDateTimeFormatter(String resource, String fallback) {
        String localizedPattern = translate("system.format", resource);
        DateTimeFormatter input =
           (localizedPattern != null && localizedPattern.charAt(0) != '$' && localizedPattern.charAt(0) != '{')
           ? DateTimeFormat.forPattern(localizedPattern) : DateTimeFormat.forStyle(fallback);
        return userJodaTimeZone == null ? input.withLocale(userLocale).withZoneUTC() : input.withLocale(userLocale).withZone(userJodaTimeZone);
    }

    private static final Map<String, String> ZONE_REPLACEMENTS = new ConcurrentHashMap<String, String>();
    static {
        ZONE_REPLACEMENTS.put("GMT+01:00", "Europe/Berlin");  // standard time (CET)
        ZONE_REPLACEMENTS.put("GMT+02:00", "Europe/Berlin");  // Daylight saving: CEST
    }

    protected void setDateFormatters(String rawLanguage, String rawZoneId) {
        String newTz = ZONE_REPLACEMENTS.get(rawZoneId);
        if (newTz != null) {
            // non-chrome browser
            LOGGER.warn("Nonstandard time zone received - probably not using Chrome browser as recommended. Replacing {} by {}",
                   rawZoneId, newTz);
            rawZoneId = newTz;
        }
        try {
            userZoneId = ZoneId.of(rawZoneId);
            userTimeZone = TimeZone.getTimeZone(userZoneId);
            userJodaTimeZone = DateTimeZone.forID(rawZoneId);
        } catch (Exception e) {
            LOGGER.error("Timezone conversion error - falling back, using UTC: {}", ExceptionUtil.causeChain(e));
            userZoneId = ZoneId.systemDefault();
            userTimeZone = TimeZone.getDefault();
            userJodaTimeZone = DateTimeZone.UTC;
        }

        userLanguage = rawLanguage == null ? "en" : rawLanguage;
        userLanguages = translationProvider.resolveLanguagesToCheck(userLanguage, true);
        userLocale = userLanguage.length() == 2 ? new Locale(userLanguage) :
            new Locale(userLanguage.substring(0, 2), userLanguage.substring(3, 5));
        LOGGER.debug("default language set to {}, language list with fallsbacks has {} entries", userLanguage, userLanguages.length);

        dayFormat = doDateTimeFormatter("day", "M-");
        timeFormat = doDateTimeFormatter("time", "-M");
        timestampFormat = doDateTimeFormatter("datetime", "MM");

        // set the session environment
        org.zkoss.zk.ui.Session current = Sessions.getCurrent();
        if (current != null) {
            current.setAttribute(org.zkoss.web.Attributes.PREFERRED_LOCALE, userLocale);
            current.setAttribute(org.zkoss.web.Attributes.PREFERRED_TIME_ZONE, userTimeZone);
            //current.setAttribute(org.zkoss.web.Attributes.PREFERRED_DATE_FORMAT_INFO, ... // see https://www.zkoss.org/javadoc/7.0.2/zk/org/zkoss/text/DateFormatInfo.html
        }
    }

    public String format(LocalDate d) {
        return dayFormat.print(d);
    }
    public String format(LocalDateTime dt) {
        DateTime ldt = dt.toDateTime(DateTimeZone.UTC);
        return timestampFormat.print(ldt);  // dt currently isnt't really a local date time, but also an instant...
    }
    public String format(LocalTime t) {
        return timeFormat.print(t);
    }
    public String format(Instant t) {
        // return timestampFormat.print(new LocalDateTime(t.getMillis(), userJodaTimeZone)); // works but is complicated
        return timestampFormat.print(t);
    }

    // ZK operates with java.util.date, provide converters which respect the time zone...
    public Date toDate(LocalDateTime ldt) {
        DateTime dt = ldt.toDateTime(userJodaTimeZone);
        return dt.toDate();
    }
    public Date toDate(Instant t) {
        DateTime dt = t.toDateTime(userJodaTimeZone);
        return dt.toDate();
    }
    public Date toDate(LocalDate t) {
        return t.toDate();
    }
//    public Date toDate(LocalTime t) {
//        return t.toDate();
//    }
    public LocalDateTime toLocalDateTime(Date t) {
        // create a localdatetime for UTC for date
        LocalDateTime ldt = new LocalDateTime(t.getTime());
        // interpret ldt as in userJodaTimeZone and convert to UTC
        return ldt;
    }
    public Instant toInstant(Date t) {
        // create an instant for a given date
        return new Instant(t.getTime());   // TODO: respect zone...
    }

    /**
     * ================================================== Boilerplate code ========================================================
     */

    public String getEncodedJwt() {
        return jwt;
    }
    public String getAuthorizationHeader() {
        return authorizationHeader;
    }
    public JwtInfo getJwtInfo() {
        return jwtInfo;
    }

    /** methods sets all of (encodedJwt, jwtInfo, and the authorizationHeader. */
    public void setJwt(String jwt) throws ApplicationException {
        dropdownDataCache.invalidateAll(); // entries relate to some tenant
        enumTranslationCache.invalidateAll();
        permissionCache.clear();
        navis.clear();
        if (jwt == null) {
            // used to disable a session.
            this.jwtInfo = null;
            this.jwt = null;
            this.authorizationHeader = null;
            invalidateSession();
            LOGGER.debug("removed authentication information from current session");
        } else {
            LOGGER.debug("Storing new Jwt in ApplicationSession");
            try {
                String [] parts = jwt.split("\\.");
                if (parts.length != 3) {
                    LOGGER.error("Received JWT does not have 3 parts: {}", jwt);
                    setJwt(null);
                    throw new MessageParserException(MessageParserException.BASE64_PARSING_ERROR);
                }
                String json = new String(Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                this.jwtInfo = JwtConverter.parseJwtInfo(json);
                this.jwt = jwt;
                this.authorizationHeader = "Bearer " + jwt;
                LOGGER.debug("received JWT with contents {} (json was {})", jwtInfo, json);
                setDateFormatters(jwtInfo.getLocale(), jwtInfo.getZoneinfo());
                invalidateInconsistentAuth(jwtInfo);
            } catch (Exception e) {
                setJwt(null);
                LOGGER.error("JWT parsing exception: {}", ExceptionUtil.causeChain(e));
            }
        }
    }

    public Long getUserRef() {
        if (jwtInfo == null)
            return null;
        return jwtInfo.getUserRef();
    }
    public String getUserId() {
        if (jwtInfo == null)
            return null;
        return jwtInfo.getUserId();
    }
    public Long getTenantRef() {
        if (jwtInfo == null)
            return null;
        return jwtInfo.getTenantRef();
    }
    public String getTenantId() {
        if (jwtInfo == null)
            return null;
        return jwtInfo.getTenantId();
    }

    public List<TenantDescription> getAllowedTenants() {
        return allowedTenants;
    }

    public TenantDescription getTenantByRef(Long ref) {
        if (ref == null)
            return null;
        return tenantsByRef.get(ref);
    }

    public void setAllowedTenants(List<TenantDescription> allowedTenants) {
        this.allowedTenants = allowedTenants;
        tenantsByRef = new ConcurrentHashMap<Long, TenantDescription>(2 * allowedTenants.size());
        for (TenantDescription e: allowedTenants)
            tenantsByRef.put(e.getTenantRef(), e);
    }

    public Instant getLastLoggedIn() {
        return lastLoggedIn;
    }

    public void setLastLoggedIn(Instant lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
    }

    public Instant getPasswordExpires() {
        return passwordExpires;
    }

    public void setPasswordExpires(Instant passwordExpires) {
        this.passwordExpires = passwordExpires;
    }

    public Integer getNumberOfIncorrentAttempts() {
        return numberOfIncorrentAttempts;
    }

    public void setNumberOfIncorrentAttempts(Integer numberOfIncorrentAttempts) {
        this.numberOfIncorrentAttempts = numberOfIncorrentAttempts;
    }

    public void storePermissions(List<PermissionEntry> perms) {
        permissionCache.clear();
        navis.clear();
        for (PermissionEntry pe: perms) {
            pe.freeze();
            permissionCache.put(pe.getResourceId(), pe.getPermissions());
            LOGGER.debug("Storing permission {} as {}", pe.getResourceId(), pe.getPermissions());
        }
    }

    /** Returns the permissions for the user on resourceId. A "U." prefix for "UI" is automatically added to the resorceId. */
    public Permissionset getPermissions(String resourceId) {
        // ad the prefix
        resourceId = PermissionType.FRONTEND.getToken() + "." + resourceId;
        // if no permission is obtains, try supersets
        for (;;) {
            Permissionset perms = permissionCache.get(resourceId);
            if (perms != null)
                return perms;
            int length = resourceId.length() - 1;
            if (length < 1)
                break;
            if (resourceId.charAt(length) == '.')  // if the last char was a dot, search before it
                --length;
            int lastDot = resourceId.lastIndexOf('.', length);
            if (lastDot < 0)
                break;
            resourceId = resourceId.substring(0, lastDot+1);  // include the dot - wildcard permissions are stored with trailing dot
        }
        return NO_PERMISSIONS;
    }

    protected SearchFilter filterForPresetSearch = null;

    public SearchFilter getFilterForPresetSearch() {
        SearchFilter tmp = filterForPresetSearch;
        filterForPresetSearch = null;   // clear it
        return tmp;
    }

    public void setFilterForPresetSearch(SearchFilter filterForPresetSearch) {
        this.filterForPresetSearch = filterForPresetSearch;
    }

    protected SearchFilter filterForPresetSearchOnDirect28 = null;

    public SearchFilter getFilterForPresetSearchOnDirect28() {
        SearchFilter tmp = filterForPresetSearchOnDirect28;
        filterForPresetSearchOnDirect28 = null;   // clear it
        return tmp;
    }

    public void setFilterForPresetSearchOnDirect28(SearchFilter filterForPresetSearchOnDirect28) {
        this.filterForPresetSearchOnDirect28 = filterForPresetSearchOnDirect28;
    }

    /**
     * Fetch logo from DB
     *
     * @return
     */
    public AImage getTenantLogo() {
        try {
            GetTenantLogoResponse response = Jdp.getRequired(T9TRemoteUtils.class)
                    .executeAndHandle(new GetTenantLogoRequest(), GetTenantLogoResponse.class);
            if (response.getTenantLogo() != null) {
                try {
                    return new AImage("logo", response.getTenantLogo().getRawData().getBytes());
                } catch (IOException e) {
                    LOGGER.error("unable to convert rawData to AImage: {}", e.getMessage());
                }
            }
        } catch (ReturnCodeException e) {
            LOGGER.error("could not query DB for search request {}", GetTenantLogoRequest.meta$$this.ret$PQON());
        }

        // fallback using logo.img
        String path = ZulUtils.i18nLabel("logo.img");
        LOGGER.info("Logo path: {} ", path);

        try {
            return new AImage(new URL(path));
        } catch (IOException e) {
            LOGGER.error("unable to get logo from the path: {} with error {}", path, e.getMessage());
        }

        return null;
    }

    public String getTenantResource(String resource) {
        if (this.jwtInfo != null && this.jwtInfo.getZ() != null) {
            String cssSelector = (String) this.jwtInfo.getZ().get("cssSelector");
            if (cssSelector != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < resource.length(); i++) {
                    char c = resource.charAt(i);
                    if (c == '.') {
                        sb.append(cssSelector);
                    }
                    sb.append(c);
                }
                return sb.toString();
            }
        }

        return resource;
    }


    /**
     * Logout the current (shiro) authenticated user
     */
    private void invalidateSession() {
        try {
            if (SecurityUtils.getSubject() != null && SecurityUtils.getSubject().isAuthenticated()) {
                SecurityUtils.getSubject().logout();
            }
        } catch (Exception e) {
            LOGGER.error("Unable to logout the user due to: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * This method check if the logged in user is consistent with the user in token (to avoid security issue)
     * @param jwtInfo
     */
    private void invalidateInconsistentAuth(JwtInfo jwtInfo) {
        if (SecurityUtils.getSubject() != null && SecurityUtils.getSubject().getPrincipal() != null) {
            String shiroAuthUser = SecurityUtils.getSubject().getPrincipal().toString();
            if (jwtInfo.getUserId() == null ||  !shiroAuthUser.equals(jwtInfo.getUserId())) {
                invalidateSession();
            }
        }
    }

}
