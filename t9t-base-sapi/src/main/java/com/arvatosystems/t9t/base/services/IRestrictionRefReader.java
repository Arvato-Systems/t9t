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
package com.arvatosystems.t9t.base.services;

import java.util.List;

/**
 * Interface which provides the link to the persistence layer for user to (anything) restrictions.
 * Its intended use it the lambda parameter of AbstractSearchRestriction.retrieveAllowedRefsUncached.
 */
@FunctionalInterface
public interface IRestrictionRefReader {
    /** Returns the configured list of allowed objectRefs for a user, which may be empty, that means the user is not restricted. */
    List<Long> objectRefsForUser(Long userRef, Long tenantRef);
}
