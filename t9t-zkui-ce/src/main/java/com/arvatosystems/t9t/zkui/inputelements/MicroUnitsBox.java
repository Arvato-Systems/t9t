package com.arvatosystems.t9t.zkui.inputelements;

import org.zkoss.zk.ui.WrongValueException;

import de.jpaw.fixedpoint.types.MicroUnits;

public class MicroUnitsBox extends Fixedpointbox<MicroUnits, MicroUnitsBox> {
    private static final long serialVersionUID = 437573760456243476L;

    public MicroUnitsBox() {
        super(s -> MicroUnits.valueOf(s));
    }

    public MicroUnitsBox(MicroUnits value) throws WrongValueException {
        this();
        setValue(value);
    }
}
