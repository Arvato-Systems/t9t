/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.msglog.jpa.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.msglog.jpa.persistence.IMessageEntityResolver;
import com.arvatosystems.t9t.msglog.request.RemoveOldMessageEntriesRequest;

import de.jpaw.dp.Jdp;

public class RemoveOldMessageEntriesRequestHandler extends AbstractRequestHandler<RemoveOldMessageEntriesRequest> {
    protected final IMessageEntityResolver resolver = Jdp.getRequired(IMessageEntityResolver.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final RemoveOldMessageEntriesRequest request) {

        if (ctx.getTenantRef().compareTo(T9tConstants.GLOBAL_TENANT_REF42) != 0) {
            throw new T9tException(T9tException.RESTRICTED_ACCESS, "Only accessible by global tenant");
        }

        final EntityManager em = resolver.getEntityManager();
        String query = "DELETE FROM MessageEntity msg WHERE executionStartedAt < :deleteUntil";

        //if TRUE, do not delete entries which have returnCode >= 200000000 (exception return codes)
        if (request.getKeepErrorRequests() != null && request.getKeepErrorRequests()) {
            query += " AND returnCode < 200000000";
        }

        final Query q = em.createQuery(query);

        final LocalDateTime thisMorning = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0));
        q.setParameter("deleteUntil", thisMorning.minusDays(request.getKeepMaxDaysAg()));
        q.executeUpdate();

        return ok();
    }
}
