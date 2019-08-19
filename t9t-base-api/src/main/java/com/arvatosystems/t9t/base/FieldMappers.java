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
package com.arvatosystems.t9t.base;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.entities.InternalTenantRef42;
import com.arvatosystems.t9t.base.misc.Variant;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.util.FieldGetter;

/**
 * Utility methods to perform field mapping (path root mapping when using the foldingComposer with the DataWithTracking object.
 *
 * @author BISC02
 *
 */
public class FieldMappers {
    private static final Logger LOGGER = LoggerFactory.getLogger(FieldMappers.class);

    private FieldMappers() {} // don't want instances of this class

    public static final List<String> TRACKING_COLUMN_NAMES = new ArrayList<String>(Arrays.asList(
            "cTimestamp", "cTechUserId", "cAppUserId", "cProcessRef",
            "mTimestamp", "mTechUserId", "mAppUserId", "mProcessRef",
            "version"));

    public static boolean isTenantRef(String fieldName) {
        return T9tConstants.TENANT_REF_FIELD_NAME42.equals(fieldName);
    }

    public static boolean isTrackingColumn(String fieldName) {
        return TRACKING_COLUMN_NAMES.contains(fieldName);
    }

    /** Converts field names based at the DTO object to field names starting at the DataWithTracking object. */
    public static String addPrefix(String fieldName) {
        if (isTenantRef(fieldName))
            return fieldName;  // provided at root level
        return (isTrackingColumn(fieldName) ? "tracking." : "data.") + fieldName;
    }

    /** Converts field names based at DataWithTracking to fieldnames at deeper level (unsure if we need this except for testing). */
    public static String removePrefix(String fieldName) {
        if (fieldName.startsWith("data.")) {
            return fieldName.substring(5);
        }
        if (fieldName.startsWith("tracking.")) {
            return fieldName.substring(9);
        }
        return fieldName; // has neither (should not occur!)
    }

    /** Remove any index of form [nnn] from the input string. */
    public static String stripIndexes(String fieldName) {
        int dotPos = fieldName.indexOf('[');
        if (dotPos < 0)
            return fieldName;  // no change: shortcut!
        // has to replace at least one array index
        final int l = fieldName.length();
        StringBuilder newName = new StringBuilder(l);
        int currentSrc = 0;
        while (dotPos >= 0) {
            // copy all until the array index start
            newName.append(fieldName.substring(currentSrc, dotPos));
            currentSrc = dotPos + 1;
            dotPos = fieldName.indexOf(']', currentSrc);
            if (dotPos <= currentSrc) {
                LOGGER.error("Malformatted field name: mismatchign brackets in {}", fieldName);
                return fieldName;  // return original one
            }
            currentSrc = dotPos + 1;
            dotPos = fieldName.indexOf('[', currentSrc);
        }
        // all instances found, copy any remaining characters
        if (currentSrc < l)
            newName.append(fieldName.substring(currentSrc));
        String result = newName.toString();  // temporary var to avoid duplicate construction of string when log level is debug
        LOGGER.trace("unindexing field name {} to {}", fieldName, result);
        return result;
    }

    public static <DTO extends BonaPortable, TRACKING extends TrackingBase> FieldDefinition getFieldDefinitionForPath(String fieldname, CrudViewModel<DTO, TRACKING> model) {
        if (model.trackingClass == null) {
            //plain object
            return  FieldGetter.getFieldDefinitionForPathname(model.dtoClass.getMetaData(), fieldname);
        }
        // determine where it is based on field name
        if (isTenantRef(fieldname))
            return FieldGetter.getFieldDefinitionForPathname(InternalTenantRef42.class$MetaData(), fieldname);
        else if (isTrackingColumn(fieldname))
            return FieldGetter.getFieldDefinitionForPathname(model.trackingClass.getMetaData(), fieldname);
        else
            return FieldGetter.getFieldDefinitionForPathname(model.dtoClass.getMetaData(), fieldname);
    }

    public static <T extends Enum<T>> T mapEnum(Enum<?> src, Class<T> dstClass, String fieldName) {
        if (src == null)
            return null;
        try {
            return Enum.valueOf(dstClass, src.name());
        } catch (IllegalArgumentException e) {
            throw new T9tException(T9tException.ENUM_MAPPING,
                    src.getClass().getCanonicalName() + "." + src.name() + " => " + dstClass.getCanonicalName()
            );
        }
    }

    /** Returns the non-null field which is set in the variant. Cannot do enums currently. */
    public static Object fromVariant(Variant v) {
        Object r;
        r = v.getIntValue();
        if (r != null)
            return r;
        r = v.getLongValue();
        if (r != null)
            return r;
        r = v.getBoolValue();
        if (r != null)
            return r;
        r = v.getNumValue();
        if (r != null)
            return r;
        r = v.getTextValue();
        if (r != null)
            return r;
        r = v.getDayValue();
        if (r != null)
            return r;
        r = v.getInstantValue();
        if (r != null)
            return r;
        return null;
    }

    /** Creates a new Variant from an Object, examining the type at runtime. Use direct assignment instead if you know the type at compile time. */
    public static Variant variantOf(Object o) {
        Variant v = new Variant();
        if (o == null)
            return v;
        if (o instanceof Integer)         v.setIntValue((Integer)o);
        else if (o instanceof Long)       v.setLongValue((Long)o);
        else if (o instanceof Boolean)    v.setBoolValue((Boolean)o);
        else if (o instanceof BigDecimal) v.setNumValue((BigDecimal)o);
        else if (o instanceof String)     v.setTextValue((String)o);
        else if (o instanceof LocalDate)  v.setDayValue((LocalDate)o);
        else if (o instanceof Instant)    v.setInstantValue((Instant)o);
        else throw new IllegalArgumentException("Cannot wrap an object of type " + o.getClass().getCanonicalName() + " inside a Variant");
        return v;
    }

    /** Indexes a DTO list into an existing map. */
    public static <D extends Ref> void index(final Map<Long, D> result, final Collection<D> data) {
        for (D d : data)
            result.put(d.getObjectRef(), d);
    }

    /** Indexes a DTO list into a new map. */
    public static <D extends Ref> Map<Long, D> index(final Collection<D> data) {
        final Map<Long, D> result = new HashMap<Long, D>(data.size() * 2);
        index(result, data);
        return result;
    }
}
