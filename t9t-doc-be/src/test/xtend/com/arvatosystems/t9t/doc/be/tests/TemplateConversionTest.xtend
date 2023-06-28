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
package com.arvatosystems.t9t.doc.be.tests

import org.junit.jupiter.api.Test
import com.arvatosystems.t9t.doc.T9tDocTools
import static extension org.junit.jupiter.api.Assertions.*

class TemplateConversionTest {
    @Test
    def testNoPattern() {
        val oldTemplate = "The quick brown fox"
        val newTemplate = T9tDocTools.convertTemplateAddOrSwapPrefix(oldTemplate, "", "so.", null)
        assertEquals(oldTemplate, newTemplate, "No conversion expected")
    }

    @Test
    def testPatternWithExclusion() {
        val oldTemplate = "Order ${d.orderId} has ${d.totalTax} tax"
        val expTemplate = "Order ${d.so.orderId} has ${d.totalTax} tax"
        val newTemplate = T9tDocTools.convertTemplateAddOrSwapPrefix(oldTemplate, "", "so.", #[ "totalTax" ])
        assertEquals(expTemplate, newTemplate, "No conversion expected")
    }

    @Test
    def testPatternShortcut() {
        val oldTemplate = "Order ${d.orderId} has ${d.totalTax} tax"
        val expTemplate = "Order ${d.so.orderId} has ${d.so.totalTax} tax"
        val newTemplate = T9tDocTools.convertTemplateAddOrSwapPrefix(oldTemplate, "", "so.", null)
        assertEquals(expTemplate, newTemplate, "No conversion expected")
    }

    @Test
    def testPatternOtherStart() {
        val oldTemplate = "Order ${ d.orderId} has sd.tax tax"
        val expTemplate = "Order ${ d.so.orderId} has sd.tax tax"
        val newTemplate = T9tDocTools.convertTemplateAddOrSwapPrefix(oldTemplate, "", "so.", null)
        assertEquals(expTemplate, newTemplate, "No conversion expected")
    }

    @Test
    def testPatternWithExclusionTwice() {
        val oldTemplate = "Order ${d.orderId} has ${d.totalTax} tax"
        val expTemplate = "Order ${d.so.orderId} has ${d.totalTax} tax"
        val midTemplate = T9tDocTools.convertTemplateAddOrSwapPrefix(oldTemplate, "", "so.", #[ "totalTax" ])
        val newTemplate = T9tDocTools.convertTemplateAddOrSwapPrefix(midTemplate, "", "so.", #[ "totalTax" ])
        assertEquals(expTemplate, newTemplate, "No conversion expected")
    }

    @Test
    def testPatternTwice() {
        val oldTemplate = "Order ${d.orderId} has ${d.totalTax} tax"
        val expTemplate = "Order ${d.so.orderId} has ${d.so.totalTax} tax"
        val midTemplate = T9tDocTools.convertTemplateAddOrSwapPrefix(oldTemplate, "", "so.", null)
        val newTemplate = T9tDocTools.convertTemplateAddOrSwapPrefix(midTemplate, "", "so.", null)
        assertEquals(expTemplate, newTemplate, "No conversion expected")
    }
}
