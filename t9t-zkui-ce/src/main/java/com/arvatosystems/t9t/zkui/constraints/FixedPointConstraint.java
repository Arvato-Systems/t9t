package com.arvatosystems.t9t.zkui.constraints;

import java.io.Serializable;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.mesg.MZul;

import com.arvatosystems.t9t.zkui.inputelements.Fixedpointbox;

import de.jpaw.fixedpoint.types.MicroUnits;

public class FixedPointConstraint implements Constraint, Serializable {

    protected final boolean noEmpty;
    protected final boolean noNegative;

    public FixedPointConstraint(boolean noEmpty, boolean noNegative) {
        this.noEmpty = noEmpty;
        this.noNegative = noNegative;
    }

    private static final long serialVersionUID = -5277770978215367184L;

    @Override
    public void validate(Component comp, Object value) throws WrongValueException {
        if (comp instanceof Fixedpointbox) {
            MicroUnits microUnits = (MicroUnits) value;
            if (noEmpty && microUnits == null) {
                throw new WrongValueException(comp, MZul.EMPTY_NOT_ALLOWED);
            }
            if (microUnits != null && noNegative && microUnits.signum() == -1) {
                throw new WrongValueException(comp, MZul.NO_NEGATIVE);
            }
        } else {
            throw new UnsupportedOperationException(
                    "Component {} is not supported.".concat(comp.getClass().getSimpleName()));
        }
    }

}
