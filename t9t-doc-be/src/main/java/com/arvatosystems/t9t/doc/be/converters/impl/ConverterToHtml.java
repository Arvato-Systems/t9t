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
package com.arvatosystems.t9t.doc.be.converters.impl;

import com.google.common.html.HtmlEscapers;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("HTML")
public class ConverterToHtml extends ConverterToAnyHtml {
    @Override
    public String convertFrom(final MediaData src) {
        final Enum<?> baseEnum = src.getMediaType().getBaseEnum();
        if (MediaType.TEXT == baseEnum && src.getText() != null) {
            return HtmlEscapers.htmlEscaper().escape(src.getText()); // text to HTML: "3 < 4" converted to "3 &lt; 4"
        }
        if (MediaType.XHTML == baseEnum && src.getText() != null) {
            return src.getText(); // valid XHTML is also valid HTML
        }
        if (MediaType.FTL == baseEnum && src.getText() != null) {
            return src.getText(); // FTL must be accepted "as is"
        }
        return super.convertFrom(src);
    }
}
