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
package com.arvatosystems.t9t.rep.services;

import com.arvatosystems.t9t.rep.ReportConfigDTO;
import com.arvatosystems.t9t.rep.ReportConfigRef;
import com.arvatosystems.t9t.rep.ReportParamsDTO;
import com.arvatosystems.t9t.rep.ReportParamsRef;

/** Defines the methods used to interact between the BE and the JPA module. */
public interface IRepPersistenceAccess {

    /** Returns a report config DTO. Shortcut for the cross module resolver. */
    ReportConfigDTO getConfigDTO(ReportConfigRef configRef) throws Exception;

    /** Returns a report params DTO. Shortcut for the cross module resolver. */
    ReportParamsDTO getParamsDTO(ReportParamsRef configRef) throws Exception;

}
