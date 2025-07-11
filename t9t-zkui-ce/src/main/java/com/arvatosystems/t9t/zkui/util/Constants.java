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
package com.arvatosystems.t9t.zkui.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Constants {
    private Constants() {
    }

    public static final String VM_ID_TENANT = "tenant";

    public static final class DateTime {
        public static final Map<String, Integer> FIRST_DAY_OF_WEEK = Map.of("de", java.util.Calendar.MONDAY);
    }

    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    public final class ZulFiles {
        private ZulFiles() {
        }
        public static final String LOGOUT                 = "/logout";
        public static final String HOME                   = "/home.zul";
        public static final String LOGIN                  = "/login.zul";
        public static final String LOGIN_SUCCESS_REDIRECT = "/screens/login/expired_credentials.zul";
        public static final String LOGIN_TENANT_SELECTION = "/screens/login/tenantSelection.zul";
        public static final String ADDITIONAL_SELECTIONS  = "/screens/login/selections.zul";
    }

    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    public final class ErrorCodes {
        private ErrorCodes() {
        }

        public static final int RETURN_CODE_SUCCESS      = 0;
        public static final int GENERAL_EXCEPTION        = 999999999;
        public static final int PARAMETER_ERROR = 300000000;
        public static final int AUTHENTICATION_EXCEPTION = 999999990;
        public static final int PWD_RESET_EXCEPTION      = 999999991;
    }

    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    public static final class Application {
        private Application() {
        }
        public enum CachingType {
            CREATE_AND_CACH,
            CREATE_WITHOUT_CACHING,
            GET_CACHED
        };

    }

    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    public static final class NaviConfig {
        private NaviConfig() {
        }

        public static final String PERMISSION = "permission";
        public static final String LINK = "link";

    }

    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    public final class KeyStrokes {
        private KeyStrokes() {
        }

        public static final char   CTRL_KEY       = 'C';
        public static final char   ALT_KEY        = 'A';
        public static final char   SHIFT_KEY      = 'S';

    }

    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    /* Properties in the bon file which are used by the ZK UI.   */
    /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
    public final class UiFieldProperties {
        private UiFieldProperties() {
        }

        public static final String FILTER_QUALIFIER = "filterQualifier";        // the default search qualifier
        public static final String QUALIFIER_FOR    = "qualifierFor";           // only accept valid qualifiers for the referenced interface
        public static final String DROPDOWN         = "dropdown";               // use a dropdown instead of (alpha)numeric entry field
        public static final String DROPDOWN_FORMAT  = "dropdownformat";         // format used to display dropdown label
        public static final String BANDBOX          = "bandbox";                // use a bandbox (search popup for bigger data sets) instead of entry field
        public static final String TRISTATE         = "tristate";               // use a dropdown with TRUE / FALSE / NULL instead of checkbox for this boolean
        public static final String ENUMS            = "enums";                  // use a restricted set of enums
        public static final String ENUMSET          = "enumset";                // display as list of enum instances in the result overview grid
        public static final String XENUMSET         = "xenumset";               // display as list of xenum instances in the result overview grid
        public static final String SHOW_TODAY       = "showToday";              // show an additional "today" button in the date picker
        public static final String NO_JAVA          = "noJava";
        public static final String NO_DDL           = "noDDL";
        public static final String NO_AUTO_MAP      = "noAutoMap";
        public static final String GENERIC_OBJECT   = "genericobject";          // the field is a generic bona portable object
        public static final String MULTI_DROPDOWN   = "multidropdown";          // dropdown with multiple selection
    }

    public static final List<String> COUNTRY_MODEL_DATA = new ArrayList<>();
    static {
        Stream<String> s = Arrays.stream(Locale.getISOCountries());
        COUNTRY_MODEL_DATA.add("XX");  // used for wildcards in t9t doc module
        COUNTRY_MODEL_DATA.add("XK");  // inofficial code for Kosovo
        COUNTRY_MODEL_DATA.add("EU");  // sometimes used for European Union, will be mapped to specific code by the backend
        COUNTRY_MODEL_DATA.addAll(s.sorted().collect(Collectors.toList()));
    }
}
