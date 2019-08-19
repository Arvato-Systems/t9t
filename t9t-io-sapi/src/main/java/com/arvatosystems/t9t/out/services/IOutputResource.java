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
package com.arvatosystems.t9t.out.services;

import java.io.OutputStream;
import java.nio.charset.Charset;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.io.DataSinkDTO;

import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;

/**
 * Represents an output destination. An output destination can be a file, a JMS queue, a socket server etc.
 *
 * @author LIEE001
 */
public interface IOutputResource {

    /**
     * Open this output resource for usage. Typical implementation can open a file handle, connection to JMS provider etc.
     * @throws T9tException if any error encountered while opening target resource
     */
    void open(DataSinkDTO config, OutputSessionParameters params, Long sinkRef, String targetName, MediaTypeDescriptor mediaType, Charset encoding);

    /**
     * Returns a possible replacement of the file name, or null if not supported. Used to return modified file names, for example when writing to compressed targets.
     */
    default String getEffectiveFilename() { return null; }

    /**
     * Returns the underlying output stream used by this OutputResource, if available. Throws an UnsupportedOperationException if this Resource does not support it, or implements internal buffering.
     */
    OutputStream getOutputStream();

    /**
     * Write bytes of data to this output resource, which corresponds to a data record, or to header / trailer data.
     * Some resources may decide to ignore header / trailer data.
     * @param data output data in bytes
     * @throws T9tException if there is an error writing output data
     */
    void write(byte[] buffer, int offset, int len, boolean isDataRecord);

    /**
     * Write string of data to this output resource. The encoding as specified in the DataSink is respected.
     * @param data output data in string
     * @throws T9tException if there is an error writing output data
     */
    void write(String data);

    /**
     * Close this output resource for cleanup.
     * @throws T9tException if any error encountered while closing target resource
     */
    void close();
}
