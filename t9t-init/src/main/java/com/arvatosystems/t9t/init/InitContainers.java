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
package com.arvatosystems.t9t.init;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.ILeanGridConfigContainer;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.base.MessagingUtil;

import de.jpaw.bonaparte.enums.BonaEnum;
import de.jpaw.bonaparte.enums.BonaNonTokenizableEnum;
import de.jpaw.bonaparte.enums.BonaTokenizableEnum;
import de.jpaw.bonaparte.pojos.meta.EnumDefinition;
import de.jpaw.bonaparte.pojos.meta.EnumSetDefinition;
import de.jpaw.bonaparte.pojos.meta.XEnumSetDefinition;
import de.jpaw.bonaparte.util.ToStringHelper;
import de.jpaw.enums.AbstractStringEnumSet;
import de.jpaw.enums.AbstractStringXEnumSet;
import de.jpaw.util.ExceptionUtil;
import de.jpaw.xenums.init.ExceptionInitializer;
import de.jpaw.xenums.init.ReflectionsPackageCache;
import de.jpaw.xenums.init.XenumInitializer;

public class InitContainers {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitContainers.class);

    private static final Map<String, EnumDefinition>     enumByPqon     = new HashMap<String, EnumDefinition>(500);
    private static final Map<String, EnumSetDefinition>  enumsetByPqon  = new HashMap<String, EnumSetDefinition>(500);
    private static final Map<String, XEnumSetDefinition> xenumsetByPqon = new HashMap<String, XEnumSetDefinition>(500);

    public static EnumDefinition getEnumByPQON(String pqon) {
        return enumByPqon.get(pqon);
    }

    public static EnumSetDefinition getEnumsetByPQON(String pqon) {
        return enumsetByPqon.get(pqon);
    }

    public static XEnumSetDefinition getXEnumsetByPQON(String pqon) {
        return xenumsetByPqon.get(pqon);
    }

    public static void checkUTC() {
        final String oldId = ZoneId.systemDefault().getId();
        if (!"GMT".equals(oldId)) {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
            LOGGER.info("Setting time zone to GMT (UTC) (was {} before)", oldId);
        } else {
            LOGGER.info("Time zone already set to GMT - good");
        }
    }

    /** Full initialization - for backend server or UI. */
    public static Reflections [] initializeT9t() {
        UiGridConfigPrefs.reset();  // clear all view model containers and reset error counter

        final Reflections [] scannedPackages = initializeT9tForClients();
        collectCrudViewModels(scannedPackages);
        LOGGER.info("{} crudViewModels found", IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.size());

        collectLeanGridConfigurations(scannedPackages);
        LOGGER.info("{} lean grid configurations found", ILeanGridConfigContainer.LEAN_GRID_CONFIG_REGISTRY.size());
        LOGGER.debug("lean grid configurations are {}", ToStringHelper.toStringML(ILeanGridConfigContainer.LEAN_GRID_CONFIG_REGISTRY.keySet()));

        return scannedPackages;
    }

    /** Subset of the initialization - for remote tests. */
    public static Reflections [] initializeT9tForClients() {
        final String javaVersion = System.getProperty("java.version");
        final String javaVendor  = System.getProperty("java.vendor");
        LOGGER.info("Running Java version {} (vendor {})", javaVersion != null ? javaVersion : "(UNDEFINED)", javaVendor != null ? javaVendor : "(UNDEFINED)");

        checkUTC();
        MessagingUtil.initializeBonaparteParsers();
        final Reflections [] scannedPackages = ReflectionsPackageCache.getAll(MessagingUtil.PACKAGES_TO_SCAN_FOR_XENUMS);

        ExceptionInitializer.initializeExceptionClasses(scannedPackages);       // init all 3 packages with one invocation
        XenumInitializer.initializeXenums(scannedPackages);

        collectEnums(scannedPackages);
        LOGGER.info("{} enums found", enumByPqon.size());

        collectStringEnumsets(scannedPackages);
        LOGGER.info("{} enumsets found", enumsetByPqon.size());

        collectXEnumsets(scannedPackages);
        LOGGER.info("{} xenumsets found", xenumsetByPqon.size());
        return scannedPackages;
    }

    private static void collectEnums(Reflections ... packages) {
        for (Reflections pkg: packages) {
            // we search separately because just looking for BonaEnum (which should work) does not give any results...
            collectEnums(pkg, BonaTokenizableEnum.class);
            collectEnums(pkg, BonaNonTokenizableEnum.class);
            // collectEnums(pkg, BonaEnum.class); // sometimes we get duplicates, sometimes not...
        }
    }

    private static void collectEnums(Reflections pkg, Class<? extends BonaEnum> subTypesOf) {
        int counter = 0;
        for (Class<? extends BonaEnum> cls : pkg.getSubTypesOf(subTypesOf)) {
            if (!cls.isInterface()) {
                // skip the base interfaces itself
                try {
                    Method method = cls.getMethod("enum$MetaData");
                    Object o = method.invoke(null);
                    if (o != null && o instanceof EnumDefinition) {
                        ++counter;
                        EnumDefinition def = (EnumDefinition)o;
                        EnumDefinition prev = enumByPqon.put(def.getName(), def);
                        if (prev != null)
                            LOGGER.error("2 different enums of same PQON {}", def.getName());
                    } else {
                        LOGGER.error("Could not obtain EnumDefinition for class {}", cls.getCanonicalName());
                    }
                } catch (Exception e) {
                    LOGGER.warn("Exception obtaining EnumDefinition from {}: {}", cls.getCanonicalName(), ExceptionUtil.causeChain(e));
                }
            }
        }
        LOGGER.debug("Found {} instances of enum subtype {}", counter, subTypesOf.getSimpleName());
    }

    private static void collectStringEnumsets(Reflections ... packages) {
        int counter = 0;
        for (Reflections pkg: packages) {
            for (Class<? extends AbstractStringEnumSet> cls : pkg.getSubTypesOf(AbstractStringEnumSet.class)) {
                try {
                    Method method = cls.getMethod("enumset$MetaData");
                    Object o = method.invoke(null);
                    if (o != null && o instanceof EnumSetDefinition) {
                        ++counter;
                        EnumSetDefinition def = (EnumSetDefinition)o;
                        EnumSetDefinition prev = enumsetByPqon.put(def.getName(), def);
                        if (prev != null)
                            LOGGER.error("2 different enumsets of same PQON {}", def.getName());
                    } else {
                        LOGGER.error("Could not obtain EnumSetDefinition for class {}", cls.getCanonicalName());
                    }
                } catch (Exception e) {
                    LOGGER.warn("Exception obtaining EnumSetDefinition from {}: {}", cls.getCanonicalName(), ExceptionUtil.causeChain(e));
                }
            }
        }
        LOGGER.debug("Found {} instances of enumset", counter);
    }

    private static void collectXEnumsets(Reflections ... packages) {
        int counter = 0;
        for (Reflections pkg: packages) {
            for (Class<? extends AbstractStringXEnumSet> cls : pkg.getSubTypesOf(AbstractStringXEnumSet.class)) {
                try {
                    Method method = cls.getMethod("xenumset$MetaData");
                    Object o = method.invoke(null);
                    if (o != null && o instanceof XEnumSetDefinition) {
                        ++counter;
                        XEnumSetDefinition def = (XEnumSetDefinition)o;
                        XEnumSetDefinition prev = xenumsetByPqon.put(def.getName(), def);
                        if (prev != null)
                            LOGGER.error("2 different xenumsets of same PQON {}", def.getName());
                    } else {
                        LOGGER.error("Could not obtain XEnumSetDefinition for class {}", cls.getCanonicalName());
                    }
                } catch (Exception e) {
                    LOGGER.warn("Exception obtaining XEnumSetDefinition from {}: {}",
                            cls.getCanonicalName(), ExceptionUtil.causeChain(e));
                }
            }
        }
        LOGGER.debug("Found {} instances of xenumset", counter);
    }

    private static void collectCrudViewModels(Reflections ... packages) {
        for (Reflections pkg: packages) {
            for (Class<? extends IViewModelContainer> cls : pkg.getSubTypesOf(IViewModelContainer.class)) {
                try {
                    LOGGER.debug("Found viewModel container {}", cls.getCanonicalName());
                    // Class.forName(cls.getCanonicalName()); // initialize class
                    cls.newInstance();     // may be faster than searching again
                } catch (Exception e) {
                    LOGGER.warn("Cannot initialize viewModelContainer {}: {}", cls.getCanonicalName(), ExceptionUtil.causeChain(e));
                }
            }
        }
    }

    private static void collectLeanGridConfigurations(Reflections ... packages) {
        for (Reflections pkg: packages) {
            for (Class<? extends ILeanGridConfigContainer> cls : pkg.getSubTypesOf(ILeanGridConfigContainer.class)) {
                try {
                    List<String> configs = cls.newInstance().getResourceNames();
                    LOGGER.debug("Grid config container {} holds {} lean grid configurations", cls.getCanonicalName(), configs.size());
                    for (String resourceId : configs) {
                        UiGridConfigPrefs.getLeanGridConfigAsObject(resourceId);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Cannot initialize leanGridConfigContainer {}: {}", cls.getCanonicalName(), ExceptionUtil.causeChain(e));
                }
            }
        }
    }

    public static Set<Class<?>> getClassesAnnotatedWith(Class<? extends Annotation> annotation) {
        final Reflections t9t = ReflectionsPackageCache.get(MessagingUtil.TWENTYEIGHT_PACKAGE_PREFIX);
        return t9t.getTypesAnnotatedWith(annotation);
    }
}
