package com.arvatosystems.t9t.ai.tools.impl;

import java.time.LocalDate;
import java.time.format.TextStyle;

import com.arvatosystems.t9t.ai.service.IAiTool;
import com.arvatosystems.t9t.ai.tools.AiToolCurrentDate;
import com.arvatosystems.t9t.ai.tools.AiToolCurrentDateResult;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named(AiToolCurrentDate.my$PQON)
@Singleton
public class AiToolToday implements IAiTool<AiToolCurrentDate, AiToolCurrentDateResult> {

    @Override
    public AiToolCurrentDateResult performToolCall(final RequestContext ctx, final AiToolCurrentDate request) {
        final AiToolCurrentDateResult result = new AiToolCurrentDateResult();
        final LocalDate today = LocalDate.now();
        result.setToday(today);
        result.setNameOfDayOfTheWeek(today.getDayOfWeek().getDisplayName(TextStyle.FULL, ctx.getLocale()));
        return result;
    }
}
