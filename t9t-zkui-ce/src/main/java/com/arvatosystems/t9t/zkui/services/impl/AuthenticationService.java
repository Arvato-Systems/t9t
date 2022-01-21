package com.arvatosystems.t9t.zkui.services.impl;

import com.arvatosystems.t9t.auth.T9tAuthException;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.zkui.exceptions.ReturnCodeException;
import com.arvatosystems.t9t.zkui.services.IAuthenticationService;
import com.arvatosystems.t9t.zkui.services.IUserDAO;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.Constants;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Executions;

@Singleton
public class AuthenticationService implements IAuthenticationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    protected final IUserDAO userDAO = Jdp.getRequired(IUserDAO.class);

    @Override
    public void login(final String username, final String password) throws T9tException {
        LOGGER.debug("Login user '{}'", username);
        try {
            AuthenticationResponse authResponse = userDAO.getAuthenticationResponse(username, password);
            loginSuccessRedirect();
        } catch (final ReturnCodeException e) {
            LOGGER.debug("Login user '{}' failed with message: {}", username, e.getMessage());
            LOGGER.warn("May Missing object data for AllowedTenants/UserHistory/UserInformation");
            throw new T9tException(T9tAuthException.LOGIN_FAILED);
        } catch (final Exception e) {
            LOGGER.debug("Login user '{}' failed with message: {}", username, e.getMessage());
            throw new T9tException(T9tAuthException.LOGIN_FAILED, e);
        }
    }

    @Override
    public void logout() {
        final ApplicationSession applicationSession = ApplicationSession.get();
        if (applicationSession.isAuthenticated()) {
            LOGGER.debug("Logout user '{}'", applicationSession.getJwtInfo().getUserId());
            applicationSession.invalidateSession();
        }
        logoutSuccessRedirect();
    }

    protected void loginSuccessRedirect() {
        LOGGER.debug("Login successful, redirect to login success page");
        Executions.sendRedirect(Constants.ZulFiles.LOGIN_SUCCESS_REDIRECT);
    }

    protected void logoutSuccessRedirect() {
        LOGGER.debug("Logout successful, redirect to logout success page");
        Executions.sendRedirect(Constants.ZulFiles.LOGIN);
    }
}
