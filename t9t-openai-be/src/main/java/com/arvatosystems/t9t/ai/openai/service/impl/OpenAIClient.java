package com.arvatosystems.t9t.ai.openai.service.impl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.ai.AiChatLogDTO;
import com.arvatosystems.t9t.ai.AiConversationRef;
import com.arvatosystems.t9t.ai.AiRoleType;
import com.arvatosystems.t9t.ai.openai.AbstractOpenAIObject;
import com.arvatosystems.t9t.ai.openai.OpenAIBetaSpecifier;
import com.arvatosystems.t9t.ai.openai.OpenAIChatCompletionChoice;
import com.arvatosystems.t9t.ai.openai.OpenAIChatCompletionReq;
import com.arvatosystems.t9t.ai.openai.OpenAICreateEmbeddingsReq;
import com.arvatosystems.t9t.ai.openai.OpenAIFunction;
import com.arvatosystems.t9t.ai.openai.OpenAIFunctionCall;
import com.arvatosystems.t9t.ai.openai.OpenAIFunctionParameters;
import com.arvatosystems.t9t.ai.openai.OpenAIMessage;
import com.arvatosystems.t9t.ai.openai.OpenAIObjectChatCompletion;
import com.arvatosystems.t9t.ai.openai.OpenAIObjectCreateEmbeddings;
import com.arvatosystems.t9t.ai.openai.OpenAIObjectFile;
import com.arvatosystems.t9t.ai.openai.OpenAIParameterType;
import com.arvatosystems.t9t.ai.openai.OpenAIPurposeType;
import com.arvatosystems.t9t.ai.openai.OpenAIQueryParameters;
import com.arvatosystems.t9t.ai.openai.OpenAIReq;
import com.arvatosystems.t9t.ai.openai.OpenAIRoleType;
import com.arvatosystems.t9t.ai.openai.OpenAISingleParameter;
import com.arvatosystems.t9t.ai.openai.OpenAITool;
import com.arvatosystems.t9t.ai.openai.OpenAIToolCall;
import com.arvatosystems.t9t.ai.openai.OpenAIToolType;
import com.arvatosystems.t9t.ai.openai.T9tOpenAIConstants;
import com.arvatosystems.t9t.ai.openai.T9tOpenAIException;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAICreateAssistantReq;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAICreateVectorStoreReq;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectAssistant;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectListAssistants;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectListThreadMessages;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectListThreadRuns;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectThreadMessage;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectThreadRun;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIObjectVectorStore;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIThread;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIThreadMessageReq;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIThreadRunReq;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIToolOutput;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIToolOutputReq;
import com.arvatosystems.t9t.ai.openai.jackson.OpenAIModule;
import com.arvatosystems.t9t.ai.openai.service.IOpenAIClient;
import com.arvatosystems.t9t.ai.service.AiToolDescriptor;
import com.arvatosystems.t9t.ai.service.AiToolRegistry;
import com.arvatosystems.t9t.ai.service.IAiTool;
import com.arvatosystems.t9t.ai.service.IAiChatLogService;
import com.arvatosystems.t9t.ai.tools.AiToolStringResult;
import com.arvatosystems.t9t.ai.tools.AbstractAiTool;
import com.arvatosystems.t9t.ai.tools.AbstractAiToolResult;
import com.arvatosystems.t9t.base.JsonUtil;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.T9tHttpClientExtensions;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.UplinkConfiguration;
import com.arvatosystems.t9t.jackson.JacksonTools;
import com.arvatosystems.t9t.jackson.JpawModule;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.json.JsonMapper.Builder;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.MimeTypes;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.bonaparte.pojos.meta.ClassDefinition;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.Multiplicity;
import de.jpaw.bonaparte.util.ToStringHelper;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ExceptionUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@Singleton
public class OpenAIClient implements IOpenAIClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAIClient.class);
    private static final OpenAIBetaSpecifier ASSISTANTS_BETA = OpenAIBetaSpecifier.ASST_V2;
    private static final String ASSISTANTS_PATH = "/v1/assistants";
    private static final String THREADS_PATH = "/v1/threads";
    private static final String RUNS_SUFFIX = "/runs";
    private static final String MESSAGES_SUFFIX = "/messages";
    private static final String SUBMIT_SUFFIX = "/submit_tool_outputs";
    private static final String FILES_PATH = "/v1/files";

    private final IAiChatLogService aiChatLogService = Jdp.getRequired(IAiChatLogService.class);

    private final boolean configured;
    private final ObjectMapper objectMapper;
    private final ObjectMapper objectMapperForToolCalls;
    private final Duration timeoutInMilliseconds;
    private final String authentication;
    private final String url;
    private final String client;
    private final HttpClient httpClient;


    public OpenAIClient() {
        final UplinkConfiguration config = ConfigProvider.getUplink(T9tOpenAIConstants.UPLINK_KEY_OPENAI);
        configured = config != null && config.getBasicAuth() != null && config.getUrl() != null;
        if (!configured) {
            LOGGER.info("No or incomplete configuration for OpenAI, service is disabled.");
            // set objectMapper and HttpClient to null, to allow them to be final
            objectMapper = null;
            objectMapperForToolCalls = null;
            timeoutInMilliseconds = null;
            url = null;
            authentication = null;
            client = null;
            httpClient = null;
        } else {
            LOGGER.info("Setting up OpenAI client.");
            objectMapper = createOpenAIObjectMapper();
            objectMapperForToolCalls = JacksonTools.createObjectMapper(false, true);
            timeoutInMilliseconds = Duration.ofMillis(T9tUtil.nvl(config.getTimeoutInMs(), 1000));
            url = config.getUrl();
            authentication = T9tOpenAIConstants.OPENAI_HTTP_AUTH + config.getBasicAuth();
            client = config.getClientId();
            httpClient = HttpClient.newBuilder().version(Version.HTTP_2)
                    .connectTimeout(timeoutInMilliseconds).build();
            LOGGER.info("OpenAI initialized.");
        }
    }

    @Nonnull
    private ObjectMapper createOpenAIObjectMapper() {
        Builder builder = JsonMapper.builder();
        builder.addModules(new JavaTimeModule(), new JpawModule(), new OpenAIModule());
        builder.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        builder.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        builder.serializationInclusion(Include.NON_NULL);
        builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return builder.build();
    }

    private HttpRequest buildRequest(@Nonnull final BodyPublisher publisher,
      @Nullable final String contentType, @Nonnull final String path, @Nullable final OpenAIBetaSpecifier betaHeader) throws Exception {

        final HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder(new URI(url + path))
                .version(Version.HTTP_2)
                .POST(publisher)
                .timeout(timeoutInMilliseconds);

        httpRequestBuilder.header(T9tConstants.HTTP_HEADER_AUTH, authentication);
        if (betaHeader != null) {
            httpRequestBuilder.header(T9tOpenAIConstants.OPENAI_HTTP_BETA, betaHeader.getToken());
        }
        if (client != null) {
            httpRequestBuilder.header(T9tOpenAIConstants.OPENAI_HTTP_CLIENT, client);
        }

        if (contentType != null) {
            httpRequestBuilder.header(T9tConstants.HTTP_HEADER_CONTENT_TYPE, contentType);
        }
        httpRequestBuilder.header(T9tConstants.HTTP_HEADER_ACCEPT,         MimeTypes.MIME_TYPE_JSON);
        httpRequestBuilder.header(T9tConstants.HTTP_HEADER_CHARSET,        T9tConstants.HTTP_CHARSET_UTF8);
        httpRequestBuilder.header(T9tConstants.HTTP_HEADER_ACCEPT_CHARSET, T9tConstants.HTTP_CHARSET_UTF8);

        return httpRequestBuilder.build();
    }


    protected <U extends AbstractOpenAIObject> U openAIHttpRequest(@Nonnull final HttpRequest httpReq, @Nonnull final Class<U> responseClass,
      @Nullable BonaPortable payload) throws Exception {
        final HttpResponse<byte[]> resp = httpClient.send(httpReq, HttpResponse.BodyHandlers.ofByteArray());
        if (resp.statusCode() != 200) {
            final String errmsg = new String(resp.body(), StandardCharsets.UTF_8);
            LOGGER.error("OpenAI request failed with status code {}: {}", resp.statusCode(), errmsg);
            switch (resp.statusCode()) {
            case 400: // invalid request
                LOGGER.error("OpenAI rejected the request: {}", errmsg);
                LOGGER.error("Request was {} {}{}", httpReq.method(), httpReq.uri(),
                  payload == null ? "" : (" with payload\n" + ToStringHelper.toStringML(payload)));
                throw new T9tException(T9tOpenAIException.OPENAI_INVALID_REQUEST);
            case 401: // invalid API key
                throw new T9tException(T9tException.NOT_AUTHENTICATED, "OpenAI");
            case 403: // invalid API key
                throw new T9tException(T9tException.NOT_AUTHORIZED, "OpenAI");
            case 404: // invalid API key or not part of organization
                throw new T9tException(T9tOpenAIException.OPENAI_ORGANIZATION);
            case 429: // Quota exceeded
                throw new T9tException(T9tOpenAIException.OPENAI_QUOTA_EXCEEDED);
            }
            throw new T9tException(T9tOpenAIException.OPENAI_CONNECTION_PROBLEM, resp.statusCode());
        }
        final U r = objectMapper.readValue(resp.body(), responseClass);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Full response from OpenAI is {}", ToStringHelper.toStringML(r));
        }
        r.validate();
        return r;
    }

    /** Perform a POST with a payload, taking path and beta information from properties. */
    protected <T extends OpenAIReq, U extends AbstractOpenAIObject> U performOpenAIRequest(final T request, final OpenAIBetaSpecifier betaHeader,
      final Class<U> responseClass) {
        final Map<String, String> requestProperties = request.ret$MetaData().getProperties();
        final String path = requestProperties.get("path");
        return performOpenAIRequest(request, path, betaHeader, responseClass);
    }

    /** Perform a POST with a multipart payload and specified path and header. */
    protected <U extends AbstractOpenAIObject> U performOpenAIRequest(@Nonnull final Map<String, Object> data,
      final String path, final OpenAIBetaSpecifier betaHeader, final Class<U> responseClass) {
        if (!configured) {
            throw new T9tException(T9tOpenAIException.OPENAI_NOT_CONFIGURED);
        }
        try {
            // create a boundary string
            final String boundary = (UUID.randomUUID().toString() + UUID.randomUUID().toString()).replace("-", "");
            final HttpRequest httpReq = buildRequest(T9tHttpClientExtensions.ofMultipartFormData(data, boundary),
             T9tConstants.HTTP_MULTIPART_FD_PREFIX + boundary, path, betaHeader);
            return openAIHttpRequest(httpReq, responseClass, null);
        } catch (final Exception e) {
            LOGGER.error("Exception in OpenAI POST request", e);
            throw new T9tException(T9tOpenAIException.OPENAI_CONNECTION_PROBLEM, e);
        }
    }

    /** Perform a POST with a payload and specified path and header. */
    protected <T extends OpenAIReq, U extends AbstractOpenAIObject> U performOpenAIRequest(final T request,
      final String path, final OpenAIBetaSpecifier betaHeader, final Class<U> responseClass) {
        if (!configured) {
            throw new T9tException(T9tOpenAIException.OPENAI_NOT_CONFIGURED);
        }
        try {
            final HttpRequest httpReq = buildRequest(BodyPublishers.ofByteArray(objectMapper.writeValueAsBytes(request)),
              MimeTypes.MIME_TYPE_JSON, path, betaHeader);
            return openAIHttpRequest(httpReq, responseClass, request);
        } catch (final Exception e) {
            LOGGER.error("Exception in OpenAI POST request", e);
            throw new T9tException(T9tOpenAIException.OPENAI_CONNECTION_PROBLEM, e);
        }
    }

    /** Perform a POST without a payload. */
    protected <U extends AbstractOpenAIObject> U performOpenAIRequestWithoutPayload(final String path, final OpenAIBetaSpecifier betaHeader,
      final Class<U> responseClass) {
        if (!configured) {
            throw new T9tException(T9tOpenAIException.OPENAI_NOT_CONFIGURED);
        }
        try {
            final HttpRequest httpReq = buildRequest(BodyPublishers.noBody(), null, path, betaHeader);
            return openAIHttpRequest(httpReq, responseClass, null);
        } catch (final Exception e) {
            LOGGER.error("Exception in OpenAI POST request", e);
            throw new T9tException(T9tOpenAIException.OPENAI_CONNECTION_PROBLEM, e);
        }
    }

    /** Perform a GET. */
    protected <U extends AbstractOpenAIObject> U performOpenAIGetRequest(final String path, final OpenAIBetaSpecifier betaHeader,
      final Class<U> responseClass) {
        if (!configured) {
            throw new T9tException(T9tOpenAIException.OPENAI_NOT_CONFIGURED);
        }
        try {
            final HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder(new URI(url + path))
                    .version(Version.HTTP_2)
                    .GET()
                    .timeout(timeoutInMilliseconds);

            httpRequestBuilder.header(T9tConstants.HTTP_HEADER_AUTH, authentication);
            if (betaHeader != null) {
                httpRequestBuilder.header(T9tOpenAIConstants.OPENAI_HTTP_BETA, betaHeader.getToken());
            }
            if (client != null) {
                httpRequestBuilder.header(T9tOpenAIConstants.OPENAI_HTTP_CLIENT, client);
            }

            httpRequestBuilder.header(T9tConstants.HTTP_HEADER_ACCEPT,         MimeTypes.MIME_TYPE_JSON);
            httpRequestBuilder.header(T9tConstants.HTTP_HEADER_ACCEPT_CHARSET, T9tConstants.HTTP_CHARSET_UTF8);

            final HttpRequest httpReq = httpRequestBuilder.build();
            return openAIHttpRequest(httpReq, responseClass, null);
        } catch (final Exception e) {
            LOGGER.error("Exception in OpenAI GET request", e);
            throw new T9tException(T9tOpenAIException.OPENAI_CONNECTION_PROBLEM, e);
        }
    }

    @Override
    public OpenAIObjectChatCompletion performOpenAIChatCompletion(final OpenAIChatCompletionReq request) {
        return performOpenAIRequest(request, null, OpenAIObjectChatCompletion.class);
    }

    @Override
    public OpenAIObjectChatCompletion performOpenAIChatCompletionWithToolCalls(final RequestContext ctx, final OpenAIChatCompletionReq request,
      final List<String> toolSelection, final int maxToolCalls) {
        // shortcut in case no tools provided or no tool calls allowed
        if (maxToolCalls <= 0) {
            // delegate to simpler variant
            return performOpenAIChatCompletion(request);
        }
        // build the tool type information from tool metadata
        request.setTools(buildToolsFromStack(toolSelection, false, false));  // code_interpreter is not supported (yet?) for chat completion

        int countToolCalls = 0;
        List<OpenAIMessage> messages = request.getMessages();
        for (;;) {
            LOGGER.debug("Calling ChatCompletion with {} messages and {} tools", request.getMessages().size(), request.getTools().size());
            final OpenAIObjectChatCompletion response = performOpenAIChatCompletion(request);
            final List<OpenAIChatCompletionChoice> choices = response.getChoices();
            if (choices.isEmpty()) {
                return response;
            }
            final OpenAIChatCompletionChoice firstChoice = choices.get(0);
            if (firstChoice.getMessage().getToolCalls() == null) {
                return response;
            }
            // append tool, get response (mocked for now)
            if (firstChoice.getMessage().getRole() == null) {
                return response;
            }
            messages.add(firstChoice.getMessage());
            // construct responses for the tool calls
            for (final OpenAIToolCall toolCall : firstChoice.getMessage().getToolCalls()) {
                // call the indicated function
                messages.add(performToolCall(ctx, toolCall, null));
                if (++countToolCalls >= maxToolCalls) {
                    return response;
                }
            }
            if (++countToolCalls >= maxToolCalls) {
                return response;
            }
        }
    }

    /**
     * Performs a callback (function call). In case a conversationRef is provided, the call is logged.
     *
     * @param ctx             the RequestContext
     * @param toolCall        the description of the function
     * @param conversationRef an optional reference to an assistant conversation
     *
     * @return the response message
     */
    protected OpenAIMessage performToolCall(@Nonnull final RequestContext ctx, @Nonnull final OpenAIToolCall toolCall, @Nullable final Long conversationRef) {
        final OpenAIFunctionCall functionCall = toolCall.getFunction();
        final OpenAIChatCompletionChoice toolResponse = new OpenAIChatCompletionChoice();
        final OpenAIMessage toolResultMessage = new OpenAIMessage();
        toolResponse.setMessage(toolResultMessage);
        toolResultMessage.setRole(OpenAIRoleType.TOOL);
        toolResultMessage.setToolCallId(toolCall.getId());

        final AiToolDescriptor<?, ?> tool = AiToolRegistry.get(functionCall.getName());
        if (tool == null) {
            LOGGER.error("Tool {} not found in registry - LLM fantasizing?", functionCall.getName());
            toolResultMessage.setContent("ERROR! Tool not found in registry");
        } else {
            LOGGER.debug("Calling {} with parameters {}", functionCall.getName(), functionCall.getArguments());
            // determine parameters as bonaparte object from JSOn representation
            try {
                final AbstractAiTool requestObject = tool.requestClass().newInstance();
                objectMapperForToolCalls.readerForUpdating(requestObject).readValue(functionCall.getArguments());
                // call the tool (hack to get it around type checks)
                final IAiTool toolInstance = tool.toolInstance();
                if (conversationRef != null) {
                    // log the call (to be completed)
                    logToolCall(ctx, conversationRef, tool.name(), requestObject);
                }
                final AbstractAiToolResult result = toolInstance.performToolCall(ctx, requestObject);
                // convert result to JSON
                if (result == null) {
                    toolResultMessage.setContent("Success! (No data returned by tool.)");
                } else if (result instanceof AiToolStringResult textResult) {
                    LOGGER.debug("Output of tool call to {} with arguments {} resulted in string {}",
                      functionCall.getName(), functionCall.getArguments(), textResult.getText());
                    toolResultMessage.setContent(textResult.getText());
                } else {
                    toolResultMessage.setContent(objectMapper.writeValueAsString(result));
                    LOGGER.debug("Output of tool call to {} with arguments {} returned object {}",
                              functionCall.getName(), functionCall.getArguments(), toolResultMessage.getContent());
                }
            } catch (final Exception e) {
                LOGGER.error("Exception in tool call", e);
                toolResultMessage.setContent("Exception: " + ExceptionUtil.causeChain(e));
            }
        }
        return toolResponse.getMessage();
    }

    protected void logToolCall(final RequestContext ctx, final Long conversationRef, final String function, final BonaPortable parameters) {
        // log the input
        final AiChatLogDTO chatLog = new AiChatLogDTO();
        chatLog.setConversationRef(new AiConversationRef(conversationRef));
        chatLog.setRoleType(AiRoleType.SYSTEM);
        chatLog.setFunctionPqon(function);
        chatLog.setFunctionParameter(parameters);
        aiChatLogService.saveAiChatLog(chatLog);
    }

    protected String stripJavadoc(final String javadoc) {
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

    protected int skipSpacesAndStars(final String javadoc, int pos, final int len, final boolean alsoNewline) {
        while (pos < len && (javadoc.charAt(pos) == ' ' || javadoc.charAt(pos) == '*'
                || (alsoNewline && (javadoc.charAt(pos) == '\n' || javadoc.charAt(pos) == '\r')))) {
            ++pos;
        }
        return pos;
    }

    protected OpenAITool createToolDescriptionFromClassDefinition(final String id, final ClassDefinition cd) {
        final OpenAITool tool = new OpenAITool();
        tool.setType(OpenAIToolType.FUNCTION);

        final OpenAIFunction function = new OpenAIFunction();
        tool.setFunction(function);
        function.setName(id);
        function.setDescription(T9tUtil.nvl(cd.getRegularComment(), stripJavadoc(cd.getJavaDoc())));
        if (!T9tUtil.isEmpty(cd.getFields())) {
            // define the parameters
            final OpenAIFunctionParameters parameters = new OpenAIFunctionParameters();
            parameters.setType(OpenAIParameterType.OBJECT);
            parameters.setProperties(buildPropertiesFromFields(cd.getFields()));
            parameters.setRequired(buildRequiredFromFields(cd.getFields()));
            function.setParameters(parameters);
        }
        return tool;
    }

    protected List<String> buildRequiredFromFields(final List<FieldDefinition> fields) {
        final List<String> required = new ArrayList<>(fields.size());
        for (final FieldDefinition field : fields) {
            if (field.getIsRequired()) {
                required.add(field.getName());
            }
        }
        return required;
    }

    protected Map<String, OpenAISingleParameter> buildPropertiesFromFields(final List<FieldDefinition> fields) {
        final Map<String, OpenAISingleParameter> properties = new HashMap<>(fields.size());
        for (final FieldDefinition field : fields) {
            final OpenAISingleParameter fieldProperties = new OpenAISingleParameter();
            fieldProperties.setType(buildTypeFromField(field));
            fieldProperties.setDescription(T9tUtil.nvl(field.getRegularComment(), field.getJavaDoc(), field.getTrailingComment(), field.getName()));
            properties.put(field.getName(), fieldProperties);
        }
        return properties;
    }

    protected OpenAIParameterType buildTypeFromField(final FieldDefinition field) {
        if (field.getMultiplicity() == Multiplicity.LIST) {
            return OpenAIParameterType.LIST;
        }
        // TODO: array of float would be embedding
        switch (field.getDataType().toLowerCase()) {
        case "string":
            return OpenAIParameterType.STRING;
        case "long":
        case "int":
        case "integer":
            return OpenAIParameterType.INTEGER;
        case "float":
        case "double":
        case "bigdecimal":
            return OpenAIParameterType.NUMBER;
        case "boolean":
            return OpenAIParameterType.BOOLEAN;
        default:
            return OpenAIParameterType.OBJECT;
        }
    }



    @Override
    public OpenAIObjectCreateEmbeddings performOpenAICreateEmbeddings(final OpenAICreateEmbeddingsReq request) {
        return performOpenAIRequest(request, ASSISTANTS_BETA, OpenAIObjectCreateEmbeddings.class);
    }

    @Override
    public List<OpenAITool> buildToolsFromStack(final List<String> selection, final boolean allowCoding, final boolean allowFileSearch) {
        final List<OpenAITool> tools;
        if (T9tUtil.isEmpty(selection)) {
            // use all available tools
            tools = new ArrayList<>(AiToolRegistry.size() + 2);
            AiToolRegistry.forEach(tool -> tools.add(createToolDescriptionFromClassDefinition(tool.name(), tool.requestClass().getMetaData())));
        } else {
            // use only the selected tools
            tools = new ArrayList<>(selection.size() + 2);
            for (final String toolName : selection) {
                final AiToolDescriptor<?, ?> tool = AiToolRegistry.get(toolName);
                if (tool == null) {
                    LOGGER.warn("Tool {} not found in registry - skipping", toolName);
                } else {
                    tools.add(createToolDescriptionFromClassDefinition(toolName, tool.requestClass().getMetaData()));
                }
            }
        }
        if (allowCoding) {
            final OpenAITool codingTool = new OpenAITool();
            codingTool.setType(OpenAIToolType.CODE_INTERPRETER);
            tools.add(codingTool);
        }
        if (allowFileSearch) {
            final OpenAITool searchTool = new OpenAITool();
            searchTool.setType(OpenAIToolType.FILE_SEARCH);
            tools.add(searchTool);
        }
        LOGGER.info("Built {} tools for request", tools.size());
        return tools;
    }

    @Override
    public void validateMetadata(@Nullable Map<String, Object> metadata) {
        if (metadata != null) {
            if (metadata.size() > T9tOpenAIConstants.MAX_METADATA_ENTRIES) {
                throw new T9tException(T9tOpenAIException.OPENAI_METADATA_TOO_LARGE, metadata.size());
            }
            for (final Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getKey().length() > T9tOpenAIConstants.MAX_METADATA_KEY_LENGTH) {
                    throw new T9tException(T9tOpenAIException.OPENAI_METADATA_KEY_TOO_LONG, entry.getKey());
                }
                if (entry.getValue() instanceof String s) {
                    if (s.length() > T9tOpenAIConstants.MAX_METADATA_VALUE_LENGTH) {
                        throw new T9tException(T9tOpenAIException.OPENAI_METADATA_VALUE_TOO_LONG, entry.getKey());
                    }
                } else {
                    throw new T9tException(T9tOpenAIException.OPENAI_METADATA_VALUE_WRONG_TYPE, entry.getKey());
                }
            }
        }
    }

    @Override
    public OpenAIObjectAssistant createAssistant(final OpenAICreateAssistantReq assistantReq,
      final boolean addAllTools, final boolean allowCoding, final boolean allowFileSearch) {
        if (addAllTools) {
            assistantReq.setTools(buildToolsFromStack(null, allowCoding, allowFileSearch));
        }
        return performOpenAIRequest(assistantReq, ASSISTANTS_BETA, OpenAIObjectAssistant.class);
    }

    @Override
    public OpenAIObjectAssistant getAssistantById(final String assistantId) {
        return performOpenAIGetRequest(ASSISTANTS_PATH + "/" + assistantId, ASSISTANTS_BETA, OpenAIObjectAssistant.class);
    }

    @Override
    public OpenAIObjectListAssistants listAssistants(final OpenAIQueryParameters queryParameters) {
        final String fullPath = addQueryParameters(ASSISTANTS_PATH, queryParameters);
        return performOpenAIGetRequest(fullPath, ASSISTANTS_BETA, OpenAIObjectListAssistants.class);
    }

    @Override
    public OpenAIThread createThread() {
        return performOpenAIRequestWithoutPayload(THREADS_PATH, ASSISTANTS_BETA, OpenAIThread.class);
    }

    @Override
    public OpenAIThread getThreadById(final String threadId) {
        return performOpenAIGetRequest(THREADS_PATH + "/" + threadId, ASSISTANTS_BETA, OpenAIThread.class);
    }

    @Override
    public OpenAIObjectThreadRun createRun(final String threadId, final OpenAIThreadRunReq request) {
        if (request.getThread() != null) {
            throw new T9tException(T9tOpenAIException.OPENAI_INVALID_REQUEST, "thread field must be null to start an existing thread");
        }
        final String fullPath = THREADS_PATH + "/" + threadId + RUNS_SUFFIX;
        return performOpenAIRequest(request, fullPath, ASSISTANTS_BETA, OpenAIObjectThreadRun.class);
    }

    @Override
    public OpenAIObjectThreadRun createThreadAndRun(final OpenAIThreadRunReq request) {
        return performOpenAIRequest(request, THREADS_PATH + RUNS_SUFFIX, ASSISTANTS_BETA, OpenAIObjectThreadRun.class);
    }

    @Override
    public OpenAIObjectListThreadRuns listThreadRuns(final String threadId, final OpenAIQueryParameters queryParameters) {
        final String fullPath = addQueryParameters(THREADS_PATH + "/" + threadId + RUNS_SUFFIX, queryParameters);
        return performOpenAIGetRequest(fullPath, ASSISTANTS_BETA, OpenAIObjectListThreadRuns.class);
    }

    @Override
    public String addQueryParameters(final String path, final OpenAIQueryParameters queryParameters) {
        if (queryParameters == null) {
            return path;  // nothing to do
        }
        boolean first = true;
        final StringBuilder fullPath = new StringBuilder(100);
        fullPath.append(path);
        if (queryParameters.getLimit() != null) {
            fullPath.append(first ? '?' : '&').append("limit=").append(queryParameters.getLimit());
            first = false;
        }
        if (queryParameters.getOrder() != null) {
            fullPath.append(first ? '?' : '&').append("order=").append(queryParameters.getOrder().getToken());
            first = false;
        }
        if (queryParameters.getBefore() != null) {
            fullPath.append(first ? '?' : '&').append("before=").append(queryParameters.getBefore());  // I hope IDs do not require URL-encoding
            first = false;
        }
        if (queryParameters.getAfter() != null) {
            fullPath.append(first ? '?' : '&').append("after=").append(queryParameters.getAfter());  // I hope IDs do not require URL-encoding
            first = false;
        }
        if (queryParameters.getRunId() != null) {
            fullPath.append(first ? '?' : '&').append("run_id=").append(queryParameters.getRunId());  // I hope IDs do not require URL-encoding
            first = false;
        }
        return fullPath.toString();
    }

    @Override
    public OpenAIObjectThreadRun getRun(final String threadId, final String runId) {
        final String fullPath = THREADS_PATH + "/" + threadId + RUNS_SUFFIX + "/" + runId;
        return performOpenAIGetRequest(fullPath, ASSISTANTS_BETA, OpenAIObjectThreadRun.class);
    }

    @Override
    public OpenAIObjectThreadRun submitToolOutputs(final String threadId, final String runId, final OpenAIToolOutputReq toolOutputs) {
        final String fullPath = THREADS_PATH + "/" + threadId + RUNS_SUFFIX + "/" + runId + SUBMIT_SUFFIX;
        return performOpenAIRequest(toolOutputs, fullPath, ASSISTANTS_BETA, OpenAIObjectThreadRun.class);
    }

    @Override
    public OpenAIObjectThreadRun createRunAndLoop(final RequestContext ctx, final String threadId, final OpenAIThreadRunReq request,
      final int maxSeconds, final long pollMillis, final Long conversationRef) {
        if (request.getThread() != null) {
            throw new T9tException(T9tOpenAIException.OPENAI_INVALID_REQUEST, "thread field must be null to start an existing thread");
        }
        final String fullPath = THREADS_PATH + "/" + threadId + RUNS_SUFFIX;
        final OpenAIObjectThreadRun initialState = performOpenAIRequest(request, fullPath, ASSISTANTS_BETA, OpenAIObjectThreadRun.class);
        return loopUntilCompletion(ctx, initialState, maxSeconds, pollMillis, conversationRef);
    }

    @Override
    public OpenAIObjectThreadRun loopUntilCompletion(final RequestContext ctx, final OpenAIObjectThreadRun initialState,
      final int maxSeconds, final long pollMillis, final Long conversationRef) {
        final Instant start = Instant.now();
        final Instant deadline = start.plusSeconds(maxSeconds);
        final String runId = initialState.getId();
        final String threadId = initialState.getThreadId();
        OpenAIObjectThreadRun currentState = initialState;
        while (Instant.now().isBefore(deadline)) {
            // check current state
            LOGGER.debug("   Current run state is {}", currentState.getStatus());
            switch (currentState.getStatus()) {
            case CANCELLED:
            case CANCELLING:
            case COMPLETED:
            case EXPIRED:
            case FAILED:
            case INCOMPLETE:
                // LOGGER.info("Returning {}", ToStringHelper.toStringML(currentState));
                return currentState;
            case IN_PROGRESS:
            case QUEUED:
                // wait
                T9tUtil.sleepAndWarnIfInterrupted(pollMillis, LOGGER, "Sleep interrupted while waiting for OpenAI run completion");
                // get fresh current state without doing anything
                currentState = getRun(threadId, runId);
                break;
            case REQUIRES_ACTION:
                final OpenAIToolOutputReq toolOutputs = computeToolOutputs(ctx, currentState, conversationRef);
                currentState = submitToolOutputs(threadId, runId, toolOutputs);
                break;
            default:
                throw new T9tException(T9tOpenAIException.OPENAI_UNKNOWN_RUN_STATUS, currentState.getStatus());
            }
        }
        // LOGGER.info("Returning {}", ToStringHelper.toStringML(currentState));
        return currentState;  // timed out!
    }

    protected OpenAIToolOutputReq computeToolOutputs(final RequestContext ctx, final OpenAIObjectThreadRun currentState, final Long conversationRef) {
        if (currentState.getRequiredAction() == null || currentState.getRequiredAction().getSubmitToolOutputs() == null) {
            throw new T9tException(T9tOpenAIException.OPENAI_EXPECTED_TOOL_OUTPUTS,
              currentState.getRequiredAction() == null ? "requiredAction" : "submitToolOutputs");
        }
        final List<OpenAIToolCall> toolCalls = currentState.getRequiredAction().getSubmitToolOutputs().getToolCalls();
        if (toolCalls == null) {
            throw new T9tException(T9tOpenAIException.OPENAI_EXPECTED_TOOL_OUTPUTS, "toolCalls null");
        }
        if (toolCalls.isEmpty()) {
            throw new T9tException(T9tOpenAIException.OPENAI_EXPECTED_TOOL_OUTPUTS, "toolCalls empty");
        }
        final OpenAIToolOutputReq toolOutputReq = new OpenAIToolOutputReq();
        final List<OpenAIToolOutput> toolOutputs = new ArrayList<>(toolCalls.size());
        toolOutputReq.setToolOutputs(toolOutputs);
        for (final OpenAIToolCall toolCall : toolCalls) {
            final OpenAIMessage result = performToolCall(ctx, toolCall, conversationRef);
            final OpenAIToolOutput toolOutput = new OpenAIToolOutput();
            toolOutput.setToolCallId(toolCall.getId());
            toolOutput.setOutput(result.getContent());
            toolOutputs.add(toolOutput);
        }
        return toolOutputReq;
    }

    @Override
    public void addMessagesToThread(final String threadId, final List<OpenAIThreadMessageReq> messages) {
        final String fullPath = THREADS_PATH + "/" + threadId + MESSAGES_SUFFIX;
        for (final OpenAIThreadMessageReq message : messages) {
            // currently the OpenAI API does not support adding more than one message at a time to a thread: Do it one by one
            performOpenAIRequest(message, fullPath, ASSISTANTS_BETA, OpenAIObjectThreadMessage.class);
        }
    }

    @Override
    public OpenAIObjectListThreadMessages listThreadMessages(final String threadId, final OpenAIQueryParameters queryParameters) {
        final String fullPath = addQueryParameters(THREADS_PATH + "/" + threadId + MESSAGES_SUFFIX, queryParameters);
        return performOpenAIGetRequest(fullPath, ASSISTANTS_BETA, OpenAIObjectListThreadMessages.class);
    }

    @Override
    public OpenAIObjectFile performOpenAIFileUpload(final MediaData content, final OpenAIPurposeType purpose) {
        final Map<String, Object> dataMap = new HashMap<>(4);
        dataMap.put("file", content);
        dataMap.put("purpose", purpose.getToken());

        // ensure we have some kind of filename
        if (JsonUtil.getZString(content.getZ(), "filename", null) == null) {
            // create dummy one
            final MediaTypeDescriptor mtd = MediaTypeInfo.getFormatByType(content.getMediaType());
            final String dummyName = "unnamed." + mtd.getDefaultFileExtension();
            if (content.getZ() == null) {
                content.setZ(Map.of("filename", dummyName));
            } else {
                content.setZ(JsonUtil.mergeZ(content.getZ(), Map.of("filename", dummyName)));
            }
        }
        return performOpenAIRequest(dataMap, FILES_PATH, null, OpenAIObjectFile.class);
    }

    @Override
    public OpenAIObjectVectorStore createVectorStore(final OpenAICreateVectorStoreReq createVectorStoreReq) {
        return performOpenAIRequest(createVectorStoreReq, ASSISTANTS_BETA, OpenAIObjectVectorStore.class);
    }
}
