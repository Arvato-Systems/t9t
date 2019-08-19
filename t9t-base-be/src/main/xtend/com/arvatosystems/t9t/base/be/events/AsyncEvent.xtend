/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.be.events

import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.services.IEventImpl
import com.arvatosystems.t9t.base.types.XTargetChannelType
import com.arvatosystems.t9t.server.services.IEvent
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.media.MediaTypeInfo
import de.jpaw.bonaparte.pojos.api.media.MediaData
import de.jpaw.dp.Jdp
import de.jpaw.dp.Named
import de.jpaw.dp.Singleton
import de.jpaw.dp.Startup
import de.jpaw.dp.StartupOnly
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor

@Startup(17000)
@Singleton
class AsyncEvent implements IEvent, StartupOnly {
    final ConcurrentMap<String, IEventImpl> implementations = new ConcurrentHashMap<String, IEventImpl>(32)

    override void onStartup() {
        implementations.putAll(Jdp.getInstanceMapPerQualifier(IEventImpl))
    }

    override void asyncEvent(XTargetChannelType channel, String address, MediaData data) {
        // determine the target
        val impl = implementations.get(channel.token)
        if (impl === null)
            throw new T9tException(T9tException.INVALID_FILTER_PARAMETERS, "no target of name " + channel.token + " implemented")
        if (data !== null) {
            if (data.mediaType === null)
                throw new T9tException(T9tException.INVALID_FILTER_PARAMETERS, "no media type specified")
            // data plausi check related to binary vs. String
            val description = MediaTypeInfo.getFormatByType(data.mediaType)
            if (description === null)
                throw new T9tException(T9tException.INVALID_FILTER_PARAMETERS, "no media type description available")
            if (description.isText) {
                if (data.rawData !== null || data.text === null)
                    throw new T9tException(T9tException.INVALID_FILTER_PARAMETERS, "text and only text field may be set for media type {}", data.mediaType.token)
            } else {
                if (data.rawData === null || data.text !== null)
                    throw new T9tException(T9tException.INVALID_FILTER_PARAMETERS, "raw data and only raw data may be set for media type {}", data.mediaType.token)
            }
            // check permission to write this. TODO: Inject IAuthorize and query create permission for C.channel.address
            impl.asyncEvent(address, data, description)
        }
    }
}

@Singleton
@Named("null")
@AddLogger
class NullEvent implements IEventImpl {

    override asyncEvent(String address, MediaData data, MediaTypeDescriptor description) {
        LOGGER.debug("NULL event target {} for media type {}", address, data.mediaType.token)
    }
}


@Singleton
@Named("log")
@AddLogger
class LogEvent implements IEventImpl {

    override asyncEvent(String address, MediaData data, MediaTypeDescriptor description) {
        LOGGER.info("Log event target {} for media type {}", address, data.mediaType.token)
    }
}
