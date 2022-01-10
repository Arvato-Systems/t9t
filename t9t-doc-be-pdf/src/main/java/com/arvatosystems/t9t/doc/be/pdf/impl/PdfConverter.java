/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.doc.be.pdf.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.doc.T9tDocException;
import com.arvatosystems.t9t.doc.services.IDocConverter;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;

import de.jpaw.bonaparte.api.media.MediaTypes;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ByteArray;
import de.jpaw.util.ExceptionUtil;

@Singleton
@Named("PDF")
public class PdfConverter implements IDocConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(PdfConverter.class);

    protected void addEmbeddedFonts(final ITextFontResolver fontResolver, final String html, final String fontname,    final String ttfname) {
        if (html.indexOf("font-family: " + fontname) > 0) {
            LOGGER.debug("Embedding {} font into PDF", fontname);
            try {
                fontResolver.addFont((("fonts/" + ttfname) + ".ttf"), BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            } catch (final DocumentException e) {
                LOGGER.error("{} font could not be added due to {} ", fontname, ExceptionUtil.causeChain(e));
                throw new T9tException(T9tDocException.CANNOT_ADD_FONT_DOC, fontname);
            } catch (final IOException e) {
                LOGGER.error("{} font could not be added due to {} ", fontname, ExceptionUtil.causeChain(e));
                throw new T9tException(T9tDocException.CANNOT_ADD_FONT_IO, fontname);
            }
        }
    }

    @Override
    public MediaData convert(final MediaData src) {
        final Enum<?> baseEnum = src.getMediaType().getBaseEnum();
        if ((baseEnum != MediaType.HTML && baseEnum != MediaType.XHTML)) {
            return null;
        }
        final MediaData md = new MediaData();
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ITextRenderer itr = new ITextRenderer();
            this.addEmbeddedFonts(itr.getFontResolver(), src.getText(), "Lato", "Lato-Regular");
            this.addEmbeddedFonts(itr.getFontResolver(), src.getText(), "Noto", "NotoSans-Regular");
            this.addEmbeddedFonts(itr.getFontResolver(), src.getText(), "Noto Sans CJK TC Regular", "NotoSansCJKTC-Regular");
            itr.setDocumentFromString(src.getText());
            itr.layout();
            itr.createPDF(baos);
            md.setMediaType(MediaTypes.MEDIA_XTYPE_PDF);
            md.setRawData(ByteArray.fromByteArrayOutputStream(baos));
        } catch (final DocumentException e) {
            LOGGER.error("PDF creation exception due to {} ", ExceptionUtil.causeChain(e));
            throw new T9tException(T9tDocException.DOCUMENT_PDF_CONVERSION_ERROR_DOC);
        } catch (final IOException e) {
            LOGGER.error("PDF creation exception due to {} ", ExceptionUtil.causeChain(e));
            throw new T9tException(T9tDocException.DOCUMENT_PDF_CONVERSION_ERROR_IO);
        }
        return md;
    }
}
