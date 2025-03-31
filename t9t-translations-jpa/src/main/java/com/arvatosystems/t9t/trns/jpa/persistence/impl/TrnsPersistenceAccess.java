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
package com.arvatosystems.t9t.trns.jpa.persistence.impl;

import java.util.List;

import com.arvatosystems.t9t.trns.TranslationsDTO;
import com.arvatosystems.t9t.trns.jpa.entities.TranslationsEntity;
import com.arvatosystems.t9t.trns.jpa.mapping.ITranslationsDTOMapper;
import com.arvatosystems.t9t.trns.services.ITrnsPersistenceAccess;

import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

@Singleton
public class TrnsPersistenceAccess implements ITrnsPersistenceAccess {

    protected final Provider<PersistenceProviderJPA> jpaContextProvider = Jdp.getProvider(PersistenceProviderJPA.class);
    protected final ITranslationsDTOMapper translationsMapper = Jdp.getRequired(ITranslationsDTOMapper.class);

    private static final String GET_TRANSLATION = "SELECT t FROM " + TranslationsEntity.class.getSimpleName()
        + " t WHERE t.tenantId = :tenantId AND t.languageCode = :languageCode";

    @Override
    public List<TranslationsDTO> readLanguage(final String tenantId, final String languageCode) {
        final EntityManager em = jpaContextProvider.get().getEntityManager();
        final TypedQuery<TranslationsEntity> query = em.createQuery(GET_TRANSLATION, TranslationsEntity.class);
        query.setParameter("tenantId", tenantId);
        query.setParameter("languageCode", languageCode);
        return translationsMapper.mapListToDto(query.getResultList());
    }

}
