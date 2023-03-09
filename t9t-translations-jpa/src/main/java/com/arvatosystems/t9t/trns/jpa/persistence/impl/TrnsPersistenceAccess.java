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
