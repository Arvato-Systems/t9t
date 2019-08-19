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
package com.arvatosystems.t9t.barcode.be.impl

import com.arvatosystems.t9t.doc.services.valueclass.ImageParameter
import com.arvatosystems.t9t.image.be.impl.AbstractImageGenerator
import com.google.zxing.BarcodeFormat
import com.google.zxing.aztec.AztecWriter
import com.google.zxing.datamatrix.DataMatrixWriter
import com.google.zxing.oned.CodaBarWriter
import com.google.zxing.oned.Code128Writer
import com.google.zxing.oned.Code39Writer
import com.google.zxing.oned.EAN13Writer
import com.google.zxing.oned.EAN8Writer
import com.google.zxing.oned.ITFWriter
import com.google.zxing.oned.UPCAWriter
import com.google.zxing.pdf417.PDF417Writer
import com.google.zxing.qrcode.QRCodeWriter
import de.jpaw.dp.Named
import de.jpaw.dp.Singleton

@Named("AZTEC")
@Singleton
class BarcodeAztecGenerator extends AbstractImageGenerator {

    override generateImage(String text, ImageParameter params) throws Exception {
        val m = new AztecWriter().encode(text, BarcodeFormat.AZTEC, params.width, params.height);
        return toImage(m, params);
    }
}

@Named("CODABAR")
@Singleton
class BarcodeCodabarGenerator extends AbstractImageGenerator {

    override generateImage(String text, ImageParameter params) throws Exception {
        val m = new CodaBarWriter().encode(text, BarcodeFormat.CODABAR, params.width, params.height);
        return toImage(m, params);
    }
}

@Named("CODE_128")
@Singleton
class BarcodeCode128Generator extends AbstractImageGenerator {

    override generateImage(String text, ImageParameter params) throws Exception {
        val m = new Code128Writer().encode(text, BarcodeFormat.CODE_128, params.width, params.height);
        return toImage(m, params);
    }
}

@Named("CODE_39")
@Singleton
class BarcodeCode39Generator extends AbstractImageGenerator {

    override generateImage(String text, ImageParameter params) throws Exception {
        val m = new Code39Writer().encode(text, BarcodeFormat.CODE_39, params.width, params.height);
        return toImage(m, params);
    }
}

@Named("DATA_MATRIX")
@Singleton
class BarcodeDataMatrixGenerator extends AbstractImageGenerator {

    override generateImage(String text, ImageParameter params) throws Exception {
        val m = new DataMatrixWriter().encode(text, BarcodeFormat.DATA_MATRIX, params.width, params.height);
        return toImage(m, params);
    }
}

@Named("EAN_13")
@Singleton
class BarcodeEan13Generator extends AbstractImageGenerator {

    override generateImage(String text, ImageParameter params) throws Exception {
        val m = new EAN13Writer().encode(text, BarcodeFormat.EAN_13, params.width, params.height);
        return toImage(m, params);
    }
}

@Named("EAN_8")
@Singleton
class BarcodeEan8Generator extends AbstractImageGenerator {

    override generateImage(String text, ImageParameter params) throws Exception {
        val m = new EAN8Writer().encode(text, BarcodeFormat.EAN_8, params.width, params.height);
        return toImage(m, params);
    }
}

@Named("ITF")
@Singleton
class BarcodeITFGenerator extends AbstractImageGenerator {

    override generateImage(String text, ImageParameter params) throws Exception {
        val m = new ITFWriter().encode(text, BarcodeFormat.ITF, params.width, params.height);
        return toImage(m, params);
    }
}

@Named("PDF_417")
@Singleton
class BarcodePdf417Generator extends AbstractImageGenerator {

    override generateImage(String text, ImageParameter params) throws Exception {
        val m = new PDF417Writer().encode(text, BarcodeFormat.PDF_417, params.width, params.height);
        return toImage(m, params);
    }
}

@Named("QR_CODE")
@Singleton
class BarcodeQRCodeGenerator extends AbstractImageGenerator {

    override generateImage(String text, ImageParameter params) throws Exception {
        val m = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, params.width, params.height);
        return toImage(m, params);
    }
}

@Named("UPC_A")
@Singleton
class BarcodeUPCAGenerator extends AbstractImageGenerator {

    override generateImage(String text, ImageParameter params) throws Exception {
        val m = new UPCAWriter().encode(text, BarcodeFormat.UPC_A, params.width, params.height);
        return toImage(m, params);
    }
}
