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
package com.arvatosystems.t9t.stdsetup

import com.arvatosystems.t9t.base.ITestConnection
import de.jpaw.annotations.AddLogger
import org.eclipse.xtend.lib.annotations.Data
import com.arvatosystems.t9t.event.SubscriberConfigDTO

import static extension com.arvatosystems.t9t.misc.extensions.MiscExtensions.*
import com.arvatosystems.t9t.base.event.InvalidateCacheEvent

@AddLogger
@Data
class T9tClusterSetup {
    ITestConnection dlg

    public def void setupCacheInvalidationEntry() {
        new SubscriberConfigDTO => [
            eventID          = InvalidateCacheEvent.BClass.INSTANCE.pqon // "t9t.base.event.InvalidateCacheEvent"
            handlerClassName = "cacheInvalidation"
            isActive         = true
            merge(dlg)
        ]
    }
}
