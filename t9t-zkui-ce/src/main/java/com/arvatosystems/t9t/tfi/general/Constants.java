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
package com.arvatosystems.t9t.tfi.general;

public class Constants {
    private Constants() {
    }

    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    public class ZulFiles {
        private ZulFiles() {
        }
        public static final String LOGOUT                 = "/logout";
        public static final String HOME                   = "/home.zul";
        public static final String LOGIN                  = "/login.zul";
        public static final String LOGIN_TENANT_SELECTION = "/screens/login/tenantSelection.zul";
    }

    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    public class ErrorCodes {
        private ErrorCodes() {
        }

        public static final int RETURN_CODE_SUCCESS      = 0;
        public static final int GENERAL_EXCEPTION        = 999999999;
        public static final int PARAMETER_ERROR = 300000000;
        public static final int AUTHENTICATION_EXCEPTION = 999999990;
        public static final int PWD_RESET_EXCEPTION      = 999999991;
    }

    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    public static class Application {
        private Application() {
        }
        public static enum CachingType {
            CREATE_AND_CACH,
            CREATE_WITHOUT_CACHING,
            GET_CACHED
        };

    }

    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    public static class NaviConfig {
        private NaviConfig() {
        }

        public static final String PERMISSION = "permission";
        public static final String LINK = "link";

    }

    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    public class KeyStrokes {
        private KeyStrokes() {
        }

        public static final String FUNCTION_BASIC = "basic";
        public static final String FUNCTION_FOCUS = "focus";
        public static final String FUNCTION_HINTS = "hints";
        public static final char   CTRL_KEY       = 'C';
        public static final char   ALT_KEY        = 'A';
        public static final char   SHIFT_KEY      = 'S';

    }
}
