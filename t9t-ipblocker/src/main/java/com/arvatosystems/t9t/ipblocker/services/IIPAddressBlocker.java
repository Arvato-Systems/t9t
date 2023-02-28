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
package com.arvatosystems.t9t.ipblocker.services;

public interface IIPAddressBlocker {

    /**
     * Records a failed authentication event (increments the "bad attempt" counter for the given IP address).
     */
    void registerBadAuthFromIp(String remoteIp);

    /**
     * Checks if the request came from a blocked IP address.
     *
     * @return true if the IP address has been blocked, false if not, or no checking is active
     */
    boolean isIpAddressBlocked(String remoteIp);
}
