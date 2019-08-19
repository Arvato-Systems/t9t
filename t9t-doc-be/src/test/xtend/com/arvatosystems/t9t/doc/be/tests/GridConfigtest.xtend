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
package com.arvatosystems.t9t.doc.be.tests

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion
import com.arvatosystems.t9t.doc.DocComponentDTO
import de.jpaw.bonaparte.api.ColumnCollector
import de.jpaw.bonaparte.pojos.ui.UIDefaults
import de.jpaw.bonaparte.util.ToStringHelper
import org.junit.Test

class GridConfigtest {
    public static final UIDefaults MY_DEFAULTS = new UIDefaults => [
        renderMaxArrayColumns = 5
        widthObject = 160
        widthCheckbox = 32
        widthEnum = 160
        widthEnumset = 300
        widthOffset = 8    // offset for all fields
        widthPerCharacter = 8  // average character width
        widthMax = 400
    ]

    @Test
    def void testColumnCollector() {
        val cc = new ColumnCollector(MY_DEFAULTS)
        cc.addToColumns(FullTrackingWithVersion.class$MetaData)
        println('''Columns are «ToStringHelper.toStringML(cc.columns)»''')
    }

    @Test
    def void testColumnCollector2() {
        val cc = new ColumnCollector(MY_DEFAULTS)
        cc.addToColumns(DocComponentDTO.class$MetaData)
        println('''Columns are «ToStringHelper.toStringML(cc.columns)»''')
    }
}
