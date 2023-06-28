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

import de.jpaw.fixedpoint.types.MicroUnits;

public class MicroUnitsFormatTest {

    @Test
    public void formatMircoUnitsWithScale() {
        MicroUnits m0 = MicroUnits.valueOf(4);
        MicroUnits m1 = MicroUnits.valueOf(4.1);
        MicroUnits m2 = MicroUnits.valueOf(4.12);
        MicroUnits m3 = MicroUnits.valueOf(4.125);

        Assertions.assertEquals("4.00", m0.toString(2), "expect 2 decimals");
        Assertions.assertEquals("4.10", m1.toString(2), "expect 2 decimals");
        Assertions.assertEquals("4.12", m2.toString(2), "expect 2 decimals");
        Assertions.assertEquals("4.125", m3.toString(2), "expect 3 decimals");
    }

    @Test
    public void formatMircoUnitsNoScale() {
        MicroUnits m0 = MicroUnits.valueOf(4);
        MicroUnits m1 = MicroUnits.valueOf(4.1);
        MicroUnits m2 = MicroUnits.valueOf(4.12);
        MicroUnits m3 = MicroUnits.valueOf(4.125);

        Assertions.assertEquals("4", m0.toString(), "expect no decimals");
        Assertions.assertEquals("4.1", m1.toString(), "expect 1 decimal");
        Assertions.assertEquals("4.12", m2.toString(), "expect 2 decimals");
        Assertions.assertEquals("4.125", m3.toString(), "expect 3 decimals");
    }
}
