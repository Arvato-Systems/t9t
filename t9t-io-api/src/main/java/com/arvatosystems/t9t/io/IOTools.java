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
package com.arvatosystems.t9t.io;

import com.arvatosystems.t9t.base.T9tException;

public final class IOTools {
    private IOTools() { }

    public static String getCsvConfigurationId(final CsvConfigurationRef ref) {
        if (ref == null)
            return null;
        if (ref instanceof CsvConfigurationKey key) {
            return key.getCsvConfigurationId();
        }
        if (ref instanceof CsvConfigurationDTO dto) {
            return dto.getCsvConfigurationId();
        }
        throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "CsvConfigurationRef of type " + ref.getClass().getCanonicalName());
    }

    public static String getDataSinkId(final DataSinkRef ref) {
        if (ref == null)
            return null;
        if (ref instanceof DataSinkKey key) {
            return key.getDataSinkId();
        }
        if (ref instanceof DataSinkDTO dto) {
            return dto.getDataSinkId();
        }
        throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "DataSinkRef of type " + ref.getClass().getCanonicalName());
    }

    public static void mergePreset(final DataSinkDTO dataSink, final DataSinkPresets preset) {
        if (preset != null) {
            dataSink.setBaseClassPqon(preset.getBaseClassPqon());
            dataSink.setJaxbContextPath(preset.getJaxbContextPath());
            dataSink.setXmlDefaultNamespace(preset.getXmlDefaultNamespace());
            dataSink.setXmlRootElementName(preset.getXmlRootElementName());
            dataSink.setXmlRecordName(preset.getXmlRecordName());
            dataSink.setXmlNamespacePrefix(preset.getXmlNamespacePrefix());
            dataSink.setXmlHeaderElements(preset.getXmlHeaderElements());
            dataSink.setXmlFooterElements(preset.getXmlFooterElements());
            dataSink.setWriteTenantId(preset.getWriteTenantId());
        }
    }
}
