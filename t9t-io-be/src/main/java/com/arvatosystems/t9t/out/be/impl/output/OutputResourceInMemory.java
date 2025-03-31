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
package com.arvatosystems.t9t.out.be.impl.output;

import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.out.services.IOutputResource;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.eclipse.xtext.xbase.lib.Exceptions;

/**
 * This implementation is used for testing only, and for that reason is not
 * annotated.
 */
public class OutputResourceInMemory implements IOutputResource {
    protected final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    protected Charset encoding;

    @Override
    public void close() {
    }

    @Override
    public OutputStream getOutputStream() {
        return baos;
    }

    @Override
    public void open(final DataSinkDTO config, final OutputSessionParameters params, final Long sinkRef,
      final String targetName, final MediaTypeDescriptor mediaType, final Charset xencoding) {
        encoding = xencoding;
    }

    @Override
    public void write(final String partitionKey, final String recordKey, final byte[] buffer, final int offset,
            final int len, final boolean isDataRecord) {
        baos.write(buffer, offset, len);
    }

    @Override
    public void write(final String partitionKey, final String recordKey, final String data) {
        try {
            baos.write(data.getBytes(encoding));
        } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
        }
    }

    public byte[] getBytes() {
        return baos.toByteArray();
    }

    @Override
    public String toString() {
        try {
            return baos.toString(encoding.name());
        } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
        }
    }
}
