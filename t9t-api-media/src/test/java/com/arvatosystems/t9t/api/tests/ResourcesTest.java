package com.arvatosystems.t9t.api.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.arvatosystems.t9t.api.media.MediaData;
import com.arvatosystems.t9t.api.media.MediaDataUtil;

public class ResourcesTest {

    @Test
    public void testPNG() throws Exception {
        MediaData d = MediaDataUtil.resourceAsPNG("/image/jpaw.png");
        Assertions.assertEquals(1048, d.getRawData().length());
    }

    @Test
    public void testJPG() throws Exception {
        MediaData d = MediaDataUtil.resourceAsJPG("/image/jpaw.jpg");
        Assertions.assertEquals(1267, d.getRawData().length());
    }

    @Test
    public void testText() throws Exception {
        MediaData d = MediaDataUtil.resourceAsText("text/hello.txt");
        Assertions.assertTrue(d.getText().startsWith("Hello, world"));
        Assertions.assertTrue(d.getText().length() <= 14);  // 13 on UNIX, 14 for CRLF on MS WIN
    }
}
