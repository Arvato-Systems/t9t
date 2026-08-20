package com.arvatosystems.t9t.ai;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.core.BonaPortableFactory;
import de.jpaw.bonaparte.pojos.meta.ClassDefinition;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.ObjectReference;
//import de.jpaw.xenums.init.ReflectionsPackageCache;

import com.arvatosystems.t9t.base.MessagingUtil;

public final class ClassWalker {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassWalker.class);

    private ClassWalker() { }

    private static final Map<String, JsonSchemaData> CACHED_CLASS_DATA = new ConcurrentHashMap<>(1000);
    private static final Reflections REFLECTIONS = new Reflections(MessagingUtil.TWENTYEIGHT_PACKAGE_PREFIX); // currently t9t packages only
    // final Reflections[] scannedPackages = ReflectionsPackageCache.getAll(MessagingUtil.getPackagesToScanForXenums());

    public static JsonSchemaData getCachedSchemaData(final String pqon) {
        return CACHED_CLASS_DATA.get(pqon);
    }

    public static JsonSchemaData getSchemaData(final String pqon) {
        // first, try to obtain the data from the cache
        final var cachedData = CACHED_CLASS_DATA.get(pqon);
        if (cachedData != null) {
            return cachedData;
        }
        // not found: obtain the bonaparte meta data, then create it in the cache
        final BonaPortableClass<?> bClass = BonaPortableFactory.getBClassForPqon(pqon);
        final ClassDefinition cd = bClass.getMetaData();
        final var newData = new JsonSchemaData(cd, new HashMap<>(32), new HashMap<>(64));
        CACHED_CLASS_DATA.put(pqon, newData);
        // populate the subclasses
        final Class<?> baseClass = bClass.getBonaPortableClass();
        final Set<?> discoveredSubTypes = REFLECTIONS.getSubTypesOf(baseClass);
        for (final Object subType : discoveredSubTypes) {
            final Class<?> subClass = (Class<?>) subType;
            if (!BonaPortable.class.isAssignableFrom(subClass)) {
                LOGGER.warn("Subclass {} of {} is not a BonaPortable, but is a subclass of {} - skipping it", subClass.getCanonicalName(), pqon, baseClass.getCanonicalName());
                continue;
            }
            final String fqonOfSubclass = subClass.getCanonicalName();
            if (!fqonOfSubclass.startsWith(MessagingUtil.TWENTYEIGHT_PACKAGE_PREFIX)) {
                LOGGER.warn("Subclass {} of {} is a BonaPortable, but is not in the {} package - skipping it", fqonOfSubclass, pqon, MessagingUtil.TWENTYEIGHT_PACKAGE_PREFIX);
                continue;
            }
            final String subPqon = fqonOfSubclass.substring(18);  // 18 is length of "com.arvatosystems."
            newData.subclasses().put(subPqon, getSchemaData(subPqon));
        }

        // populated the referenced PQONs
        final Map<String, JsonSchemaData> referencedPqons = newData.referencedPqons();
        // do not merge myself
        // referencedPqons.put(pqon, newData);
        // merge the subclasses itself
        referencedPqons.putAll(newData.subclasses());
        // merge the refs of the subclasses:
        for (final JsonSchemaData subData : newData.subclasses().values()) {
            referencedPqons.putAll(subData.referencedPqons());
        }
        // then, walk all fields of this class and all its superclasses, and add the referenced PQONs
        for (ClassDefinition currentClass = cd; currentClass != null; currentClass = currentClass.getParentMeta()) {
            for (final FieldDefinition field : currentClass.getFields()) {
                if (field instanceof ObjectReference objRef && objRef.getLowerBound() != null) {
                    final String refPqon = objRef.getLowerBound().getName();
                    referencedPqons.put(refPqon, getSchemaData(refPqon));
                }
            }
        }
        // remove self, in case it got in by a cycle
        referencedPqons.remove(pqon);
        return newData;
    }
}
