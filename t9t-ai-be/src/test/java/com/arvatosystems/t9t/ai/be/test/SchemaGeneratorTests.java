package com.arvatosystems.t9t.ai.be.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import de.jpaw.bonaparte.pojos.meta.ClassDefinition;

import com.arvatosystems.t9t.ai.JsonSchemaCreator;
import com.arvatosystems.t9t.ai.jsonSchema.JsonSchemaObject;
import com.arvatosystems.t9t.base.entities.AdditionalHistoryTableColumns;
import com.arvatosystems.t9t.jackson.JacksonTools;

/**
 * Unit tests to validate that schema generation works as expected. The tests compare the generated JSON schema with the expected JSON schema defined as constants.
 *
 * WARNING; The tests are fragile and depend on the implementation of the JDK Map, because the order of the generated properties is undefined.
 */
public class SchemaGeneratorTests {
    final ObjectMapper objectMapper = JacksonTools.createObjectMapper();

    /**
     * Defines the expected JSON schema as a constant, when no PQONs are generated.
     * The replaceAll removes any line feeds and leading spaces, so that the string can be compared with the generated JSON schema.
     * Due to that, the "@PQON" property is not the first in the schema EXPECTED_RESULT_WITH_PQON.
     */
    static final String EXPECTED_RESULT_NO_PQON = """
        {
            "type":"object","description":"no PQON",
            "properties":{
                "crud":{
                    "type":"string",
                    "enum":["EXECUTE","SEARCH","LOOKUP","CREATE","READ","UPDATE","DELETE","INACTIVATE","ACTIVATE","VERIFY","MERGE","PATCH","EXPORT","IMPORT","CONFIGURE","CONTEXT","ADMIN","APPROVE","REJECT","CUSTOM"]
                },
                "localTimestamp":{"type":"string","format":"date-time"},
                "historyNo":{"type":"number","description":"maybe we do not need this, because we have the version now"}
            },
            "required":["crud","historyNo","localTimestamp"],
            "additionalProperties":false
        }
        """.replaceAll("\n *", "");

    /**
     * Defines the expected JSON schema as a constant, when PQONs are generated.
     * The replaceAll removes any line feeds and leading spaces, so that the string can be compared with the generated JSON schema.
     */
    static final String EXPECTED_RESULT_WITH_PQON = """
        {
            "type":"object","description":"with PQON",
            "properties":{
                "crud":{
                    "type":"string",
                    "enum":["EXECUTE","SEARCH","LOOKUP","CREATE","READ","UPDATE","DELETE","INACTIVATE","ACTIVATE","VERIFY","MERGE","PATCH","EXPORT","IMPORT","CONFIGURE","CONTEXT","ADMIN","APPROVE","REJECT","CUSTOM"]
                },
                "localTimestamp":{"type":"string","format":"date-time"},
                "@PQON":{"type":"string","description":"The partially qualified object name (PQON) of the object.","enum":["t9t.base.entities.AdditionalHistoryTableColumns"]},
                "historyNo":{"type":"number","description":"maybe we do not need this, because we have the version now"}
            },
            "required":["@PQON","crud","historyNo","localTimestamp"],
            "additionalProperties":false
        }
        """.replaceAll("\n *", "");

    @Test
    public void testSchemaGenerationWithoutPqon() throws JsonProcessingException {
        final ClassDefinition classDef = AdditionalHistoryTableColumns.BClass.INSTANCE.getMetaData();
        final JsonSchemaObject obj = JsonSchemaCreator.buildJsonSchemaObject(classDef, "no PQON", false);
        final String jsonSchema = objectMapper.writeValueAsString(obj);
        System.out.println(jsonSchema);
        System.out.println(EXPECTED_RESULT_NO_PQON);

        assertEquals(EXPECTED_RESULT_NO_PQON, jsonSchema, "JSON schema without PQON differs.");
    }

    @Test
    public void testSchemaGenerationWithPqon() throws JsonProcessingException {
        final ClassDefinition classDef = AdditionalHistoryTableColumns.BClass.INSTANCE.getMetaData();
        final JsonSchemaObject obj = JsonSchemaCreator.buildJsonSchemaObject(classDef, "with PQON", true);
        final String jsonSchema = objectMapper.writeValueAsString(obj);
        System.out.println(jsonSchema);
        System.out.println(EXPECTED_RESULT_WITH_PQON);

        assertEquals(EXPECTED_RESULT_WITH_PQON, jsonSchema, "JSON schema with PQON differs.");
    }
}
