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
package com.arvatosystems.t9t.barcode.be.impl;

import java.util.Hashtable;

import com.arvatosystems.t9t.doc.services.ImageParameter;
import com.arvatosystems.t9t.image.be.impl.AbstractImageGenerator;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named("QR_CODE")
@Singleton
public class BarcodeQRCodeGenerator extends AbstractImageGenerator {
    @Override
    public MediaData generateImage(final String text, final ImageParameter params) throws Exception {
        final Hashtable<EncodeHintType, String> hints = new Hashtable<>();
        if (params.encoding != null) {
            hints.put(EncodeHintType.CHARACTER_SET, params.encoding);
        }
        final BitMatrix m = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, params.width, params.height, hints);
        return toImage(m, params);
    }
}
