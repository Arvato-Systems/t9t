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
package com.arvatosystems.t9t.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;
import de.jpaw.bonaparte.pojos.meta.ClassDefinition;
import de.jpaw.bonaparte.pojos.meta.EnumDataItem;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.Multiplicity;
import de.jpaw.bonaparte.pojos.meta.ObjectReference;

import com.arvatosystems.t9t.ai.mcp.AbstractJsonSchemaField;
import com.arvatosystems.t9t.ai.mcp.JsonSchemaArray;
import com.arvatosystems.t9t.ai.mcp.JsonSchemaBoolean;
import com.arvatosystems.t9t.ai.mcp.JsonSchemaEnum;
import com.arvatosystems.t9t.ai.mcp.JsonSchemaNumber;
import com.arvatosystems.t9t.ai.mcp.JsonSchemaObject;
import com.arvatosystems.t9t.ai.mcp.JsonSchemaSet;
import com.arvatosystems.t9t.ai.mcp.JsonSchemaString;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.T9tUtil;

public final class T9tAiTools {
    private T9tAiTools() { }

    private static final int MAX_ENUM_NAME_LENGTH = 80;

    /** Returns a description of a class from meta data. */
    public static String getToolDescription(@Nonnull final ClassDefinition cd) {
        if (cd.getRegularComment() != null) {
            // if there is a regular comment, use it
            return cd.getRegularComment();
        }
        // return the stripped javadoc
        return stripJavadoc(cd.getJavaDoc());
    }

    public static String stripJavadoc(@Nullable final String javadoc) {
        if (javadoc == null) {
            return "";
        }
        final int len = javadoc.length();
        final StringBuilder sb = new StringBuilder(len);
        int i = skipSpacesAndStars(javadoc, 3, len, true);
        // loop. End if the previous was a '*' and the current is a '/'
        while (i < len) {
            final char c = javadoc.charAt(i);
            if (c == '/' && javadoc.charAt(i - 1) == '*') {
                break;  // we're done!
            }
            // transfer until new line, then again skip initial spaces and stars
            sb.append(c);
            if (c == '\n') {
                i = skipSpacesAndStars(javadoc, i + 1, len, false);
            } else {
                ++i;
            }
        }
        return sb.toString();
    }

    private static int skipSpacesAndStars(final String javadoc, int pos, final int len, final boolean alsoNewline) {
        while (pos < len && (javadoc.charAt(pos) == ' ' || javadoc.charAt(pos) == '*'
                || (alsoNewline && (javadoc.charAt(pos) == '\n' || javadoc.charAt(pos) == '\r')))) {
            ++pos;
        }
        return pos;
    }

    /** Returns the list of required parameters. */
    public static List<String> buildRequiredFromFields(final List<FieldDefinition> fields) {
        final List<String> required = new ArrayList<>(fields.size());
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
    public static JsonSchemaObject buildJsonSchemaObject(@Nullable final ClassDefinition metaData, @Nullable final String description) {
        final JsonSchemaObject object = new JsonSchemaObject();
        object.setType("object");
        object.setDescription(description);
        if (metaData != null && !T9tUtil.isEmpty(metaData.getFields())) {
            final Map<String, AbstractJsonSchemaField> properties = new HashMap<>(metaData.getFields().size() * 2);
            for (final FieldDefinition field : metaData.getFields()) {
                final AbstractJsonSchemaField fieldDef = buildField(field);
                if (fieldDef != null) {
                    properties.put(field.getName(), fieldDef);
                }
            }
            object.setProperties(properties);
            object.setRequired(T9tAiTools.buildRequiredFromFields(metaData.getFields()));
        }
        return object;
    }

    private static AbstractJsonSchemaField buildField(final FieldDefinition metaData) {
        final String comment = metaData.getTrailingComment();
        switch (metaData.getMultiplicity()) {
        case SCALAR:
            return buildFieldNoArray(metaData, comment);
        case ARRAY:
        case LIST:
            return new JsonSchemaArray("array", comment, metaData.getMinCount(), metaData.getMaxCount() > 0 ? metaData.getMaxCount() : null,
                buildFieldNoArray(metaData, null));
        case MAP:
            return buildJsonSchemaObject(null, comment); // maps are represented as objects in JSON schema
        case SET:
            return new JsonSchemaSet("array", comment, true, buildFieldNoArray(metaData, null));
        default:
            throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "Multiplicity: " + metaData.getMultiplicity());
        }
    }

    private static AbstractJsonSchemaField buildFieldNoArray(final FieldDefinition metaData, final String comment) {
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
            return buildJsonSchemaObject(objectReference.getLowerBound(), comment);
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
