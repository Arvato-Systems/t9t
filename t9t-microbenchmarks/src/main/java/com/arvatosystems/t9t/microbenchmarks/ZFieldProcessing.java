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
package com.arvatosystems.t9t.microbenchmarks;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import com.arvatosystems.t9t.base.IHaveZField;
import com.arvatosystems.t9t.base.JsonUtil;
import com.arvatosystems.t9t.base.T9tUtil;

import de.jpaw.bonaparte.core.BonaparteJsonEscaper;
import de.jpaw.json.JsonException;
import de.jpaw.json.JsonParser;

// java -jar target/t9t-microbenchmarks.jar -i 3 -f 3 -wf 1 -wi 3 ".*ZFieldProcessing.*"
// (takes about 12 minutes)

//Benchmark                             Mode  Cnt     Score   Error  Units
//ZFieldProcessing.mixedPlainWithIfs    avgt       2304,405          ns/op
//ZFieldProcessing.mixedUnoptimized     avgt       3337,240          ns/op
//ZFieldProcessing.mixedWithInterface   avgt       2301,641          ns/op
//ZFieldProcessing.mixedWithLambdas     avgt       2301,910          ns/op
//ZFieldProcessing.singlePlainWithIfs   avgt          2,507          ns/op
//ZFieldProcessing.singleUnoptimized    avgt       1063,581          ns/op
//ZFieldProcessing.singleWithInterface  avgt          2,534          ns/op
//ZFieldProcessing.singleWithLambdas    avgt          2,617          ns/op

@State(value = Scope.Thread)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class ZFieldProcessing {

    // class Inputs prevents the constants from being propagated too early
    @State(Scope.Benchmark)
    public static class Inputs {
        final String zFieldFilled = "{ \"hshshs\": \"hsfsdjkfh\", \"h223djkfh\": 3.1415926535, \"ggsggs\": true, \"iikkkll\": null }";
        final String zFieldEmpty = null;

        final Map<String, Object> addMapFilled = new HashMap<>();
        final Map<String, Object> addMapEmpty = Collections.emptyMap();

        public Inputs() {
            addMapFilled.put("numval", 2.718281828);
            addMapFilled.put("textval", "Hello, world");
            addMapFilled.put("boolval", true);
            addMapFilled.put("nullval", null);
        }
    }

    // Dummy entity has a z field, with same getter / setter as in generated entities
    static class DummyEntity implements IHaveZField {
        private String z;

        DummyEntity(final String initVal) {
            z = initVal;
        }

        // same code as Z getter in generated entity
        public Map<String, Object> getZ() {
            try {
                return z == null ? null : new JsonParser(z, false).parseObject();
            } catch (final JsonException e) {
                throw new RuntimeException(e);
            }
        }
        public void setZ(Map<String, Object> x) {
            z = BonaparteJsonEscaper.asJson(x);
        }
    }

    @Benchmark
    public void mixedUnoptimized(Blackhole bh, Inputs input) {
        // perform all 4 operations
        // case 1: populated + add something
        final DummyEntity dummy1 = new DummyEntity(input.zFieldFilled);
        dummy1.setZ(JsonUtil.mergeZ(dummy1.getZ(), input.addMapFilled));
        bh.consume(dummy1);

        // case 2: populated + add nothing
        final DummyEntity dummy2 = new DummyEntity(input.zFieldFilled);
        dummy2.setZ(JsonUtil.mergeZ(dummy2.getZ(), input.addMapEmpty));
        bh.consume(dummy2);

        // case 3: not populated + add something
        final DummyEntity dummy3 = new DummyEntity(input.zFieldEmpty);
        dummy3.setZ(JsonUtil.mergeZ(dummy3.getZ(), input.addMapFilled));
        bh.consume(dummy3);

        // case 4: not populated + add nothing
        final DummyEntity dummy4 = new DummyEntity(input.zFieldEmpty);
        dummy4.setZ(JsonUtil.mergeZ(dummy4.getZ(), input.addMapEmpty));
        bh.consume(dummy4);
    }

    @Benchmark
    public void mixedPlainWithIfs(Blackhole bh, Inputs input) {
        // perform all 4 operations
        // case 1: populated + add something
        final DummyEntity dummy1 = new DummyEntity(input.zFieldFilled);
        if (!T9tUtil.isEmpty(input.addMapFilled)) {
            dummy1.setZ(JsonUtil.mergeZ(dummy1.getZ(), input.addMapFilled));
        }
        bh.consume(dummy1);

        // case 2: populated + add nothing
        final DummyEntity dummy2 = new DummyEntity(input.zFieldFilled);
        if (!T9tUtil.isEmpty(input.addMapEmpty)) {
            dummy2.setZ(JsonUtil.mergeZ(dummy2.getZ(), input.addMapEmpty));
        }
        bh.consume(dummy2);

        // case 3: not populated + add something
        final DummyEntity dummy3 = new DummyEntity(input.zFieldEmpty);
        if (!T9tUtil.isEmpty(input.addMapFilled)) {
            dummy3.setZ(JsonUtil.mergeZ(dummy3.getZ(), input.addMapFilled));
        }
        bh.consume(dummy3);

        // case 4: not populated + add nothing
        final DummyEntity dummy4 = new DummyEntity(input.zFieldEmpty);
        if (!T9tUtil.isEmpty(input.addMapEmpty)) {
            dummy4.setZ(JsonUtil.mergeZ(dummy4.getZ(), input.addMapEmpty));
        }
        bh.consume(dummy4);
    }

    @Benchmark
    public void mixedWithLambdas(Blackhole bh, Inputs input) {
        // perform all 4 operations
        // case 1: populated + add something
        final DummyEntity dummy1 = new DummyEntity(input.zFieldFilled);
        JsonUtil.mergeZ(dummy1::setZ, dummy1::getZ, input.addMapFilled);
        bh.consume(dummy1);

        // case 2: populated + add nothing
        final DummyEntity dummy2 = new DummyEntity(input.zFieldFilled);
        JsonUtil.mergeZ(dummy2::setZ, dummy2::getZ, input.addMapEmpty);
        bh.consume(dummy2);

        // case 3: not populated + add something
        final DummyEntity dummy3 = new DummyEntity(input.zFieldEmpty);
        JsonUtil.mergeZ(dummy3::setZ, dummy3::getZ, input.addMapFilled);
        bh.consume(dummy3);

        // case 4: not populated + add nothing
        final DummyEntity dummy4 = new DummyEntity(input.zFieldEmpty);
        JsonUtil.mergeZ(dummy4::setZ, dummy4::getZ, input.addMapEmpty);
        bh.consume(dummy4);
    }

    @Benchmark
    public void mixedWithInterface(Blackhole bh, Inputs input) {
        // perform all 4 operations
        // case 1: populated + add something
        final DummyEntity dummy1 = new DummyEntity(input.zFieldFilled);
        JsonUtil.mergeZ(dummy1, input.addMapFilled);
        bh.consume(dummy1);

        // case 2: populated + add nothing
        final DummyEntity dummy2 = new DummyEntity(input.zFieldFilled);
        JsonUtil.mergeZ(dummy2, input.addMapEmpty);
        bh.consume(dummy2);

        // case 3: not populated + add something
        final DummyEntity dummy3 = new DummyEntity(input.zFieldEmpty);
        JsonUtil.mergeZ(dummy3, input.addMapFilled);
        bh.consume(dummy3);

        // case 4: not populated + add nothing
        final DummyEntity dummy4 = new DummyEntity(input.zFieldEmpty);
        JsonUtil.mergeZ(dummy4, input.addMapEmpty);
        bh.consume(dummy4);
    }

    @Benchmark
    public void singleUnoptimized(Blackhole bh, Inputs input) {
        // perform all 4 operations

        // case 2: populated + add nothing
        final DummyEntity dummy2 = new DummyEntity(input.zFieldFilled);
        dummy2.setZ(JsonUtil.mergeZ(dummy2.getZ(), input.addMapEmpty));
        bh.consume(dummy2);
    }

    @Benchmark
    public void singlePlainWithIfs(Blackhole bh, Inputs input) {
        // perform all 4 operations

        // case 2: populated + add nothing
        final DummyEntity dummy2 = new DummyEntity(input.zFieldFilled);
        if (!T9tUtil.isEmpty(input.addMapEmpty)) {
            dummy2.setZ(JsonUtil.mergeZ(dummy2.getZ(), input.addMapEmpty));
        }
        bh.consume(dummy2);
    }

    @Benchmark
    public void singleWithLambdas(Blackhole bh, Inputs input) {
        // perform all 4 operations

        // case 2: populated + add nothing
        final DummyEntity dummy2 = new DummyEntity(input.zFieldFilled);
        JsonUtil.mergeZ(dummy2::setZ, dummy2::getZ, input.addMapEmpty);
        bh.consume(dummy2);
    }

    @Benchmark
    public void singleWithInterface(Blackhole bh, Inputs input) {
        // perform all 4 operations

        // case 2: populated + add nothing
        final DummyEntity dummy2 = new DummyEntity(input.zFieldFilled);
        JsonUtil.mergeZ(dummy2, input.addMapEmpty);
        bh.consume(dummy2);
    }
}
