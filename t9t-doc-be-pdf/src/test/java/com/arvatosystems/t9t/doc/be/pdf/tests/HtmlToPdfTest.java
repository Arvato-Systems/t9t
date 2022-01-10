package com.arvatosystems.t9t.doc.be.pdf.tests;

import com.arvatosystems.t9t.doc.services.IDocConverter;
import com.arvatosystems.t9t.jdp.Init;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.dp.Jdp;

import org.junit.jupiter.api.Test;

public class HtmlToPdfTest {

    @Test
    public void testHtmlToPdf() throws Exception {
        Init.initializeT9t();
        final String text = "<html>"
                + "             <head>"
                + "                 <title>Mego-Test</title>"
                + "             </head>"
                + "             <body>"
                + "                 <h1>Title</h1>"
                + "                 Some text"
                + "             </body>"
                + "          </html>";
        final MediaData src = new MediaData();
        src.setMediaType(MediaType.HTML);
        src.setText(text);

        final IDocConverter docConverter = Jdp.getRequired(IDocConverter.class, MediaType.PDF.name());
        final MediaData dst = docConverter.convert(src);
        if (dst == null || dst.getRawData() == null) {
            throw new Exception("Could not convert HTML to PDF");
        }
        System.out.println("Length of generated PDF is " + dst.getRawData().length() + " bytes");
    }
}
