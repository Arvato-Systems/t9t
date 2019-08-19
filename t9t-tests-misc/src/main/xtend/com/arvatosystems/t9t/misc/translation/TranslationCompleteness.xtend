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
package com.arvatosystems.t9t.misc.translation

import com.arvatosystems.t9t.base.IGridConfigContainer
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.uiprefs.request.GridConfigRequest
import com.arvatosystems.t9t.uiprefs.request.GridConfigResponse
import de.jpaw.annotations.AddLogger
import java.util.HashSet
import java.util.Set

@AddLogger
class TranslationCompleteness {

    def public static void gridHeadersOneLanguageTest(ITestConnection dlg, String language, Set<String> gridnames, boolean alsoInvisible) {
        var int incompleteGrids = 0
        val Set<String> badGrids = new HashSet<String>(20)

        for (grid : gridnames) {
            val rq = new GridConfigRequest => [
                gridId                      = grid;
                selection                   = 0;
                translateInvisibleHeaders   = alsoInvisible;
                noFallbackLanguages         = true
                overrideLanguage            = language;
                validate
            ]
            val headers = dlg.typeIO(rq, GridConfigResponse).headers
            val missing = headers.filter[startsWith("${")]
            if (missing.size > 0) {
                LOGGER.error('''Untranslated headers for «grid» in «language»: «missing.join(', ')»''')
                incompleteGrids += 1
                badGrids.add(grid)
            }
        }
        if (incompleteGrids > 0) {
            LOGGER.error('''«incompleteGrids» of «gridnames.size» grids have incompletely translated headers for language «language»: «badGrids.join(', ')»''')
            throw new Exception("Missing translations for " + language)
        }
        if (gridnames.size > 0)
            LOGGER.info('''All «gridnames.size» grid headers completely translated for language «language», good!''')
    }

    def public static allGridnames() {
        return IGridConfigContainer.GRID_CONFIG_REGISTRY.keySet
    }

    def public static void gridHeadersTestOneLanguage(ITestConnection dlg, String language) {
        // collect all grids
        dlg.gridHeadersOneLanguageTest(language, allGridnames, false)
    }
}
