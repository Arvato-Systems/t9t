package com.arvatosystems.t9t.ai.tools.impl;

import java.time.LocalDate;
import java.time.format.TextStyle;

import com.arvatosystems.t9t.ai.service.IAITool;
import com.arvatosystems.t9t.ai.tools.AIToolCurrentDate;
import com.arvatosystems.t9t.ai.tools.AIToolCurrentDateResult;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named(AIToolCurrentDate.my$PQON)
@Singleton
public class AIToolToday implements IAITool<AIToolCurrentDate, AIToolCurrentDateResult> {

    @Override
    public AIToolCurrentDateResult performToolCall(final RequestContext ctx, final AIToolCurrentDate request) {
        final AIToolCurrentDateResult result = new AIToolCurrentDateResult();
        final LocalDate today = LocalDate.now();
        result.setToday(today);
        result.setNameOfDayOfTheWeek(today.getDayOfWeek().getDisplayName(TextStyle.FULL, ctx.getLocale()));
        return result;
    }
}
