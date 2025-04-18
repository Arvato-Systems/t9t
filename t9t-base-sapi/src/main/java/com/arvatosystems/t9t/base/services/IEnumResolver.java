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
package com.arvatosystems.t9t.base.services;

import jakarta.annotation.Nonnull;
import java.util.List;

public interface IEnumResolver {
    /** Returns an integer for a NonTokenizableEnum, the token String for a TokenizableEnum (or null if the token was "").
     * Throws a RuntimeException if the enumPqon is not known or that enum does not have an instance of the given name.
     * @param enumPqon
     * @param instanceName
     * @return
     */
    Object getTokenByPqonAndInstance(String enumPqon, String instanceName);

    Object getTokenBySetPqonAndInstance(String enumsetPqon, String instanceName);

    String getTokenByXEnumPqonAndInstance(String xenumPqon, String instanceName);

    String getTokenByXEnumSetPqonAndInstance(String xenumsetPqon, String instanceName);

    Object getTokenByPqonAndOrdinal(@Nonnull String enumPqon, @Nonnull Integer ordinal);

    List<Object> getTokensByPqonAndInstances(@Nonnull String enumPqon, @Nonnull List<String> instanceNames);

    List<Object> getTokensByPqonAndOrdinals(@Nonnull String enumPqon, @Nonnull List<Integer> ordinals);
}
