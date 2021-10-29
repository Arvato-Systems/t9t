package com.arvatosystems.t9t.converters;

import de.jpaw.fixedpoint.types.MicroUnits;
import java.math.BigDecimal;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zul.Decimalbox;

/**
 * This is ZK Data binding converter that is ONLY needed by custom decimal field that required on a custom page.
 * Regular microunit fields that render by field28, cell28 will never use it.
 */
public class MicroUnitsConverter implements Converter<BigDecimal, MicroUnits, Decimalbox> {

    @Override
    public BigDecimal coerceToUi(MicroUnits microUnits, Decimalbox component, BindContext ctx) {
        return microUnits == null ? null : microUnits.toBigDecimal();
    }

    @Override
    public MicroUnits coerceToBean(BigDecimal value, Decimalbox component, BindContext ctx) {
        return value == null ? null :  MicroUnits.of(value);
    }

}
