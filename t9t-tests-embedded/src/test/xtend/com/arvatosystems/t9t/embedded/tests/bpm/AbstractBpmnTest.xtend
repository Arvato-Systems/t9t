/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.embedded.tests.bpm

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.search.ReadAllResponse
import com.arvatosystems.t9t.bpmn.ProcessExecutionStatusDTO
import com.arvatosystems.t9t.bpmn.request.ProcessExecutionStatusSearchRequest
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS
import de.jpaw.bonaparte.pojos.api.NoTracking
import org.junit.jupiter.api.Assertions

@AddLogger
abstract class AbstractBpmnTest {

    def checkNumExecs(ITestConnection dlg, int expectedNumberOfEntries, int expectedStatusCode) {
        val resp = dlg.typeIO((new ProcessExecutionStatusSearchRequest), ReadAllResponse)
        Assertions.assertEquals(expectedNumberOfEntries, resp.dataList.size, "mismatch in number of process exec status entries")
        if (expectedNumberOfEntries != 0) {
            val d0 = resp.dataList.get(0) as DataWithTrackingS<ProcessExecutionStatusDTO, NoTracking>
            val dto = d0.data
            Assertions.assertEquals(expectedStatusCode, dto.returnCode, "process exec status code, details are" + dto.errorDetails)
        }
    }
}