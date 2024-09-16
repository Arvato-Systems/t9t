package com.arvatosystems.t9t.zkui.util;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tException;

import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.meta.ClassDefinition;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.dp.ReflectionsPackageCache;

public final class GridConfigUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(GridConfigUtil.class);

    private GridConfigUtil() {
    }

    public static Set<String> getTrackingFieldNames() {
        final Set<String> trackingFields = new HashSet<String>();
        final Reflections[] reflections = ReflectionsPackageCache.getAll(MessagingUtil.BONAPARTE_PACKAGE_PREFIX, MessagingUtil.TWENTYEIGHT_PACKAGE_PREFIX);
        for (final Reflections reflection : reflections) {
            for (final Class<? extends TrackingBase> cls : reflection.getSubTypesOf(TrackingBase.class)) {
                try {
                    final Method method = cls.getDeclaredMethod("class$MetaData");
                    final ClassDefinition classDefinition = (ClassDefinition) method.invoke(null);
                    for (final FieldDefinition fieldDefinition : classDefinition.getFields()) {
                        trackingFields.add(fieldDefinition.getName());
                    }
                } catch (Exception ex) {
                    LOGGER.error("Error occured while getting tracking field", ex);
                    throw new T9tException(T9tException.GENERAL_SERVER_ERROR, ex.getMessage());
                }
            }
        }
        return trackingFields;
    }
}
