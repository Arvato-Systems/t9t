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
package com.arvatosystems.t9t.io.jpa.mapping.impl

import com.arvatosystems.t9t.annotations.jpa.AutoHandler
import com.arvatosystems.t9t.annotations.jpa.AutoMap42
import com.arvatosystems.t9t.io.CsvConfigurationDTO
import com.arvatosystems.t9t.io.jpa.entities.CsvConfigurationEntity
import com.arvatosystems.t9t.io.jpa.persistence.ICsvConfigurationEntityResolver

@AutoMap42
class CsvConfigurationMappers {
    ICsvConfigurationEntityResolver entityResolver

    @AutoHandler("S42")
    def void e2dCsvConfigurationDTO         (CsvConfigurationEntity entity, CsvConfigurationDTO dto) {}
}
