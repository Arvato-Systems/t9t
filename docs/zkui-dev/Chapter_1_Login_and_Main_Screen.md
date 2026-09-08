# 1       Login and Main Screen Requirements

This chapter reverse-engineers the existing ZK UI behavior for the application shell only:

* login
* tenant and additional selections directly after login
* the main shell / home screen
* logout
* language changes
* password change and expired-password handling
* forgotten-password handling
* invoking screens from the shell

Business screens themselves are out of scope.

## 1.1      Entry points and routing

The shell has these relevant entry points:

* `/login.zul` for interactive username / password login
* `/screens/login/forgotPassword.zul` for password-reset initiation
* `/screens/login/tenantSelection.zul` for tenant selection after authentication
* `/screens/login/selections.zul` for optional chained post-login selections
* `/screens/login/expired_credentials.zul` for mandatory password change when the password is expired
* `/home.zul` for the authenticated application shell
* `/redirect.zul` for token-based entry with optional target screen and language

Unauthenticated access to protected pages is redirected to `/login.zul`. Authenticated users with expired passwords are redirected to `/screens/login/expired_credentials.zul`.  
Sources: `/home/runner/work/t9t/t9t/t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/init/WorkbenchInit.java`, `/home/runner/work/t9t/t9t/t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/util/Constants.java`

## 1.2      Login page requirements

The login page shall provide:

* company branding / logo
* user ID input (`maxlength=16`)
* password input (`maxlength=64`)
* password visibility toggle
* language combobox populated from translated configuration
* remember-me checkbox
* primary login button
* optional Microsoft login button
* forgotten-password link
* inline error message for failed login

Behavioral requirements:

* The login buttons remain disabled until the browser reports the real client time zone.
* The selected language updates the preferred locale in the ZK session immediately, without reloading the page.
* Login stores the selected language in `LANGUAGE_COOKIE`.
* Remember-me stores or clears `USERNAME_COOKIE`.
* When Microsoft login is enabled, a dedicated button starts the Azure AD authorization redirect.
* If `/login.zul` is opened while already authenticated, the user is redirected to `/home.zul` when a tenant is already selected, otherwise to tenant selection.

Sources: `/home/runner/work/t9t/t9t/t9t-zkui-screens/src/main/webapp/login.zul`, `/home/runner/work/t9t/t9t/t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/viewmodel/support/LoginViewModel.java`, `/home/runner/work/t9t/t9t/t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/services/impl/AuthenticationService.java`

## 1.3      Post-login flow requirements

After successful credential validation, the existing UI redirects to `/screens/login/expired_credentials.zul`.

From there:

* If the password is still valid, the page immediately redirects to tenant selection.
* If the password is expired, the user must change the password before continuing.

After tenant selection:

* If exactly one tenant is allowed, it is auto-selected and the user continues automatically.
* If multiple tenants are allowed, the user selects one explicitly.
* The chosen tenant is stored in `TENANT_COOKIE`.
* Switching tenant also refreshes permissions for the session.
* If `login.additional.selections.qualifier` is configured, the user is routed through `/screens/login/selections.zul`; otherwise the user enters `/home.zul`.

Additional selection requirements:

* A resolver identified by the `qualifier` query parameter controls the selection page.
* The resolver may provide either a database-backed dropdown or a static list.
* If a resolver returns no selectable values, a configured `login.additional.selections.<qualifier>.defaultIfEmpty` value is used automatically.
* Resolvers may chain to another qualifier; the final step routes to `/home.zul`.

Sources: `/home/runner/work/t9t/t9t/t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/viewmodel/support/TenantSelectionViewModel.java`, `/home/runner/work/t9t/t9t/t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/viewmodel/support/SelectionsViewModel.java`, `/home/runner/work/t9t/t9t/t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/services/ISelectionsResolver.java`

## 1.4      Main shell requirements

The authenticated shell shall contain:

* current page title area
* optional jump-back button
* optional global search box
* environment indicator text with optional CSS class
* top-level navigation menu
* current tenant switch action
* language switch action
* change-password action
* logout action
* central content area where screens are loaded
* initial welcome / status panel

Behavioral requirements:

* The environment indicator is populated from backend configuration keys `CFG_FILE_KEY_ENVIRONMENT_TEXT` and `CFG_FILE_KEY_ENVIRONMENT_CSS`.
* The search box can be disabled by `header.searchBox.disable`.
* The jump-back button can be disabled by `header.jumpBackButton.disable`.
* The welcome panel shows the user name, last successful login, failed-login count, and a password-expiry warning when fewer than 10 days remain.
* Changing the language in the shell stores `LANGUAGE_COOKIE`, updates the preferred locale, calls the backend language switch, refreshes permissions, and reloads the current page.
* Tenant switching is exposed from the header and opens tenant selection as a modal dialog when more than one tenant is available.

Sources: `/home/runner/work/t9t/t9t/t9t-zkui-screens/src/main/webapp/home.zul`, `/home/runner/work/t9t/t9t/t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/viewmodel/support/ApplicationViewModel.java`, `/home/runner/work/t9t/t9t/t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/util/T9tConfigConstants.java`

## 1.5      Navigation and screen invocation requirements

Menu requirements:

* The top-level menu is built from `t9t-zkui-configuration.properties`.
* Only configured and visible entries are shown.
* Entries may be grouped into nested folders by category.
* Clicking a menu entry loads its ZUL screen into the central panel.

Default-screen requirements:

* On entering `/home.zul` without a `link` parameter, the shell requests the user default screen from the backend.
* Each visible menu item exposes a context menu action to set itself as the user default screen.
* The context menu also supports resetting the user default screen.

Direct screen invocation requirements:

* `/home.zul?link=<zul path>` opens the matching configured screen directly after shell startup.
* `/redirect.zul` accepts `token`, `tenantId`, optional `lang`, and optional `link`; it authenticates the session, switches tenant and language, refreshes permissions, and forwards to `/home.zul`, optionally with `?link=...`.
* Internal jumps use `ApplicationUtil.navJumpToScreen(...)` or `JumpTool.jump(...)`.
* Jumps to screens that are not visible main-menu entries are opened without caching.
* Jump-based navigation can carry preset filters and an optional back target.
* The jump-back button returns to the supplied back target once and then hides again.

The current default title-bar search implementation routes to `screens/user_admin/user28.zul` and applies a `name LIKE` filter.  
Sources: `/home/runner/work/t9t/t9t/t9t-zkui-screens/src/main/webapp/WEB-INF/resources/t9t-zkui-configuration.properties`, `/home/runner/work/t9t/t9t/t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/services/impl/DefaultNavBarCreator.java`, `/home/runner/work/t9t/t9t/t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/viewmodel/support/ApplicationViewModel.java`, `/home/runner/work/t9t/t9t/t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/util/ApplicationUtil.java`, `/home/runner/work/t9t/t9t/t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/util/JumpTool.java`, `/home/runner/work/t9t/t9t/t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/viewmodel/support/RedirectViewModel.java`, `/home/runner/work/t9t/t9t/t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/services/impl/TitleBarSearch.java`

## 1.6      Password change requirements

Two password-change variants exist:

* mandatory change on `/screens/login/expired_credentials.zul`
* voluntary change from the shell via `screens/session/change_pwd.zul`

Common requirements:

* collect old password, new password, and retyped password
* support password visibility toggles
* block submission when new password and retyped password differ
* retrieve password requirements from the backend at page initialization
* render only the active requirement rules returned by the backend
* validate the new password against those rules before submitting
* execute the backend password-change operation
* refresh permissions afterwards

Outcome requirements:

* Successful voluntary change shows a success message and clears the form.
* If backend password synchronization fails with the dedicated warning code, the UI shows the warning but still clears the expiry state locally.
* Successful mandatory change continues to tenant selection instead of returning to the shell.
* The mandatory password-change page also exposes logout.

Sources: `/home/runner/work/t9t/t9t/t9t-zkui-screens/src/main/webapp/screens/session/change_pwd.zul`, `/home/runner/work/t9t/t9t/t9t-zkui-screens/src/main/webapp/screens/login/expired_credentials.zul`, `/home/runner/work/t9t/t9t/t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/viewmodel/support/ChangePwdViewModel28.java`, `/home/runner/work/t9t/t9t/t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/viewmodel/support/ExpiredCredentialsViewModel28.java`

## 1.7      Forgotten-password requirements

The forgotten-password page shall:

* be reachable without an authenticated user session
* collect user ID and email address
* submit a reset-password request through a dedicated API-key based backend flow
* show a success message and then return to `/login.zul`

Configuration requirement:

* `forget.password.api.key` must be present and must be a valid UUID; otherwise the flow fails as unauthorized / misconfigured.

Session-handling requirement:

* Any temporary API-key-backed JWT created for the reset flow is logged out again and removed from the local session afterwards.

Sources: `/home/runner/work/t9t/t9t/t9t-zkui-screens/src/main/webapp/screens/login/forgotPassword.zul`, `/home/runner/work/t9t/t9t/t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/viewmodel/support/ForgotPasswordViewModel28.java`

## 1.8      Logout and session-expiry requirements

Logout requirements:

* logout is available from the shell header, tenant-selection page, additional-selection page, and expired-password page
* when authenticated, logout informs the backend to close the server session, then invalidates the local session
* standard logout returns to `/login.zul`
* Microsoft-authenticated sessions additionally redirect through the Azure AD sign-out endpoint before returning to `/login.zul`

Session-expiry requirements:

* shortly before session expiry, the shell shows a modal warning
* the warning offers `Continue` and `Exit`
* `Continue` keeps the session alive
* `Exit` logs the user out
* if the session expires, the shell triggers logout

Sources: `/home/runner/work/t9t/t9t/t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/services/impl/AuthenticationService.java`, `/home/runner/work/t9t/t9t/t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/viewmodel/support/ApplicationViewModel.java`, `/home/runner/work/t9t/t9t/t9t-zkui-ce/src/main/java/com/arvatosystems/t9t/zkui/viewmodel/support/LogoutViewModel.java`
