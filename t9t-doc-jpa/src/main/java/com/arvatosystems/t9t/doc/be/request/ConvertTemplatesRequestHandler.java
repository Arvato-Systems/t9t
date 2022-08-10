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
package com.arvatosystems.t9t.doc.be.request;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.search.DummySearchCriteria;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.doc.DocEmailReceiverDTO;
import com.arvatosystems.t9t.doc.DocTemplateDTO;
import com.arvatosystems.t9t.doc.T9tDocException;
import com.arvatosystems.t9t.doc.T9tDocTools;
import com.arvatosystems.t9t.doc.api.TemplateType;
import com.arvatosystems.t9t.doc.jpa.entities.DocConfigEntity;
import com.arvatosystems.t9t.doc.jpa.entities.DocEmailCfgEntity;
import com.arvatosystems.t9t.doc.jpa.entities.DocTemplateEntity;
import com.arvatosystems.t9t.doc.jpa.persistence.IDocConfigEntityResolver;
import com.arvatosystems.t9t.doc.jpa.persistence.IDocEmailCfgEntityResolver;
import com.arvatosystems.t9t.doc.jpa.persistence.IDocTemplateEntityResolver;
import com.arvatosystems.t9t.doc.request.ConvertTemplatesRequest;
import com.arvatosystems.t9t.doc.services.ITemplateConversion;

import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.dp.Jdp;
import jakarta.persistence.EntityManager;

public class ConvertTemplatesRequestHandler extends AbstractRequestHandler<ConvertTemplatesRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertTemplatesRequestHandler.class);

    protected final IDocConfigEntityResolver   configResolver    = Jdp.getRequired(IDocConfigEntityResolver.class);
    protected final IDocEmailCfgEntityResolver emailCfgResolver  = Jdp.getRequired(IDocEmailCfgEntityResolver.class);
    protected final IDocTemplateEntityResolver templateResolver  = Jdp.getRequired(IDocTemplateEntityResolver.class);
    protected final ITemplateConversion defaultConversionService = Jdp.getRequired(ITemplateConversion.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final ConvertTemplatesRequest request) throws Exception {
        final UnicodeFilter tenantFilter = new UnicodeFilter(T9tConstants.TENANT_ID_FIELD_NAME);
        final SearchCriteria tenantCriteria = new DummySearchCriteria();
        tenantFilter.setEqualsValue(ctx.tenantId);
        tenantFilter.freeze();
        tenantCriteria.setSearchFilter(tenantFilter);
        tenantCriteria.freeze();

        String offendingDocumentId = null;
        final SearchCriteria criteriaToApply;
        if (request.getDocumentId() == null) {
            // a filter for tenantId is always required, because we could get the fallback templates stored under @ tenant in the read operation
            // and writing these back would fail.
            criteriaToApply = tenantCriteria;
        } else {
            final UnicodeFilter templateFilter = new UnicodeFilter("documentId");
            templateFilter.setEqualsValue(request.getDocumentId());
            criteriaToApply = new DummySearchCriteria();
            criteriaToApply.setSearchFilter(SearchFilters.and(tenantFilter, templateFilter));
            criteriaToApply.freeze(); // freeze to be secure, as it's used 3 times
        }

        final ITemplateConversion conversionToApply = request.getCustomRules() == null ? defaultConversionService : (id, template) -> {
            return T9tDocTools.convertTemplateAddOrSwapPrefix(template, request.getCustomRules());
        };

        // first, convert all templates
        final EntityManager em = templateResolver.getEntityManager();
        final List<DocTemplateEntity> templates = templateResolver.search(criteriaToApply);
        for (final DocTemplateEntity t: templates) {
            final String newTemplate = conversionToApply.convertTemplate(t.getDocumentId(), t.getTemplate());
            if (newTemplate.length() >= DocTemplateDTO.meta$$template.getLength()) {
                LOGGER.error("Conversion of template {} would exceed the maximum allowed size", t.getDocumentId());
                offendingDocumentId = t.getDocumentId();
            } else {
                t.setTemplate(newTemplate);
            }
        }
        if (offendingDocumentId != null) {
            // throw an exception, returning one offending ID. (for any others, see the logs)
            throw new T9tException(T9tDocException.CONVERSION_EXCEEDS_MAX_TEMPLATE_SIZE, offendingDocumentId);
        }
        // save memory
        em.flush();
        em.clear();

        // run through all doc configs, for possible inline subjects
        final List<DocConfigEntity> configs = configResolver.search(criteriaToApply);
        for (final DocConfigEntity t: configs) {
            final DocEmailReceiverDTO emailCfg = t.getEmailSettings();
            if (emailCfg != null && emailCfg.getSubjectType() == TemplateType.INLINE) {
                final String newTemplate = conversionToApply.convertTemplate(emailCfg.getEmailSubject(), t.getDocumentId());
                if (newTemplate.length() >= DocEmailReceiverDTO.meta$$emailSubject.getLength()) {
                    LOGGER.error("Conversion of email subject {} would exceed the maximum allowed size", t.getDocumentId());
                    offendingDocumentId = t.getDocumentId();
                } else {
                    emailCfg.setEmailSubject(newTemplate);
                    t.setEmailSettings(emailCfg);
                }
            }
        }
        if (offendingDocumentId != null) {
            // throw an exception, returning one offending ID. (for any others, see the logs)
            throw new T9tException(T9tDocException.CONVERSION_EXCEEDS_MAX_SUBJECT_SIZE, offendingDocumentId);
        }
        // save memory
        em.flush();
        em.clear();

        // run through all email configs, for possible inline subjects
        final List<DocEmailCfgEntity> emailConfigs = emailCfgResolver.search(criteriaToApply);
        for (final DocEmailCfgEntity t: emailConfigs) {
            final DocEmailReceiverDTO emailCfg = t.getEmailSettings();
            if (emailCfg != null && emailCfg.getSubjectType() == TemplateType.INLINE) {
                final String newTemplate = conversionToApply.convertTemplate(emailCfg.getEmailSubject(), t.getDocumentId());
                if (newTemplate.length() >= DocEmailReceiverDTO.meta$$emailSubject.getLength()) {
                    LOGGER.error("Conversion of email subject {} would exceed the maximum allowed size", t.getDocumentId());
                    offendingDocumentId = t.getDocumentId();
                } else {
                    emailCfg.setEmailSubject(newTemplate);
                    t.setEmailSettings(emailCfg);
                }
            }
        }
        if (offendingDocumentId != null) {
            // throw an exception, returning one offending ID. (for any others, see the logs)
            throw new T9tException(T9tDocException.CONVERSION_EXCEEDS_MAX_SUBJECT_SIZE, offendingDocumentId);
        }

        return ok();
    }
}
