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
package com.arvatosystems.t9t.zkui.viewmodel.framework;

import org.zkoss.bind.annotation.Init;

import com.arvatosystems.t9t.doc.DocConfigDTO;
import com.arvatosystems.t9t.doc.DocConfigRef;
import com.arvatosystems.t9t.doc.DocEmailReceiverDTO;
import com.arvatosystems.t9t.zkui.viewmodel.CrudSurrogateKeyVM;

import de.jpaw.bonaparte.pojos.api.TrackingBase;

/**
 * @author NGTZ001
 * @param <DATA>
 */
@Init(superclass = true)
public class DocConfigViewModel extends CrudSurrogateKeyVM<DocConfigRef, DocConfigDTO, TrackingBase> {

    @Override
    protected void clearData() {
        super.clearData();
        data.setEmailSettings(new DocEmailReceiverDTO());
    }

}
