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
package com.arvatosystems.t9t.remote.tests.simple

import com.arvatosystems.t9t.base.MessagingUtil
import com.arvatosystems.t9t.base.api.RequestParameters
import com.arvatosystems.t9t.base.request.BatchRequest
import com.arvatosystems.t9t.base.request.LogMessageRequest
import com.arvatosystems.t9t.base.request.PauseRequest
import com.arvatosystems.t9t.core.CannedRequestDTO
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.core.CompactByteArrayComposer
import de.jpaw.bonaparte.core.CompactByteArrayParser
import de.jpaw.bonaparte.core.StaticMeta
import de.jpaw.util.ByteUtil
import org.junit.Test

@AddLogger
class Serialization {
    @Test
    def void serializationTest() {
        MessagingUtil.initializeBonaparteParsers

        val batchedTask = new BatchRequest => [
            commands = #[
                new LogMessageRequest("Hello BEFORE pause"),
                new PauseRequest => [
                    delayInMillis   = 4500
                ],
                new LogMessageRequest("Hello AFTER pause")
            ]
        ]

        val compact = CompactByteArrayComposer.marshal(StaticMeta.OUTER_BONAPORTABLE , batchedTask)
        LOGGER.info("Serialized object has {} bytes length", compact.length)
        LOGGER.info("Serialized object is\n{}", ByteUtil.dump(compact, 1000))

        CompactByteArrayParser.unmarshal(compact, StaticMeta.OUTER_BONAPORTABLE, BatchRequest)
    }


    @Test
    def void serialization2Test() {
        MessagingUtil.initializeBonaparteParsers

        val batchedTask = new BatchRequest => [
            commands = #[
                new LogMessageRequest("Hello BEFORE pause"),
                new PauseRequest => [
                    delayInMillis   = 4500
                ],
                new LogMessageRequest("Hello AFTER pause")
            ]
        ]

        val compact = CompactByteArrayComposer.marshal(CannedRequestDTO.meta$$request, batchedTask)
        LOGGER.info("Serialized object has {} bytes length", compact.length)
        LOGGER.info("Serialized object is\n{}", ByteUtil.dump(compact, 1000))

        CompactByteArrayParser.unmarshal(compact, CannedRequestDTO.meta$$request, RequestParameters)
    }
}
