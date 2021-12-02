package com.arvatosystems.t9t.zkui.inputelements;

import org.zkoss.zk.ui.WrongValueException;

import de.jpaw.fixedpoint.types.MilliUnits;

public class MilliUnitsBox extends Fixedpointbox<MilliUnits, MilliUnitsBox> {
    private static final long serialVersionUID = 437573760456243473L;

    public MilliUnitsBox() {
        super(s -> MilliUnits.valueOf(s));
    }

    public MilliUnitsBox(MilliUnits value) throws WrongValueException {
        this();
        setValue(value);
    }
}
