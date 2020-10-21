package com.arvatosystems.t9t.io;

import com.arvatosystems.t9t.base.T9tException;

public class IOTools {
    private IOTools() {}

    public static String getCsvConfigurationId(CsvConfigurationRef ref) {
        if (ref == null)
            return null;
        if (ref instanceof CsvConfigurationKey) {
            return ((CsvConfigurationKey)ref).getCsvConfigurationId();
        }
        if (ref instanceof CsvConfigurationDTO) {
            return ((CsvConfigurationDTO)ref).getCsvConfigurationId();
        }
        throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "CsvConfigurationRef of type " + ref.getClass().getCanonicalName());
    }

    public static String getDataSinkId(DataSinkRef ref) {
        if (ref == null)
            return null;
        if (ref instanceof DataSinkKey) {
            return ((DataSinkKey)ref).getDataSinkId();
        }
        if (ref instanceof DataSinkDTO) {
            return ((DataSinkDTO)ref).getDataSinkId();
        }
        throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "DataSinkRef of type " + ref.getClass().getCanonicalName());
    }

    public static void mergePreset(DataSinkDTO dataSink, DataSinkPresets preset) {
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
