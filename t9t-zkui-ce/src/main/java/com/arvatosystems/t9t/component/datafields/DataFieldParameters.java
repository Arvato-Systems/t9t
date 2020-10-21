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
package com.arvatosystems.t9t.component.datafields;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.pojos.meta.FieldDefinition;

public class DataFieldParameters {
    public final FieldDefinition cfg;
    public final String path;
    public final Boolean overrideRequired;
    public final ApplicationSession as;
    public final String enumZulRestrictions;
    public final String decimals;  // only relevant for decimal fields. null means use precision of bon file. Alphanumeric data means treat it as a currency.

    public DataFieldParameters(FieldDefinition cfg, String path, Boolean overrideRequired, ApplicationSession as, String enumZulRestrictions, String decimals) {
        this.cfg = cfg;
        this.path = path;
        this.overrideRequired = overrideRequired;
        this.as = as;
        this.enumZulRestrictions = enumZulRestrictions;
        this.decimals = decimals;
    }
}
