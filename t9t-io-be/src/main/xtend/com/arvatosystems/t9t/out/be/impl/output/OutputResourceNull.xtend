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
package com.arvatosystems.t9t.out.be.impl.output

import com.arvatosystems.t9t.base.output.OutputSessionParameters
import com.arvatosystems.t9t.io.DataSinkDTO
import com.arvatosystems.t9t.out.services.IOutputResource
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor
import de.jpaw.dp.Dependent
import de.jpaw.dp.Named
import java.io.OutputStream
import java.nio.charset.Charset

@AddLogger
@Named("NULL")  // name of CommunicationTargetChannelType instance
@Dependent
class OutputResourceNull implements IOutputResource {

    static class DevNull extends OutputStream {
        override write(int i) {}
        override write(byte [] buffer) {}
        override write(byte [] buffer, int off, int len) {}
    }

    static final OutputStream NULL_STREAM = new DevNull

    override close() {}

    override getOutputStream() {
        return NULL_STREAM
    }

    override open(DataSinkDTO config, OutputSessionParameters params, Long sinkRef, String targetName, MediaTypeDescriptor mediaType, Charset encoding) {}

    override write(byte[] buffer, int offset, int len, boolean isDataRecord) {}

    override write(String data) {}
}
