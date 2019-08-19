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
package com.arvatosystems.t9t.base.jpa.impl;

import java.util.Collections;
import java.util.List;

import com.arvatosystems.t9t.base.jpa.IResolverSurrogateKey42;
import com.arvatosystems.t9t.base.search.TwoRefs;
import com.arvatosystems.t9t.base.services.IRefGenerator;

import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.dp.Alternative;
import de.jpaw.dp.Jdp;

/** Base implementation of the IEntityResolver interface, suitable for entities with an artificial "Long" key. */
@Alternative
public abstract class AbstractResolverSurrogateKey42<
    REF extends Ref,
    TRACKING extends TrackingBase,
    ENTITY extends BonaPersistableKey<Long> & BonaPersistableTracking<TRACKING>
    > extends AbstractResolverAnyKey42<Long, TRACKING, ENTITY> implements IResolverSurrogateKey42<REF, TRACKING, ENTITY> {

    private static final List<SortColumn> DEFAULT_SORT_ORDER = Collections.singletonList(new SortColumn("objectRef", false)); // descending: newest first!

    // @Inject
    private final IRefGenerator genericRefGenerator = Jdp.getRequired(IRefGenerator.class);

    @Override
    public final boolean hasArtificialPrimaryKey() {
        return true;
    }

    /**
     * Subroutine used to resolve nested references (i.e. objects containing Object references itself). This code has to be provided by hand, i.e. standard
     * implementation is just a hook and performs no activity.
     *
     * A typical implementation would work as follows: if (arg instanceof MySpecialArg) { // manual code to convert to MyStandardArg return convertedArg; }
     *
     * If no matching type is found, the method should always fall back and return super.resolverNestedRefs() to allow nested implementations.
     * */
    protected REF resolveNestedRefs(REF arg) {
        return arg;
    }

    @Override
    public Long getRef(REF entityRef, boolean onlyActive) {
        if (entityRef == null) {
            return null;        // play null-safe
        }
        if (entityRef.getObjectRef() != null) {
            // shortcut: the required information is available already! (ignores onlyActive because we resolved already before)
            return entityRef.getObjectRef();
        }
        return getEntityDataByGenericKey(resolveNestedRefs(entityRef), Long.class, (t, cb) -> t.<Long>get("objectRef"), onlyActive);
    }

    @Override
    public <Z> Z getField(REF entityRef, boolean onlyActive, String fieldName, Class<Z> cls) {
        if (entityRef == null) {
            return null;        // play null-safe
        }
        return getEntityDataByGenericKey(resolveNestedRefs(entityRef), cls, (t, cb) -> t.get(fieldName), onlyActive);
    }

    @Override
    public TwoRefs getRefs(REF entityRef, boolean onlyActive, String fieldName) {
        return getEntityDataByGenericKey(resolveNestedRefs(entityRef), TwoRefs.class,
            (t, cb) -> cb.construct(TwoRefs.class, t.get("objectRef"), t.get(fieldName)),
            onlyActive);
    }

    @Override
    public ENTITY getEntityData(REF entityRef, boolean onlyActive) {

        if (entityRef == null) {
            return null;        // play null-safe
        }
        Long r = entityRef.getObjectRef();
        if (r != null) {
            return findInternal(r, onlyActive, true);
        }
        // preprocess the reference
        return getEntityDataByGenericKey(resolveNestedRefs(entityRef), onlyActive);
    }

    /** Sets the default sort order to sort by artificial key. */
    @Override
    protected List<SortColumn> getDefaultSortColumns() {
        return DEFAULT_SORT_ORDER;
    }

    @Override
    public Long createNewPrimaryKey() {
        return Long.valueOf(genericRefGenerator.generateRef(getRtti()));
    }

    @Override
    protected void createNewArtificialKeyIfRequired(final ENTITY entity) {
        if (entity.ret$Key() == null)
            entity.put$Key(createNewPrimaryKey());
    }
}
