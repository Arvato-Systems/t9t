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
package com.arvatosystems.t9t.zkui.azure.ad;

import java.io.Serializable;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.nimbusds.jwt.SignedJWT;

public class IdentityContextData implements Serializable {
    private static final long serialVersionUID = 2L;
    private String nonce = null;
    private String state = null;
    private LocalDateTime stateDate = null;
    private boolean authenticated = false;
    private String username = null;
    private String accessToken = null;
    private String idToken = null;
    private IAccount account = null;
    private Map<String, Object> idTokenClaims = new HashMap<>();
    private Map<String, Object> accessTokenClaims = new HashMap<>();

    public IAccount getAccount() {
        return account;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Map<String, Object> getIdTokenClaims() {
        return idTokenClaims;
    }

    public Map<String, Object> getAccessTokenClaims() {
        return accessTokenClaims;
    }

    public String getUsername() {
        return username;
    }

    public boolean getAuthenticated() {
        return authenticated;
    }

    public String getNonce() {
        return this.nonce;
    }

    public String getState() {
        return this.state;
    }

    public LocalDateTime getStateDate() {
        return this.stateDate;
    }

    public void setNonce(final String nonce) {
        this.nonce = nonce;
    }

    public void setState(final String state) {
        this.state = state;
        this.stateDate = LocalDateTime.now();
    }

    public void populateIdTokenAndClaims(final String token) throws ParseException {
        this.idToken = token;
        this.idTokenClaims = getTokenClaims(this.idToken);
    }

    public void setAuthResult(final IAuthenticationResult authResult) throws ParseException {
        this.account = authResult.account();
        this.accessToken = authResult.accessToken();
        this.accessTokenClaims = getTokenClaims(this.accessToken);
        if (this.idToken == null) {
            populateIdTokenAndClaims(authResult.idToken());
        }
        this.username = fethUsername(accessTokenClaims);
        this.authenticated = true;
    }

    private Map<String, Object> getTokenClaims(final String rawToken) throws ParseException {
        return SignedJWT.parse(rawToken).getJWTClaimsSet().getClaims();
    }

    private String fethUsername(final Map<String, Object> tokenClaims) {
        String value = null;
        value = (String) tokenClaims.get("unique_name");
        if (value != null) {
            return value;
        }
        value = (String) tokenClaims.get("upn");
        if (value != null) {
            return value;
        }
        return value;
    }
}
