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
package com.arvatosystems.t9t.doc.jpa.impl;

import com.arvatosystems.t9t.base.MessagingUtil
import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.doc.DocConfigDTO
import com.arvatosystems.t9t.doc.DocConfigKey
import com.arvatosystems.t9t.doc.DocConstants
import com.arvatosystems.t9t.doc.DocEmailCfgDTO
import com.arvatosystems.t9t.doc.DocModuleCfgDTO
import com.arvatosystems.t9t.doc.DocTemplateDTO
import com.arvatosystems.t9t.doc.T9tDocExtException
import com.arvatosystems.t9t.doc.api.DocumentSelector
import com.arvatosystems.t9t.doc.jpa.entities.DocComponentEntity
import com.arvatosystems.t9t.doc.jpa.entities.DocEmailCfgEntity
import com.arvatosystems.t9t.doc.jpa.entities.DocTemplateEntity
import com.arvatosystems.t9t.doc.jpa.mapping.IDocConfigDTOMapper
import com.arvatosystems.t9t.doc.jpa.mapping.IDocEmailCfgDTOMapper
import com.arvatosystems.t9t.doc.jpa.mapping.IDocTemplateDTOMapper
import com.arvatosystems.t9t.doc.jpa.persistence.IDocComponentEntityResolver
import com.arvatosystems.t9t.doc.jpa.persistence.IDocConfigEntityResolver
import com.arvatosystems.t9t.doc.jpa.persistence.IDocEmailCfgEntityResolver
import com.arvatosystems.t9t.doc.jpa.persistence.IDocTemplateEntityResolver
import com.arvatosystems.t9t.doc.services.IDocPersistenceAccess
import com.google.common.collect.ImmutableMap
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.media.MediaData
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton
import java.util.Arrays
import java.util.Map
import java.util.concurrent.ConcurrentHashMap
import javax.persistence.TypedQuery

import static extension com.arvatosystems.t9t.base.services.CollectionExtensions.*
import com.arvatosystems.t9t.base.T9tException

@Singleton
@AddLogger
class DocPersistenceAccess implements IDocPersistenceAccess, DocConstants, T9tConstants {
    static final Map<String,MediaData> NO_COMPONENTS = ImmutableMap.of();

    @Inject IDocConfigEntityResolver        docConfigResolver
    @Inject IDocConfigDTOMapper             docConfigMapper
    @Inject IDocEmailCfgEntityResolver      docEmailCfgResolver
    @Inject IDocEmailCfgDTOMapper           docEmailCfgMapper
    @Inject IDocTemplateEntityResolver      docTemplateResolver
    @Inject IDocTemplateDTOMapper           docTemplateMapper
    @Inject IDocComponentEntityResolver     docComponentResolver


    override DocConfigDTO getDocConfigDTO(String templateId) {
        return docConfigMapper.mapToDto(docConfigResolver.getEntityData(new DocConfigKey(templateId), true));
    }

    def private String getQueryStringForTextOrBinaryComponents(String entityName, String mainVariable, boolean mainVarEquality) {
        return '''
            SELECT r FROM «entityName» r
             WHERE r.tenantRef    in (:tenantRef,:globalTenantRef)
               AND r.entityId     in (:entityId,'-')
               AND r.languageCode in (:languages)
               AND r.currencyCode in (:currencyCode,'XXX')
               AND r.countryCode  in (:countryCode,'XX')
               «IF mainVarEquality»AND r.«mainVariable» =:«mainVariable»«ELSE»   ORDER BY r.«mainVariable»«ENDIF»
            '''
    }

    def protected void setCommonQueryVariables(TypedQuery<?> entityQuery, DocumentSelector selector, Long tenantRef, boolean considerGlobalTenant) {
        entityQuery.setParameter("entityId",        selector.entityId);
        entityQuery.setParameter("countryCode",     selector.countryCode);
        entityQuery.setParameter("currencyCode",    selector.currencyCode);
        entityQuery.setParameter("languages",       Arrays.asList(MessagingUtil.getLanguagesWithFallback(selector.languageCode, "xx")));
        entityQuery.setParameter("tenantRef",       tenantRef)
        entityQuery.setParameter("globalTenantRef", if (considerGlobalTenant) GLOBAL_TENANT_REF42 else tenantRef);
    }

    override DocEmailCfgDTO getDocEmailCfgDTO(DocModuleCfgDTO moduleCfg, String templateId, DocumentSelector selector) {
        val Class<? extends DocEmailCfgEntity> entityClass = docEmailCfgResolver.entityClass
        val query = getQueryStringForTextOrBinaryComponents(entityClass.simpleName, "documentId", true);

        val em = docEmailCfgResolver.entityManager
        val TypedQuery<? extends DocEmailCfgEntity> entityQuery = em.createQuery(query, entityClass);
        entityQuery.setCommonQueryVariables(selector, docEmailCfgResolver.sharedTenantRef, moduleCfg.considerGlobalTemplates)
        entityQuery.setParameter("documentId", templateId)

        val docEmailCfgEntities = entityQuery.resultList

        if (docEmailCfgEntities.nullOrEmpty) {
            LOGGER.error("docEmailCfg not found for ID {}, language {}", templateId, selector.languageCode);
            throw new T9tException(T9tDocExtException.CONFIGURATION_NOT_FOUND_ERROR);
        }
        // select the best fit
        return docEmailCfgMapper.mapToDto(docEmailCfgEntities.ofMaxWeight [ getWeight(moduleCfg) ]);
    }

    override DocTemplateDTO getDocTemplateDTO(DocModuleCfgDTO moduleCfg, String templateId, DocumentSelector selector) {
        val Class<? extends DocTemplateEntity> entityClass = docTemplateResolver.entityClass
        val query = getQueryStringForTextOrBinaryComponents(entityClass.simpleName, "documentId", true);

        val em = docTemplateResolver.entityManager
        val TypedQuery<? extends DocTemplateEntity> entityQuery = em.createQuery(query, entityClass);
        entityQuery.setCommonQueryVariables(selector, docTemplateResolver.sharedTenantRef, moduleCfg.considerGlobalTemplates)
        entityQuery.setParameter("documentId", templateId)

        val docTemplateEntities = entityQuery.resultList

        if (docTemplateEntities.nullOrEmpty) {
            LOGGER.error("docTemplate not found for ID {}, language {}", templateId, selector.languageCode);
            throw new T9tException(T9tDocExtException.CONFIGURATION_NOT_FOUND_ERROR);
        }
        // select the best fit
        return docTemplateMapper.mapToDto(docTemplateEntities.ofMaxWeight [ getWeight(moduleCfg) ]);
    }

    def protected int getWeight(DocEmailCfgEntity it, DocModuleCfgDTO moduleCfg) {
        return
            prio ?:
            (if (GLOBAL_TENANT_REF42   == tenantRef)    0 else moduleCfg.weightTenantMatch) +
            (if (DEFAULT_COUNTRY_CODE  == countryCode)  0 else moduleCfg.weightCountryMatch) +
            (if (DEFAULT_CURRENCY_CODE == currencyCode) 0 else moduleCfg.weightCurrencyMatch) +
            (if (DEFAULT_ENTITY_ID     == entityId)     0 else moduleCfg.weightEntityMatch) +
            (if (DEFAULT_LANGUAGE_CODE == languageCode) 0 else moduleCfg.weightLanguageMatch * languageCode.length)
    }
    def protected int getWeight(DocTemplateEntity it, DocModuleCfgDTO moduleCfg) {
        return
            prio ?:
            (if (GLOBAL_TENANT_REF42   == tenantRef)    0 else moduleCfg.weightTenantMatch) +
            (if (DEFAULT_COUNTRY_CODE  == countryCode)  0 else moduleCfg.weightCountryMatch) +
            (if (DEFAULT_CURRENCY_CODE == currencyCode) 0 else moduleCfg.weightCurrencyMatch) +
            (if (DEFAULT_ENTITY_ID     == entityId)     0 else moduleCfg.weightEntityMatch) +
            (if (DEFAULT_LANGUAGE_CODE == languageCode) 0 else moduleCfg.weightLanguageMatch * languageCode.length)
    }
    def protected int getWeight(DocComponentEntity it, DocModuleCfgDTO moduleCfg) {
        return
            prio ?:
            (if (GLOBAL_TENANT_REF42   == tenantRef)    0 else moduleCfg.weightTenantMatch) +
            (if (DEFAULT_COUNTRY_CODE  == countryCode)  0 else moduleCfg.weightCountryMatch) +
            (if (DEFAULT_CURRENCY_CODE == currencyCode) 0 else moduleCfg.weightCurrencyMatch) +
            (if (DEFAULT_ENTITY_ID     == entityId)     0 else moduleCfg.weightEntityMatch) +
            (if (DEFAULT_LANGUAGE_CODE == languageCode) 0 else moduleCfg.weightLanguageMatch * languageCode.length)
    }

    override Map<String,MediaData> getDocComponents (DocModuleCfgDTO moduleCfg, DocumentSelector selector) {
        val Class<? extends DocComponentEntity> entityClass = docComponentResolver.entityClass
        val query = getQueryStringForTextOrBinaryComponents(entityClass.simpleName, "documentId", false);

        val em = docComponentResolver.entityManager
        val TypedQuery<? extends DocComponentEntity> entityQuery = em.createQuery(query, entityClass);
        entityQuery.setCommonQueryVariables(selector, docComponentResolver.sharedTenantRef, moduleCfg.considerGlobalTexts)

        val docComponentEntities = entityQuery.resultList

        if (docComponentEntities.nullOrEmpty) {
            return NO_COMPONENTS;     // empty result, weird but OK
        }

        // filter: for every documentId, select the one of the best weight
        val map = new ConcurrentHashMap<String,MediaData>(docComponentEntities.size)   // guess some practical size
        var String lastVariableId = null
        var int currentWeight = Integer.MIN_VALUE

        for (e : docComponentEntities) {
            val newWeight = e.getWeight(moduleCfg)
            if (lastVariableId === null || lastVariableId != e.documentId || newWeight > currentWeight) {
                // definitely store this one
                lastVariableId  = e.documentId
                currentWeight   = newWeight
                val data        = e.data
                // store the CID (name)
                if (data.z === null)
                    data.z = ImmutableMap.of("cid", e.documentId) // set a read-only uniform map
                else
                    data.z.put("cid", e.documentId)     // add mapping to existing data
                data.freeze                             // the other fields should be immutable as well because we use it in a cache returning references
                map.put(e.documentId, data)
            }
        }
        return map
    }
}
