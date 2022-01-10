/**
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.out.be.impl.output;

import java.io.OutputStream;
import java.nio.charset.Charset;

import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.out.services.IOutputResource;

import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;

@Named("NULL")
@Dependent
public class OutputResourceNull implements IOutputResource {
    public static class DevNull extends OutputStream {
        @Override
        public void write(final int i) {
        }

        @Override
        public void write(final byte[] buffer) {
        }

        @Override
        public void write(final byte[] buffer, final int off, final int len) {
        }
    }

    private static final OutputStream NULL_STREAM = new OutputResourceNull.DevNull();

    @Override
    public void close() {
    }

    @Override
    public OutputStream getOutputStream() {
        return OutputResourceNull.NULL_STREAM;
    }

    @Override
    public void open(final DataSinkDTO config, final OutputSessionParameters params, final Long sinkRef,
        final String targetName, final MediaTypeDescriptor mediaType, final Charset encoding) {
    }

    @Override
    public void write(final String partitionKey, final String recordKey, final byte[] buffer, final int offset,
        final int len, final boolean isDataRecord) {
    }

    @Override
    public void write(final String partitionKey, final String recordKey, final String data) {
    }
}
