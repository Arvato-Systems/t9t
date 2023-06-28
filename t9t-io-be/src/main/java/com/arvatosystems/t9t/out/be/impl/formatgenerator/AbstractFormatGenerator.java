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
package com.arvatosystems.t9t.out.be.impl.formatgenerator;

import java.io.IOException;
import java.nio.charset.Charset;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.out.services.FoldableParams;
import com.arvatosystems.t9t.out.services.ICommunicationFormatGenerator;
import com.arvatosystems.t9t.out.services.IOutputResource;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;
import de.jpaw.util.ApplicationException;

public abstract class AbstractFormatGenerator implements ICommunicationFormatGenerator {

    protected IOutputResource outputResource = null;
    protected MediaXType effectiveType;
    protected DataSinkDTO sinkCfg;
    protected OutputSessionParameters outputSessionParameters;
    protected Charset encoding;
    protected FoldableParams foldableParams;
    protected String tenantId;

    protected void openHook() throws IOException, ApplicationException {
        if (foldableParams != null) {
            throw new T9tException(T9tIOException.NO_FOLDING_SUPPORT, effectiveType.name());
        }
    }

    @Override
    public void open(final DataSinkDTO mySinkCfg, final OutputSessionParameters myOutputSessionParameters, final MediaXType myEffectiveType,
      final FoldableParams myFoldableParams, final IOutputResource myOutputResource, final Charset myEncoding, final String myTenantId) throws IOException {
        this.outputResource = myOutputResource;
        this.effectiveType = myEffectiveType;
        this.sinkCfg = mySinkCfg;
        this.outputSessionParameters = myOutputSessionParameters;
        this.encoding = myEncoding;
        this.foldableParams = myFoldableParams;
        this.tenantId = myTenantId;
        openHook();
    }

    @Override
    public void generateData(final int recordNo, final int mappedRecordNo, final long recordId, final String partitionKey, final String recordKey,
      final BonaPortable record) throws IOException {
        throw new T9tException(T9tIOException.NO_RECORD_BASED_OUTPUT, effectiveType.name());
    }

    @Override
    public void close() throws IOException, ApplicationException {
        outputResource = null;
    }
}
