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

import java.util.Set;
import java.util.function.BiConsumer;

import com.arvatosystems.t9t.base.search.GetDataResponse;

import de.jpaw.bonaparte.jpa.BonaData;
import de.jpaw.bonaparte.jpa.BonaKey;
import de.jpaw.bonaparte.pojos.apiw.Ref;

public interface IDataGetter {
    /** Query data using a POJO constructor. */
    public <D extends Ref> GetDataResponse<D> query(IResolverSurrogateKey42 resolver, String constructor, Class<D> keyClass, Set<Long> refs);

    /** Query data using some DTO mapping. */
    public <D extends Ref, E extends BonaKey<Long> & BonaData<D>> GetDataResponse<D> query(IResolverSurrogateKey42 resolver, Class<E> entityClass, Class<D> keyClass, Set<Long> refs);

    /** Query data using some DTO mapping, via entity graph. */
    public <D extends Ref, E extends BonaKey<Long> & BonaData<D>> GetDataResponse<D> query(IResolverSurrogateKey42 resolver, Class<E> entityClass, Class<D> keyClass, Set<Long> refs,
      String entityGraphName, BiConsumer<D, E> updater);
}
