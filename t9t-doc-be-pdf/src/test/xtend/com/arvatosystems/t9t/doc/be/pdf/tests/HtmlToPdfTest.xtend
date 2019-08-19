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
package com.arvatosystems.t9t.doc.be.pdf.tests

import com.arvatosystems.t9t.doc.services.IDocConverter
import com.arvatosystems.t9t.jdp.Init
import de.jpaw.bonaparte.pojos.api.media.MediaData
import de.jpaw.bonaparte.pojos.api.media.MediaType
import org.junit.Test

import static extension de.jpaw.dp.Jdp.*

class HtmlToPdfTest {

    @Test
    def public void testHtmlToPdf() {
        Init.initializeT9t
        val src = new MediaData => [
            text = '''
                <html>
                    <head>
                        <title>Mego-Test</title>
                    </head>
                    <body>
                        <h1>Title</h1>
                        Some text
                    </body>
                </html>
            '''
            mediaType = MediaType.HTML
        ]
        val dst = IDocConverter.getRequired(MediaType.PDF.name)?.convert(src)
        if (dst === null || dst.rawData === null)
            throw new Exception("Could not convert HTML to PDF")
        println('''Length of generated PDF is «dst.rawData.length» bytes''')
    }
}
