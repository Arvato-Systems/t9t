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
package com.arvatosystems.t9t.xml.imports;

import com.arvatosystems.t9t.auth.RoleKey;
import com.arvatosystems.t9t.auth.RoleRef;
import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.UserKey;
import com.arvatosystems.t9t.auth.UserRef;
import com.arvatosystems.t9t.auth.request.UserCrudRequest;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.in.be.impl.AbstractInputDataTransformer;
import com.arvatosystems.t9t.io.DataSinkPresets;
import com.arvatosystems.t9t.xml.User001;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;

@Named("xmlUser001Import")
@Dependent
public class UserImportDataTransformer extends AbstractInputDataTransformer<User001> {
    @Override
    public RequestParameters transform(User001 xml) {
        UserCrudRequest crud = new UserCrudRequest();
        crud.setCrud(OperationType.MERGE);
        crud.setNaturalKey(new UserKey(xml.getUserId()));
        crud.setData(transformSub(xml));
        return crud;
    }

    @Override
    public DataSinkPresets getDefaultConfiguration(boolean isInput) {
        DataSinkPresets dataSinkPresets = new DataSinkPresets();
        dataSinkPresets.setBaseClassPqon("t9t.com.arvatosystems.t9t.xml.User001");
        dataSinkPresets.setJaxbContextPath("com.arvatosystems.t9t.com.arvatosystems.t9t.xml");
        dataSinkPresets.setXmlDefaultNamespace("http://arvatosystems.com/schema/t9t_config.xsd");
        dataSinkPresets.setXmlNamespacePrefix("t9t_xml");
        dataSinkPresets.setXmlRootElementName("t9t_xml:UserMaster");
        dataSinkPresets.setXmlRecordName("records");
        dataSinkPresets.setXmlHeaderElements(null);
        dataSinkPresets.setXmlFooterElements(null);
        dataSinkPresets.setWriteTenantId(true);
        return dataSinkPresets;
    }

    protected UserDTO transformSub(User001 xml) {
        UserDTO dto = new UserDTO();

        RoleRef roleRef = null;
        if (xml.getRoleId() !=  null) {
            roleRef = new RoleKey(xml.getRoleId());
        }

        UserRef supervisorRef = null;
        if (xml.getSupervisorId() !=  null) {
            supervisorRef = new UserKey(xml.getSupervisorId());
        }

        dto.setUserId(xml.getUserId());
        dto.setName(xml.getName());
        dto.setEmailAddress(xml.getEmailAddress());
        dto.setIsActive(xml.getIsActive() != null ? xml.getIsActive() : Boolean.TRUE);
        dto.setRoleRef(roleRef);
        dto.setIsTechnical((xml.getIsTechnical()));
        dto.setOffice(xml.getOffice());
        dto.setDepartment(xml.getDepartment());
        dto.setJobTitle(xml.getJobTitle());
        dto.setPhoneNo(xml.getPhoneNo());
        dto.setMobilePhoneNo(xml.getMobilePhoneNo());
        dto.setExternalAuth(xml.getExternalAuth());
        dto.setSupervisorRef(supervisorRef);
        dto.setZ(xml.getZ());
        return dto;
    }
}
