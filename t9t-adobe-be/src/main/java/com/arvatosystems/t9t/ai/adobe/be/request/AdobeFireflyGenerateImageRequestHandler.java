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
package com.arvatosystems.t9t.ai.adobe.be.request;

import com.arvatosystems.t9t.ai.adobe.request.AdobeFireflyGenerateImageRequest;
import com.arvatosystems.t9t.ai.adobe.request.AdobeFireflyGenerateImageResponse;
import com.arvatosystems.t9t.ai.adobe.service.IAdobeFireflyClient;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class AdobeFireflyGenerateImageRequestHandler extends AbstractRequestHandler<AdobeFireflyGenerateImageRequest> {
    private final IAdobeFireflyClient adobeFireflyClient = Jdp.getRequired(IAdobeFireflyClient.class);

    @Override
    public AdobeFireflyGenerateImageResponse execute(final RequestContext ctx, final AdobeFireflyGenerateImageRequest request) throws Exception {
        return adobeFireflyClient.generateImage(ctx, request);
    }
}
