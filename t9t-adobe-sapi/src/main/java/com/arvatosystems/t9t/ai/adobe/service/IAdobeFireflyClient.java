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
package com.arvatosystems.t9t.ai.adobe.service;

import com.arvatosystems.t9t.ai.adobe.request.AdobeFireflyGenerateImageRequest;
import com.arvatosystems.t9t.ai.adobe.request.AdobeFireflyGenerateImageResponse;
import com.arvatosystems.t9t.base.services.RequestContext;

import jakarta.annotation.Nonnull;

/**
 * Interface for the Adobe Firefly client.
 * The implementation performs the low level calls to the Adobe Firefly API.
 */
public interface IAdobeFireflyClient {
    /**
     * Generate an image using Adobe Firefly API.
     * This method will submit the request and poll for results asynchronously.
     *
     * @param ctx the request context
     * @param request the image generation request
     * @return the response containing the generated image URL and seed
     * @throws Exception if the request fails or times out
     */
    @Nonnull
    AdobeFireflyGenerateImageResponse generateImage(@Nonnull RequestContext ctx, @Nonnull AdobeFireflyGenerateImageRequest request) throws Exception;
}
