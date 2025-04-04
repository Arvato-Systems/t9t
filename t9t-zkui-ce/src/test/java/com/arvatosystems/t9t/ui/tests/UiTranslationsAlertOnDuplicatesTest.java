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
package com.arvatosystems.t9t.ui.tests;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.arvatosystems.t9t.jdp.Init;
import com.arvatosystems.t9t.translation.be.TranslationsStack;

public class UiTranslationsAlertOnDuplicatesTest {

    @Test
    public void alertOnDuplicateTranslationsTest() throws Exception {
        Init.initializeT9t();  // initialize
        final int numDuplicates = TranslationsStack.getNumberOfDuplicateTranslations();
        if (numDuplicates != 0) {
            System.out.println(numDuplicates + " overwritten translation entries exist - check logs for details");
            throw new Exception(numDuplicates + " duplicate translation entries exist");
        }
    }

    @Disabled // run this one manually only
    @Test
    public void alertOnTranslationsMatchingDefaultTest() throws Exception {
        Init.initializeT9t();  // initialize
        TranslationsStack.checkDuplicates(true, true, false);
    }

    @Disabled // run this one manually only
    @Test
    public void alertOnTranslationsDeMatchingEnTest() throws Exception {
        Init.initializeT9t();  // initialize
        TranslationsStack.checkDuplicates(false, true, true);
    }
}
