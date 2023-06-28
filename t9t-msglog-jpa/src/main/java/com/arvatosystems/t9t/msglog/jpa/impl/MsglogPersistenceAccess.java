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
package com.arvatosystems.t9t.msglog.jpa.impl;

import com.arvatosystems.t9t.msglog.MessageDTO;
import com.arvatosystems.t9t.msglog.jpa.entities.MessageEntity;
import com.arvatosystems.t9t.msglog.services.IMsglogPersistenceAccess;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * Class which writes message log entities.
 * This is a special implementation because no request context is available.
 * It is comparable to AsyncMessageUpdater in t9t-io-jpa.
*/
@Singleton
public class MsglogPersistenceAccess implements IMsglogPersistenceAccess {
    private final EntityManagerFactory emf = Jdp.getRequired(EntityManagerFactory.class);

    @Override
    public void open() {
    }

    @Override
    public void write(List<MessageDTO> entries) {
        final EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        for (MessageDTO m : entries) {
            final MessageEntity e = new MessageEntity();
            e.put$Data(m);
            // sanity check / sanitizing
            if (e.getErrorDetails() != null && e.getErrorDetails().length() > MessageDTO.meta$$errorDetails.getLength()) {
                e.setErrorDetails(e.getErrorDetails().substring(0, MessageDTO.meta$$errorDetails.getLength()));
            }
            e.put$Key(m.getObjectRef());
            em.persist(e);
        }
        em.getTransaction().commit();
        em.clear();
        em.close();
    }

    @Override
    public void close() {
    }
}
