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

import java.util.List;

import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.OutboundMessageDTO;
import com.arvatosystems.t9t.io.SinkDTO;

/** Defines the communication layer between the backend modules (business logic / persistence layer). */
public interface IOutPersistenceAccess {

    DataSinkDTO getDataSinkDTO(String dataSinkId);

    List<DataSinkDTO> getDataSinkDTOsForEnvironment(String environment);

    /** Assigns a new primary key for the sink (required before persisting it, because it will be used by the OutboundMessageDTOs as well). */
    Long getNewSinkKey();

    void storeNewSink(SinkDTO sink);

    Long getNewOutboundMessageKey();
    void storeOutboundMessage(OutboundMessageDTO sink);
}
