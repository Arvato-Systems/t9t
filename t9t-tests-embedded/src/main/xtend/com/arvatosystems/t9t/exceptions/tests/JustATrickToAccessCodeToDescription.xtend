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
package com.arvatosystems.t9t.exceptions.tests

import de.jpaw.util.ApplicationException

class JustATrickToAccessCodeToDescription extends ApplicationException {

    new(int errorCode) {
        super(errorCode)
    }

    def static boolean validateAllExceptions() {
        var boolean isOk = true
        for (e: codeToDescription.entrySet) {
            if (e.key != 0) {
                val classification = e.key / CLASSIFICATION_FACTOR
                if (classification < 1 || classification > 9) {
                    println('''Code «e.key» is not of 9 digit size as required. Text: «e.value»''')
                    isOk = false
                }
            }
        }
        return isOk
    }

    def static void listAllExceptions() {
        val all = codeToDescription.entrySet
        all.sortBy[key].forEach[ println('''«key»: «value»''') ]
        println('''Listed «all.size» exceptions''')
    }
}
