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