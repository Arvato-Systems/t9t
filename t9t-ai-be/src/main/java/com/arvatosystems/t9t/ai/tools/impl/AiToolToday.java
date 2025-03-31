/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
