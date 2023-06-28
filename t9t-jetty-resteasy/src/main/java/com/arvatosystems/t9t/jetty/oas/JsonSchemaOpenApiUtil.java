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
package com.arvatosystems.t9t.jetty.oas;

import com.fasterxml.jackson.databind.JavaType;

import de.jpaw.util.ByteArray;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.XML;


public final class JsonSchemaOpenApiUtil {
    private static final String SCHEMA_NAME = "Json";
    private static final String SCHEMA_REF = "#/components/schemas/" + SCHEMA_NAME;
    private static final String XML_PREFIX_BON = "bon";
    private static final String XML_NAME_KVP = "kvp";
    private static final String PROPERTY_KEY = "key";
    private static final String PROPERTY_NUM = "num";
    private static final String PROPERTY_NUMS = "nums";
    private static final String PROPERTY_VALUE = "value";
    private static final String PROPERTY_VALUES = "values";
    private static final String PROPERTY_BOOL = "bool";
    private static final String PROPERTY_BOOLS = "bools";
    private static final String PROPERTY_OBJ = "obj";
    private static final String PROPERTY_OBJS = "objs";
    private static final String PROPERTY_ANY = "any";
    private static final String PROPERTY_ANYS = "anys";
    private static final String Z_FIELD = "z";

    private JsonSchemaOpenApiUtil() { }

    /**
     * Add JSON schema (z field) to the OpenApi specification
     * @param oas
     */
    public static void addJsonSchema(final OpenAPI oas) {
        final XML propertyXml = new XML().prefix(XML_PREFIX_BON);
        final Schema jsonSchema = new ObjectSchema();

        final Schema additionalProperties = new ObjectSchema().xml(new XML().prefix(XML_PREFIX_BON).name(XML_NAME_KVP));

        final Schema keyProperty = new StringSchema().xml(propertyXml)
                .example("Key is mandatory and pair with one of the following value type.")
                .description("Key is mandatory and pair with one of the following value type.");
        additionalProperties.addProperties(PROPERTY_KEY, keyProperty);

        final Schema numProperty = new NumberSchema().xml(propertyXml).nullable(true);
        additionalProperties.addProperties(PROPERTY_NUM, numProperty);

        final Schema numsProperty = new ArraySchema().items(new NumberSchema()).xml(propertyXml).nullable(true);
        additionalProperties.addProperties(PROPERTY_NUMS, numsProperty);

        final Schema valueProperty = new StringSchema().xml(propertyXml).nullable(true);
        additionalProperties.addProperties(PROPERTY_VALUE, valueProperty);

        final Schema valuesProperty = new ArraySchema().items(new StringSchema()).xml(propertyXml).nullable(true);
        additionalProperties.addProperties(PROPERTY_VALUES, valuesProperty);

        final Schema boolProperty = new BooleanSchema().xml(propertyXml).nullable(true);
        additionalProperties.addProperties(PROPERTY_BOOL, boolProperty);

        final Schema boolsProperty = new ArraySchema().items(new BooleanSchema()).xml(propertyXml).type("boolean")
                .nullable(true);
        additionalProperties.addProperties(PROPERTY_BOOLS, boolsProperty);

        // There is an issue in displaying the example if $ref is set.
        // There is also an issue to display schema in a circular loop.
        final Schema objProperty = new StringSchema().xml(propertyXml).nullable(true).example("Json object.").description("Json object.");
//        objProperty.set$ref(SCHEMA_NAME);
        additionalProperties.addProperties(PROPERTY_OBJ, objProperty);

        final Schema objsProperty = new ArraySchema().items(objProperty)
                .xml(propertyXml).nullable(true).example(new String[] { "Array of Json object." })
                .description("Array of Json object.");
        additionalProperties.addProperties(PROPERTY_OBJS, objsProperty);

        final Schema anyProperty = new StringSchema().xml(propertyXml).nullable(true).example("Any type.")
                .description("Any type.");
        additionalProperties.addProperties(PROPERTY_ANY, anyProperty);

        final Schema anysProperty = new ArraySchema().items(new StringSchema()).xml(propertyXml).nullable(true)
                .example(new String[] { "Array of any type." }).description("Array of any type.");
        additionalProperties.addProperties(PROPERTY_ANYS, anysProperty);

        jsonSchema.setAdditionalProperties(additionalProperties);
        jsonSchema.description("z field");
        jsonSchema.nullable(true);
        oas.schema(SCHEMA_NAME, jsonSchema);
    }

    /**
     * Returns an implementation of ModelConverter which sets $ref to Json schema if the property is z field.
     */
    public static ModelConverter getJsonModelConverter() {
        return (type, context, chain) -> {
            if (Z_FIELD.equals(type.getPropertyName())) {
                final ObjectSchema os = new ObjectSchema();
                os.set$ref(SCHEMA_REF);
                os.setNullable(true);
                return os;
            }

            if (chain.hasNext()) {
                return chain.next().resolve(type, context, chain);
            } else {
                return null;
            }
        };
    }

    /**
     * Returns an implementation of ModelConverter which describes binary data.
     */
    public static ModelConverter getByteArrayModelConverter() {
        return (type, context, chain) -> {
            if (type.isSchemaProperty()) {
                final JavaType myType = Json.mapper().constructType(type.getType());
                if (myType != null) {
                    final Class<?> cls = myType.getRawClass();
                    if (ByteArray.class.isAssignableFrom(cls)) {
                        final StringSchema ss = new StringSchema();
                        ss.setExample("SGVsbG8gd29ybGQh");
                        ss.setDescription("Binary data, as base64 encoded string");
                        return ss;
                    }
                }
            }

            if (chain.hasNext()) {
                return chain.next().resolve(type, context, chain);
            } else {
                return null;
            }
        };
    }
}
