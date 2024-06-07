package com.arvatosystems.t9t.ai.openai.jackson;

import java.time.Instant;

import com.arvatosystems.t9t.ai.openai.OpenAIFileStatusType;
import com.arvatosystems.t9t.ai.openai.OpenAIFinishReasonType;
import com.arvatosystems.t9t.ai.openai.OpenAIImageDetailType;
import com.arvatosystems.t9t.ai.openai.OpenAIObjectType;
import com.arvatosystems.t9t.ai.openai.OpenAIParameterType;
import com.arvatosystems.t9t.ai.openai.OpenAIPurposeType;
import com.arvatosystems.t9t.ai.openai.OpenAIResponseFormatType;
import com.arvatosystems.t9t.ai.openai.OpenAIRoleType;
import com.arvatosystems.t9t.ai.openai.OpenAISortOrderType;
import com.arvatosystems.t9t.ai.openai.OpenAIToolChoiceType;
import com.arvatosystems.t9t.ai.openai.OpenAIToolType;
import com.arvatosystems.t9t.ai.openai.OpenAIVectorStoreStatusType;
import com.arvatosystems.t9t.ai.openai.assistants.OpenAIRunStatusType;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.jpaw.enums.TokenizableEnum;

public class OpenAIModule extends SimpleModule {
    private static final long serialVersionUID = -5852568871709312156L;

    public OpenAIModule() {
        super();

        this.addSerializer(TokenizableEnum.class, new OpenAIEnumSerializer());
        this.addSerializer(Instant.class, new OpenAIInstantSerializer());

        this.addDeserializer(OpenAIRoleType.class,           new OpenAIEnumDeserializer<OpenAIRoleType>(OpenAIRoleType::factory));
        this.addDeserializer(OpenAIResponseFormatType.class, new OpenAIEnumDeserializer<OpenAIResponseFormatType>(OpenAIResponseFormatType::factory));
        this.addDeserializer(OpenAIToolType.class,           new OpenAIEnumDeserializer<OpenAIToolType>(OpenAIToolType::factory));
        this.addDeserializer(OpenAIToolChoiceType.class,     new OpenAIEnumDeserializer<OpenAIToolChoiceType>(OpenAIToolChoiceType::factory));
        this.addDeserializer(OpenAIFinishReasonType.class,   new OpenAIEnumDeserializer<OpenAIFinishReasonType>(OpenAIFinishReasonType::factory));
        this.addDeserializer(OpenAIParameterType.class,      new OpenAIEnumDeserializer<OpenAIParameterType>(OpenAIParameterType::factory));
        this.addDeserializer(OpenAIRunStatusType.class,      new OpenAIEnumDeserializer<OpenAIRunStatusType>(OpenAIRunStatusType::factory));
        this.addDeserializer(OpenAISortOrderType.class,      new OpenAIEnumDeserializer<OpenAISortOrderType>(OpenAISortOrderType::factory));
        this.addDeserializer(OpenAIObjectType.class,         new OpenAIEnumDeserializer<OpenAIObjectType>(OpenAIObjectType::factory));
        this.addDeserializer(OpenAIPurposeType.class,        new OpenAIEnumDeserializer<OpenAIPurposeType>(OpenAIPurposeType::factory));
        this.addDeserializer(OpenAIFileStatusType.class,     new OpenAIEnumDeserializer<OpenAIFileStatusType>(OpenAIFileStatusType::factory));
        this.addDeserializer(OpenAIVectorStoreStatusType.class, new OpenAIEnumDeserializer<OpenAIVectorStoreStatusType>(OpenAIVectorStoreStatusType::factory));
        this.addDeserializer(OpenAIImageDetailType.class,    new OpenAIEnumDeserializer<OpenAIImageDetailType>(OpenAIImageDetailType::factory));

        this.addDeserializer(Instant.class,                  new OpenAIInstantDeserializer());
    }
}
