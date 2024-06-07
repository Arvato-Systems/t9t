/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.ai.jpa.mapping.impl

import com.arvatosystems.t9t.ai.AiModuleCfgDTO
import com.arvatosystems.t9t.ai.jpa.entities.AiModuleCfgEntity
import com.arvatosystems.t9t.ai.jpa.persistence.IAiModuleCfgEntityResolver
import com.arvatosystems.t9t.annotations.jpa.active.AutoMap42

@AutoMap42
class AiModuleCfgEntityMapper {
    IAiModuleCfgEntityResolver entityResolver

    def void d2eAiModuleCfgDTO(AiModuleCfgEntity entity, AiModuleCfgDTO dto, boolean onlyActive) {}
    def void e2dAiModuleCfgDTO(AiModuleCfgEntity entity, AiModuleCfgDTO dto) {}

}
