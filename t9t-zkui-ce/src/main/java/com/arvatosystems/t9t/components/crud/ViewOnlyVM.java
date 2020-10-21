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
package com.arvatosystems.t9t.components.crud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;

@Init(superclass=true)
public class ViewOnlyVM<DTO extends BonaPortable, TRACKING extends TrackingBase> extends AbstractViewOnlyVM<DTO, TRACKING> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ViewOnlyVM.class);

    @Command
    @NotifyChange("*")
    public void setSelectionData(@BindingParam("dwt") DataWithTracking<DTO, TRACKING> dwt) {
        if (dwt != null) {
            LOGGER.debug("setSelectionData(some data)");
            loadData(dwt);
        } else {
            LOGGER.debug("setSelectionData(null)");
            clearData();
        }
    }
}
