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
package com.arvatosystems.t9t.out.jpa.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.ExportStatusEnum;
import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.io.AsyncMessageDTO;
import com.arvatosystems.t9t.io.AsyncQueueDTO;
import com.arvatosystems.t9t.io.jpa.entities.AsyncChannelEntity;
import com.arvatosystems.t9t.io.jpa.entities.AsyncMessageEntity;
import com.arvatosystems.t9t.io.jpa.entities.AsyncQueueEntity;
import com.arvatosystems.t9t.out.services.IAsyncMessageUpdater;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

/**
 * Class which updates a single message entity.
 * This is a special implementation because no request context is available.
 * It is comparable to MsglogPersistenceAccess in t9t-msglog-jpa.
 */
@Singleton
public class AsyncMessageUpdater implements IAsyncMessageUpdater {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncMessageUpdater.class);
    protected final EntityManagerFactory emf = Jdp.getRequired(EntityManagerFactory.class);

    @Override
    public void updateMessage(Long objectRef, ExportStatusEnum newStatus, Integer httpCode, Integer clientCode, String clientReference) {
        final EntityManager em = emf.createEntityManager();
        final int allowedLength = AsyncMessageDTO.meta$$reference.getLength();
        if (clientReference != null && clientReference.length() > allowedLength)
            clientReference = clientReference.substring(0, allowedLength);
        em.getTransaction().begin();
        final AsyncMessageEntity m = em.find(AsyncMessageEntity.class, objectRef);
        if (m != null) {
            m.setAttempts(m.getAttempts() + 1);
            m.setLastAttempt(new Instant());
            m.setStatus(newStatus);
            m.setHttpResponseCode(httpCode);
            m.setReturnCode(clientCode);
            m.setReference(clientReference);
        }
        em.getTransaction().commit();
        em.clear();
        em.close();
    }

    @Override
    public List<AsyncQueueDTO> getActiveQueues() {
        final EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        TypedQuery<AsyncQueueEntity> query = em.createQuery("SELECT q FROM AsyncQueueEntity q WHERE q.isActive = :active", AsyncQueueEntity.class);
        query.setParameter("active", Boolean.TRUE);
        final List<AsyncQueueEntity> results = query.getResultList();
        em.getTransaction().commit();
        em.clear();
        em.close();

        LOGGER.info("Reading async queues: {} queues found", results.size());
        final List<AsyncQueueDTO> resultDTOs = new ArrayList<>(results.size());
        for (AsyncQueueEntity q: results)
            resultDTOs.add(q.ret$Data());
        return resultDTOs;
    }

    @Override
    public AsyncChannelDTO readChannelConfig(String channelId, Long tenantRef) {
        LOGGER.debug("Reading async channel configuration for channelId {}, tenantRef {}", channelId, tenantRef);
        final EntityManager em = emf.createEntityManager();
        List<AsyncChannelEntity> results = null;
        try {
            em.getTransaction().begin();
            TypedQuery<AsyncChannelEntity> query = em.createQuery("SELECT cfg FROM AsyncChannelEntity cfg WHERE cfg.asyncChannelId = :channelId and cfg.tenantRef = :tenantRef", AsyncChannelEntity.class);
            query.setParameter("tenantRef", tenantRef);
            query.setParameter("channelId", channelId);
            results = query.getResultList();
            em.getTransaction().commit();
            em.clear();
        } finally {
            em.close();
        }
        if (results.size() != 1)
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, "ChannelId " + channelId + " for tenant " + tenantRef);
        AsyncChannelDTO dto = results.get(0).ret$Data();
        dto.freeze();  // ensure it stays immutable in cache
        return dto;
    }
}
