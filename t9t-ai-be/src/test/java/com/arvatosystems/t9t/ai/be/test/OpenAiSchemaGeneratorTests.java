package com.arvatosystems.t9t.ai.be.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import de.jpaw.bonaparte.pojos.meta.ClassDefinition;

import com.arvatosystems.t9t.ai.ClassWalker;
import com.arvatosystems.t9t.ai.JsonSchemaCreatorWithOpenAiWorkaround;
import com.arvatosystems.t9t.ai.JsonSchemaData;
import com.arvatosystems.t9t.ai.jsonSchema.JsonSchemaObject;
import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.entities.AdditionalHistoryTableColumns;
import com.arvatosystems.t9t.jackson.JacksonTools;

/**
 * Unit tests to validate that schema generation works as expected. The tests compare the generated JSON schema with the expected JSON schema defined as constants.
 *
 * WARNING; The tests are fragile and depend on the implementation of the JDK Map, because the order of the generated properties is undefined.
 */
public class OpenAiSchemaGeneratorTests {
    final ObjectMapper objectMapper = JacksonTools.createObjectMapper();

    /**
     * Defines the expected JSON schema as a constant, when no PQONs are generated.
     * The replaceAll removes any line feeds and leading spaces, so that the string can be compared with the generated JSON schema.
     * Due to that, the "@PQON" property is not the first in the schema EXPECTED_RESULT_WITH_PQON.
     */
    static final String EXPECTED_RESULT_NO_PQON = """
        {
          "$defs" : {
            "t9t.base.entities.AdditionalHistoryTableColumns" : {
              "type" : "object",
              "properties" : {
                "crud" : {
                  "type" : "string",
                  "enum" : [ "EXECUTE", "SEARCH", "LOOKUP", "CREATE", "READ", "UPDATE", "DELETE", "INACTIVATE", "ACTIVATE", "VERIFY", "MERGE", "PATCH", "EXPORT", "IMPORT", "CONFIGURE", "CONTEXT", "ADMIN", "APPROVE", "REJECT", "CUSTOM" ]
                },
                "localTimestamp" : {
                  "type" : "string",
                  "format" : "date-time"
                },
                "@PQON" : {
                  "type" : "string",
                  "description" : "The partially qualified object name (PQON) of the object.",
                  "enum" : [ "t9t.base.entities.AdditionalHistoryTableColumns" ]
                },
                "historyNo" : {
                  "type" : "number",
                  "description" : "maybe we do not need this, because we have the version now"
                }
              },
              "required" : [ "@PQON", "crud", "historyNo", "localTimestamp" ],
              "additionalProperties" : false
            }
          },
          "$ref" : "#/$defs/t9t.base.entities.AdditionalHistoryTableColumns"
        }
        """;

    @Test
    public void testSchemaGenerationOpenAi() throws JsonProcessingException {
        MessagingUtil.initializeBonaparteParsers();
        final ClassDefinition classDef = AdditionalHistoryTableColumns.BClass.INSTANCE.getMetaData();
        final JsonSchemaData schemaData = ClassWalker.getSchemaData(classDef.getName());
        final var schemaCreator = new JsonSchemaCreatorWithOpenAiWorkaround(true);
        final JsonSchemaObject obj = schemaCreator.buildJsonSchemaObject(classDef, "no PQON", true, true);
        obj.setDefs(schemaCreator.createDefs(schemaData, Map.of()));
        final String jsonSchema = JacksonTools.prettyPrint(objectMapper, obj);
        System.out.println(jsonSchema);
        System.out.println(EXPECTED_RESULT_NO_PQON);

        assertEquals(EXPECTED_RESULT_NO_PQON.replace("\n", ""), jsonSchema.replace("\n", ""), "JSON schema without PQON differs.");
    }

    @Test
    public void testSchemaGenerationWithoutWorkaroundUsesPlainRefForOptionalObjectField() throws JsonProcessingException {
        MessagingUtil.initializeBonaparteParsers();
        final String pqon = "t9t.ai.mcp.AiToolSpecification";
        final JsonSchemaData schemaData = ClassWalker.getSchemaData(pqon);
        final var schemaCreator = new JsonSchemaCreatorWithOpenAiWorkaround(false);
        final JsonSchemaObject obj = schemaCreator.buildJsonSchemaObject(schemaData.classDefinition(), null, true, true);
        obj.setDefs(schemaCreator.createDefs(schemaData, Map.of()));

        final JsonNode outputSchemaNode = objectMapper.readTree(objectMapper.writeValueAsString(obj))
                .get("$defs").get(pqon).get("properties").get("outputSchema");

        assertTrue(outputSchemaNode.has("$ref"), "Optional object fields should use a plain $ref without the workaround.");
        assertFalse(outputSchemaNode.has("anyOf"), "Optional object fields should not use anyOf without the workaround.");
    }
}
