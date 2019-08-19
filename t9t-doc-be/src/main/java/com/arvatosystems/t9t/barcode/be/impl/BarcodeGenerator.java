/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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

import com.arvatosystems.t9t.barcode.api.BarcodeFormat;
import com.arvatosystems.t9t.doc.services.IBarcodeGenerator;
import com.arvatosystems.t9t.doc.services.valueclass.ImageParameter;
import com.arvatosystems.t9t.image.be.impl.AbstractImageGenerator;
import com.google.zxing.aztec.AztecWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.CodaBarWriter;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.Code39Writer;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.oned.EAN8Writer;
import com.google.zxing.oned.ITFWriter;
import com.google.zxing.oned.UPCAWriter;
import com.google.zxing.pdf417.PDF417Writer;
import com.google.zxing.qrcode.QRCodeWriter;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.dp.Singleton;

@Singleton
public class BarcodeGenerator extends AbstractImageGenerator implements IBarcodeGenerator {

    @Override
    public MediaData generateBarcode(BarcodeFormat fmt, String text, ImageParameter params) throws Exception {
        com.google.zxing.BarcodeFormat zxingFormat = com.google.zxing.BarcodeFormat.valueOf(fmt.name());        // convert to ZXing format

        BitMatrix m = null;
        switch (zxingFormat) {
        case AZTEC:
            m = new AztecWriter().encode(text, zxingFormat, params.width, params.height);
            break;
        case CODABAR:
            m = new CodaBarWriter().encode(text, zxingFormat, params.width, params.height);
            break;
        case CODE_128:
            m = new Code128Writer().encode(text, zxingFormat, params.width, params.height);
            break;
        case CODE_39:
            m = new Code39Writer().encode(text, zxingFormat, params.width, params.height);
            break;
//        case CODE_93:
//            m = new Code93Writer().encode(text, zxingFormat, params.width, params.height);
//            break;
        case DATA_MATRIX:
            break;
        case EAN_13:
            m = new EAN13Writer().encode(text, zxingFormat, params.width, params.height);
            break;
        case EAN_8:
            m = new EAN8Writer().encode(text, zxingFormat, params.width, params.height);
            break;
        case ITF:
            m = new ITFWriter().encode(text, zxingFormat, params.width, params.height);
            break;
//        case MAXICODE:
//            m = new MAX().encode(text, zxingFormat, params.width, params.height);
//            break;
        case PDF_417:
            m = new PDF417Writer().encode(text, zxingFormat, params.width, params.height);
            break;
        case QR_CODE:
            m = new QRCodeWriter().encode(text, zxingFormat, params.width, params.height);
            break;
//        case RSS_14:
//            m = new RSS().encode(text, zxingFormat, params.width, params.height);
//            break;
//        case RSS_EXPANDED:
//            m = new ITFWriter().encode(text, zxingFormat, params.width, params.height);
//            break;
        case UPC_A:
            m = new UPCAWriter().encode(text, zxingFormat, params.width, params.height);
            break;
//        case UPC_E:
//            m = new UP().encode(text, zxingFormat, params.width, params.height);
//            break;
//        case UPC_EAN_EXTENSION:
//            writer1 = new UPCEANWriter();
//            break;
        default:
            break;

        }

        return toImage(m, params);
    }

    @Override
    public MediaData generateImage(String text, ImageParameter params) throws Exception {
        throw new UnsupportedOperationException();
    }
}
