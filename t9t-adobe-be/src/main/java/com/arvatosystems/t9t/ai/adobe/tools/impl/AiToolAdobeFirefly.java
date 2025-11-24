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
package com.arvatosystems.t9t.ai.adobe.tools.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.ai.adobe.request.AdobeFireflyGenerateImageRequest;
import com.arvatosystems.t9t.ai.adobe.request.AdobeFireflyGenerateImageResponse;
import com.arvatosystems.t9t.ai.adobe.service.IAdobeFireflyClient;
import com.arvatosystems.t9t.ai.service.IAiTool;
import com.arvatosystems.t9t.ai.tools.adobe.AiToolAdobeFireflyGenerateImage;
import com.arvatosystems.t9t.ai.tools.adobe.AiToolAdobeFireflyGenerateImageResult;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named(AiToolAdobeFireflyGenerateImage.my$PQON)
@Singleton
public class AiToolAdobeFirefly implements IAiTool<AiToolAdobeFireflyGenerateImage, AiToolAdobeFireflyGenerateImageResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiToolAdobeFirefly.class);

    private final IAdobeFireflyClient adobeFireflyClient = Jdp.getRequired(IAdobeFireflyClient.class);

    @Override
    public AiToolAdobeFireflyGenerateImageResult performToolCall(final RequestContext ctx, final AiToolAdobeFireflyGenerateImage request) {
        LOGGER.debug("Executing Adobe Firefly image generation tool with prompt: {}", request.getPositivePrompt());

        try {
            // Map the tool request to the service request
            final AdobeFireflyGenerateImageRequest serviceRequest = new AdobeFireflyGenerateImageRequest();
            serviceRequest.setModelVersion(request.getModelVersion());
            serviceRequest.setCustomModelId(request.getCustomModelId());
            serviceRequest.setPositivePrompt(request.getPositivePrompt());
            serviceRequest.setNegativePrompt(request.getNegativePrompt());
            serviceRequest.setSeed(request.getSeed());
            serviceRequest.setWidth(request.getWidth());
            serviceRequest.setHeight(request.getHeight());

            // Execute the request
            final AdobeFireflyGenerateImageResponse serviceResponse = adobeFireflyClient.generateImage(ctx, serviceRequest);

            // Map the service response to the tool result
            final AiToolAdobeFireflyGenerateImageResult result = new AiToolAdobeFireflyGenerateImageResult();
            result.setSeed(serviceResponse.getSeed());
            result.setImageUrl(serviceResponse.getImageUrl());

            LOGGER.debug("Adobe Firefly image generation completed successfully, image URL: {}", result.getImageUrl());
            return result;
        } catch (final Exception e) {
            LOGGER.error("Error executing Adobe Firefly image generation tool", e);
            // Return a result with null values to indicate failure
            final AiToolAdobeFireflyGenerateImageResult errorResult = new AiToolAdobeFireflyGenerateImageResult();
            return errorResult;
        }
    }
}
