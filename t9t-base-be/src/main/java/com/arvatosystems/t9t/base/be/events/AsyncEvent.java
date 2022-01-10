/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.IEventImpl;
import com.arvatosystems.t9t.base.types.XTargetChannelType;
import com.arvatosystems.t9t.server.services.IEvent;
import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupOnly;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Startup(17000)
@Singleton
public class AsyncEvent implements IEvent, StartupOnly {
    final ConcurrentMap<String, IEventImpl> implementations = new ConcurrentHashMap<String, IEventImpl>(32);

    @Override
    public void onStartup() {
        implementations.putAll(Jdp.getInstanceMapPerQualifier(IEventImpl.class));
    }

    @Override
    public void asyncEvent(final XTargetChannelType channel, final String address, final MediaData data) {
     // determine the target
        final IEventImpl impl = implementations.get(channel.getToken());
        if (impl == null) {
            throw new T9tException(T9tException.INVALID_FILTER_PARAMETERS, "no target of name " + channel.getToken() + " implemented");
        }

        if (data != null) {
            if (data.getMediaType() == null)
                throw new T9tException(T9tException.INVALID_FILTER_PARAMETERS, "no media type specified");
            // data plausi check related to binary vs. String
            final MediaTypeDescriptor description = MediaTypeInfo.getFormatByType(data.getMediaType());
            if (description == null) {
                throw new T9tException(T9tException.INVALID_FILTER_PARAMETERS, "no media type description available");
            }

            if (description.getIsText()) {
                if (data.getRawData() != null || data.getText() == null) {
                    throw new T9tException(T9tException.INVALID_FILTER_PARAMETERS, "text and only text field may be set for media type {}",
                            data.getMediaType().getToken());
                }
            } else {
                if (data.getRawData() == null || data.getText() != null)
                    throw new T9tException(T9tException.INVALID_FILTER_PARAMETERS, "raw data and only raw data may be set for media type {}",
                            data.getMediaType().getToken());
            }
            // check permission to write this. TODO: Inject IAuthorize and query create permission for C.channel.address
            impl.asyncEvent(address, data, description);
        }
    }
}
