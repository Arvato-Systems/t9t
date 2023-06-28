/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.arvatosystems.t9t.zkui.util.PasswordUtils;

public class PasswordUtilsTest {

    @Test
    public void lengthTest() {
        final PasswordUtils passwordUtils = new PasswordUtils(10, 0, 0, 0, 0, 0, 0);
        final String validPassword = "1234567890";
        final String invalidPassword = "123456789";
        Assertions.assertTrue(passwordUtils.verifyPasswordStrength(null, validPassword), "The password 1234567890 should pass: >= 10 characters ");
        Assertions.assertFalse(passwordUtils.verifyPasswordStrength(null, invalidPassword), "The password 1234567890 should not pass: < 10 characters ");
    }

    @Test
    public void lowerTest() {
        final PasswordUtils passwordUtils = new PasswordUtils(0, 2, 0, 0, 0, 0, 0);
        final String validPassword = "a2B3C4d3E6!@$";
        final String invalidPassword = "A2j4L3OF22!!";
        Assertions.assertTrue(passwordUtils.verifyPasswordStrength(null, validPassword), "The password a2B3C4d3E6!@$ should pass: contain 2 lower");
        Assertions.assertFalse(passwordUtils.verifyPasswordStrength(null, invalidPassword), "The password qwerty12345!! should not pass: < 2 lower ");
    }

    @Test
    public void upperTest() {
        final PasswordUtils passwordUtils = new PasswordUtils(0, 0, 1, 0, 0, 0, 0);
        final String validPassword = "abCdef12345!@$";
        final String invalidPassword = "qwerty12345!!";
        Assertions.assertTrue(passwordUtils.verifyPasswordStrength(null, validPassword), "The password abCdef12345!@$ should pass: contain 1 upper ");
        Assertions.assertFalse(passwordUtils.verifyPasswordStrength(null, invalidPassword), "The password qwerty12345!! should not pass: no upper ");
    }

    @Test
    public void letterTest() {
        final PasswordUtils passwordUtils = new PasswordUtils(0, 0, 0, 0, 2, 0, 0);
        final String validPassword = "!!!123456a23k!!";
        final String invalidPassword = "!!#$@@@b2322!!";
        Assertions.assertTrue(passwordUtils.verifyPasswordStrength(null, validPassword), "The password !!!123456a23k!! should pass: contain 2 letters ");
        Assertions.assertFalse(passwordUtils.verifyPasswordStrength(null, invalidPassword), "The password !!#$@@@2322!! should not pass: > 2 letters ");
    }

    @Test
    public void digitTest() {
        final PasswordUtils passwordUtils = new PasswordUtils(0, 0, 0, 1, 0, 0, 0);
        final String validPassword = "abCdef1|!@$";
        final String invalidPassword = "qwerty$&#*!!";
        Assertions.assertTrue(passwordUtils.verifyPasswordStrength(null, validPassword), "The password abCdef1|!@$ should pass: contain 1 digit ");
        Assertions.assertFalse(passwordUtils.verifyPasswordStrength(null, invalidPassword), "The password qwerty$&#*!! should not pass: no digit ");
    }

    @Test
    public void specialTest() {
        final PasswordUtils passwordUtils = new PasswordUtils(0, 0, 0, 0, 0, 1, 0);
        final String validPassword = "abCdef1|!@$";
        final String invalidPassword = "qwerty9387471263fkkljz";
        Assertions.assertTrue(passwordUtils.verifyPasswordStrength(null, validPassword), "The password abCdef1|!@$ should pass: contain special case ");
        Assertions.assertFalse(passwordUtils.verifyPasswordStrength(null, invalidPassword),
                "The password qwerty9387471263fkkljz should not pass: no special case ");
    }

    @Test
    public void maxSubStringTest() {
        final PasswordUtils passwordUtils = new PasswordUtils(0, 0, 0, 0, 0, 0, 3);
        final String oldPassword = "1234567890";
        final String validPassword = "a12b34c56d78e90";
        final String invalidPassword = "a123b45c67d89e0";
        Assertions.assertTrue(passwordUtils.verifyPasswordStrength(oldPassword, validPassword),
                "The password a12b34c56d78e90 should pass: does not have max substring > 3 characters ");
        Assertions.assertFalse(passwordUtils.verifyPasswordStrength(oldPassword, invalidPassword),
                "The password 1234567890 should not pass: contains substring with 3 characters ");
    }
}
