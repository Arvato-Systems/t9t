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
package com.arvatosystems.t9t.base.services;

import java.util.Collection;
import java.util.function.ToIntFunction;

/** Extension methods for xtend classes, working on collections.
 * Replaces jpaw8 dependency. */
public class CollectionExtensions {

    /** Returns the collection element which has the maximum weight of all. */
    public static <T> T ofMaxWeight(Collection<T> list, ToIntFunction<? super T> evaluator) {
        int bestWeightSoFar = Integer.MIN_VALUE;
        T best = null;
        for (T e : list) {
            int newWeight = evaluator.applyAsInt(e);
            if (best == null || newWeight > bestWeightSoFar) {
                best = e;
                bestWeightSoFar = newWeight;
            }
        }
        return best;
    }
}
