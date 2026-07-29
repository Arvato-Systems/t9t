package com.arvatosystems.t9t.ai.be.request;

import java.util.Map;

import com.arvatosystems.t9t.ai.ClassWalker;
import com.arvatosystems.t9t.ai.JsonSchemaCreatorWithOpenAiWorkaround;
import com.arvatosystems.t9t.ai.request.CreateJsonSchemaRequest;
import com.arvatosystems.t9t.ai.request.CreateJsonSchemaResponse;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

public class CreateJsonSchemaRequestHandler extends AbstractRequestHandler<CreateJsonSchemaRequest> {

    @Override
    public CreateJsonSchemaResponse execute(final RequestContext ctx, final CreateJsonSchemaRequest request) throws Exception {
        final var schemaData = ClassWalker.getSchemaData(request.getPqon());

        final var jsonSchemaObject = JsonSchemaCreatorWithOpenAiWorkaround.buildJsonSchemaObject(schemaData.classDefinition(), null, true, true);
        jsonSchemaObject.setDefs(JsonSchemaCreatorWithOpenAiWorkaround.createDefs(schemaData, T9tUtil.nvl(request.getMappings(), Map.of())));

        // create the response object and set the JSON schema
        final CreateJsonSchemaResponse response = new CreateJsonSchemaResponse();
        response.setJsonSchema(jsonSchemaObject);
        return response;
    }
}
