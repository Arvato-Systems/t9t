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

import com.arvatosystems.t9t.doc.services.IDocComponentConverter;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Jdp;

import java.util.Map;

//converter which references images using CID: (for email use)
//this class is instance per use due to the attachments instance variable
public class ConverterToHtmlUsingCids implements IDocComponentConverter {
    protected final IDocComponentConverter defaultConverter = Jdp.getRequired(IDocComponentConverter.class, "HTML");
    protected final Map<String, MediaData> attachments;

    public ConverterToHtmlUsingCids(final Map<String, MediaData> attachments) {
      this.attachments = attachments;
    }

    @Override
    public String convertFrom(final MediaData src) {
        final MediaTypeDescriptor descriptor = MediaTypeInfo.getFormatByType(src.getMediaType());
        final Map<String, Object> z = src.getZ();

        if (src.getRawData() != null && z != null) {
            final String cid = (String) z.get("cid");
            if (descriptor != null && cid != null) {
                attachments.put(cid, src);
                switch (descriptor.getFormatCategory()) {
                case IMAGE:
                    // TODO: does CID allow additional attributes such as style and alt?
                    return "<img src=\"cid:" + cid + "\"" + ConverterHtmlUtil.getSizeSpec(z) + ConverterHtmlUtil.addSpec(z, "alt")
                            + ConverterHtmlUtil.addSpec(z, "style") + "/>";
                case AUDIO: // assume the text is the URL
                    return "<audio" + ConverterHtmlUtil.addBoolean(z, "controls") + "><source src=\"cid:" + cid + "\" type=\"" + descriptor.getMimeType()
                            + "\"></audio>";
                case VIDEO: // assume the text is the URL
                    return "<video" + ConverterHtmlUtil.addBoolean(z, "controls") + "><source src=\"cid:" + cid + "\"" + ConverterHtmlUtil.getSizeSpec(z)
                            + " type=\"" + descriptor.getMimeType() + "\"></video>";
                default:
                    return "(unknown type " + descriptor.getMimeType() + " for cid " + cid + ")";
                }
            }
        }
        return defaultConverter.convertFrom(src);
    }
}
