/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.Binder;
import org.zkoss.bind.impl.BinderUtil;
import org.zkoss.lang.Library;
import org.zkoss.text.MessageFormats;
import org.zkoss.util.Locales;
import org.zkoss.util.resource.Labels;
import org.zkoss.web.Attributes;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.impl.InputElement;

import com.arvatosystems.t9t.authc.api.TenantDescription;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.init.InitContainers;
import com.arvatosystems.t9t.tfi.general.Constants;
import com.arvatosystems.t9t.tfi.model.bean.ComboBoxItem;
import com.arvatosystems.t9t.tfi.model.bean.ErrorPopupEntity;
import com.arvatosystems.t9t.tfi.services.ReturnCodeException;
import com.google.common.collect.ImmutableList;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.bonaparte.pojos.meta.EnumDefinition;
import de.jpaw.bonaparte.util.ToStringHelper;
import de.jpaw.enums.TokenizableEnum;
import de.jpaw.util.ExceptionUtil;

/**
 * Util for Searchbuild and i18n.
 *
 * @author INCI02
 */
public class ZulUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZulUtils.class);

    public static void findErrorMessages(final Component component) {
        if (component instanceof InputElement) {
            final InputElement element = (InputElement) component;
            if (StringUtils.isNotBlank(element.getErrorMessage())) {
                LOGGER.debug("Component:{} - ErrorMessage:{}", component, element.getErrorMessage());
            }
        }
        for (final Component childComponent : component.getChildren()) {
            findErrorMessages(childComponent);
        }
    }

    public static void debugOutput(final Component component, int level) {
        LOGGER.debug("{}:{}", StringUtils.leftPad(String.valueOf(level), level*4), component);
        level++;
        for (final Component childComponent : component.getChildren()) {
            debugOutput(childComponent, level);
        }
    }


    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    /*  translations should use the standard (going via ApplicationSession translator)
    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

    public static boolean isI18nLabelNull(final String label) {
        if (label == null) {
            return false;
        }
        return label.charAt(0) == '{';
    }
    /**
     * @param label
     *        String
     * @return String
     */
    public static String newI18nLabel(final String label) {
        return newI18nDefaultLabel(label, null);
    }

    /**
     * i18nDefaultLabel.
     * @param label
     *        String
     * @param defaultValue
     *        String
     * @return String
     *
     * Just a shorthand for the translation in the application session
     */
    public static String newI18nDefaultLabel(final String label, final String defaultValue) {
        final String result = ApplicationSession.get().translateWithDefault(null, label, defaultValue);
        LOGGER.debug("i18nDefaultLabel({}) with default {} returns {}", label, defaultValue, result);
        return result != null ? result : "{" + label + "}";
    }

    /**
     * @param label
     *        String
     * @param args
     *        String
     * @return String
     */
    public static String newI18nLabel(final String label, final String args) {
        final Object[] arguments = args == null ? null : args.replace("{", "").replace("}", "").split(",");

        return newI18nLabel(label, arguments);
    }

    public static String newI18nLabel(final String label, final Object[] arguments) {
        final String result1 = newI18nDefaultLabel(label, null);
        if (arguments == null || arguments.length == 0)
            return result1;
        final Locale userLocale = ApplicationSession.get().getUserLocale();
        return MessageFormats.format(result1, arguments, userLocale);
    }

    /**
     * translation a key without a path.
     * @param key
     * @return String
     */
    public static String translate(final String key) {
        return translate(null, key);
    }

    /**
     * translate a key with a path
     * @param path
     * @param key
     * @return String
     */
    public static String translate(final String path, final String key) {
        return translate(path, key, "");
    }

    /**
     * translate a key with path with stringify arguments
     * @param path
     * @param key
     * @param args
     * @return
     */
    public static String translate(final String path, final String key, final String args) {
        final Object[] arguments = args == null || args == "" ? null : args.replace("{", "").replace("}", "").split(",");
        return translate(path, key, arguments);
    }

    public static String translate(final String path, final String key, final Object[] arguments) {
        final String result = ApplicationSession.get().translate(path, key, arguments);
        LOGGER.debug("translate({}) with path {} with arguments {} returns {}", key, path, arguments, result);
        return result != null ? result : "{" + key + "}";
    }

    public static final ConcurrentMap<String, Object> MISSING_TRANSLATIONS = new ConcurrentHashMap<String, Object>(100);
    public static void dumpMissingTranslations() {
        for (final String s: MISSING_TRANSLATIONS.keySet())
            System.out.println(s);
    }

    /**
     * Read config by key from configuration property file
     * @param key
     * @return
     */
    public static String readConfig(final String key) {
        return Labels.getLabel(key);
    }

    /**
     * Read boolean config by key from configuration property file
     * @param key
     * @return
     */
    private static final Map<String, Boolean> booleanConfigCache = new ConcurrentHashMap<>(20);

    public static boolean readBooleanConfig(final String key) {
        return booleanConfigCache.computeIfAbsent(key, k -> {
            Boolean result = Boolean.FALSE;
            final String configInString = ZulUtils.readConfig(k);
            if (configInString != null) {
                result = Boolean.valueOf(configInString);
            }
            LOGGER.info("Evaluated boolean config property {} to be {}", key, result);
            return result;
        });
    }

    /**
     * Read tenant config by key from configuration property file
     * use current tenant as key, fallback to @ if not found
     */
    public static String readTenantConfig(final String key) {
        final String currentTenantId = ApplicationSession.get().getTenantId();
        String value = null;
        if (currentTenantId != null) {
            value = readConfig(currentTenantId + ":" + key);
        }
        // fallback to @
        if (value == null) {
            value = readConfig("@:" + key);
        }
        return value;
    }

    /**
     * @param tenantRef
     * @return
     */
    @Deprecated
    public static String getTenantIdByRef(final Long tenantRef) {
        if (T9tConstants.GLOBAL_TENANT_REF42.equals(tenantRef)) {
            return T9tConstants.GLOBAL_TENANT_ID;
        }
        final List<TenantDescription> tenants = ApplicationSession.get().getAllowedTenants();
        for (final TenantDescription td : tenants) {
            if (td.getTenantRef().equals(tenantRef))
                return td.getTenantId();
        }
        LOGGER.warn("Mapping to ref {} to undisclosed tenant", tenantRef); // should not happen, it means the user has selected data to which no permissions exist
        return "?";  // undisclosed...
    }

    @Deprecated // only used by some not yet updated pricing module screens
    public static Object[] createEnumComboModelByProperty(final String enumPQON, final Class<?> enumClazz, final Boolean consistEmptyItem,
            final String... hardAvailableEnum) {
        Object[] comboModel = null;
        try {
            final ArrayList<Object> filteredEnumsList = new ArrayList<Object>();
            final Set<String> filteredEnumSet = new HashSet<>();

            if (hardAvailableEnum != null && hardAvailableEnum.length > 0) {
                for (int i = 0; i < hardAvailableEnum.length; i++) {
                    filteredEnumSet.add(hardAvailableEnum[i]);
                }
            } else {
                // If HardAvailableEnum is not provided, get all the values in
                // the enum instead
                final Field idsField = enumClazz.getDeclaredField("_ids");
                idsField.setAccessible(true);
                final Object _ids = idsField.get(enumClazz);
                LOGGER.debug("_ids retrieved. ");
                for (final String s : (ImmutableList<String>) _ids) {
                    LOGGER.debug("_ids loop {}", s);
                    filteredEnumSet.add(s);
                }
            }

            final Map<String, String> translations = ApplicationSession.get().translateEnum(enumPQON);

            final Method factoryMethod = enumClazz.getMethod("factory", String.class);

            final EnumDefinition enumDefinition = InitContainers.getEnumByPQON(enumPQON);
            LOGGER.debug("enumDefinition ids={}, tokens = {}", enumDefinition.getIds(), enumDefinition.getTokens());

            final Map<String, String> idToTokenMap = new HashMap<String, String>();
            for (int i = 0; i < enumDefinition.getIds().size(); i++) {
                idToTokenMap.put(enumDefinition.getIds().get(i), enumDefinition.getTokens().get(i));
            }

            LOGGER.debug("Tranlated enums for pqon {} is {}", enumPQON, translations);
            if (translations != null) {
                translations.entrySet().forEach(entry -> {
                    if (filteredEnumSet.contains(entry.getKey())) { // only
                                                                    // appear in
                                                                    // both it
                                                                    // will be
                                                                    // displayed
                        final ComboBoxItem cbi = new ComboBoxItem(entry.getValue(), idToTokenMap.get(entry.getKey()));
                        LOGGER.debug("Added combobox item where name =  {}, value =  {}", cbi.getName(),
                                cbi.getValue());
                        try {
                            filteredEnumsList.add(factoryMethod.invoke(enumClazz, idToTokenMap.get(entry.getKey())));
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            LOGGER.error("ERROR in createEnumComboModelByProperty: enumPQON={}, {}", enumPQON,
                                    ExceptionUtil.causeChain(e));
                        }
                    }
                });
            }
            if (consistEmptyItem) {
                filteredEnumsList.add(null);
            }
            comboModel = filteredEnumsList.toArray();
        } catch (final Exception e) {
            LOGGER.error("ERROR in createEnumComboModelByProperty: enumPQON={}, {}", enumPQON,
                    ExceptionUtil.causeChain(e));
            return null;
        }
        return comboModel;
    }


    // Using reflection results in really bad performance. This is why this class has been deprecated.
    @Deprecated
    private static boolean isEnum(final String classAsString) {
        try {
            final Class<?> cls = Class.forName(classAsString);
            // cls.isEnum() can't be used because of the xenums
            return TokenizableEnum.class.isAssignableFrom(cls);
        } catch (final Exception e) {
            return false;
        }
    }

    @Deprecated // used by very old screens only! Use ApplicationSession.translate() instead.
    public static String getLabelByKey(final String comboClass, final String key) {
        if (isEnum(comboClass)) {
            LOGGER.warn("#### using outdated enum translation for {} / {}", comboClass, key);
            return "##FIXME##";  // old enum translation is no longer supported

        } else {
            LOGGER.debug("#### getLabelByKey({}, {})  Not an enum!!", comboClass, key);
            final String propertyValues = ZulUtils.readConfig(comboClass);
            final String[] listOfItems = propertyValues.split("\n");
            for (int i = 0; i < listOfItems.length; i++) {
                final String[] keyValue = listOfItems[i].split("=");
                if (keyValue[0].trim().equals(key)) {
                    String name = null;
                    if (keyValue.length < 2) {
                        // this will be the wrong value but it will be displayed. So the developer can see that something is not translated
                        name = keyValue[0].trim();
                    } else {
                        name = keyValue[1].trim();
                    }
                    return name;
                }
            }
            return key;
        }
    }

    @Deprecated  // only used by not yet updated pricing screens
    public static String getEnumTokenTranslationByPQON(final String enumPQON, String key) {
        LOGGER.debug("getEnumTokenTranslationByPQON({}, {})", enumPQON, key);

        if (key != null && enumPQON != null && !key.isEmpty() && !enumPQON.isEmpty()) {
            final Map<String, String> translations = ApplicationSession.get().translateEnum(enumPQON);
            LOGGER.debug("translations {}", translations);
            final EnumDefinition enumDefinition = InitContainers.getEnumByPQON(enumPQON);
            LOGGER.debug("enumDefinition ids={}, tokens = {}", enumDefinition.getIds(), enumDefinition.getTokens());

            final Map<String, String> idToTokenMap = new HashMap<String, String>();
            for (int i = 0; i < enumDefinition.getIds().size(); i++) {
                idToTokenMap.put(enumDefinition.getTokens().get(i), enumDefinition.getIds().get(i));
            }

            key = translations.get(idToTokenMap.get(key));
        }
        return key;
    }


    // old method, still used as of 3.2.0
    public static void resizeComponent(final Component componentToBeResized, final String vflexMin, final String vflexMax, final Component fireResizeNotificationToComponent, final boolean setToMaxVflex) throws Exception {
       //Clients.alert("onOpen: self.isOpen(): "+ self.isOpen());
       final HtmlBasedComponent componentToBeResizedHtml = (HtmlBasedComponent) componentToBeResized;
        if (setToMaxVflex) { // set to max
            componentToBeResizedHtml.setVflex(vflexMax);
            Clients.resize(fireResizeNotificationToComponent);
            Thread.sleep(200l);
        } else { // set to min
            componentToBeResizedHtml.setVflex(vflexMin);
            Thread.sleep(200l);
            Clients.resize(fireResizeNotificationToComponent);
            Thread.sleep(100l);
        }
        Clients.resize(fireResizeNotificationToComponent);
    }

    public static DecimalFormat getLocalizedDecimalFormat(final String pattern) {
        final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locales.getCurrent());
        df.applyPattern(pattern);
        return df;
    }

    public static String getDefaultLanguageCode() {
        final Session current = Sessions.getCurrent();
        if ((current != null) && (current.getAttribute(Attributes.PREFERRED_LOCALE) != null)) {
            final Locale locale = (Locale) current.getAttribute(Attributes.PREFERRED_LOCALE);
            return locale.toString();
        } else {
            return Library.getProperty(Attributes.PREFERRED_LOCALE);
        }
    }

    public static String join(final List<String> list, final String separator) {
        return list != null ? StringUtils.join(list.toArray(new String[list.size()]), separator) : null;
    }

    public static String convertToString(final Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    public static void download(final String file) {
        try {
            Filedownload.save(new File(file), null);
        } catch (final FileNotFoundException e) {
            Messagebox.show(e.getMessage());
        }
    }

    public static HashMap<?,?> asMap(final Object arg) {
        if ((arg == null) || !(arg instanceof HashMap)) {
            throw new IllegalArgumentException("Error getting argument HashMap<>. Passed arg is not instanceof java.util.HashMap. It is " + arg);
        }
        return new HashMap<>(((HashMap<?, ?>) arg));
    }

    public static String bonaPortableToStringMultiline(final BonaPortable bonaPortable) {
        if (bonaPortable == null) {
            return null;
        }
        final int maxList = ToStringHelper.maxList;
        final int maxMap = ToStringHelper.maxMap;
        final int maxSet = ToStringHelper.maxSet;
        // set to unlimited
        ToStringHelper.maxList = -1;
        ToStringHelper.maxMap = -1;
        ToStringHelper.maxSet = -1;

        final String readableBonaPortable = ToStringHelper.toStringML(bonaPortable);

        // restore old values
        ToStringHelper.maxList = maxList;
        ToStringHelper.maxMap = maxMap;
        ToStringHelper.maxSet = maxSet;
        return readableBonaPortable;
    }

    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Error Popup Handling~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    //    public static String errorPopupTitle(ReturnCodeException returnCodeException) {
    //        String titleRetunValue = ZulUtils.i18nLabel("err.title");
    //        String returnCodePrefix;
    //        if (null == returnCodeException) {
    //            return titleRetunValue;
    //        }
    //        try {
    //            returnCodePrefix = String.valueOf(returnCodeException.getReturnCode());
    //            returnCodePrefix = returnCodePrefix.substring(0, 1);
    //            titleRetunValue = readErrorPopupConfiguration(returnCodePrefix).getPopupTitle();
    //        } catch (Exception e) {
    //            LOGGER.error("ERROR: {}", e);
    //            return titleRetunValue;
    //        }
    //        return titleRetunValue;
    //    }

    //
    //    public static String errorPopupErrorIntroduction(ReturnCodeException returnCodeException) {
    //        String errorIntroductionRetunValue = null;
    //        String returnCodePrefix;
    //        if (null == returnCodeException) {
    //            return errorIntroductionRetunValue;
    //        }
    //        try {
    //            returnCodePrefix = String.valueOf(returnCodeException.getReturnCode());
    //            returnCodePrefix = returnCodePrefix.substring(0, 1);
    //            errorIntroductionRetunValue = readErrorPopupConfiguration(returnCodePrefix).getErrorIntroduction();
    //        } catch (Exception e) {
    //            LOGGER.error("ERROR: {}", e);
    //            return errorIntroductionRetunValue;
    //        }
    //        return errorIntroductionRetunValue;
    //    }
    //
    public static ErrorPopupEntity getErrorPopupInfo(final ReturnCodeException returnCodeException) {
        return getErrorPopupInfo(returnCodeException, null);
    }

    public static ErrorPopupEntity getErrorPopupInfo(final ReturnCodeException returnCodeException, final String inScreenId) {
        ErrorPopupEntity returnValue = null;
        String returnCodePrefix;
        if (null == returnCodeException) {
            return returnValue;
        }
        try {
            //
            returnCodePrefix = String.valueOf(returnCodeException.getReturnCode());
            returnCodePrefix = returnCodePrefix.substring(0, 1);
            returnValue = readErrorPopupConfiguration(returnCodePrefix, inScreenId);

            if (returnValue == null) {
                LOGGER.error("ERROR: "+returnCodeException.getReturnMessage());
                returnValue = new ErrorPopupEntity();
                returnValue.setPopupTitle(ZulUtils.translate("err", "title"));
                returnValue.setPopupImg("~./zul/img/msgbox/stop-btn.png");
                returnValue.setReturnCode(String.valueOf(returnCodeException.getReturnCode()));
                returnValue.setReturnMessage(returnCodeException.getReturnMessage());
                returnValue.setErrorDetails(returnCodeException.getErrorDetails());
            } else {
                returnValue.setReturnCode(String.valueOf(returnCodeException.getReturnCode()));
                returnValue.setReturnMessage(returnCodeException.getReturnMessage());
                returnValue.setErrorDetails(returnCodeException.getErrorDetails());
            }

        } catch (final Exception e) {
            LOGGER.error("ERROR in getErrorPopupInfo: screenId={}, retCodeEx={}\nNew exception= {}",
                    inScreenId,
                    ExceptionUtil.causeChain(returnCodeException),
                    ExceptionUtil.causeChain(e));
            returnValue = new ErrorPopupEntity();
            returnValue.setPopupTitle(ZulUtils.translate("err","title"));
            returnValue.setPopupImg("~./zul/img/msgbox/stop-btn.png");
            returnValue.setReturnCode(String.valueOf(Constants.ErrorCodes.GENERAL_EXCEPTION));
            returnValue.setReturnMessage("general error");
        }
        return returnValue;
    }

    private static ErrorPopupEntity readErrorPopupConfiguration(final String returnCodePrefix, final String inScreenId) {
        ErrorPopupEntity nonScreenIdBasedConfig = null;
        nonScreenIdBasedConfig = null;

        final String[] menuConfigurations = ZulUtils.readConfig("error.popup.config").split("\\s*,\\s*"); // trim and split each element

        //String[] errorPopupConfigurations = ZulUtils.i18nLabel("err.popup.config").split("\n");
        if (menuConfigurations[0].trim().equals("")) {
            return null;
        } else {
            for (final String menuConfigKey : menuConfigurations) {
                final String menuConfig = ZulUtils.readConfig("error.popup." + menuConfigKey);
                final String[] menuLines = menuConfig.split("\n");
                for (final String errorPopupConfigLine : menuLines) {
                    // LOGGER.debug("Error poup lines: {} ",
                    // errorPopupConfigLine);
                    final Object[] errorPopupConfigItems = errorPopupConfigLine.split("\\s*,\\s*");

                    final String screenId = String.valueOf(errorPopupConfigItems[0]).equals("NULL") ? null
                            : String.valueOf(errorPopupConfigItems[0]);
                    final String returnCode = String.valueOf(errorPopupConfigItems[1]).equals("NULL") ? null
                            : String.valueOf(errorPopupConfigItems[1]);
                    final String popupTitle = String.valueOf(errorPopupConfigItems[2]).equals("NULL") ? null
                            : String.valueOf(errorPopupConfigItems[2]);
                    final String poupImage = String.valueOf(errorPopupConfigItems[3]).equals("NULL") ? null
                            : String.valueOf(errorPopupConfigItems[3]);
                    final String introduction = String.valueOf(errorPopupConfigItems[4]).equals("NULL") ? null
                            : String.valueOf(errorPopupConfigItems[4]);

                    if ((screenId != null) && (inScreenId != null) && screenId.equals(inScreenId)
                            && returnCodePrefix.equals(returnCode)) {
                        return new ErrorPopupEntity(returnCode, popupTitle, poupImage, introduction);
                    } else if ((screenId == null) && returnCodePrefix.equals(returnCode)) {
                        nonScreenIdBasedConfig = new ErrorPopupEntity(returnCode, popupTitle, poupImage, introduction);
                    }

                }
            }

        }
        return nonScreenIdBasedConfig;
    }
    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

    private static boolean isPermitted(final String permissionKey, final OperationType operation) {
        final Permissionset perms = ApplicationSession.get().getPermissions(permissionKey);
        return perms != null && perms.contains(operation);
    }
    @Deprecated // only used by old hotbucket screen
    public static boolean isCreatePermitted(final String permissionKey) {
        return isPermitted(permissionKey, OperationType.CREATE);
    }

    @Deprecated // only used by old clarification screen
    public static boolean isUpdatePermitted(final String permissionKey) {
        return isPermitted(permissionKey, OperationType.UPDATE);
    }

    public static Object getViewModel(final Component component) {
        final Binder binderListbox = BinderUtil.getBinder(component, true);
        return binderListbox.getViewModel();
    }
}
