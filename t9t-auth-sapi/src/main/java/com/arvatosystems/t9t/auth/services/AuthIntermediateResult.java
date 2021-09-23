package com.arvatosystems.t9t.auth.services;

import java.time.Instant;

import com.arvatosystems.t9t.auth.ApiKeyDTO;
import com.arvatosystems.t9t.auth.TenantDTO;
import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.UserStatusDTO;

public class AuthIntermediateResult {
    private int                   returnCode;        // 0 = success, anything else is an error
    private Long                  tenantRef;
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
    public Long getTenantRef() {
        return tenantRef;
    }
    public void setTenantRef(Long tenantRef) {
        this.tenantRef = tenantRef;
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
