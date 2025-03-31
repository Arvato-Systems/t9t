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
package com.arvatosystems.t9t.auth.services;

import java.time.Instant;

import com.arvatosystems.t9t.auth.ApiKeyDTO;
import com.arvatosystems.t9t.auth.TenantDTO;
import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.UserStatusDTO;

public final class AuthIntermediateResult {
    private int                   returnCode;        // 0 = success, anything else is an error
    private String                tenantId;
    private ApiKeyDTO             apiKey;
    private UserDTO               user;
    private TenantDTO             tenant;
    private UserStatusDTO         userStatus;
    private Instant               authExpires;

    // below is 100% boilerplate... Waiting for Java 17
    public int getReturnCode() {
        return returnCode;
    }
    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }
    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    public ApiKeyDTO getApiKey() {
        return apiKey;
    }
    public void setApiKey(ApiKeyDTO apiKey) {
        this.apiKey = apiKey;
    }
    public UserDTO getUser() {
        return user;
    }
    public void setUser(UserDTO user) {
        this.user = user;
    }
    public TenantDTO getTenant() {
        return tenant;
    }
    public void setTenant(TenantDTO tenant) {
        this.tenant = tenant;
    }
    public UserStatusDTO getUserStatus() {
        return userStatus;
    }
    public void setUserStatus(UserStatusDTO userStatus) {
        this.userStatus = userStatus;
    }
    public Instant getAuthExpires() {
        return authExpires;
    }
    public void setAuthExpires(Instant authExpires) {
        this.authExpires = authExpires;
    }
}
