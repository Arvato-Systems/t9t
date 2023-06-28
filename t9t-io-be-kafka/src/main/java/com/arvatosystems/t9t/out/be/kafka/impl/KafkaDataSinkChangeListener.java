/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.out.be.kafka.impl;

import java.util.function.BiConsumer;

import com.arvatosystems.t9t.annotations.IsLogicallyFinal;
import com.arvatosystems.t9t.base.event.EventParameters;
import com.arvatosystems.t9t.base.services.IEventHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.event.DataSinkChangedEvent;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("IOKafkaDataSinkChange")
public class KafkaDataSinkChangeListener implements IEventHandler {

    @IsLogicallyFinal
    private BiConsumer<RequestContext, DataSinkChangedEvent> processor;

    public void setProcessor(final BiConsumer<RequestContext, DataSinkChangedEvent> processor) {
        this.processor = processor;
    }

    @Override
    public int execute(final RequestContext ctx, final EventParameters untypedEvent) {
        final DataSinkChangedEvent event = (DataSinkChangedEvent) untypedEvent;
        if (processor != null) {
            processor.accept(ctx, event);
        }
        return 0;
    }
}
