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
package com.arvatosystems.t9t.doc.jpa.impl;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.CollectionExtensions;
import com.arvatosystems.t9t.doc.DocConfigDTO;
import com.arvatosystems.t9t.doc.DocConfigKey;
import com.arvatosystems.t9t.doc.DocConstants;
import com.arvatosystems.t9t.doc.DocEmailCfgDTO;
import com.arvatosystems.t9t.doc.DocModuleCfgDTO;
import com.arvatosystems.t9t.doc.DocTemplateDTO;
import com.arvatosystems.t9t.doc.T9tDocExtException;
import com.arvatosystems.t9t.doc.api.DocumentSelector;
import com.arvatosystems.t9t.doc.jpa.entities.DocComponentEntity;
import com.arvatosystems.t9t.doc.jpa.entities.DocEmailCfgEntity;
import com.arvatosystems.t9t.doc.jpa.entities.DocTemplateEntity;
import com.arvatosystems.t9t.doc.jpa.mapping.IDocConfigDTOMapper;
import com.arvatosystems.t9t.doc.jpa.mapping.IDocEmailCfgDTOMapper;
import com.arvatosystems.t9t.doc.jpa.mapping.IDocTemplateDTOMapper;
import com.arvatosystems.t9t.doc.jpa.persistence.IDocComponentEntityResolver;
import com.arvatosystems.t9t.doc.jpa.persistence.IDocConfigEntityResolver;
import com.arvatosystems.t9t.doc.jpa.persistence.IDocEmailCfgEntityResolver;
import com.arvatosystems.t9t.doc.jpa.persistence.IDocTemplateEntityResolver;
import com.arvatosystems.t9t.doc.services.IDocPersistenceAccess;
import com.google.common.collect.ImmutableMap;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DocPersistenceAccess implements IDocPersistenceAccess {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocPersistenceAccess.class);
    private static final Map<String, MediaData> NO_COMPONENTS = ImmutableMap.of();
    private static final String DOCUMENT_ID = "documentId";

    private final IDocConfigEntityResolver docConfigResolver = Jdp.getRequired(IDocConfigEntityResolver.class);
    private final IDocConfigDTOMapper docConfigMapper = Jdp.getRequired(IDocConfigDTOMapper.class);
    private final IDocEmailCfgEntityResolver docEmailCfgResolver = Jdp.getRequired(IDocEmailCfgEntityResolver.class);
    private final IDocEmailCfgDTOMapper docEmailCfgMapper = Jdp.getRequired(IDocEmailCfgDTOMapper.class);
    private final IDocTemplateEntityResolver docTemplateResolver = Jdp.getRequired(IDocTemplateEntityResolver.class);
    private final IDocTemplateDTOMapper docTemplateMapper = Jdp.getRequired(IDocTemplateDTOMapper.class);
    private final IDocComponentEntityResolver docComponentResolver = Jdp.getRequired(IDocComponentEntityResolver.class);

    @Override
    public DocConfigDTO getDocConfigDTO(final String templateId) {
        return docConfigMapper.mapToDto(docConfigResolver.getEntityData(new DocConfigKey(templateId), true));
    }

    @Override
    public DocEmailCfgDTO getDocEmailCfgDTO(final DocModuleCfgDTO moduleCfg, final String templateId, final DocumentSelector selector) {
        final Class<? extends DocEmailCfgEntity> entityClass = docEmailCfgResolver.getEntityClass();
        final String query = getQueryStringForTextOrBinaryComponents(entityClass.getSimpleName(), DOCUMENT_ID, true);

        final EntityManager em = docEmailCfgResolver.getEntityManager();
        final TypedQuery<? extends DocEmailCfgEntity> entityQuery = em.createQuery(query, entityClass);
        setCommonQueryVariables(entityQuery, selector, docEmailCfgResolver.getSharedTenantRef(), moduleCfg.getConsiderGlobalTemplates());
        entityQuery.setParameter(DOCUMENT_ID, templateId);

        final List<? extends DocEmailCfgEntity> docEmailCfgEntities = entityQuery.getResultList();

        if (docEmailCfgEntities == null || docEmailCfgEntities.isEmpty()) {
            LOGGER.error("docEmailCfg not found for ID {}, language {}", templateId, selector.getLanguageCode());
            throw new T9tException(T9tDocExtException.CONFIGURATION_NOT_FOUND_ERROR);
        }
        // select the best fit
        return docEmailCfgMapper.mapToDto(CollectionExtensions.ofMaxWeight(docEmailCfgEntities, (final DocEmailCfgEntity emailCfg) -> {
            return getWeight(emailCfg, moduleCfg);
        }));
    }

    @Override
    public DocTemplateDTO getDocTemplateDTO(final DocModuleCfgDTO moduleCfg, final String templateId, final DocumentSelector selector) {
        final Class<? extends DocTemplateEntity> entityClass = docTemplateResolver.getEntityClass();
        final String query = getQueryStringForTextOrBinaryComponents(entityClass.getSimpleName(), DOCUMENT_ID, true);

        final EntityManager em = docTemplateResolver.getEntityManager();
        final TypedQuery<? extends DocTemplateEntity> entityQuery = em.createQuery(query, entityClass);
        setCommonQueryVariables(entityQuery, selector, docTemplateResolver.getSharedTenantRef(), moduleCfg.getConsiderGlobalTemplates());
        entityQuery.setParameter(DOCUMENT_ID, templateId);

        final List<? extends DocTemplateEntity> docTemplateEntities = entityQuery.getResultList();

        if (docTemplateEntities == null || docTemplateEntities.isEmpty()) {
            LOGGER.error("docTemplate not found for ID {}, language {}", templateId, selector.getLanguageCode());
            throw new T9tException(T9tDocExtException.CONFIGURATION_NOT_FOUND_ERROR);
        }
        // select the best fit
        return docTemplateMapper.mapToDto(CollectionExtensions.ofMaxWeight(docTemplateEntities, (final DocTemplateEntity template) -> {
            return getWeight(template, moduleCfg);
        }));
    }

    @Override
    public Map<String, MediaData> getDocComponents(final DocModuleCfgDTO moduleCfg, final DocumentSelector selector) {
        final Class<? extends DocComponentEntity> entityClass = docComponentResolver.getEntityClass();
        final String query = getQueryStringForTextOrBinaryComponents(entityClass.getSimpleName(), DOCUMENT_ID, false);

        final EntityManager em = docComponentResolver.getEntityManager();
        final TypedQuery<? extends DocComponentEntity> entityQuery = em.createQuery(query, entityClass);
        setCommonQueryVariables(entityQuery, selector, docComponentResolver.getSharedTenantRef(), moduleCfg.getConsiderGlobalTexts());

        final List<? extends DocComponentEntity> docComponentEntities = entityQuery.getResultList();

        if (docComponentEntities == null || docComponentEntities.isEmpty()) {
            return NO_COMPONENTS; // empty result, weird but OK
        }

        // filter: for every documentId, select the one of the best weight
        final Map<String, MediaData> map = new ConcurrentHashMap<>(docComponentEntities.size()); // guess some practical size
        String lastVariableId = null;
        int currentWeight = Integer.MIN_VALUE;

        for (final DocComponentEntity component : docComponentEntities) {
            final int newWeight = getWeight(component, moduleCfg);
            if (lastVariableId == null || !lastVariableId.equals(component.getDocumentId()) || newWeight > currentWeight) {
                // definitely store this one
                lastVariableId = component.getDocumentId();
                currentWeight = newWeight;
                final MediaData data = component.getData();
                // store the CID (name)
                if (data.getZ() == null) {
                    data.setZ(ImmutableMap.of("cid", component.getDocumentId())); // set a read-only uniform map
                } else {
                    data.getZ().put("cid", component.getDocumentId()); // add mapping to existing data
                }
                data.freeze(); // the other fields should be immutable as well because we use it in a cache
                               // returning references
                map.put(component.getDocumentId(), data);
            }
        }
        return map;
    }

    protected void setCommonQueryVariables(final TypedQuery<?> entityQuery, final DocumentSelector selector, final Long tenantRef,
            final boolean considerGlobalTenant) {
        entityQuery.setParameter("entityId", selector.getEntityId());
        entityQuery.setParameter("countryCode", selector.getCountryCode());
        entityQuery.setParameter("currencyCode", selector.getCurrencyCode());
        entityQuery.setParameter("languages", Arrays.asList(MessagingUtil.getLanguagesWithFallback(selector.getLanguageCode(), "xx")));
        entityQuery.setParameter("tenantRef", tenantRef);
        entityQuery.setParameter("globalTenantRef", considerGlobalTenant ? T9tConstants.GLOBAL_TENANT_REF42 : tenantRef);
    }

    protected int getWeight(final DocEmailCfgEntity emailCfg, final DocModuleCfgDTO moduleCfg) {
        if (emailCfg.getPrio() != null) {
            return emailCfg.getPrio();
        }
        int weight = 0;
        if (!T9tConstants.GLOBAL_TENANT_REF42.equals(emailCfg.getTenantRef())) {
            weight += moduleCfg.getWeightTenantMatch();
        }
        if (!DocConstants.DEFAULT_COUNTRY_CODE.equals(emailCfg.getCountryCode())) {
            weight += moduleCfg.getWeightCountryMatch();
        }
        if (!DocConstants.DEFAULT_CURRENCY_CODE.equals(emailCfg.getCurrencyCode())) {
            weight += moduleCfg.getWeightCurrencyMatch();
        }
        if (!DocConstants.DEFAULT_ENTITY_ID.equals(emailCfg.getEntityId())) {
            weight += moduleCfg.getWeightEntityMatch();
        }
        if (!DocConstants.DEFAULT_LANGUAGE_CODE.equals(emailCfg.getLanguageCode())) {
            weight += moduleCfg.getWeightLanguageMatch() * emailCfg.getLanguageCode().length();
        }
        return weight;
    }

    protected int getWeight(final DocTemplateEntity template, final DocModuleCfgDTO moduleCfg) {
        if (template.getPrio() != null) {
            return template.getPrio();
        }
        int weight = 0;
        if (!T9tConstants.GLOBAL_TENANT_REF42.equals(template.getTenantRef())) {
            weight += moduleCfg.getWeightTenantMatch();
        }
        if (!DocConstants.DEFAULT_COUNTRY_CODE.equals(template.getCountryCode())) {
            weight += moduleCfg.getWeightCountryMatch();
        }
        if (!DocConstants.DEFAULT_CURRENCY_CODE.equals(template.getCurrencyCode())) {
            weight += moduleCfg.getWeightCurrencyMatch();
        }
        if (!DocConstants.DEFAULT_ENTITY_ID.equals(template.getEntityId())) {
            weight += moduleCfg.getWeightEntityMatch();
        }
        if (!DocConstants.DEFAULT_LANGUAGE_CODE.equals(template.getLanguageCode())) {
            weight += moduleCfg.getWeightLanguageMatch() * template.getLanguageCode().length();
        }
        return weight;
    }

    protected int getWeight(final DocComponentEntity component, final DocModuleCfgDTO moduleCfg) {
        if (component.getPrio() != null) {
            return component.getPrio();
        }
        int weight = 0;
        if (!T9tConstants.GLOBAL_TENANT_REF42.equals(component.getTenantRef())) {
            weight += moduleCfg.getWeightTenantMatch();
        }
        if (!DocConstants.DEFAULT_COUNTRY_CODE.equals(component.getCountryCode())) {
            weight += moduleCfg.getWeightCountryMatch();
        }
        if (!DocConstants.DEFAULT_CURRENCY_CODE.equals(component.getCurrencyCode())) {
            weight += moduleCfg.getWeightCurrencyMatch();
        }
        if (!DocConstants.DEFAULT_ENTITY_ID.equals(component.getEntityId())) {
            weight += moduleCfg.getWeightEntityMatch();
        }
        if (!DocConstants.DEFAULT_LANGUAGE_CODE.equals(component.getLanguageCode())) {
            weight += moduleCfg.getWeightLanguageMatch() * component.getLanguageCode().length();
        }
        return weight;
    }

    private String getQueryStringForTextOrBinaryComponents(final String entityName, final String mainVariable, final boolean mainVarEquality) {
        String sql =
            "SELECT r FROM " + entityName + " r"
            + " WHERE r.tenantRef  IN (:tenantRef,:globalTenantRef)"
            + " AND r.entityId     IN (:entityId,'-')"
            + " AND r.languageCode IN (:languages)"
            + " AND r.currencyCode IN (:currencyCode,'XXX')"
            + " AND r.countryCode  IN (:countryCode,'XX')";
        if (mainVarEquality) {
            sql += " AND r." + mainVariable  + " =:" + mainVariable;
        } else {
            sql += " ORDER BY r." + mainVariable;
        }
        return sql;
    }
}
