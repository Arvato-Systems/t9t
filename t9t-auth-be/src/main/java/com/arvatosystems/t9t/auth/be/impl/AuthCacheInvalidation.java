/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.auth.be.impl;

import com.arvatosystems.t9t.base.services.IAuthCacheInvalidation;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class AuthCacheInvalidation implements IAuthCacheInvalidation {
    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);

	@Override
	public void invalidateAuthCache(RequestContext ctx, String classname, Long ref, String id) {
		// first, do the standard cache invalidation
		executor.clearCache(ctx, classname, null);
		// also, do the specific auth cache invalidation
		executor.clearCache(ctx, AUTH_CACHE_ID, null);
	}
}
