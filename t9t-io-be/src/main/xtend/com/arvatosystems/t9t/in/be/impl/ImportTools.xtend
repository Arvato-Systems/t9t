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
package com.arvatosystems.t9t.in.be.impl

import com.arvatosystems.t9t.in.services.IInputSession
import de.jpaw.dp.Jdp
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.Map
import java.util.UUID

/** Utility class to test an import. */
class ImportTools {
    /** Imports from a String. */
    def static importFromString(String source, UUID apiKey, String sourceName, String dataSinkId, Map<String, Object> map) {
        val is = new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8))
        importFromStream(is, apiKey, sourceName, dataSinkId, map)
    }

    /** Imports from an InputStream. */
    def static importFromStream(InputStream is, UUID apiKey, String sourceName, String dataSinkId, Map<String, Object> map) {
        // process a file / input stream, which can be binary
        val inputSession    = Jdp.getRequired(IInputSession)
        inputSession.open(dataSinkId, apiKey, sourceName, map)
        inputSession.process(is)
        inputSession.close
        is.close
    }
}
