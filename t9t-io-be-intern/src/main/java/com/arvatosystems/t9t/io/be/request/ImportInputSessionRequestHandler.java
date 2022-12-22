/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.io.be.request;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.in.be.impl.ImportTools;
import com.arvatosystems.t9t.io.request.AbstractImportData;
import com.arvatosystems.t9t.io.request.ImportFromFile;
import com.arvatosystems.t9t.io.request.ImportFromRaw;
import com.arvatosystems.t9t.io.request.ImportFromString;
import com.arvatosystems.t9t.io.request.ImportInputSessionRequest;
import de.jpaw.util.ApplicationException;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ImportInputSessionRequestHandler extends AbstractRequestHandler<ImportInputSessionRequest> {

    @Override
    public ServiceResponse execute(RequestContext ctx, ImportInputSessionRequest rq) throws Exception {
        ImportTools.importFromStream(getInput(rq.getData()), rq.getApiKey(), rq.getSourceName(), rq.getDataSinkId(), rq.getAdditionalParameters());
        return ok();
    }

    // specific input types: return all of them as stream
    private InputStream getInputByImportFromString(ImportFromString ifs) {
        return new ByteArrayInputStream(ifs.getText().getBytes(StandardCharsets.UTF_8));
    }

    private InputStream getInputByImportFromRaw(ImportFromRaw ifr) {
        return ifr.getData().asByteArrayInputStream();
    }

    private InputStream getInputByImportFromFile(ImportFromFile iff) throws FileNotFoundException {
        if (iff.getIsResource()) {
            return iff.getClass().getResourceAsStream(iff.getPathname());
        }
        return new FileInputStream(iff.getPathname());
    }

    private InputStream getInputByGeneric(AbstractImportData aid) {
        throw new ApplicationException(T9tException.NOT_YET_IMPLEMENTED, "No valid import source for " + aid.getClass().getCanonicalName());
    }

    public InputStream getInput(final AbstractImportData input) throws FileNotFoundException {
        if (input instanceof ImportFromFile importFromFile) {
            return getInputByImportFromFile(importFromFile);
        } else if (input instanceof ImportFromRaw importFromRaw) {
            return getInputByImportFromRaw(importFromRaw);
        } else if (input instanceof ImportFromString importFromString) {
            return getInputByImportFromString(importFromString);
        } else if (input != null) {
            return getInputByGeneric(input);
        } else {
            throw new IllegalArgumentException("Unhandled parameter types: " + Arrays.<Object>asList(input).toString());
        }
    }
}
