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
package com.arvatosystems.t9t.base.be.tests;

import java.nio.charset.Charset;

import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

public class ShowIds {

    @Test
    public void showZoneIds() throws Exception {
        for (String s: DateTimeZone.getAvailableIDs()) {
            System.out.println(s);
        }
        Assert.assertTrue(DateTimeZone.getAvailableIDs().contains("Europe/Berlin"));
    }
    @Test
    public void showEncodings() throws Exception {
        for (String s: Charset.availableCharsets().keySet()) {
            System.out.println(s);
        }
        Assert.assertTrue(Charset.availableCharsets().get("ISO-8859-1") != null);
    }
}
