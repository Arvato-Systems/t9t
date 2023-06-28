/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.ExportStatusEnum;
import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.io.AsyncHttpResponse;
import com.arvatosystems.t9t.io.AsyncMessageDTO;
import com.arvatosystems.t9t.io.AsyncQueueDTO;
import com.arvatosystems.t9t.io.jpa.entities.AsyncChannelEntity;
import com.arvatosystems.t9t.io.jpa.entities.AsyncMessageEntity;
import com.arvatosystems.t9t.io.jpa.entities.AsyncQueueEntity;
import com.arvatosystems.t9t.out.services.IAsyncMessageUpdater;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ExceptionUtil;

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
    public void updateMessage(final Long objectRef, final ExportStatusEnum newStatus, final Integer httpCode,
      final AsyncHttpResponse resp) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            final AsyncMessageEntity m = em.find(AsyncMessageEntity.class, objectRef);
            LOGGER.debug("Updating asyncMessageRef {} to new Status {} (http {}): {}", objectRef, newStatus, httpCode, m == null ? "NOT FOUND" : "OK");
            if (m != null) {
                // message was persisted initially, or had an error before
                m.setAttempts(m.getAttempts() + 1);
                m.setLastAttempt(Instant.now());
                m.setStatus(newStatus);
                m.setHttpResponseCode(httpCode);
                updateByResponse(m, resp);
            } else if (newStatus != ExportStatusEnum.RESPONSE_OK) {
                // initially not persisted, but we encountered an error, and should do so now
                // issue is that we do not have any valid tenant reference, nor request context to populate the creation tracking fields
                final AsyncMessageEntity msg = new AsyncMessageEntity();
                msg.setAttempts(1);
                msg.setLastAttempt(Instant.now());
                msg.setStatus(newStatus);
                msg.setHttpResponseCode(httpCode);
                updateByResponse(msg, resp);
                // not creating due to missing context
            }
            em.getTransaction().commit();
            em.clear();
        } catch (final Throwable e) {
            LOGGER.error("Msg update problem: {}", ExceptionUtil.causeChain(e));
        }
    }

    protected void updateByResponse(final AsyncMessageEntity m, final AsyncHttpResponse resp) {
        if (resp != null) {
            m.setReturnCode(resp.getClientReturnCode());
            m.setReference(MessagingUtil.truncField(resp.getClientReference(), AsyncMessageDTO.meta$$reference.getLength()));
            m.setErrorDetails(MessagingUtil.truncField(resp.getErrorDetails(), AsyncMessageDTO.meta$$errorDetails.getLength()));
            m.setLastResponseTime(resp.getResponseTime());
        }
    }

    @Override
    public List<AsyncQueueDTO> getActiveQueues() {
        final EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        final TypedQuery<AsyncQueueEntity> query = em.createQuery("SELECT q FROM AsyncQueueEntity q WHERE q.isActive = :active", AsyncQueueEntity.class);
        query.setParameter("active", Boolean.TRUE);
        final List<AsyncQueueEntity> results = query.getResultList();
        em.getTransaction().commit();
        em.clear();
        em.close();

        LOGGER.info("Reading async queues: {} queues found", results.size());
        final List<AsyncQueueDTO> resultDTOs = new ArrayList<>(results.size());
        for (final AsyncQueueEntity q: results) {
            resultDTOs.add(q.ret$Data());
        }
        return resultDTOs;
    }

    @Override
    public AsyncChannelDTO readChannelConfig(final String channelId, final String tenantId) {
        LOGGER.debug("Reading async channel configuration for channelId {}, tenantId {}", channelId, tenantId);
        final EntityManager em = emf.createEntityManager();
        List<AsyncChannelEntity> results = null;
        try {
            em.getTransaction().begin();
            final TypedQuery<AsyncChannelEntity> query
              = em.createQuery("SELECT cfg FROM AsyncChannelEntity cfg WHERE cfg.asyncChannelId = :channelId and cfg.tenantId = :tenantId",
                AsyncChannelEntity.class);
            query.setParameter("tenantId", tenantId);
            query.setParameter("channelId", channelId);
            results = query.getResultList();
            em.getTransaction().commit();
            em.clear();
        } finally {
            em.close();
        }
        if (results.size() != 1)
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, "ChannelId " + channelId + " for tenant " + tenantId);
        final AsyncChannelDTO dto = results.get(0).ret$Data();
        dto.freeze();  // ensure it stays immutable in cache
        return dto;
    }
}
