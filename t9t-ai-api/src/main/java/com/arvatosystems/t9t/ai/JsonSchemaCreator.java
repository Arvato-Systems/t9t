package com.arvatosystems.t9t.ai;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Nullable;

import de.jpaw.bonaparte.core.MimeTypes;
import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;
import de.jpaw.bonaparte.pojos.meta.ClassDefinition;
import de.jpaw.bonaparte.pojos.meta.EnumDataItem;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.Multiplicity;
import de.jpaw.bonaparte.pojos.meta.ObjectReference;

import com.arvatosystems.t9t.ai.jsonSchema.AbstractJsonSchemaField;
import com.arvatosystems.t9t.ai.jsonSchema.JsonSchemaArray;
import com.arvatosystems.t9t.ai.jsonSchema.JsonSchemaBoolean;
import com.arvatosystems.t9t.ai.jsonSchema.JsonSchemaEnum;
import com.arvatosystems.t9t.ai.jsonSchema.JsonSchemaNumber;
import com.arvatosystems.t9t.ai.jsonSchema.JsonSchemaObject;
import com.arvatosystems.t9t.ai.jsonSchema.JsonSchemaSet;
import com.arvatosystems.t9t.ai.jsonSchema.JsonSchemaString;
import com.arvatosystems.t9t.base.T9tException;

public final class JsonSchemaCreator {
    private JsonSchemaCreator() { }

    private static final int MAX_ENUM_NAME_LENGTH = 80;

    /** Returns the list of required parameters. */
    public static List<String> buildRequiredFromFields(final List<FieldDefinition> fields, final boolean addPqon) {
        final List<String> required = new ArrayList<>(fields.size());
        if (addPqon) {
            required.add(MimeTypes.JSON_FIELD_PQON);
        }
        for (final FieldDefinition field : fields) {
            if (field.getMultiplicity() == Multiplicity.SCALAR ? field.getIsRequired() : field.getIsAggregateRequired()) {
                required.add(field.getName());
            }
        }
        return required;
    }

    /**
     * Builds a JSON schema object from the class definition.
     *
     * @param metaData the class definition containing field information
     * @param description the description
     * @return a JsonSchemaObject representing the class definition
     */
    public static JsonSchemaObject buildJsonSchemaObject(@Nullable final ClassDefinition metaData, @Nullable final String description, final boolean addPqon) {
        final JsonSchemaObject object = new JsonSchemaObject();
        object.setType("object");
        object.setDescription(description);
        if (metaData == null) {
            // generic object without specific fields, nor PQON
            object.setProperties(Map.of());
            object.setRequired(List.of());
            return object;
        }
        final Map<String, AbstractJsonSchemaField> properties = new HashMap<>(metaData.getFields().size() * 2);
        if (addPqon && !metaData.getIsAbstract()) {
            // require the "@PQON" field to be present in the JSON representation of the object, so that we can deserialize it correctly
            properties.put(MimeTypes.JSON_FIELD_PQON, new JsonSchemaEnum("string", "The partially qualified object name (PQON) of the object.", List.of(metaData.getName())));
        }
        for (final FieldDefinition field : metaData.getFields()) {
            final AbstractJsonSchemaField fieldDef = buildField(field, addPqon);
            if (fieldDef != null) {
                properties.put(field.getName(), fieldDef);
            }
        }
        object.setProperties(properties);
        object.setRequired(buildRequiredFromFields(metaData.getFields(), addPqon));
        return object;
    }

    private static AbstractJsonSchemaField buildField(final FieldDefinition metaData, final boolean addPqon) {
        final String comment = metaData.getTrailingComment();
        switch (metaData.getMultiplicity()) {
        case SCALAR:
            return buildFieldNoArray(metaData, comment, addPqon);
        case ARRAY:
        case LIST:
            return new JsonSchemaArray("array", comment, metaData.getMinCount(), metaData.getMaxCount() > 0 ? metaData.getMaxCount() : null,
                buildFieldNoArray(metaData, null, addPqon));
        case MAP:
            return buildJsonSchemaObject(null, comment, addPqon); // maps are represented as objects in JSON schema
        case SET:
            return new JsonSchemaSet("array", comment, true, buildFieldNoArray(metaData, null, addPqon));
        default:
            throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "Multiplicity: " + metaData.getMultiplicity());
        }
    }

    private static AbstractJsonSchemaField buildFieldNoArray(final FieldDefinition metaData, final String comment, final boolean addPqon) {
        switch (metaData.getDataCategory()) {
        case BASICNUMERIC:
        case NUMERIC:
            return new JsonSchemaNumber("number", comment);
        case STRING: {
            final AlphanumericElementaryDataItem ad = (AlphanumericElementaryDataItem)metaData;
            return new JsonSchemaString("string", comment, ad.getMinLength(), ad.getLength(), null, null);
        }
        case MISC: {
            final String bonaparteType = metaData.getBonaparteType().toLowerCase();
            switch (bonaparteType) {
            case "uuid":
                return new JsonSchemaString("string", comment, null, null, null, "uuid");
            case "boolean":
                return new JsonSchemaBoolean("boolean", comment);
            default:
                throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "Miscellaneous type: " + bonaparteType);
            }
        }
        case ENUM:
        case ENUMALPHA: {
            final EnumDataItem enumData = (EnumDataItem) metaData;
            // return as string with enum values
            return new JsonSchemaEnum("string", comment, enumData.getBaseEnum().getIds());
        }
        case XENUM:
            return new JsonSchemaString("string", comment, null, MAX_ENUM_NAME_LENGTH, null, null);
        case ENUMSET:
        case ENUMSETALPHA:
        case XENUMSET:
            // return as array of strings
            return new JsonSchemaSet("array", comment, true, new JsonSchemaString("string", comment, null, MAX_ENUM_NAME_LENGTH, null, null));
        case OBJECT: {
            final ObjectReference objectReference = (ObjectReference) metaData;
            return buildJsonSchemaObject(objectReference.getLowerBound(), comment, addPqon);
        }
        case TEMPORAL: {
            final String bonaparteType = metaData.getBonaparteType().toLowerCase();
            switch (bonaparteType) {
            case "day":
                return new JsonSchemaString("string", comment, 10, 10, null, "date");
            case "time":
                return new JsonSchemaString("string", comment, null, null, null, "time");
            case "timestamp":
            case "instant":
                return new JsonSchemaString("string", comment, null, null, null, "date-time");
            default:
                throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "Miscellaneous type: " + bonaparteType);
            }
        }
        case BINARY:
            break;
        default:
            break;
        }
        return null;
    }
}
