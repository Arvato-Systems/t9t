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
package com.arvatosystems.t9t.out.services;

import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.SinkDTO;

public interface ISinkToCamelProducer {
    void sendSinkOverCamel(SinkDTO sink, DataSinkDTO sinkCfg) throws Exception;

    void sendSinkOverCamelUsingTargetFileName(SinkDTO sink, DataSinkDTO sinkCfg, String targetFileName) throws Exception;

    void sendSinkOverCamelUsingTargetCamelRoute(SinkDTO sink, DataSinkDTO sinkCfg, String targetCamelRoute) throws Exception;

    void sendSinkOverCamelUsingTargetFileNameAndTargetCamelRoute(SinkDTO sink, DataSinkDTO sinkCfg,
            String targetFileName, String targetCamelRoute) throws Exception;
}
