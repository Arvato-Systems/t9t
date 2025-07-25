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
package com.arvatosystems.t9t.zkui.converters.grid;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.jpaw.bonaparte.pojos.meta.AlphanumericEnumSetDataItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.zkui.util.Constants;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.enums.BonaEnum;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.NumericEnumSetDataItem;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.enums.XEnum;
import de.jpaw.util.ExceptionUtil;

@Singleton
@Named("all")
public class AllItemConverters  implements IItemConverter<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AllItemConverters.class);
    private static final Map<String, IItemConverter<?>> REGISTRY = new ConcurrentHashMap<String, IItemConverter<?>>(50);

    public static final void register(String key, IItemConverter<?> itemConverter) {
        REGISTRY.put(key, itemConverter);
    }

    public static IItemConverter<?> getConverter(Object value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
        if (value instanceof Long) {
//            return Jdp.getOptional(ILongItemConverter.class, fieldName);
//            if (T9tConstants.TENANT_ID_FIELD_NAME.equals(fieldName))
//                return REGISTRY.get(T9tConstants.TENANT_ID_FIELD_NAME);
//            if ("userRef".equals(fieldName))
//                return REGISTRY.get("userRef");
            @SuppressWarnings("rawtypes")
            IItemConverter longFieldConverter = REGISTRY.get(fieldName);  // allow to plug in additional converters
            if (longFieldConverter != null) {
                // LOGGER.debug("got Long converter for {}", fieldName);
                return longFieldConverter;
            }
            // else fall through
            // LOGGER.debug("NO Long converter for {}", fieldName);
        }
        final boolean isGenericObject = meta.getProperties() != null && meta.getProperties().containsKey(Constants.UiFieldProperties.GENERIC_OBJECT);
        if (isGenericObject) {
            @SuppressWarnings("rawtypes")
            final IItemConverter genericObjectConverter = REGISTRY.get(Constants.UiFieldProperties.GENERIC_OBJECT);
            if (genericObjectConverter != null) {
                return genericObjectConverter;
            }
        }
        final String className = value.getClass().getName();
        if (meta.getClass() == NumericEnumSetDataItem.class)
            return REGISTRY.get("numericenumset");
        if (meta instanceof AlphanumericEnumSetDataItem) {
            return REGISTRY.get("stringenumset");
        }
        @SuppressWarnings("rawtypes")
        IItemConverter classConverter = REGISTRY.get(className);
        if (classConverter != null)
            return classConverter;
        if (value instanceof Map) {
            @SuppressWarnings("rawtypes")
            IItemConverter mapFieldConverter = REGISTRY.get("java.util.Map");
            if (mapFieldConverter != null) {
                return mapFieldConverter;
            }
        }
        // subtypes
        if (value instanceof BonaEnum)
            return REGISTRY.get("bonaenum");
        if (value instanceof Enum)
            return REGISTRY.get("enum");
        if (value instanceof XEnum)
            return REGISTRY.get("xenum");
        // this does not work because enumsets are converted to number or string before marshalling
        // would need to test the metadata object, which is currently not transferred to this method
//        if (value instanceof EnumSetMarker)
//            return REGISTRY.get("enumset");
        if (meta.getProperties() != null && meta.getProperties().containsKey(Constants.UiFieldProperties.DROPDOWN)) {
            return REGISTRY.get(Constants.UiFieldProperties.DROPDOWN);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getFormattedLabel(Object value, BonaPortable wholeDataObject, String fieldName, FieldDefinition d) {
        // check for a fixed type
        if (value == null)
            return null;
        @SuppressWarnings("rawtypes")
        IItemConverter classConverter = getConverter(value, wholeDataObject, fieldName, d);
        if (classConverter != null) {
            try {
                return classConverter.getFormattedLabel(value, wholeDataObject, fieldName, d);
            } catch (Exception e) {
                LOGGER.error("apply LABEL item converter {} for {} threw an exception: {}",
                        classConverter.getClass().getSimpleName(), value, ExceptionUtil.causeChain(e));
                return null;
            }
        }
        return value.toString();
    }
}
