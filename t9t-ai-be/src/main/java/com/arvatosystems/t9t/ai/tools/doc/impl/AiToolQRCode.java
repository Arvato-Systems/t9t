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
package com.arvatosystems.t9t.ai.tools.doc.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.ai.service.IAiTool;
import com.arvatosystems.t9t.ai.tools.AiToolMediaDataResult;
import com.arvatosystems.t9t.ai.tools.doc.AiToolCreateQRCode;
import com.arvatosystems.t9t.barcode.api.FlipMode;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.doc.services.IImageGenerator;
import com.arvatosystems.t9t.doc.services.ImageParameter;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named(AiToolCreateQRCode.my$PQON)
@Singleton
public class AiToolQRCode implements IAiTool<AiToolCreateQRCode, AiToolMediaDataResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiToolQRCode.class);

    private final IImageGenerator qrCodeGenerator = Jdp.getRequired(IImageGenerator.class, "QR_CODE");

    @Override
    public AiToolMediaDataResult performToolCall(final RequestContext ctx, final AiToolCreateQRCode request) {
        final int width = request.getWidth() != null ? request.getWidth().intValue() : 128;
        final ImageParameter params = new ImageParameter(width, width, 0, FlipMode.NO_FLIPPING, 1.0);
        final AiToolMediaDataResult result = new AiToolMediaDataResult();
        try {
            result.setMediaData(qrCodeGenerator.generateImage(request.getText(), params));
        } catch (final Exception e) {
            LOGGER.error("Error generating QR code: ", e);
            throw new T9tException(T9tException.GENERAL_EXCEPTION, e.getMessage());
        }
        return result;
    }
}
