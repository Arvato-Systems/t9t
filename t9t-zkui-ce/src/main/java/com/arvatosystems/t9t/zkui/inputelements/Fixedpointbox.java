package com.arvatosystems.t9t.zkui.inputelements;

import java.util.function.Function;

import org.zkoss.math.BigDecimals;
import org.zkoss.zk.ui.ArithmeticWrongValueException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.sys.ObjectPropertyAccess;
import org.zkoss.zk.ui.sys.PropertyAccess;
import org.zkoss.zul.impl.NumberInputElement;
import org.zkoss.zul.mesg.MZul;

import com.arvatosystems.t9t.zkui.constraints.FixedPointConstraint;

import de.jpaw.fixedpoint.FixedPointBase;

/**
 * An edit box for fixed point numbers.
 */
public class Fixedpointbox<T extends FixedPointBase<T>, S extends Fixedpointbox<T, S>> extends NumberInputElement {
    private final Function<String, T> factory;
    private final ObjectPropertyAccess propertyAccess;

    protected Fixedpointbox(final Function<String, T> factory) {
        this.factory = factory;
        propertyAccess = new ObjectPropertyAccess() {
            public void setValue(Component cmp, Object value) {
                if (value instanceof FixedPointBase) {
                    ((S) cmp).setValue((T) value);
                } else if (value instanceof String || value == null) {
                    ((S) cmp).setValue((String) value);
                }
            }

            public T getValue(Component cmp) {
                return ((S) cmp).getValue();
            }
        };
        setCols(11);  // make it the same as a Decimalbox width, for which this one should be a drop-in replacement
    }

    /**
     * Returns the value, might be null unless a constraint stops it.
     * @exception WrongValueException if user entered a wrong value
     */
    public T getValue() throws WrongValueException {
        return (T) getTargetValue();
    }

    public void setValue(String str) {
        setValue(str == null ? null : factory.apply(str));
    }

    /**
     * Sets the value.
     * @exception WrongValueException if value is wrong
     */
    public void setValue(T value) throws WrongValueException {
        validate(value);
        setRawValue(value);
    }


    //-- super --//
    public String getZclass() {
        return _zclass == null ? "z-decimalbox" : _zclass; // use the same as DecimalBox (should be a drop in replacement)
    }

    protected Object marshall(Object value) {
        return value != null ? value.toString() : value;
    }
    protected Object unmarshall(Object value) {
        return value == null ? null : factory.apply((String)value);
    }

    protected Object coerceFromString(String value) throws WrongValueException {
        final Object[] vals = toNumberOnly(value);
        final String val = (String) vals[0];
        if (val == null || val.length() == 0)
            return null;

        try {
            return factory.apply(val);
        } catch (NumberFormatException ex) {
            throw showCustomError(new WrongValueException(this, MZul.NUMBER_REQUIRED, value));
        }
    }

    protected String coerceToString(Object value) {
        try {
            return value != null && getFormat() == null ? value instanceof FixedPointBase
                ? BigDecimals.toLocaleString(((FixedPointBase) value).toBigDecimal(), getDefaultLocale()) : value.toString()
                : formatNumber(value, null);
        } catch (ArithmeticException ex) {
            throw new ArithmeticWrongValueException(this, ex.getMessage(), ex, value);
        }
    }

    public PropertyAccess getPropertyAccess(String prop) {
        if ("value".equals(prop)) {
            return propertyAccess;
        }
        return super.getPropertyAccess(prop);
    }

    @Override
    public void setConstraint(String constraints) {
        if (constraints != null) {
            boolean noEmpty = false;
            boolean noNegative = false;
            if (constraints.contains("no empty"))
                noEmpty = true;

            if (constraints.contains("no negative"))
                noNegative = true;

            setConstraint(new FixedPointConstraint(noEmpty, noNegative));
        }
    }
}
