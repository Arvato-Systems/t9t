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
import java.util.List;

import com.arvatosystems.t9t.base.jpa.IEntityMapper28;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.search.ReadAll28Response;
import com.arvatosystems.t9t.base.services.IOutputSession;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Jdp;

/** base implementation of the IEntityMapper28 interface, only suitable for simple configuration data tables */
public abstract class AbstractEntityMapper28<KEY extends Serializable, DTO extends BonaPortable, TRACKING extends TrackingBase, ENTITY extends BonaPersistableKey<KEY> & BonaPersistableTracking<TRACKING>>
  extends AbstractEntityMapper<KEY, DTO, TRACKING, ENTITY> implements IEntityMapper28<KEY, DTO, TRACKING, ENTITY> {
    private final List<DataWithTrackingS<DTO, TRACKING>> EMPTY_RESULT_LIST = new ArrayList<DataWithTrackingS<DTO, TRACKING>>(0);

    @Override
    public final DataWithTrackingS<DTO, TRACKING> mapToDwt(ENTITY entity) {
        if (entity == null) {
            return null;
        }
        DataWithTrackingS<DTO, TRACKING> entry = new DataWithTrackingS<DTO, TRACKING>();
        entry.setTracking(entity.ret$Tracking());
        // entry.setIsActive(entity.ret$Active());
        entry.setData(mapToDto(entity));
        entry.setTenantId(getTenantId(entity));  // either tenantId or tenantRef has been defined in the data (category D) or it is of no interest to the caller
        return entry;
    }

    @Override
    public final List<DataWithTrackingS<DTO, TRACKING>> mapListToDwt(Collection<ENTITY> entityList) {
        if (entityList == null) {
            return null;
        }
        List<DataWithTrackingS<DTO, TRACKING>> resultList = new ArrayList<>(entityList.size());
        for (ENTITY entity : entityList) {
            resultList.add(mapToDwt(entity));
        }
        return resultList;
    }

    @Override
    public final ReadAll28Response<DTO, TRACKING> createReadAllResponse(List<ENTITY> data, OutputSessionParameters op) throws Exception {
        ReadAll28Response<DTO, TRACKING> rs = new ReadAll28Response<DTO, TRACKING>();
        if (op == null) {
            // fill the result
            rs.setDataList(mapListToDwt(data));
        } else {
            // push output into an outputSession (export it)
            try (IOutputSession outputSession = Jdp.getRequired(IOutputSession.class)) {
                Long sinkRef = outputSession.open(op);
                if (outputSession.getUnwrapTracking(op.getUnwrapTracking())) {
                    op.setSmartMappingForDataWithTracking(Boolean.FALSE);
                    for (ENTITY entity : data) {
                        outputSession.store(mapToDto(entity));
                    }
                } else {
                    op.setSmartMappingForDataWithTracking(Boolean.TRUE);
                    DataWithTrackingS<DTO, TRACKING> entry = new DataWithTrackingS<DTO, TRACKING>();
                    for (ENTITY entity : data) {
                        entry.setTracking(entity.ret$Tracking());
                        entry.setData(mapToDto(entity));
                        entry.setTenantId(getTenantId(entity));  // either tenantId or tenantRef has been defined in the data (category D) or it is of no interest to the caller
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
    public String getTenantId(ENTITY e) {
        return null;
    }

    /** Sets the entity's tenantRef without the use of reflection, or NOOP if the entity does not contain
     * a tenantRef field.
     * @param e - an instance of the Entity
     * @param tenantRef - the tenant to be set (if null, the current call's tenant ref wil be used)
     */
    @Override
    public void setTenantId(ENTITY e, String tenantId) {
    }
}
