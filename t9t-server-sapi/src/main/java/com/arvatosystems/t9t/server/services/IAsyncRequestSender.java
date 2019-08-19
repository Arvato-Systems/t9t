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
package com.arvatosystems.t9t.server.services;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.types.XTargetChannelType;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;

public interface IAsyncRequestSender {

    /** Sends data to the specified target.
     * Required authorization for the target.
     *  Uses the IEvent interface to perform the actual sending.
     *  Different methods for various encodings are provided.
     *  Leave serializationFormat null to use the default (which is configuration dependent). */
    void asyncRequest(XTargetChannelType channel, String address, BonaPortable rq, MediaXType serializationFormat);

    /** Sends data to the specified target, with the JWT from the current topic appended. (RequestContext required)
     * Required authorization for the target.
     *  Uses the IEvent interface to perform the actual sending.
     *  Different methods for various encodings are provided.
     *  Leave serializationFormat null to use the default (which is configuration dependent). */
    void asyncServiceRequest(XTargetChannelType channel, String address, RequestParameters rq, MediaXType serializationFormat);
}
