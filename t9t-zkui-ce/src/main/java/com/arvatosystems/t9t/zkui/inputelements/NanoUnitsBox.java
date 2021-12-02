package com.arvatosystems.t9t.zkui.inputelements;

import org.zkoss.zk.ui.WrongValueException;

import de.jpaw.fixedpoint.types.NanoUnits;

public class NanoUnitsBox extends Fixedpointbox<NanoUnits, NanoUnitsBox> {
    private static final long serialVersionUID = 437573760456243479L;

    public NanoUnitsBox() {
        super(s -> NanoUnits.valueOf(s));
    }

    public NanoUnitsBox(NanoUnits value) throws WrongValueException {
        this();
        setValue(value);
    }
}
