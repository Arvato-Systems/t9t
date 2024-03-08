package com.arvatosystems.t9t.microbenchmarks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

// java -jar target/t9t-microbenchmarks.jar -i 3 -f 3 -wf 1 -wi 3 ".*StreamsVsFor.*"
// (takes about 6 minutes)

// N = 3: (very small average iteration count)
//Benchmark                  Mode  Cnt   Score   Error  Units
//StreamsVsFor.classicFor    avgt    9  12,045 ± 0,075  ns/op
//StreamsVsFor.forWithIndex  avgt    9   9,279 ± 0,077  ns/op
//StreamsVsFor.usingStreams  avgt    9  51,486 ± 0,961  ns/op

// N = 5: (small average iteration count)
//Benchmark                  Mode  Cnt   Score   Error  Units
//StreamsVsFor.classicFor    avgt    9  14,441 ± 0,199  ns/op
//StreamsVsFor.forWithIndex  avgt    9  12,124 ± 0,150  ns/op
//StreamsVsFor.usingStreams  avgt    9  52,290 ± 0,970  ns/op

// N = 10: (small to medium average iteration count)
//Benchmark                  Mode  Cnt   Score   Error  Units
//StreamsVsFor.classicFor    avgt    9  19,089 ± 0,096  ns/op
//StreamsVsFor.forWithIndex  avgt    9  16,506 ± 0,204  ns/op
//StreamsVsFor.usingStreams  avgt    9  59,284 ± 1,421  ns/op

// N = 20: (medium average iteration count)
//Benchmark                  Mode  Cnt   Score   Error  Units
//StreamsVsFor.classicFor    avgt    9  32,684 ± 1,081  ns/op
//StreamsVsFor.forWithIndex  avgt    9  31,474 ± 1,081  ns/op
//StreamsVsFor.usingStreams  avgt    9  73,062 ± 1,322  ns/op


@State(value = Scope.Thread)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class StreamsVsFor {
    private static final int N = 10;  // selects some limit on the number of iterations

    private static int counter = 0;
    private static final List<List<Integer>> LIST_OF_LIST_OF_INTS = new ArrayList<>(20);
    static {
        for (int i = 0; i < 20; ++i) {
            final List<Integer> newList = new ArrayList<>(i);
            for (int j = 0; j < i; ++j) {
                newList.add(i + j);
            }
            LIST_OF_LIST_OF_INTS.add(newList);
        }
    }

    // class Inputs prevents the constants from being propagated too early
    @State(Scope.Benchmark)
    public static class Inputs {
        private static final int[] MASKS = {
                1, 2, 4, 8, 12, 3, 5, 17, 9, 11,
                4, 8, 16, 1, 0, 6, 31, 31, 31, 31
        };
        public List<Integer> getList() {
            ++counter;
            if (counter >= N) {
                counter = 0;
            }
            return LIST_OF_LIST_OF_INTS.get(counter);
        }
        public Predicate<Integer> getPredicate() {
            return i -> (i & MASKS[counter]) == 0;
        }

        public Inputs() {
        }
    }

    @Benchmark
    public void forWithIndex(final Blackhole bh, final Inputs input) { // FORTRAN style...
        final List<Integer> data = input.getList();
        final Predicate<Integer> pred = input.getPredicate();

        final List<Integer> output = new ArrayList<>(data.size());
        for (int i = 0; i < data.size(); ++i) { // for loop using a primitive int as counter (no object creation at all)
            final Integer value = data.get(i);
            if (pred.test(value)) {
                output.add(value);
            }
        }
        bh.consume(output);
    }

    @Benchmark
    public void classicFor(final Blackhole bh, final Inputs input) {  // classic Java...
        final List<Integer> data = input.getList();
        final Predicate<Integer> pred = input.getPredicate();

        final List<Integer> output = new ArrayList<>(data.size());
        for (final Integer value: data) { // for loop using an Iterator (single object created for whole loop)
            if (pred.test(value)) {
                output.add(value);
            }
        }
        bh.consume(output);
    }

    @Benchmark
    public void usingStreams(final Blackhole bh, final Inputs input) {  // Java with streams...
        final List<Integer> data = input.getList();
        final Predicate<Integer> pred = input.getPredicate();

        final List<Integer> output = data.stream().filter(pred).toList();
        bh.consume(output);
    }
}
