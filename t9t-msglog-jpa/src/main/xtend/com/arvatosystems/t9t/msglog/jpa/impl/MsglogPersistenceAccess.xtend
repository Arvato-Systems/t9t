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
package com.arvatosystems.t9t.msglog.jpa.impl

import com.arvatosystems.t9t.msglog.MessageDTO
import com.arvatosystems.t9t.msglog.services.IMsglogPersistenceAccess
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton
import java.util.List
import javax.persistence.EntityManagerFactory
import com.arvatosystems.t9t.msglog.jpa.entities.MessageEntity
import de.jpaw.annotations.AddLogger

/**
 * Class which writes message log entities.
 * This is a special implementation because no request context is available.
 * It is comparable to AsyncMessageUpdater in t9t-io-jpa.
*/
@Singleton
@AddLogger
class MsglogPersistenceAccess implements IMsglogPersistenceAccess {
    @Inject EntityManagerFactory emf

    override open() {
    }

    override close() {
    }

    override write(List<MessageDTO> entries) {
        val em = emf.createEntityManager
        em.transaction.begin
        for (m : entries) {
            val e = new MessageEntity
            e.put$Data(m)
            // sanity check / sanitizing
            if (e.errorDetails !== null && e.errorDetails.length > MessageDTO.meta$$errorDetails.length)
                e.errorDetails = e.errorDetails.substring(0, MessageDTO.meta$$errorDetails.length)
            e.put$Key(m.objectRef)
            em.persist(e)
        }
        em.transaction.commit
        em.clear
        em.close
    }
}
