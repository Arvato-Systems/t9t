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
package com.arvatosystems.t9t.out.jpa.impl;

import java.util.List;

import jakarta.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.io.CommunicationTargetChannelType;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.OutboundMessageDTO;
import com.arvatosystems.t9t.io.SinkDTO;
import com.arvatosystems.t9t.io.jpa.entities.DataSinkEntity;
import com.arvatosystems.t9t.io.jpa.entities.SinkEntity;
import com.arvatosystems.t9t.io.jpa.mapping.IDataSinkDTOMapper;
import com.arvatosystems.t9t.io.jpa.mapping.IOutboundMessageDTOMapper;
import com.arvatosystems.t9t.io.jpa.mapping.ISinkDTOMapper;
import com.arvatosystems.t9t.io.jpa.persistence.IDataSinkEntityResolver;
import com.arvatosystems.t9t.io.jpa.persistence.IOutboundMessageEntityResolver;
import com.arvatosystems.t9t.io.jpa.persistence.ISinkEntityResolver;
import com.arvatosystems.t9t.out.services.IOutPersistenceAccess;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class OutPersistenceAccess implements IOutPersistenceAccess {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutPersistenceAccess.class);

    private final IDataSinkEntityResolver dataSinkEntityResolver = Jdp.getRequired(IDataSinkEntityResolver.class);
    private final IDataSinkDTOMapper      dataSinkMapper         = Jdp.getRequired(IDataSinkDTOMapper.class);
    private final ISinkEntityResolver     sinkResolver           = Jdp.getRequired(ISinkEntityResolver.class);
    private final ISinkDTOMapper          sinkMapper             = Jdp.getRequired(ISinkDTOMapper.class);
    private final IOutboundMessageEntityResolver outboundMessageResolver = Jdp.getRequired(IOutboundMessageEntityResolver.class);
    private final IOutboundMessageDTOMapper      outboundMessageMapper   = Jdp.getRequired(IOutboundMessageDTOMapper.class);

    /**
     * Read the configuration data. The configuration record can be tenant
     * specific or general (@). We assume that the ref for the general tenant
     * (@) is the smallest one, because that record is created first.
     *
     * @param dataSinkId
     *            the data sink id to lookup
     * @throws T9tException
     *             if configuration can't be found
     */
    @Override
    public DataSinkDTO getDataSinkDTO(final String dataSinkId) {
        if (dataSinkId == null) {
            throw new T9tException(T9tException.ILE_REQUIRED_PARAMETER_IS_NULL, "dataSinkId");
        }

        final List<DataSinkEntity> sinks = dataSinkEntityResolver.findByDataSinkIdWithDefault(true, dataSinkId);
        if ((sinks == null) || sinks.isEmpty()) {
            throw new T9tException(T9tException.MISSING_CONFIGURATION, "dataSinkId=" + dataSinkId);
        }

        if (sinks.size() > 2) {
            throw new T9tException(T9tException.ILE_RESULT_SET_WRONG_SIZE, "dataSinkId=" + dataSinkId);
        }

        LOGGER.debug("Export for dataSinkId {} will use configuration record of tenantId = {}", dataSinkId, sinks.get(0).getTenantId());
        return dataSinkMapper.mapToDto(sinks.get(0));
    }

    @Override
    public Long getNewSinkKey() {
        return sinkResolver.createNewPrimaryKey();
    }

    @Override
    public void storeNewSink(final SinkDTO sink) {
        sinkResolver.save(sinkMapper.mapToEntity(sink));
    }

    @Override
    public SinkDTO getSink(final Long sinkRef) {
        return sinkMapper.mapToDto(sinkRef);
    }

    @Override
    public Long getNewOutboundMessageKey() {
        return outboundMessageResolver.createNewPrimaryKey();
    }

    @Override
    public void storeOutboundMessage(final OutboundMessageDTO sink) {
        outboundMessageResolver.save(outboundMessageMapper.mapToEntity(sink));
    }

    @Override
    public List<DataSinkDTO> getDataSinkDTOsForEnvironmentAndChannel(final String environment, final CommunicationTargetChannelType channel) {
        final String sql = "SELECT i FROM DataSinkEntity i WHERE "
                + (channel     == null ? "" : "i.commTargetChannelType = :commTargetChannelType AND ")
                + (environment == null ? "" : "i.environment = :environment AND ")
                + "i.isInput = true AND i.isActive = true ORDER BY i.tenantId";
        LOGGER.info("Querying DataSinks for environment {} and channel {} with <{}>", environment, channel, sql);
        final TypedQuery<DataSinkEntity> typedQuery = dataSinkEntityResolver.constructQuery(sql);
        if (channel != null) {
            typedQuery.setParameter("commTargetChannelType", channel.getToken());
        }
        if (environment != null) {
            typedQuery.setParameter("environment", environment);
        }
        final List<DataSinkEntity> resultList = typedQuery.getResultList();
        return dataSinkMapper.mapListToDto(resultList);
    }

    @Override
    public void markAsProcessed(final Long sinkRef) {
        final SinkEntity sink = sinkResolver.getEntityDataForKey(sinkRef);
        sink.setProcessed(Boolean.TRUE);
    }
}
