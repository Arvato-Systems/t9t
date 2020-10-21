package com.arvatosystems.t9t.tfi.web.security;

import org.apache.shiro.authc.AuthenticationToken;

public class JwtAuthenticationToken implements AuthenticationToken {

    /**
     *
     */
    private static final long serialVersionUID = -844469816581043188L;
    private Object userId;
    private String token;

    public JwtAuthenticationToken(Object userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return getUserId();
    }

    @Override
    public Object getCredentials() {
        return getToken();
    }

    public Object getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
