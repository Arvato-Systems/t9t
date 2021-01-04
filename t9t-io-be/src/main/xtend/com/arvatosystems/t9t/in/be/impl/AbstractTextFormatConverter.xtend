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
package com.arvatosystems.t9t.in.be.impl

import de.jpaw.annotations.AddLogger
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.IOException

@AddLogger
abstract class AbstractTextFormatConverter extends AbstractInputFormatConverter {

    def abstract void process(String textLine);

    override process(InputStream is) {
        val singleLineComment = cfg.singleLineComment === null ? null cfg.singleLineComment  // avoid the need for duplicate test to null or ""
        var int linesToSkip   = cfg.linesToSkip ?: 0
        val streamReader      = new BufferedReader(new InputStreamReader(is))
        try {
            for (var String line = streamReader.readLine(); line !== null; line = streamReader.readLine()) {
                if (singleLineComment === null || !line.startsWith(singleLineComment)) {
                    if (linesToSkip > 0) {
                        linesToSkip--
                    } else {
                        process(line)
                    }
                }
            }
        } finally {
            try {
                streamReader.close();
            } catch (IOException f) {
                // should (hopefully) never happen because its already caught beforehand.
            }
        }
    }
}
