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
package com.arvatosystems.t9t.base.jpa.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.arvatosystems.t9t.base.jpa.IEntityMapper42;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.google.common.collect.ImmutableList;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW;
import de.jpaw.dp.Jdp;

/** base implementation of the IEntityMapper42 interface, only suitable for simple configuration data tables */
public abstract class AbstractEntityMapper42<
  KEY extends Serializable,
  DTO extends BonaPortable,
  TRACKING extends TrackingBase,
  ENTITY extends BonaPersistableKey<KEY> & BonaPersistableTracking<TRACKING>
> extends AbstractEntityMapper<KEY, DTO, TRACKING, ENTITY> implements IEntityMapper42<KEY, DTO, TRACKING, ENTITY> {
    private static final List EMPTY_RESULT_LIST = ImmutableList.of();

    @Override
    public final DataWithTrackingW<DTO, TRACKING> mapToDwt(final ENTITY entity) {
        if (entity == null) {
            return null;
        }
        final DataWithTrackingW<DTO, TRACKING> entry = new DataWithTrackingW<>();
        entry.setTracking(entity.ret$Tracking());
        entry.setData(mapToDto(entity));
        entry.setTenantRef(getTenantRef(entity)); // tenantRef has been defined in the data (category D) or it is of no interest to the caller
        return entry;
    }

    @Override
    public final List<DataWithTrackingW<DTO, TRACKING>> mapListToDwt(final Collection<ENTITY> entityList) {
        if (entityList == null) {
            return null;
        }
        final List<DataWithTrackingW<DTO, TRACKING>> resultList = new ArrayList<>(entityList.size());
        if (!haveCollectionToDtoMapper()) {
            // use single element mapping
            for (final ENTITY entity : entityList) {
                resultList.add(mapToDwt(entity));
            }
        } else {
            // accept the use of an intermediate list, but use the bulk mapper
            final List<DTO> resultList2 = new ArrayList<>(entityList.size());
            batchMapToDto(entityList, resultList2, NO_GRAPH, null, new HashMap<>());
            int i = 0;
            for (final ENTITY entity : entityList) {
                final DataWithTrackingW<DTO, TRACKING> entry = new DataWithTrackingW<>();
                entry.setTracking(entity.ret$Tracking());
                entry.setData(resultList2.get(i));
                entry.setTenantRef(getTenantRef(entity)); // tenantRef has been defined in the data (category D) or it is of no interest to the caller
                resultList.add(entry);
                ++i;
            }
        }
        return resultList;
    }


    @Override
    public final ReadAllResponse<DTO, TRACKING> createReadAllResponse(final List<ENTITY> data, final OutputSessionParameters op) throws Exception {
        final ReadAllResponse<DTO, TRACKING> rs = new ReadAllResponse<>();
        if (op == null) {
            // fill the result
            rs.setDataList(mapListToDwt(data));
        } else {
            // push output into an outputSession (export it)
            try (IOutputSession outputSession = Jdp.getRequired(IOutputSession.class)) {
                final Long sinkRef = outputSession.open(op);
                if (outputSession.getUnwrapTracking(op.getUnwrapTracking())) {
                    op.setSmartMappingForDataWithTracking(Boolean.FALSE);
                    for (final ENTITY entity : data) {
                        outputSession.store(mapToDto(entity));
                    }
                } else {
                    op.setSmartMappingForDataWithTracking(Boolean.TRUE);
                    final DataWithTrackingW<DTO, TRACKING> entry = new DataWithTrackingW<>();
                    for (final ENTITY entity : data) {
                        entry.setTracking(entity.ret$Tracking());
                        entry.setData(mapToDto(entity));
                        entry.setTenantRef(getTenantRef(entity)); // tenantRef has been defined in the data (category D) or it is of no interest to the caller
                        outputSession.store(entry);
                    }
                }
                // successful close: store ref
                rs.setSinkRef(sinkRef);
                rs.setDataList(EMPTY_RESULT_LIST);
            }
        }
        rs.setReturnCode(0);
        return rs;
    }


    /** returns the entity's tenantRef without the use of reflection, or null if the entity does not contain
     * a tenantRef field.
     * @param e
     * @return the tenantRef
     */
    @Override
    public Long getTenantRef(final ENTITY e) {
        return null;
    }

    /** Sets the entity's tenantRef without the use of reflection, or NOOP if the entity does not contain
     * a tenantRef field.
     * @param e - an instance of the Entity
     * @param tenantRef - the tenant to be set (if null, the current call's tenant ref wil be used)
     */
    @Override
    public void setTenantRef(final ENTITY e, final Long tenantRef) {
    }
}
