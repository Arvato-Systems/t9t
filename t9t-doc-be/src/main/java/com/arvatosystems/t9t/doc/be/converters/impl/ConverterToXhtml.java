/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("XHTML")
public class ConverterToXhtml extends ConverterToAnyHtml {
    @Override
    public String convertFrom(final MediaData src) {
        if (MediaType.TEXT.equals(src.getMediaType().getBaseEnum()) && src.getText() != null) {
            // XHTML escaping: different from html, see https://en.wikipedia.org/wiki/List_of_XML_and_HTML_character_entity_references
            boolean escaped = false;
            final StringBuilder sb = new StringBuilder(src.getText().length() + 20);
            final int len = src.getText().length();
            for (int i = 0; i < len; i++) {
                int c = src.getText().charAt(i);
                // see https://en.wikipedia.org/wiki/List_of_XML_and_HTML_character_entity_references#Predefined_entities_in_XML
                // numeric format &#xhhhh; probably needs EXACTLY 4 hex digits (2 didn't work), with that the named character entities are shorter...
                switch (c) {
                case 0x22:
                    escaped = true;
                    sb.append("&quot;");
                    break;
                case 0x26:
                    escaped = true;
                    sb.append("&amp;");
                    break;
                case 0x27:
                    escaped = true;
                    sb.append("&apos;");
                    break;
                case 0x3c:
                    escaped = true;
                    sb.append("&lt;");
                    break;
                case 0x3e:
                    escaped = true;
                    sb.append("&gt;");
                    break;
                default:
                    sb.append((char) c);
                    break;
                }
            }
            if (!escaped) {
                return src.getText();
            } else {
                return sb.toString();
            }
        }
        return super.convertFrom(src);
    }
}
