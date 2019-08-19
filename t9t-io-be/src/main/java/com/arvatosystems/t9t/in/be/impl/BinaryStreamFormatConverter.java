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
package com.arvatosystems.t9t.in.be.impl;

import java.io.IOException;
import java.io.InputStream;

import com.arvatosystems.t9t.io.BinaryImportDTO;

import de.jpaw.bonaparte.pojos.api.media.MediaXType;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;
import de.jpaw.util.ByteArray;

/**
 * Binary format converter which allows import of arbitrary binary data.
 * It is up to the configured IInputDataTransformer to create a valid request. (e.g. an file upload request.)
 */
@Dependent
@Named("BINARY")
public class BinaryStreamFormatConverter extends AbstractInputFormatConverter {

    @Override
    public void process(InputStream is) {
        try {
            final String sourceURI = inputSession.getSourceURI();
            final MediaXType formatType = cfg.getCommFormatType();

            final ByteArray inputData = ByteArray.fromInputStream(is, 11500000);

            inputSession.process(new BinaryImportDTO(sourceURI, formatType, inputData));
        } catch (IOException e) {
            throw new RuntimeException("Error reading binary input data", e);
        }
    }

}
