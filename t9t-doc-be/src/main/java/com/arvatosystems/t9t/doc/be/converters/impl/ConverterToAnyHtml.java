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
import com.google.common.html.HtmlEscapers;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;

//extend this class to add conversions from additional format types
public abstract class ConverterToAnyHtml implements IDocComponentConverter {
    @Override
    public String convertFrom(final MediaData src) {
        final MediaTypeDescriptor descriptor = MediaTypeInfo.getFormatByType(src.getMediaType());
        final Map<String, Object> z = src.getZ();

        if (src.getRawData() != null && descriptor != null) {
            switch (descriptor.getFormatCategory()) {
            case IMAGE:
                return "<img src=\"data:" + descriptor.getMimeType() + ";base64," + src.getRawData().asBase64() + "\"" + ConverterHtmlUtil.getSizeSpec(z)
                        + ConverterHtmlUtil.addSpec(z, "alt") + ConverterHtmlUtil.addSpec(z, "style") + "/>";
            default:
                return null;
            }
        }
        if (src.getText() != null) {
            if (MediaType.TEXT.equals(src.getMediaType().getBaseEnum())) {
                return HtmlEscapers.htmlEscaper().escape(src.getText()); // text to HTML: "3 < 4" converted to "3 &lt; 4"
            }

            if (descriptor != null) {
                switch (descriptor.getFormatCategory()) {
                case IMAGE: // assume the text is the URL
                    return "<img src=\"" + src.getText() + "\"" + ConverterHtmlUtil.getSizeSpec(z) + ConverterHtmlUtil.addSpec(z, "alt")
                            + ConverterHtmlUtil.addSpec(z, "style") + "/>";
                case AUDIO: // assume the text is the URL
                    return "<audio" + ConverterHtmlUtil.addBoolean(z, "controls") + "><source src=\"" + src.getText() + "\" type=\"" + descriptor.getMimeType()
                            + "\"></audio>";
                case VIDEO: // assume the text is the URL
                    return "<video" + ConverterHtmlUtil.addBoolean(z, "controls") + "><source src=\"" + src.getText() + "\"" + ConverterHtmlUtil.getSizeSpec(z)
                            + " type=\"" + descriptor.getMimeType() + "\"></video>";
                default:
                    return null;
                }
            }
        }
        return null; // policy is to skip unknowns, this will be reported as a warning
    }
}
