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

import java.util.List;

import com.arvatosystems.t9t.io.CommunicationTargetChannelType;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.OutboundMessageDTO;
import com.arvatosystems.t9t.io.SinkDTO;

/** Defines the communication layer between the backend modules (business logic / persistence layer). */
public interface IOutPersistenceAccess {

    /** Retrieve the DataSinkDTO for a given ID, either from the current tenant, or for the global one. */
    DataSinkDTO getDataSinkDTO(String dataSinkId);

    /** Retrieve all DataSinkDTO for a given environment, which are INPUT. */
    @Deprecated
    default List<DataSinkDTO> getDataSinkDTOsForEnvironment(final String environment) {
        return getDataSinkDTOsForEnvironmentAndChannel(environment, null);
    }

    /** Retrieve all DataSinkDTO for a given channel, which are INPUT. */
    @Deprecated
    default List<DataSinkDTO> getDataSinkDTOsForChannel(final CommunicationTargetChannelType channel) {
        return getDataSinkDTOsForEnvironmentAndChannel(null, channel);
    }

    /** Retrieve all DataSinkDTO for a given channel and environment, which are INPUT. */
    List<DataSinkDTO> getDataSinkDTOsForEnvironmentAndChannel(String environment, CommunicationTargetChannelType channel);

    /** Assigns a new primary key for the sink (required before persisting it, because it will be used by the OutboundMessageDTOs as well). */
    Long getNewSinkKey();

    /** Persist a sink. */
    void storeNewSink(SinkDTO sink);

    /** Retrieve a sink record. */
    SinkDTO getSink(Long sinkRef);

    Long getNewOutboundMessageKey();
    void storeOutboundMessage(OutboundMessageDTO sink);

    /** Mark a sink as "has been processed". */
    void markAsProcessed(Long sinkRef);
}
