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
package com.arvatosystems.t9t.xml.exports;

import com.arvatosystems.t9t.auth.RoleDTO;
import com.arvatosystems.t9t.auth.RoleKey;
import com.arvatosystems.t9t.auth.RoleRef;
import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.UserKey;
import com.arvatosystems.t9t.auth.UserRef;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.DataSinkPresets;
import com.arvatosystems.t9t.out.services.IPreOutputDataTransformer;
import com.arvatosystems.t9t.xml.User001;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;

import java.util.Collections;
import java.util.List;

@Named("xmlUserExport")
@Dependent
public class UserExportDataTransformer implements IPreOutputDataTransformer {
    @Override
    public List<BonaPortable> transformData(BonaPortable record, DataSinkDTO sinkCfg, OutputSessionParameters outputSessionParameters) {
        UserDTO internal = ((UserDTO) record);
        User001 external = new User001();
        String name = internal.getName() != null ? internal.getName() : "?";

        external.setUserId(internal.getUserId());
        external.setName(name);
        external.setEmailAddress(internal.getEmailAddress());
        external.setIsActive(Boolean.valueOf(internal.getIsActive()));
        external.setRoleId(getRoleId(internal.getRoleRef()));
        external.setIsTechnical(Boolean.valueOf(internal.getIsTechnical()));
        external.setOffice(internal.getOffice());
        external.setDepartment(internal.getDepartment());
        external.setJobTitle(internal.getJobTitle());
        external.setPhoneNo(internal.getPhoneNo());
        external.setMobilePhoneNo(internal.getMobilePhoneNo());
        external.setExternalAuth(internal.getExternalAuth());
        external.setSupervisorId(getUserId(internal.getSupervisorRef()));
        external.setZ(internal.getZ());

        return Collections.singletonList(external);
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

    protected String getRoleId(RoleRef ref) {
        if (ref == null) {
            return null;
        }
        if (ref instanceof RoleKey) {
            return ((RoleKey) ref).getRoleId();
        }
        if (ref instanceof RoleDTO) {
            return ((RoleDTO) ref).getRoleId();
        }
        return null;
    }

    protected String getUserId(UserRef ref) {
        if (ref == null) {
            return null;
        }
        if (ref instanceof UserKey) {
            return ((UserKey) ref).getUserId();
        }
        if (ref instanceof UserDTO) {
            return ((UserDTO) ref).getUserId();
        }
        return null;
    }
}
