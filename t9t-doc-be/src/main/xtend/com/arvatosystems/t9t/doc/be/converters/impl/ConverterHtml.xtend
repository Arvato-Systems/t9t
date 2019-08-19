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
package com.arvatosystems.t9t.doc.be.converters.impl

import com.arvatosystems.t9t.doc.services.IDocComponentConverter
import com.google.common.html.HtmlEscapers
import de.jpaw.bonaparte.api.media.MediaTypeInfo
import de.jpaw.bonaparte.pojos.api.media.MediaData
import de.jpaw.bonaparte.pojos.api.media.MediaType
import de.jpaw.dp.Named
import de.jpaw.dp.Singleton
import java.util.Map

import static extension com.arvatosystems.t9t.doc.be.converters.impl.ConverterHtmlUtil.*
import de.jpaw.dp.Inject

final class ConverterHtmlUtil {
    def static getDimension(Map<String, Object> z, String key) {
        val o = z.get(key)
        if (o !== null && o instanceof Number)
            return o.toString + "px"
        else
            return o?.toString
    }

    def static String sizeSpec(Map<String, Object> z) {
        if (z !== null) {
            val widthStr    = z.getDimension("width")
            val heightStr   = z.getDimension("height")
            if (widthStr !== null && heightStr !== null)
                return ''' width="«widthStr»" height="«heightStr»"'''
        }
        return null
    }

    def static String addSpec(Map<String, Object> z, String keyword) {
        val str = z?.get(keyword)
        return if (str !== null) ''' «keyword»="«HtmlEscapers.htmlEscaper.escape(str.toString)»"'''
    }

    def static String addBoolean(Map<String, Object> z, String keyword) {
        val str = z?.get(keyword)
        return if (str !== null && str instanceof Boolean && (str as Boolean).booleanValue) " " + keyword
    }
}

@Singleton
@Named("HTML")
class ConverterToHtml extends ConverterToAnyHtml {
    override convertFrom(MediaData src) {
        if (src.mediaType.baseEnum == MediaType.TEXT && src.text !== null)
            return HtmlEscapers.htmlEscaper.escape(src.text)      // text to HTML: "3 < 4" converted to "3 &lt; 4"
        if (src.mediaType.baseEnum == MediaType.XHTML && src.text !== null)
            return src.text      // valid XHTML is also valid HTML
        return super.convertFrom(src)
    }
}

@Singleton
@Named("XHTML")
class ConverterToXhtml extends ConverterToAnyHtml {
    override convertFrom(MediaData src) {
        if (src.mediaType.baseEnum == MediaType.TEXT && src.text !== null) {
            // XHTML escaping: different from html, see https://en.wikipedia.org/wiki/List_of_XML_and_HTML_character_entity_references
            var boolean escaped = false
            val StringBuilder sb = new StringBuilder(src.text.length + 20)
            val len = src.text.length
            for (var int i = 0; i < len; i += 1) {
                val c = src.text.charAt(i) as int
                // see https://en.wikipedia.org/wiki/List_of_XML_and_HTML_character_entity_references#Predefined_entities_in_XML
                // numeric format &#xhhhh; probably needs EXACTLY 4 hex digits (2 didn't work), with that the named character entities are shorter...
                switch (c) {
                case 0x22: { escaped = true; sb.append("&quot;"); }
                case 0x26: { escaped = true; sb.append("&amp;"); }
                case 0x27: { escaped = true; sb.append("&apos;"); }
                case 0x3c: { escaped = true; sb.append("&lt;"); }
                case 0x3e: { escaped = true; sb.append("&gt;"); }
                default:   sb.append(c as char)
                }
            }
            return if (!escaped) src.text else sb.toString
        }
        return super.convertFrom(src)
    }
}

// extend this class to add conversions from additional format types
abstract class ConverterToAnyHtml implements IDocComponentConverter {

    override convertFrom(MediaData src) {
        val descriptor = MediaTypeInfo.getFormatByType(src.mediaType)
        val z = src.z

        if (src.rawData !== null) {
            if (descriptor !== null) {
                switch (descriptor.formatCategory) {
                case IMAGE:
                    return '''<img src="data:«descriptor.mimeType»;base64,«src.rawData.asBase64»"«z.sizeSpec»«z.addSpec("alt")»«z.addSpec("style")»/>'''
                default:
                    return null
                }
            }
        }
        if (src.text !== null) {
            if (src.mediaType.baseEnum == MediaType.TEXT)
                return HtmlEscapers.htmlEscaper.escape(src.text)      // text to HTML: "3 < 4" converted to "3 &lt; 4"

            if (descriptor !== null) {
                switch (descriptor.formatCategory) {
                case IMAGE: // assume the text is the URL
                    return '''<img src="«src.text»"«z.sizeSpec»«z.addSpec("alt")»«z.addSpec("style")»/>'''
                case AUDIO: // assume the text is the URL
                    return '''<audio«z.addBoolean("controls")»><source src="«src.text»" type="«descriptor.mimeType»"></audio>'''
                case VIDEO: // assume the text is the URL
                    return '''<video«z.addBoolean("controls")»><source src="«src.text»"«z.sizeSpec» type="«descriptor.mimeType»"></video>'''
                default:
                    return null
                }
            }

        }
        return null;        // policy is to skip unknowns, this will be reported as a warning
    }
}

// converter which references images using CID: (for email use)
// this class is instance per use due to the attachments instance variable
class ConverterToHtmlUsingCids implements IDocComponentConverter {
    @Inject
    @Named("HTML")
    protected IDocComponentConverter defaultConverter

    protected final Map<String,MediaData> attachments
    new(Map<String,MediaData> attachments) {
        this.attachments = attachments
    }

    override convertFrom(MediaData src) {
        val descriptor = MediaTypeInfo.getFormatByType(src.mediaType)
        val z = src.z

        if (src.rawData !== null && src.z !== null) {
            val cid = src.z.get("cid") as String
            if (descriptor !== null && cid !== null) {
                attachments.put(cid, src)
                switch (descriptor.formatCategory) {
                case IMAGE:
                        // TODO: does CID allow additional attributes such as style and alt?
                        return '''<img src="cid:«cid»"«z.sizeSpec»«z.addSpec("alt")»«z.addSpec("style")»/>'''
                case AUDIO: // assume the text is the URL
                    return '''<audio«z.addBoolean("controls")»><source src="cid:«cid»" type="«descriptor.mimeType»"></audio>'''
                case VIDEO: // assume the text is the URL
                    return '''<video«z.addBoolean("controls")»><source src="cid:«cid»"«z.sizeSpec» type="«descriptor.mimeType»"></video>'''
                default:
                    return '''(unknown type «descriptor.mimeType» for cid «cid»)'''
                }
            }
        }
        return defaultConverter.convertFrom(src)
    }
}
