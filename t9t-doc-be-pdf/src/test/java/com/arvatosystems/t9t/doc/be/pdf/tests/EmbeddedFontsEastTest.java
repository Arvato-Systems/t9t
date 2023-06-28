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
package com.arvatosystems.t9t.doc.be.pdf.tests;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

public class EmbeddedFontsEastTest {

    @Test
    public void test() throws DocumentException, IOException {
        final ITextRenderer renderer = new ITextRenderer();
        final ITextFontResolver fontResolver = renderer.getFontResolver();
        fontResolver.addFont("fonts/Lato-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        fontResolver.addFont("fonts/NotoSans-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        final String content = "<!DOCTYPE html>"
                + "            <html>"
                + "                <head>"
                + "                    <meta charset=\"utf-8\"/>"
                + "                    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />"
                + "                    <meta http-equiv=\"X-UA-compatible\" content=\"text/html\" />"
                + "                    <title>Invoice</title>"
                + "                    <style type=\"text/css\">"
                + "                        @page {"
                + "                            size: 8in 14.8in;"
                + "                            margin: 0m;"
                + "                        }"
                + "                        body {"
                + "                            line-height: 1.5;"
                + "                            font-size: 8pt;"
                + "                            font-family: Lato, sans-serif;"
                + "                        }"
                + "                        @font-face {"
                + "                            font-family: \\'Lato\\';"
                + "                            font-style: normal;"
                + "                            font-weight: 400;"
                + "                            -fs-pdf-font-embed: embed;"
                + "                            -fs-pdf-font-encoding: Identity-H;"
                + "                        }"
                + "                      }"
                + "                    </style>"
                + "                </head>"
                + "                <body style=\"font-family:Lato,sans-serif;margin:0; padding:0; background:#ffffff;\">"
                + "                    <p>"
                + "                        Dziękujemy za zaufanie, jakim obdarzyli Państwo nasze usługi. W załączeniu przesyłamy fakturę."
                + "                    </p>"
                + "                </body>"
                + "            </html>";

        renderer.setDocumentFromString(content);
        renderer.layout();
        final File outputPDFFile = File.createTempFile("test", "PDF");
        final FileOutputStream os = new FileOutputStream(outputPDFFile);
        renderer.createPDF(os);
    }

    @Test
    public void testTW() throws DocumentException, IOException {
        final ITextRenderer renderer = new ITextRenderer();
        final ITextFontResolver fontResolver = renderer.getFontResolver();
        fontResolver.addFont("fonts/NotoSansCJKTC-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        final String content = "<!DOCTYPE html>"
                + "            <html>"
                + "                <head>"
                + "                    <meta charset=\"utf-8\"/>"
                + "                    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />"
                + "                    <meta http-equiv=\"X-UA-compatible\" content=\"text/html\" />"
                + "                    <title>Invoice</title>"
                + "                    <style type=\"text/css\">"
                + "                        @page {"
                + "                            size: 8in 14.8in;"
                + "                            margin: 0m;"
                + "                        }"
                + "                        body {"
                + "                            line-height: 1.5;"
                + "                            font-size: 8pt;"
                + "                            font-family: Noto Sans CJK TC Regular, sans-serif;"
                + "                        }"
                + "                        @font-face {"
                + "                            font-family: Noto Sans CJK TC Regular;"
                + "                            font-style: normal;"
                + "                            font-weight: 400;"
                + "                            -fs-pdf-font-embed: embed;"
                + "                            -fs-pdf-font-encoding: Identity-H;"
                + "                        }"
                + "                    </style>"
                + "                </head>"
                + "                <body style=\"font-family:Noto Sans CJK TC Regular,sans-serif;margin:0; padding:0; background:#ffffff;\">"
                + "                    <p>"
                + "                        Test TW: 謝謝您，我們已收到您在訂單總覽，如果您想要瀏覽您在"
                + "                    </p>"
                + "                </body>"
                + "            </html>";

        renderer.setDocumentFromString(content);
        renderer.layout();
        final File outputPDFFile = File.createTempFile("test", ".PDF");
        final FileOutputStream os = new FileOutputStream(outputPDFFile);
        renderer.createPDF(os);
    }
}
