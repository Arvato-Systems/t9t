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
package com.arvatosystems.t9t.schemaLoader.util;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @author TWEL006
 */
public class SimpleSqlParserTest {

    @Test
    public void testLiterals() {
        assertEquals(asList(";", "';';"), SimpleSqlParser.getStatements(";';';"));
        assertEquals(asList(";", "\";\";"), SimpleSqlParser.getStatements(";\";\";"));
        assertEquals(asList(";", "'\\';';"), SimpleSqlParser.getStatements(";'\\';';"));
        assertEquals(asList(";", "\"\\';\";"), SimpleSqlParser.getStatements(";\"\\';\";"));
        assertEquals(asList(";", "'\\\";';"), SimpleSqlParser.getStatements(";'\\\";';"));
        assertEquals(asList(";", "\"\\\";\";"), SimpleSqlParser.getStatements(";\"\\\";\";"));
    }


    @Test
    public void testComments() {
        assertEquals(asList(";", "\n;"), SimpleSqlParser.getStatements(";--;\n;"));
        assertEquals(asList(";", ";"), SimpleSqlParser.getStatements(";/*;\n;*/;"));
    }

    @Test
    public void testSplit() {
        assertEquals(asList("A;", "B;", "C"), SimpleSqlParser.getStatements("A;B;C"));
        assertEquals(asList("A;", "B;", "C;"), SimpleSqlParser.getStatements("A;B;C;"));
    }
}
