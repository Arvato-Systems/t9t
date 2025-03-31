/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.viewmodel;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import jakarta.annotation.Nonnull;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;

public abstract class AbstractGenericObjectVM<GENERICOBJ extends BonaPortable, DTO extends BonaPortable, TRACKING extends TrackingBase,
    PARENT extends AbstractViewOnlyVM<DTO, TRACKING>> extends AbstractViewOnlyVM<DTO, TRACKING> {

    protected GENERICOBJ genericObject;

    @Init(superclass = true)
    public void init(@ExecutionArgParam("parentVm") final PARENT parentVm) {
        parentVm.setChildViewModel(this);
    }

    @Override
    protected void loadData(@Nonnull final DataWithTracking<DTO, TRACKING> dwt) {
        genericObject = getGenericObject(dwt);
        BindUtils.postNotifyChange(this, "genericObject");
    }

    @Override
    protected void enrichData(@Nonnull final DTO data) {
        populateGenericObject(data);
    }

    public GENERICOBJ getGenericObject() {
        return genericObject;
    }

    protected abstract GENERICOBJ getGenericObject(@Nonnull DataWithTracking<DTO, TRACKING> dwt);

    protected abstract void populateGenericObject(@Nonnull DTO data);
}
