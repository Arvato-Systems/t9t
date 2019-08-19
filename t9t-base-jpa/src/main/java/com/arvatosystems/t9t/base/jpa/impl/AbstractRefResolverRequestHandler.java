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

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.crud.RefResolverRequest;
import com.arvatosystems.t9t.base.crud.RefResolverResponse;
import com.arvatosystems.t9t.base.jpa.IResolverSurrogateKey;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;

import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.Ref;

public abstract class AbstractRefResolverRequestHandler<REF extends Ref, TRACKING extends TrackingBase, ENTITY extends BonaPersistableKey<Long> & BonaPersistableTracking<TRACKING>, REQUEST extends RefResolverRequest<REF>>
        extends AbstractRequestHandler<REQUEST> {

    protected abstract IResolverSurrogateKey<REF, TRACKING, ENTITY> getResolver();

    @Override
    public ServiceResponse execute(REQUEST request) throws Exception {
        RefResolverResponse rs = new RefResolverResponse();
        rs.setKey(getResolver().getRef(request.getRef(), true));
        rs.setReturnCode(0);
        return rs;
    }

}
