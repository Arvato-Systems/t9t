/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.doc.services;

import com.arvatosystems.t9t.barcode.api.BarcodeFormat;

import de.jpaw.bonaparte.pojos.api.media.MediaData;

// special case of IImageGenerator
public interface IBarcodeGenerator {
    MediaData generateBarcode(BarcodeFormat fmt, String text, ImageParameter params) throws Exception;
}
