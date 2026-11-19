/*
 * Copyright (c) 2012 - 2026 Arvato Systems GmbH
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
