package com.arvatosystems.t9t.itemConverter;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.fixedpoint.types.MicroUnits;

@Singleton
@Named("de.jpaw.fixedpoint.types.MicroUnits")
public class MicroUnitsConverter implements IItemConverter<MicroUnits> {
    @Override
    public boolean isRightAligned() {
        return true;
    }

    @Override
    public String getFormattedLabel(MicroUnits value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
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
    public Object getConvertedValue(MicroUnits value, BonaPortable wholeDataObject, String fieldName, FieldDefinition meta) {
        return value;
    }
}
