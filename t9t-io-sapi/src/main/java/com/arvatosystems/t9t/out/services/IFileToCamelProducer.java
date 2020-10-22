/*
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
package com.arvatosystems.t9t.out.services;

import com.arvatosystems.t9t.io.DataSinkDTO;

import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;

public interface IFileToCamelProducer {
    public void sendFileOverCamel(String fileName, MediaTypeDescriptor fileType, DataSinkDTO sinkCfg);

    public void sendFileOverCamelUsingTargetFileName(String fileName, MediaTypeDescriptor fileType, DataSinkDTO sinkCfg, String targetFileName);

    public void sendFileOverCamelUsingTargetCamelRoute(String fileName, MediaTypeDescriptor fileType, DataSinkDTO sinkCfg, String targetCamelRoute);

    public void sendFileOverCamelUsingTargetFileNameAndTargetCamelRoute(String fileName, MediaTypeDescriptor fileType, DataSinkDTO sinkCfg, String targetFileName, String targetCamelRoute);
}
