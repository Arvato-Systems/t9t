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
package com.arvatosystems.t9t;

import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

public class SetTest {

    public static <T> Set<T> union(Set<T> setA, Set<T> setB) {
        Set<T> tmp = new TreeSet<T>(setA);
        tmp.addAll(setB);
        return tmp;
    }

    public static <T> Set<T> intersection(Set<T> setA, Set<T> setB) {
        Set<T> tmp = new TreeSet<T>();
        for (T x : setA)
            if (setB.contains(x))
                tmp.add(x);
        return tmp;
    }

    public static <T> Set<T> difference(Set<T> setA, Set<T> setB) {
        Set<T> tmp = new TreeSet<T>(setA);
        tmp.removeAll(setB);
        return tmp;
    }

    public static <T> Set<T> symDifference(Set<T> setA, Set<T> setB) {
        Set<T> tmpA;
        Set<T> tmpB;

        tmpA = union(setA, setB);
        tmpB = intersection(setA, setB);
        return difference(tmpA, tmpB);
    }

    public static <T> boolean isSubset(Set<T> setA, Set<T> setB) {
        return setB.containsAll(setA);
    }

    public static <T> boolean isSuperset(Set<T> setA, Set<T> setB) {
        return setA.containsAll(setB);
    }

    @Test
    public void test() {
        TreeSet<Character> set1 = new TreeSet<Character>();
        TreeSet<Character> set2 = new TreeSet<Character>();

        set1.add('A');
        set1.add('B');
        set1.add('C');
        set1.add('D');

        set2.add('C');
        set2.add('D');
        set2.add('E');
        set2.add('F');

        System.out.println("set1: " + set1);
        System.out.println("set2: " + set2);

        System.out.println("Union: " + union(set1, set2));
        System.out.println("Intersection: " + intersection(set1, set2));
        System.out.println("Difference (set1 - set2): " + difference(set1, set2));
        System.out.println("Symmetric Difference: " + symDifference(set1, set2));

        TreeSet<Character> set3 = new TreeSet<Character>(set1);

        set3.remove('D');
        System.out.println("set3: " + set3);

        System.out.println("Is set1 a subset of set2? " + isSubset(set1, set3));
        System.out.println("Is set1 a superset of set2? " + isSuperset(set1, set3));
        System.out.println("Is set3 a subset of set1? " + isSubset(set3, set1));
        System.out.println("Is set3 a superset of set1? " + isSuperset(set3, set1));

    }

}
