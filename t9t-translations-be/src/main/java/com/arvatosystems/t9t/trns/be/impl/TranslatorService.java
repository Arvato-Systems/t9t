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
package com.arvatosystems.t9t.trns.be.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.trns.TextCategory;
import com.arvatosystems.t9t.base.trns.TranslationsPartialKey;
import com.arvatosystems.t9t.server.services.ITranslator;
import com.arvatosystems.t9t.trns.TranslationsDTO;
import com.arvatosystems.t9t.trns.TranslationsUtil;
import com.arvatosystems.t9t.trns.TrnsModuleCfgDTO;
import com.arvatosystems.t9t.trns.services.ITrnsPersistenceAccess;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;

@Singleton
public class TranslatorService implements ITranslator {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranslatorService.class);

    protected final ITrnsPersistenceAccess persistenceAccess = Jdp.getRequired(ITrnsPersistenceAccess.class);
    //@Inject
    protected final Provider<RequestContext> contextProvider = Jdp.getProvider(RequestContext.class);

    protected static interface ITranslationsCache {
        String get(String key);         // returns the translation for a specified key.
    }

    protected static class EmptyTranslationCache implements ITranslationsCache {
        @Override
        public String get(String key) {
            return null;
        }
    }

    protected static final ITranslationsCache EMPTY_CACHE = new EmptyTranslationCache();

    /** Caches all translated texts for a tenant / language combination. */
    protected static class TranslationCache implements ITranslationsCache {
        private final Map<String, String> id2Translation;

        TranslationCache(List<TranslationsDTO> translations) {
            id2Translation = new ConcurrentHashMap<String, String>(translations.size());
            for (TranslationsDTO e: translations)
                id2Translation.put(TranslationsUtil.getKey(e), e.getText());
        }

        @Override
        public String get(String key) {
            return id2Translation.get(key);
        }

    }
    protected static final Map<String, ITranslationsCache> CACHE = new ConcurrentHashMap<String, ITranslationsCache>(20);
    protected static final Map<String, Object> MUTEXES = new ConcurrentHashMap<String, Object>(20);

    protected ITranslationsCache getCache(Long tenantRef, String tenantId, String languageCode) {
        String key = tenantId + ":" + languageCode;
        ITranslationsCache cache = CACHE.get(key);
        if (cache != null)
            return cache;
        // read the translations from disk and create a new Cache
        // use a mutex to ensure that the expensive disk read is not performed twice
        Object mutex = new Object();
        Object mutex2 = MUTEXES.putIfAbsent(key, mutex);
        if (mutex2 == null)
            mutex2 = mutex;
        synchronized(mutex2) {
            // retry the cache get to avoid a race condition
            cache = CACHE.get(key);
            if (cache != null)
                return cache;       // race condition
            try {
                List<TranslationsDTO> texts = persistenceAccess.readLanguage(tenantRef, languageCode);
                cache = texts.isEmpty() ? EMPTY_CACHE : new TranslationCache(texts);
            } catch (Exception e) {
                LOGGER.error("Cannot read translations for " + e.getMessage(), e);
                cache = EMPTY_CACHE;
            }
            CACHE.put(key, cache);
        }
        return cache;
    }


    @Override
    public List<String> getTranslations(String languageCode, TextCategory category, List<String> qualifiedIds) {
        // TODO Auto-generated method stub
        return null;
    }

    public TrnsModuleCfgDTO tenantCfg;  // TODO!


    protected String resolveText(ITranslationsCache cache, TranslationsPartialKey key) {
        String text = cache.get(TranslationsUtil.getKey(key));
        return (text == null) ? cache.get(key.getId()) : text;
    }


    @Override
    public List<String> getTranslations(String languageCode, List<TranslationsPartialKey> keys) {
        RequestContext ctx = contextProvider.get();
        final boolean isLongLanguage = languageCode.length() > 2;
        final String shortLanguage = isLongLanguage ? languageCode.substring(0, 2) : languageCode;
        final List<ITranslationsCache> caches = new ArrayList<ITranslationsCache>(4);

        if (tenantCfg.getAttemptLocalTenant() && !T9tConstants.GLOBAL_TENANT_ID.equals(ctx.tenantId)) {
            // specific tenant
            if (tenantCfg.getAttemptDialects() && isLongLanguage)
                caches.add(getCache(ctx.tenantRef, ctx.tenantId, languageCode));
            caches.add(getCache(ctx.tenantRef, ctx.tenantId, shortLanguage));
        }
        // add default tenant fallback
        if (tenantCfg.getAttemptDialects() && isLongLanguage)
            caches.add(getCache(T9tConstants.GLOBAL_TENANT_REF42, T9tConstants.GLOBAL_TENANT_ID, languageCode));
        caches.add(getCache(T9tConstants.GLOBAL_TENANT_REF42, T9tConstants.GLOBAL_TENANT_ID, shortLanguage));

        // traverse the caches
        List<String> results = new ArrayList<String>(keys.size());
        for (TranslationsPartialKey key : keys) {
            String r = null;
            for (int i = 0; i < caches.size(); ++i) {
                r = resolveText(caches.get(i), key);
                if (r != null)
                    break;
            }
            if (r == null)
                r = "{" + key.getId() + "}";
            results.add(r);
        }
        return results;
    }
}
