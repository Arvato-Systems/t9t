package com.arvatosystems.t9t.tfi.component;

import de.jpaw.fixedpoint.types.MicroUnits;

public class MicroUnitsConverter implements Converter {

    @Override
    public String getFormattedLabel(Object value, Object wholeDataObject, String fieldName) {
        if (value == null) {
            return null;
        }

        if (value instanceof MicroUnits) {
            return value.toString();
        } else {
            throw new UnsupportedOperationException("Instance " + value.getClass().getName() + " is not supported. Field:" + fieldName + "->" + value);
        }
    }

    @Override
    public Object getConvertedValue(Object value, Object wholeDataObject, String fieldName) {
        return value;
    }

}
