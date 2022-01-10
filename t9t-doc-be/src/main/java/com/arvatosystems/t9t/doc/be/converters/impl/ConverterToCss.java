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
package com.arvatosystems.t9t.doc.be.converters.impl;

import java.util.Map;

import com.arvatosystems.t9t.doc.services.IDocComponentConverter;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.pojos.api.media.MediaCategory;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

// extend this class to add conversions from additional format types
@Singleton
@Named("CSS")
public class ConverterToCss implements IDocComponentConverter {
    @Override
    public String convertFrom(final MediaData src) {
        final MediaTypeDescriptor descriptor = MediaTypeInfo.getFormatByType(src.getMediaType());
        final Map<String, Object> z = src.getZ();

        if (src.getRawData() != null) {
            if (descriptor != null && MediaCategory.IMAGE == descriptor.getFormatCategory()) {
                return ConverterHtmlUtil.getSizeSpec(z) + " background-image:url(data:" + descriptor.getMimeType() + ";base64," + src.getRawData().asBase64()
                        + ");\\n";
            }
        }
        if (src.getText() != null) {
            if (descriptor != null && MediaCategory.IMAGE == descriptor.getFormatCategory()) {
                return ConverterHtmlUtil.getSizeSpec(z) + " background-image:url(" + src.getText() + ");\\n";
            }
        }
        return null; // policy is to skip unknowns, this will be reported as a warning
    }
}
