/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataLoaderTest {

    public static final String WHITE_SPACES = "\\s*";

    public static final String ANY_CHARS = ".*";
    public static final String QUOTE = "(?:'|\")";
    public static final String ALL_BUT_QUOTE = "[^'|\"]*";

    /**
     * ## table: pg_cfg_client_infos_t
     * ## where: s_user_id_created = ':FB'
     * ## order_by: s_client_id,s_merchant_id,s_name
     */
    public static final String REGEX_CONFIG = "^##" + WHITE_SPACES + // starts
                                                                     // with ##
            "(table|where|order_by)" + // keywords
            WHITE_SPACES + ":" + WHITE_SPACES + // :
            "(" + ANY_CHARS + ")" // actual value can by any string
    ;
    public static final Pattern PATTERN_CONFIG = Pattern.compile(REGEX_CONFIG, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /**
     * ## column: s_user_id_created = ':FB'
     * ## column: d_timestamp_created = SYSTIMESTAMP
     * ## column: s_user_id = ':FB'
     * ## column: d_timestamp = SYSTIMESTAMP
     * ## column: n_sequence_no = 0
     * ## column: s_name --name of the parameter
     * ## column: s_value --value of the parameter
     * ## column: c_type --type of the parameter
     */
    public static final String REGEX_CONFIG_COL = "^##" + WHITE_SPACES + // starts
            "column:" + WHITE_SPACES + // column keyword
            "([A-Za-z][A-Za-z0-9_]+)" + // column name
            WHITE_SPACES + "=?" + "(" + ANY_CHARS + ")" // optionally = default
                                                        // value
    ;
    public static final Pattern PATTERN_CONFIG_COL = Pattern.compile(REGEX_CONFIG_COL, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);


    public static final String REGEX_PASSWORD = "(.*password=)([^;]*)(.*)";
    public static final Pattern PATTERN_PASSWORD = Pattern.compile(REGEX_PASSWORD, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);


    @Test
    public void testEnvParsing() throws Exception {

        String value = "${ENV.USERNAME}";
        String changed = value.replaceFirst("^\\$\\{ENV\\.", "").replaceFirst("}$", "");
        System.out.println(value + "\t\t --> " + changed);
        Assertions.assertEquals(changed, "USERNAME");

        value = "${ENV.US{}ERNAME}";
        changed = value.replaceFirst("^\\$\\{ENV\\.", "").replaceFirst("}$", "");
        System.out.println(value + "\t --> " + changed);
        Assertions.assertEquals(changed, "US{}ERNAME");

        value = "${ENV_US{}ERNAME}";
        changed = value.replaceFirst("^\\$\\{ENV\\.", "").replaceFirst("}$", "");
        System.out.println(value + "\t --> " + changed);
        Assertions.assertEquals(changed, "${ENV_US{}ERNAME");

        value = "${ENV.USERNAME}X}";
        changed = value.replaceFirst("^\\$\\{ENV\\.", "").replaceFirst("}$", "");
        System.out.println(value + "\t --> " + changed);
        Assertions.assertEquals(changed, "USERNAME}X");

    }

    @Test
    public void testPatternConfiguration() throws Exception {

        String conf1 = "##      table:   pg_cfg_client_infos_t";
        String conf2 = "##      where:   s_user_id_created     = 'test'";
        String conf3 = "##   order_by:   s_client_id,s_merchant_id,s_name";
        String conf4 = "##   tableBAD:   pg_cfg_client_infos_t";

        Map<String, String> matchedConf;
        matchedConf = this.matchKeyValue(PATTERN_CONFIG, conf1);
        System.out.println(matchedConf);
        Assertions.assertEquals(matchedConf.toString(), "{table=pg_cfg_client_infos_t}");

        matchedConf = this.matchKeyValue(PATTERN_CONFIG, conf2);
        System.out.println(matchedConf);
        Assertions.assertEquals(matchedConf.toString(), "{where=s_user_id_created     = 'test'}");

        matchedConf = this.matchKeyValue(PATTERN_CONFIG, conf3);
        System.out.println(matchedConf);
        Assertions.assertEquals(matchedConf.toString(), "{order_by=s_client_id,s_merchant_id,s_name}");

        matchedConf = this.matchKeyValue(PATTERN_CONFIG, conf4);
        System.out.println(matchedConf);
        Assertions.assertEquals(matchedConf.toString(), "{}");

    }

    @Test
    public void testPatternConfigurationColumn() throws Exception {

        String conf1 = "## column: s_user_id_created = ':FB'         ";
        String conf2 = "## column: d_timestamp_created = SYSTIMESTAMP";
        String conf3 = "## column: s_user_id = ':FB'                 ";
        String conf4 = "## column: d_timestamp = SYSTIMESTAMP        ";
        String conf5 = "## column: n_sequence_no = 0                 ";
        String conf6 = "## column: s_name --name of the parameter    ";
        String conf7 = "## column: s_value = 'testxx' --value of the parameter  ";
        String conf8 = "## column: c_type    ";

        Map<String, String> matchedConf;

        matchedConf = this.matchKeyValue(PATTERN_CONFIG_COL, conf1);
        System.out.println(matchedConf);
        Assertions.assertEquals(matchedConf.toString(), "{s_user_id_created= ':FB'         }");

        matchedConf = this.matchKeyValue(PATTERN_CONFIG_COL, conf2);
        System.out.println(matchedConf);
        Assertions.assertEquals(matchedConf.toString(), "{d_timestamp_created= SYSTIMESTAMP}");

        matchedConf = this.matchKeyValue(PATTERN_CONFIG_COL, conf3);
        System.out.println(matchedConf);
        Assertions.assertEquals(matchedConf.toString(), "{s_user_id= ':FB'                 }");

        matchedConf = this.matchKeyValue(PATTERN_CONFIG_COL, conf4);
        System.out.println(matchedConf);
        Assertions.assertEquals(matchedConf.toString(), "{d_timestamp= SYSTIMESTAMP        }");

        matchedConf = this.matchKeyValue(PATTERN_CONFIG_COL, conf5);
        System.out.println(matchedConf);
        Assertions.assertEquals(matchedConf.toString(), "{n_sequence_no= 0                 }");

        matchedConf = this.matchKeyValue(PATTERN_CONFIG_COL, conf6);
        System.out.println(matchedConf);
        Assertions.assertEquals(matchedConf.toString(), "{s_name=}");

        matchedConf = this.matchKeyValue(PATTERN_CONFIG_COL, conf7);
        System.out.println(matchedConf);
        Assertions.assertEquals(matchedConf.toString(), "{s_value= 'testxx' }");

        matchedConf = this.matchKeyValue(PATTERN_CONFIG_COL, conf8);
        System.out.println(matchedConf);
        Assertions.assertEquals(matchedConf.toString(), "{c_type=}");

    }

    protected Map<String, String> matchKeyValue(Pattern pattern, String stringToMatch) {
        Map<String, String> matched = new HashMap<String, String>();
        Matcher formMatcher = pattern.matcher(stringToMatch);

        while (formMatcher.find()) {
            if (formMatcher.groupCount() == 1) {
                matched.put(formMatcher.group(1), null);
            } else if (formMatcher.groupCount() > 1) {
                String value = formMatcher.group(2);
                value = value.replaceFirst("--.*", ""); // remove potential
                                                        // comments --
                matched.put(formMatcher.group(1), value);
                if (formMatcher.groupCount() > 2)
                    System.out.println("The pattern has more than 2 groups");
            }
        }
        return matched;

    }
    protected String matchValue(Pattern pattern, String stringToMatch) {
        StringBuffer result = new StringBuffer();
        Matcher formMatcher = pattern.matcher(stringToMatch);
        while ( formMatcher.find() ){
            // this is needed for better testing
            for (int i = 1 ; i <= formMatcher.groupCount(); i++) {
                System.out.println("formMatcher.group("+i+") = " + formMatcher.group(i));
            }
            formMatcher.appendReplacement(result,
                    Matcher.quoteReplacement(formMatcher.group(1) + "#"+formMatcher.group(2)+"#" + formMatcher.group(3)));
        }
        formMatcher.appendTail(result);
        return result.toString();
    }

}
