package com.arvatosystems.t9t.ai.be.request;

import com.arvatosystems.t9t.ai.T9tAiTools;
import com.arvatosystems.t9t.ai.mcp.AbstractJsonSchemaField;
import com.arvatosystems.t9t.ai.mcp.AiToolSpecification;
import com.arvatosystems.t9t.ai.mcp.JsonSchemaArray;
import com.arvatosystems.t9t.ai.mcp.JsonSchemaBoolean;
import com.arvatosystems.t9t.ai.mcp.JsonSchemaEnum;
import com.arvatosystems.t9t.ai.mcp.JsonSchemaNumber;
import com.arvatosystems.t9t.ai.mcp.JsonSchemaObject;
import com.arvatosystems.t9t.ai.mcp.JsonSchemaSet;
import com.arvatosystems.t9t.ai.mcp.JsonSchemaString;
import com.arvatosystems.t9t.ai.request.AiGetToolsRequest;
import com.arvatosystems.t9t.ai.request.AiGetToolsResponse;
import com.arvatosystems.t9t.ai.service.AiToolRegistry;
import com.arvatosystems.t9t.ai.tools.AiToolMediaDataResult;
import com.arvatosystems.t9t.ai.tools.AiToolNoResult;
import com.arvatosystems.t9t.ai.tools.AiToolStringResult;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.server.services.IAuthorize;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;
import de.jpaw.bonaparte.pojos.meta.ClassDefinition;
import de.jpaw.bonaparte.pojos.meta.EnumDataItem;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.ObjectReference;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiGetToolsRequestHandler extends AbstractReadOnlyRequestHandler<AiGetToolsRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiGetToolsRequestHandler.class);
    private static final int MAX_ENUM_NAME_LENGTH = 80;

    private final IAuthorize authorizer = Jdp.getRequired(IAuthorize.class);

    @Override
    public AiGetToolsResponse execute(final RequestContext ctx, final AiGetToolsRequest request) {
        final List<AiToolSpecification> toolSpecifications = new ArrayList<>(AiToolRegistry.size());
        AiToolRegistry.forEach(tool -> {
            final ClassDefinition metaData = tool.requestClass().getMetaData();
            final Permissionset permissions = authorizer.getPermissions(ctx.internalHeaderParameters.getJwtInfo(), PermissionType.TOOL_CALL, tool.name());
            if (!permissions.contains(OperationType.EXECUTE)) {
                return; // skip tool if no EXECUTE permission
            }
            final AiToolSpecification spec = new AiToolSpecification();
            spec.setName(tool.name());
            spec.setTitle(null);  // TODO: not yet available
            spec.setDescription(T9tAiTools.getToolDescription(metaData));
            spec.setInputSchema(buildObject(metaData, null));
            // optionally specify result structure
            final ClassDefinition resultMetaData = tool.resultClass().getMetaData();
            if (resultMetaData != null) {
                final String resultType = resultMetaData.getClassRef().getCanonicalName();
                if (resultType.equals(AiToolNoResult.class.getCanonicalName())) {
                    spec.setOutputSchema(null); // no result
                } else if (resultType.equals(AiToolStringResult.class.getCanonicalName())) {
                    spec.setOutputSchema(null); // uses unstructured result
                } else if (resultType.equals(AiToolMediaDataResult.class.getCanonicalName())) {
                    spec.setOutputSchema(null); // uses unstructured result
                } else {
                    spec.setOutputSchema(buildObject(resultMetaData, null));
                }
            }

            toolSpecifications.add(spec);
        });
        LOGGER.info("User {} has permissions to {} of total {} AI tools", ctx.internalHeaderParameters.getJwtInfo().getUserId(), toolSpecifications.size(), AiToolRegistry.size());
        final AiGetToolsResponse response = new AiGetToolsResponse();
        response.setTools(toolSpecifications);
        return response;
    }

    /**
     * Builds a JSON schema object from the class definition.
     *
     * @param metaData the class definition containing field information
     * @return a JsonSchemaObject representing the class definition
     */
    private JsonSchemaObject buildObject(@Nullable final ClassDefinition metaData, @Nullable final String description) {
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

    private AbstractJsonSchemaField buildField(final FieldDefinition metaData) {
        final String comment = metaData.getTrailingComment();
        switch (metaData.getMultiplicity()) {
        case SCALAR:
            return buildFieldNoArray(metaData, comment);
        case ARRAY:
        case LIST:
            return new JsonSchemaArray("array", comment, metaData.getMinCount(), metaData.getMaxCount() > 0 ? metaData.getMaxCount() : null,
                buildFieldNoArray(metaData, null));
        case MAP:
            return buildObject(null, comment); // maps are represented as objects in JSON schema
        case SET:
            return new JsonSchemaSet("array", comment, true, buildFieldNoArray(metaData, null));
        default:
            throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "Multiplicity: " + metaData.getMultiplicity());
        }
    }

    private AbstractJsonSchemaField buildFieldNoArray(final FieldDefinition metaData, final String comment) {
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
            return buildObject(objectReference.getLowerBound(), comment);
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
