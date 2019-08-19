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
package com.arvatosystems.t9t.barcode.be.api;

import com.arvatosystems.t9t.barcode.api.GenerateBarcodeRequest;
import com.arvatosystems.t9t.barcode.api.GenerateBarcodeResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.doc.services.IBarcodeGenerator;
import com.arvatosystems.t9t.doc.services.valueclass.ImageParameter;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.dp.Jdp;

public class GenerateBarcodeRequestHandler extends AbstractReadOnlyRequestHandler<GenerateBarcodeRequest> {

    protected final IBarcodeGenerator generator = Jdp.getRequired(IBarcodeGenerator.class);

    @Override
    public GenerateBarcodeResponse execute(GenerateBarcodeRequest rq) throws Exception {
        final ImageParameter params = new ImageParameter(rq.getWidth(), rq.getHeight(), rq.getRotation(), rq.getFlipMode(), rq.getScale());
        MediaData m = generator.generateBarcode(rq.getBarcodeFormat(), rq.getText(), params);

        GenerateBarcodeResponse r = new GenerateBarcodeResponse();
        r.setBarcode(m);
        return r;
    }
}
