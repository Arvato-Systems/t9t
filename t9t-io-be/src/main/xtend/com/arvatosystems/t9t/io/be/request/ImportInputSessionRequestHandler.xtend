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
package com.arvatosystems.t9t.io.be.request

import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.io.request.AbstractImportData
import com.arvatosystems.t9t.io.request.ImportFromFile
import com.arvatosystems.t9t.io.request.ImportFromRaw
import com.arvatosystems.t9t.io.request.ImportFromString
import com.arvatosystems.t9t.io.request.ImportInputSessionRequest
import de.jpaw.annotations.AddLogger
import de.jpaw.util.ApplicationException
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import com.arvatosystems.t9t.in.be.impl.ImportTools

@AddLogger
class ImportInputSessionRequestHandler extends AbstractRequestHandler<ImportInputSessionRequest> {

    override execute(RequestContext nullCtx, ImportInputSessionRequest rq) {
        ImportTools.importFromStream(getInput(rq.data), rq.apiKey, rq.sourceName, rq.dataSinkId, rq.additionalParameters)
        return ok
    }


    // specific input types: return all of them as stream
    def dispatch getInput(ImportFromString ifs) {
        return new ByteArrayInputStream(ifs.text.getBytes(StandardCharsets.UTF_8))
    }
    def dispatch getInput(ImportFromRaw ifr) {
        return ifr.data.asByteArrayInputStream
    }
    def dispatch getInput(ImportFromFile iff) {
        if (iff.isResource)
            return iff.class.getResourceAsStream(iff.pathname)
        else
            return new FileInputStream(iff.pathname)
    }
    def dispatch getInput(AbstractImportData aid) {
        throw new ApplicationException(T9tException.NOT_YET_IMPLEMENTED, "No valid import source for " + aid.class.canonicalName)
    }
}
