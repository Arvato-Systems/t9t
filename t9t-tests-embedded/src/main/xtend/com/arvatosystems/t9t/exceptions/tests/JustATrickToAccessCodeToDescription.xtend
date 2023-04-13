/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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

import com.arvatosystems.t9t.base.T9tException
import de.jpaw.util.ApplicationException
import java.util.TreeMap
import java.util.concurrent.atomic.AtomicBoolean

class JustATrickToAccessCodeToDescription extends ApplicationException {

    new(int errorCode) {
        super(errorCode)
    }

    def static boolean validateAllExceptions() {
        val AtomicBoolean isOk = new AtomicBoolean(true)
        forEachCode([ key, value |
            if (key != 0 && key != T9tException.PASSWORD_EXPIRED) {
                val classification = key / CLASSIFICATION_FACTOR
                if (classification < 1 || classification > 9) {
                    println('''Code «key» is not of 9 digit size as required. Text: «value»''')
                    isOk.set(false)
                }
            }
        ])
        return isOk.get
    }

    def static void listAllExceptions() {
        val sortedCopy = new TreeMap<Integer, String>()
        forEachCode([ key, value | sortedCopy.put(key, value)])
        sortedCopy.forEach[ key, value | println('''«key»: «value»''') ]
        println('''Listed «sortedCopy.size» exceptions''')
    }
}
