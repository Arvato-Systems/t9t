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
package com.arvatosystems.t9t.doc;

import com.arvatosystems.t9t.base.T9tException;

public class T9tDocTools {
    private T9tDocTools() {}

    public static String getMailingGroupId(MailingGroupRef ref) {
        if (ref == null)
            return null;
        if (ref instanceof MailingGroupKey) {
            return ((MailingGroupKey)ref).getMailingGroupId();
        }
        if (ref instanceof MailingGroupDTO) {
            return ((MailingGroupDTO)ref).getMailingGroupId();
        }
        throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "MailingGroupRef of type " + ref.getClass().getCanonicalName());
    }
}
