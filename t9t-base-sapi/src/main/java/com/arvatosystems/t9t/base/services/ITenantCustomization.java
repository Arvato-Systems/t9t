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
package com.arvatosystems.t9t.base.services;

import com.arvatosystems.t9t.base.api.RequestParameters;

import de.jpaw.bonaparte.core.BonaPortable;

/** A TenantCustomization is a shared object which can be cached and provides customization information for a given tenant.
 * For tenants which don't use customization at all, a constant object NOCustomization is returned.
 * Usually instances are kept unless no access for the given tenant is done within 75 seconds.
 *
 * @author Michael Bischoff
 *
 */
public interface ITenantCustomization {
    // DTO mapping
    public <DTO extends BonaPortable> DTO newDtoInstance(int rtti, Class<DTO> baseClass);

    public <DTO extends BonaPortable> Class<? extends DTO> getDtoClass(int rtti, Class<DTO> baseClass);

    // Entity mapping
    public <ENTITY> ENTITY newEntityInstance(int rtti, Class<ENTITY> baseClass);

    public <ENTITY> Class<ENTITY> getEntityClass(int rtti, Class<ENTITY> baseClass);

    // Request handler name mapping
    public String getRequestHandlerClassname(RequestParameters params);

    /** Return a cached instance of a request handler. */
    public <P extends RequestParameters> IRequestHandler<P> getRequestHandler(P params);
}
