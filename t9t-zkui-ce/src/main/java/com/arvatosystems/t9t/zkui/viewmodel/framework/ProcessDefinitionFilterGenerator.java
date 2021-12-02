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
package com.arvatosystems.t9t.zkui.viewmodel.framework;

import com.arvatosystems.t9t.bpmn2.ProcessDefinitionDTO;
import com.arvatosystems.t9t.zkui.components.IFilterGenerator;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.AsciiFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Named("processDefinition")
public class ProcessDefinitionFilterGenerator implements IFilterGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessDefinitionFilterGenerator.class);

    @Override
    public SearchFilter createFilter(BonaPortable data) {
        AsciiFilter f = new AsciiFilter();
        f.setFieldName("processDefinitionId");
        f.setEqualsValue(((ProcessDefinitionDTO)data).getProcessDefinitionId());
        return f;
    }
}
