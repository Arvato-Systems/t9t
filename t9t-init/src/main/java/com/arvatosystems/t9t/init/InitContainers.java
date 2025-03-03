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
package com.arvatosystems.t9t.init;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.ILeanGridConfigContainer;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.MutableInt;
import com.arvatosystems.t9t.base.RandomNumberGenerators;
import com.arvatosystems.t9t.cfg.Packages;

import de.jpaw.bonaparte.enums.BonaEnum;
import de.jpaw.bonaparte.enums.BonaNonTokenizableEnum;
import de.jpaw.bonaparte.enums.BonaTokenizableEnum;
import de.jpaw.bonaparte.pojos.meta.EnumDefinition;
import de.jpaw.bonaparte.pojos.meta.EnumSetDefinition;
import de.jpaw.bonaparte.pojos.meta.XEnumSetDefinition;
import de.jpaw.bonaparte.util.ToStringHelper;
import de.jpaw.enums.AbstractStringEnumSet;
import de.jpaw.enums.AbstractStringXEnumSet;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ExceptionUtil;
import de.jpaw.xenums.init.ExceptionInitializer;
import de.jpaw.xenums.init.ReflectionsPackageCache;
import de.jpaw.xenums.init.XenumInitializer;

public final class InitContainers {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitContainers.class);

    private static final Map<String, EnumDefinition>     ENUM_BY_PQON     = new HashMap<>(500);
    private static final Map<String, EnumSetDefinition>  ENUMSET_BY_PQON  = new HashMap<>(500);
    private static final Map<String, XEnumSetDefinition> XENUMSET_BY_PQON = new HashMap<>(500);

    private InitContainers() { }

    public static EnumDefinition getEnumByPQON(final String pqon) {
        return ENUM_BY_PQON.get(pqon);
    }

    public static EnumSetDefinition getEnumsetByPQON(final String pqon) {
        return ENUMSET_BY_PQON.get(pqon);
    }

    public static XEnumSetDefinition getXEnumsetByPQON(final String pqon) {
        return XENUMSET_BY_PQON.get(pqon);
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
    public static Reflections[] initializeT9t() {
        UiGridConfigPrefs.reset();  // clear all view model containers and reset error counter

        final Reflections[] scannedPackages = initializeT9tForClients();
        collectCrudViewModels(scannedPackages);
        LOGGER.info("{} crudViewModels found", IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.size());

        collectLeanGridConfigurations(scannedPackages);
        LOGGER.info("{} lean grid configurations found", ILeanGridConfigContainer.LEAN_GRID_CONFIG_REGISTRY.size());
        LOGGER.debug("lean grid configurations are {}", ToStringHelper.toStringML(ILeanGridConfigContainer.LEAN_GRID_CONFIG_REGISTRY.keySet()));

        return scannedPackages;
    }

    /** Subset of the initialization - for remote tests. */
    public static Reflections[] initializeT9tForClients() {
        final String javaVersion = System.getProperty("java.version");
        final String javaVendor  = System.getProperty("java.vendor");
        LOGGER.info("Running Java version {} (vendor {})", javaVersion != null ? javaVersion : "(UNDEFINED)", javaVendor != null ? javaVendor : "(UNDEFINED)");
        LOGGER.info("Instance signature is {}", RandomNumberGenerators.getInstanceSignatue());
        LOGGER.info("OS type assumed to be {}, hostname {}", MessagingUtil.IS_MS_WINDOWS ? "MS Windows" : "*nix or MacOS", MessagingUtil.HOSTNAME);

        checkUTC();
        MessagingUtil.initializeBonaparteParsers();
        final Reflections[] scannedPackages = ReflectionsPackageCache.getAll(MessagingUtil.getPackagesToScanForXenums());

        ExceptionInitializer.initializeExceptionClasses(scannedPackages);       // init all 3 packages with one invocation
        XenumInitializer.initializeXenums(scannedPackages);

        // stop in case we have duplicate exception codes
        if (ApplicationException.checkForDuplicates()) {
            LOGGER.error("Found duplicate exception codes - terminating");
            throw new RuntimeException("Nonunique exception codes");
        }

        collectEnums(scannedPackages);
        LOGGER.info("{} enums found", ENUM_BY_PQON.size());

        collectStringEnumsets(scannedPackages);
        LOGGER.info("{} enumsets found", ENUMSET_BY_PQON.size());

        collectXEnumsets(scannedPackages);
        LOGGER.info("{} xenumsets found", XENUMSET_BY_PQON.size());
        return scannedPackages;
    }

    private static void collectEnums(final Reflections... packages) {
        for (final Reflections pkg: packages) {
            // we search separately because just looking for BonaEnum (which should work) does not give any results...
            collectEnums(pkg, BonaTokenizableEnum.class);
            collectEnums(pkg, BonaNonTokenizableEnum.class);
            // collectEnums(pkg, BonaEnum.class); // sometimes we get duplicates, sometimes not...
        }
    }

    private static void collectEnums(final Reflections pkg, final Class<? extends BonaEnum> subTypesOf) {
        int counter = 0;
        for (final Class<? extends BonaEnum> cls : pkg.getSubTypesOf(subTypesOf)) {
            if (!cls.isInterface()) {
                // skip the base interfaces itself
                try {
                    final Method method = cls.getMethod("enum$MetaData");
                    final Object o = method.invoke(null);
                    if (o instanceof EnumDefinition def) {
                        ++counter;
                        final EnumDefinition prev = ENUM_BY_PQON.put(def.getName(), def);
                        if (prev != null)
                            LOGGER.error("2 different enums of same PQON {}", def.getName());
                    } else {
                        LOGGER.error("Could not obtain EnumDefinition for class {}", cls.getCanonicalName());
                    }
                } catch (final Exception e) {
                    LOGGER.warn("Exception obtaining EnumDefinition from {}: {}", cls.getCanonicalName(), ExceptionUtil.causeChain(e));
                }
            }
        }
        LOGGER.debug("Found {} instances of enum subtype {}", counter, subTypesOf.getSimpleName());
    }

    private static void collectStringEnumsets(final Reflections... packages) {
        int counter = 0;
        for (final Reflections pkg: packages) {
            for (final Class<? extends AbstractStringEnumSet> cls : pkg.getSubTypesOf(AbstractStringEnumSet.class)) {
                try {
                    final Method method = cls.getMethod("enumset$MetaData");
                    final Object o = method.invoke(null);
                    if (o instanceof EnumSetDefinition def) {
                        ++counter;
                        final EnumSetDefinition prev = ENUMSET_BY_PQON.put(def.getName(), def);
                        if (prev != null)
                            LOGGER.error("2 different enumsets of same PQON {}", def.getName());
                    } else {
                        LOGGER.error("Could not obtain EnumSetDefinition for class {}", cls.getCanonicalName());
                    }
                } catch (final Exception e) {
                    LOGGER.warn("Exception obtaining EnumSetDefinition from {}: {}", cls.getCanonicalName(), ExceptionUtil.causeChain(e));
                }
            }
        }
        LOGGER.debug("Found {} instances of enumset", counter);
    }

    private static void collectXEnumsets(final Reflections... packages) {
        int counter = 0;
        for (final Reflections pkg: packages) {
            for (final Class<? extends AbstractStringXEnumSet> cls : pkg.getSubTypesOf(AbstractStringXEnumSet.class)) {
                try {
                    final Method method = cls.getMethod("xenumset$MetaData");
                    final Object o = method.invoke(null);
                    if (o != null && o instanceof XEnumSetDefinition def) {
                        ++counter;
                        final XEnumSetDefinition prev = XENUMSET_BY_PQON.put(def.getName(), def);
                        if (prev != null)
                            LOGGER.error("2 different xenumsets of same PQON {}", def.getName());
                    } else {
                        LOGGER.error("Could not obtain XEnumSetDefinition for class {}", cls.getCanonicalName());
                    }
                } catch (final Exception e) {
                    LOGGER.warn("Exception obtaining XEnumSetDefinition from {}: {}",
                            cls.getCanonicalName(), ExceptionUtil.causeChain(e));
                }
            }
        }
        LOGGER.debug("Found {} instances of xenumset", counter);
    }

    private static void collectCrudViewModels(final Reflections... packages) {
        for (final Reflections pkg: packages) {
            for (final Class<? extends IViewModelContainer> cls : pkg.getSubTypesOf(IViewModelContainer.class)) {
                try {
                    LOGGER.debug("Found viewModel container {}", cls.getCanonicalName());
                    // create an instance of the class and use it to invoke the registration method
                    cls.getDeclaredConstructor().newInstance().register();
                } catch (final Exception e) {
                    LOGGER.warn("Cannot initialize viewModelContainer {}: {}", cls.getCanonicalName(), ExceptionUtil.causeChain(e));
                }
            }
        }
    }

    private static void collectLeanGridConfigurations(final Reflections... packages) {
        // first, process all entries from IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY
        for (final Map.Entry<String, CrudViewModel<?, ?>> vm: IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.entrySet()) {
            // read the grid config. If a search request has been specified the grid config is required, otherwise it is treated as optional (could be related to a popup)
            UiGridConfigPrefs.getLeanGridConfigAsObject(vm.getKey(), vm.getValue().searchClass == null);
        }
        // now search for all explicitly listed ILeanGridConfigContainer
        final List<ILeanGridConfigContainer> clses = new ArrayList<>(100);
        for (final Reflections pkg: packages) {
            for (final Class<? extends ILeanGridConfigContainer> cls : pkg.getSubTypesOf(ILeanGridConfigContainer.class)) {
                try {
                    clses.add(cls.getDeclaredConstructor().newInstance());
                } catch (final Exception e) {
                    LOGGER.warn("Cannot initialize leanGridConfigContainer {}: {}", cls.getCanonicalName(), ExceptionUtil.causeChain(e));
                }
            }
        }

        // sort by the order
        Collections.sort(clses, (c1, c2) -> c1.getOrder() - c2.getOrder());

        for (final ILeanGridConfigContainer cls : clses) {
            final List<String> configs = cls.getResourceNames();
            LOGGER.debug("Grid config container {} holds {} lean grid configurations (sort order {})",
                    cls.getClass().getCanonicalName(), configs.size(), cls.getOrder());
            for (final String resourceId : configs) {
                if (IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.containsKey(resourceId)) {
                    LOGGER.warn("Duplicate definition of gridId {}", resourceId);
                }
                // read the grid config resource. Because it is mentioned in a lean grid config container, it is mandatory!
                UiGridConfigPrefs.getLeanGridConfigAsObject(resourceId, false);
            }
        }
        // apply the overrides & extends
        UiGridConfigPrefs.postProcess();
    }

    public static int getErrorCount() {
        // delegate to UiGridConfigPrefs class
        return UiGridConfigPrefs.getErrorCount();
    }

    public static Set<Class<?>> getClassesAnnotatedWith(final Class<? extends Annotation> annotation) {
        final Reflections t9t = ReflectionsPackageCache.get(MessagingUtil.TWENTYEIGHT_PACKAGE_PREFIX);
        final Set<Class<?>> classesInT9t = t9t.getTypesAnnotatedWith(annotation);
        final int numExtraPackages = Packages.numberOfExtraPackages();
        if (numExtraPackages == 0) {
            // no union required, return as is
            return classesInT9t;
        }
        final MutableInt totalNumberOfClasses = new MutableInt(classesInT9t.size());
        final List<Set<Class<?>>> extraSets = new ArrayList<>(numExtraPackages);
        Packages.walkExtraPackages((prefix, packageName) -> {
            final Reflections extra = ReflectionsPackageCache.get(packageName);
            final Set<Class<?>> extraClasses = extra.getTypesAnnotatedWith(annotation);
            extraSets.add(extraClasses);
            totalNumberOfClasses.add(extraClasses.size());
        });
        // merge them all
        final Set<Class<?>> union = new HashSet<>(totalNumberOfClasses.getValue());
        union.addAll(classesInT9t);
        for (final Set<Class<?>> extraSet: extraSets) {
            union.addAll(extraSet);
        }
        return union;
    }
}
