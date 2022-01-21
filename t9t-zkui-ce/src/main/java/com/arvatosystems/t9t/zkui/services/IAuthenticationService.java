package com.arvatosystems.t9t.zkui.services;

import com.arvatosystems.t9t.base.T9tException;

public interface IAuthenticationService {
    void login(String username, String password) throws T9tException;
    void logout();
}
