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
package com.arvatosystems.t9t.base.jpa;

import com.arvatosystems.t9t.base.T9tException;

import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.Ref;

/**
 * Define restriction that can be applied before/after entity is resolved.
 * Implementation not necessarily implements all {@linkplain ResolverRestriction#apply()} methods.
 * If it's not implemented, a {@linkplain UnsupportedOperationException} should be throw instead.
 * As to which {@linkplain ResolverRestriction#apply()} method to be implemented, this really depends
 * on the structure of the entity as well as the data required for the restriction to be applied.
 * <p>
 * Most of the time, restriction should be called "wrapping" the actual resolve method itself:
 * <pre>
 *    locationsResolver.findActive(restriction.apply(someParams.getLocationRef().getObjectRef()), true);
 * </pre>
 * @author LIEE001
 * @param <T> reference type
 * @param <TRACKING> tracking type
 * @param <ENTITY> entity type
 */
public interface IResolverRestriction <T extends Ref, TRACKING extends TrackingBase, ENTITY extends BonaPersistableKey<Long> & BonaPersistableTracking<TRACKING>> {

    /**
     * Apply restriction. This should be mainly used before resolving the entity itself.
     * @param ref entity object reference
     * @return the passed entity object reference
     * @throws T9tException if the passed object reference is restricted!
     */
    Long apply(Long ref);

    /**
     * Apply restriction. This should be mainly used before resolving the entity itself.
     * @param ref entity reference
     * @return the passed entity reference
     * @throws T9tException if the passed reference is restricted!
     */
    T apply(T ref);

    /**
     * Apply restriction. This should be mainly used after resolving the entity itself.
     * Depending on the type of restriction, sometimes its NOT possible to apply the restriction
     * without getting the actual entity data first i.e. the restriction depends on the data which
     * is not available on the entity object reference or entity key.
     * @param entity entity
     * @return the passed entity
     * @throws T9tException if the passed entity is restricted!
     */
    ENTITY apply(ENTITY entity);
}
