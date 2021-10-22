package com.arvatosystems.t9t.component.datafields;

import java.math.BigDecimal;

import org.zkoss.zul.Decimalbox;

import de.jpaw.bonaparte.pojos.meta.BasicNumericElementaryDataItem;
import de.jpaw.fixedpoint.types.MicroUnits;

public class MicroUnitsDataField extends  AbstractDataField<Decimalbox, MicroUnits> {
    protected final Decimalbox c = new Decimalbox();
    protected int decimals;

    @Override
    public boolean empty() {
        return c.getValue() == null;
    }

    public MicroUnitsDataField(DataFieldParameters params) {
        super(params);
        BasicNumericElementaryDataItem cfg2 = (BasicNumericElementaryDataItem)params.cfg;
        setConstraints(c, cfg2.getIsSigned() ? null : "no negative");
        c.setMaxlength(cfg2.getTotalDigits() + 2); // 2 chars for sign and decimal point
        setDecimals(params.decimals);
    }

    @Override
    public void clear() {
        c.setValue("");
    }

    @Override
    public Decimalbox getComponent() {
        return c;
    }

    @Override
    public MicroUnits getValue() {
        BigDecimal num = c.getValue();
        if (num == null)
            return null;
        return MicroUnits.of(num);
    }

    @Override
    public void setValue(MicroUnits data) {
        c.setValue(data.toString());
    }

    public void setDecimals(String decimalInfo) {
        if (decimalInfo == null)
            decimals = ((BasicNumericElementaryDataItem)cfg).getDecimalDigits();
        else if (decimalInfo.length() == 1 && Character.isDigit(decimalInfo.charAt(0)))
            decimals = Integer.valueOf(decimalInfo);

        c.setScale(decimals);
    }
}
