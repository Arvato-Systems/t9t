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
package com.arvatosystems.t9t.out.be.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.arvatosystems.t9t.io.CSVTools;

public class UnescaperTest {

    @Test
    public void testUnescapes() throws Exception {
        Assertions.assertEquals("\r\fHello", CSVTools.optRegExp(true, "\\r\\fHello"));
    }

    @Test
    public void testUnescapesOctalInMiddle() throws Exception {
        Assertions.assertEquals("Hello World", CSVTools.optRegExp(true, "Hello\\040World"));
    }

    @Test
    public void testUnescapesOctalNearEndOk() throws Exception {
        Assertions.assertEquals(" ", CSVTools.optRegExp(true, "\\040"));
    }

    @Test
    public void testUnescapesOctalIncomplete() throws Exception {
        Assertions.assertEquals("12", CSVTools.optRegExp(true, "\\12"));
    }

    @Test
    public void testUnescapesHexInMiddle() throws Exception {
        Assertions.assertEquals("Hello World", CSVTools.optRegExp(true, "Hello\\x0020World"));
    }

    @Test
    public void testUnescapesHexNearEndOk() throws Exception {
        Assertions.assertEquals("Hello ", CSVTools.optRegExp(true, "Hello\\x0020"));
    }

    @Test
    public void testUnescapesHexIncomplete() throws Exception {
        Assertions.assertEquals("x003", CSVTools.optRegExp(true, "\\x003"));
    }
}
