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
package com.arvatosystems.t9t.base.be.events;

import com.arvatosystems.t9t.base.services.IEventImpl;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Named("log")
public class LogEvent implements IEventImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogEvent.class);

    @Override
    public void asyncEvent(String address, MediaData data, MediaTypeDescriptor description) {
        LOGGER.debug("Log event target {} for media type {}", address, data.getMediaType().getToken());
    }
}
