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
package com.arvatosystems.t9t.base.services;

import com.arvatosystems.t9t.base.api.RequestParameters;

public interface IMessaging {
    /** Publish a message to some event bus, for any subscriber to receive it. */
    void publishMessage(String topic, Object data);

    /** Send a message to a queue, for a specific receiver to consume it (excatly one receiver). */
    void sendMessage(String queue, Object data);

    /** Execute a command remotely. Uses a mapped userId / tenantId. */
    void executeRemote(String queue, RequestParameters rq);
}
