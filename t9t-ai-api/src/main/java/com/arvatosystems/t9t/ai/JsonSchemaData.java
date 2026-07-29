package com.arvatosystems.t9t.ai;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.pojos.meta.ClassDefinition;

/**
 * Data structure which is used to determine the recursive structure for a JSON schema.
 */
public record JsonSchemaData(
        // String pqon,                                // PQON of the class (is part of classDefinition)
        ClassDefinition classDefinition,            // class definition (meta data) of the class
        Map<String, JsonSchemaData> subclasses,     // all subclasses of this class, by PQON
        Map<String, JsonSchemaData> referencedPqons // all defs needed for fields
    ) {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonSchemaData.class);

    public void log(final int depth) {
        final String subclassNames = this.subclasses().isEmpty() ? "none" : String.join(", ", this.subclasses().keySet());
        final String referencedPqonNames = this.referencedPqons().isEmpty() ? "none" : String.join(", ", this.referencedPqons().keySet());
        LOGGER.debug("\nPQON {}\n  subclasses: {}\n  references {}\n", classDefinition.getName(), subclassNames, referencedPqonNames);

        if (depth > 0) {
            for (final var subClassData : this.subclasses().values()) {
                subClassData.log(depth - 1);
            }
            for (final var refClassData : this.referencedPqons().values()) {
                refClassData.log(depth - 1);
            }
        }
    }
}
