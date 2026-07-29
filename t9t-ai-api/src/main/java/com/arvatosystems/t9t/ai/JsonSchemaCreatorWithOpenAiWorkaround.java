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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import de.jpaw.bonaparte.core.MimeTypes;
import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;
import de.jpaw.bonaparte.pojos.meta.ClassDefinition;
import de.jpaw.bonaparte.pojos.meta.EnumDataItem;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
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

/** Generator for JSON schema, with workaround to allow using the strict setting with OpenAI (they are not able to work with optional fields). */
public final class JsonSchemaCreatorWithOpenAiWorkaround {
    private JsonSchemaCreatorWithOpenAiWorkaround() { }

    private static final int MAX_ENUM_NAME_LENGTH = 80;

    public static void addDefs(final JsonSchemaObject schemaObject, final Map<String, JsonSchemaObject> defs) {
        schemaObject.setDefs(defs);
    }

    public static Map<String, JsonSchemaObject> createDefs(@Nonnull final JsonSchemaData schemaData, @Nonnull final Map<String, String> redirectionMap) {
        final Set<String> pqonsAlreadyDone = new HashSet<>(64);
        final Map<String, JsonSchemaObject> defs = new HashMap<>(2 * schemaData.referencedPqons().size());

        createDefsForMyselfAndSubclassesRecursively(pqonsAlreadyDone, defs, schemaData, redirectionMap);
        return defs;
    }

    private static void createDefsForMyselfAndSubclassesRecursively(final Set<String> pqonsAlreadyDone, final Map<String, JsonSchemaObject> defs, final JsonSchemaData schemaDataOrg, @Nonnull final Map<String, String> redirectionMap) {
        final String pqon = schemaDataOrg.classDefinition().getName();
        if (pqonsAlreadyDone.contains(pqon)) {
            return;
        }
        pqonsAlreadyDone.add(pqon);
        // check for replacement / shortcut (i.e. mapping all possible Ref subclasses to the related Key class)
        final String replacement = redirectionMap.get(pqon);
        final JsonSchemaData schemaData = replacement != null ? ClassWalker.getSchemaData(replacement) : schemaDataOrg;
        // check
        if (schemaData == null) {
            throw new T9tException(T9tException.INVALID_CONFIGURATION, "Schema data for " + replacement + " not found");
        }

        if (schemaData.subclasses().isEmpty()) {
            // only do myself, no recursion needed
            defs.put(pqon, buildJsonSchemaObject(schemaData.classDefinition(), null, true, false));
            createDefsForReferencesRecursively(pqonsAlreadyDone, defs, schemaData, redirectionMap);
        } else if (getNonAbstractSubclassIfUnique(schemaData.subclasses().values()) instanceof JsonSchemaData uniqueSubclass) {
            // only one non-abstract subclass, so we can use a ref to it - an entry for the subclass itself is not really needed
            defs.put(pqon, buildJsonSchemaObject(uniqueSubclass.classDefinition(), null, true, false));
            // recurse into that unique subclass, so that it is also available in the defs map
            createDefsForReferencesRecursively(pqonsAlreadyDone, defs, uniqueSubclass, redirectionMap);
        } else {
            // multiple subclasses, recurse
            defs.put(pqon, makeAnyOf(schemaData));
            // also recurse into the subclasses, so that they are also available in the defs map
            for (final var subClass: schemaData.subclasses().values()) {
                createDefsForMyselfAndSubclassesRecursively(pqonsAlreadyDone, defs, subClass, redirectionMap);
            }
        }
    }

    private static void createDefsForReferencesRecursively(final Set<String> pqonsAlreadyDone, final Map<String, JsonSchemaObject> defs, final JsonSchemaData schemaData, @Nonnull final Map<String, String> redirectionMap) {
        for (final var ref: schemaData.referencedPqons().values()) {
            // create the object definition for either self, a single subclass, or anyOf the possible subclass
            createDefsForMyselfAndSubclassesRecursively(pqonsAlreadyDone, defs, ref, redirectionMap);
        }
    }

    private static JsonSchemaData getNonAbstractSubclassIfUnique(final Collection<JsonSchemaData> candidates) {
        JsonSchemaData found = null;
        for (final var candidate: candidates) {
            if (candidate.classDefinition().getIsAbstract()) {
                continue;
            }
            if (found != null) {
                // found more than one non-abstract subclass
                return null;
            }
            found = candidate;
        }
        return found;
    }

    /**
     * Create an "anyOf" for the non-abstract subclasses of the schemaData.
     * Always use refs for the subclasses, so that we can use the "defs" map to resolve them.
     */
    private static JsonSchemaObject makeAnyOf(final JsonSchemaData schemaData) {
        final List<Object> anyOfList = new ArrayList<>(schemaData.subclasses().size());
        for (final var subClass: schemaData.subclasses().values()) {
            if (!subClass.classDefinition().getIsAbstract()) {
                // only add if the class is not abstract
                final JsonSchemaObject subClassSchema = new JsonSchemaObject();
                subClassSchema.setRef("#/$defs/" + subClass.classDefinition().getName());
                anyOfList.add(subClassSchema);
            }
        }
        final JsonSchemaObject anyOfObject = new JsonSchemaObject();
        anyOfObject.setAnyOf(anyOfList);
        return anyOfObject;
    }

    /** Returns the list of required parameters. */
    public static List<String> buildRequiredFromFields(final ClassDefinition classDefinition) {
        final List<String> required = new ArrayList<>(classDefinition.getFields().size() + 8);
        required.add(MimeTypes.JSON_FIELD_PQON);
        recurseRequiredFields(classDefinition, required);
        return required;
    }

    private static void recurseRequiredFields(final ClassDefinition classDefinition, final List<String> required) {
        // if the class definition is null, we have reached the top of the hierarchy
        if (classDefinition == null) {
            return;
        }
        // first, add the fields of the superclass
        recurseRequiredFields(classDefinition.getParentMeta(), required);
        // now, add the fields of the current class
        for (final FieldDefinition field : classDefinition.getFields()) {
            required.add(field.getName());
        }
    }

    private static Object makeType(final boolean required, final String type) {
        if (required) {
            return type;
        } else {
            return List.of(type, "null");
        }
    }

    /**
     * Builds a JSON schema object from the class definition.
     *
     * @param metaData the class definition containing field information
     * @param description the description
     * @param isRequired whether the object is required or false, when optional
     * @param useRefs when a ref is desired (i.e. always, except when creating the defs map)
     * @return a JsonSchemaObject representing the class definition
     */
    public static JsonSchemaObject buildJsonSchemaObject(@Nullable final ClassDefinition metaData, @Nullable final String description, final boolean isRequired, final boolean useRefs) {
        final JsonSchemaObject object = new JsonSchemaObject();
        if (useRefs && metaData != null) {
            // do the reference variant
            final String reference = "#/$defs/" + metaData.getName();
            if (isRequired) {
                object.setRef(reference);
            } else {
                object.setAnyOf(List.of(
                        Map.of("$ref", reference),
                        Map.of("type", "null")
                ));
            }
            return object;
        }
        object.setType(makeType(isRequired, "object"));
        object.setDescription(description);
        object.setAddProps(Boolean.FALSE);
        if (metaData == null) {
            // generic object without specific fields, nor PQON
            // OpenAi does not like if it is completely empty
//            object.setProperties(Map.of());
//            object.setRequired(List.of());
            object.setType("null");
            object.setAddProps(null);
            object.setTitle("Generic map - unsupported by OpenAI, cannot use");
            return object;
        }
        final Map<String, AbstractJsonSchemaField> properties = new HashMap<>(metaData.getFields().size() * 2);
        if (!metaData.getIsAbstract()) {
            // require the "@PQON" field to be present in the JSON representation of the object, so that we can deserialize it correctly
            properties.put(MimeTypes.JSON_FIELD_PQON, new JsonSchemaEnum("string", "The partially qualified object name (PQON) of the object.", List.of(metaData.getName())));
        }
        recurseBuildJsonSchemaObjectFields(metaData, properties);
        object.setProperties(properties);
        object.setRequired(buildRequiredFromFields(metaData));
        return object;
    }

    private static void recurseBuildJsonSchemaObjectFields(final ClassDefinition classDefinition, final Map<String, AbstractJsonSchemaField> properties) {
        if (classDefinition == null) {
            return;
        }
        recurseBuildJsonSchemaObjectFields(classDefinition.getParentMeta(), properties);
        for (final FieldDefinition field : classDefinition.getFields()) {
            final AbstractJsonSchemaField fieldDef = buildField(field);
            properties.put(field.getName(), fieldDef);
        }
    }

    private static AbstractJsonSchemaField buildField(final FieldDefinition metaData) {
        final String comment = metaData.getTrailingComment();
        switch (metaData.getMultiplicity()) {
        case SCALAR:
            return buildFieldNoArray(metaData, comment);
        case ARRAY:
        case LIST:
            return new JsonSchemaArray(makeType(metaData.getIsAggregateRequired(), "array"), comment, metaData.getMinCount(), metaData.getMaxCount() > 0 ? metaData.getMaxCount() : null,
                buildFieldNoArray(metaData, null));
        case MAP:
            return buildJsonSchemaObject(null, comment, metaData.getIsAggregateRequired(), true); // maps are represented as objects in JSON schema
        case SET:
            return new JsonSchemaSet(makeType(metaData.getIsAggregateRequired(), "array"), comment, null, buildFieldNoArray(metaData, null));
        default:
            throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "Multiplicity: " + metaData.getMultiplicity());
        }
    }

    private static AbstractJsonSchemaField buildFieldNoArray(final FieldDefinition metaData, final String comment) {
        final boolean required = metaData.getIsRequired();
        final Object str = makeType(required, "string");  // predefine because it occurs so often
        switch (metaData.getDataCategory()) {
        case BASICNUMERIC:
        case NUMERIC:
            return new JsonSchemaNumber(makeType(required, "number"), comment);
        case STRING: {
            final AlphanumericElementaryDataItem ad = (AlphanumericElementaryDataItem)metaData;
            return new JsonSchemaString(str, comment, ad.getMinLength(), ad.getLength(), null, null);
        }
        case MISC: {
            final String bonaparteType = metaData.getBonaparteType().toLowerCase();
            switch (bonaparteType) {
            case "uuid":
                return new JsonSchemaString(str, comment, null, null, null, "uuid");
            case "boolean":
                return new JsonSchemaBoolean(makeType(required, "boolean"), comment);
            default:
                throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "Miscellaneous type: " + bonaparteType);
            }
        }
        case ENUM:
        case ENUMALPHA: {
            final EnumDataItem enumData = (EnumDataItem) metaData;
            // return as string with enum values
            return new JsonSchemaEnum(str, comment, enumData.getBaseEnum().getIds());
        }
        case XENUM:
            return new JsonSchemaString(str, comment, null, MAX_ENUM_NAME_LENGTH, null, null);
        case ENUMSET:
        case ENUMSETALPHA:
        case XENUMSET:
            // return as array of strings
            return new JsonSchemaSet(makeType(required, "array"), comment, null, new JsonSchemaString("string", comment, null, MAX_ENUM_NAME_LENGTH, null, null));
        case OBJECT: {
            final ObjectReference objectReference = (ObjectReference) metaData;
            return buildJsonSchemaObject(objectReference.getLowerBound(), comment, required, true);
        }
        case TEMPORAL: {
            final String bonaparteType = metaData.getBonaparteType().toLowerCase();
            switch (bonaparteType) {
            case "day":
                return new JsonSchemaString(str, comment, 10, 10, null, "date");
            case "time":
                return new JsonSchemaString(str, comment, null, null, null, "time");
            case "timestamp":
            case "instant":
                return new JsonSchemaString(str, comment, null, null, null, "date-time");
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
