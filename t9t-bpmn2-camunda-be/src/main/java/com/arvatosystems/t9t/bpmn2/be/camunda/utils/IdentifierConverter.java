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
package com.arvatosystems.t9t.bpmn2.be.camunda.utils;

import java.util.Objects;

import com.arvatosystems.t9t.base.T9tConstants;

public abstract class IdentifierConverter {

    public static Long bpmnTenantIdToT9tTenantRef(String tenantId) {
        if (tenantId == null) {
            return T9tConstants.GLOBAL_TENANT_REF42;
        }

        return Long.valueOf(tenantId);
    }

    public static String t9tTenantRefToBPMNTenantId(Long tenantRef) {
        if (T9tConstants.GLOBAL_TENANT_REF42.equals(tenantRef)) {
            return null;
        }

        return Objects.toString(tenantRef, null);
    }

    public static Long bpmnUserIdToT9tUserRef(String userId) {
        if (userId == null) {
            return null;
        }

        return Long.valueOf(userId);
    }

    public static String t9tUserRefToBPMNUserId(Long userRef) {
        return Objects.toString(userRef, null);
    }

    public static String toValidIdentifier(String s) {
        if (s == null) {
            return null;
        }

        final StringBuilder buffer = new StringBuilder(s.length());

        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);

            if (buffer.length() == 0 && !Character.isJavaIdentifierStart(c)) {
                // buffer.append('_');
                // Do nothing since _ will be replaced as valid start identifier!
            } else if (buffer.length() > 0 && !Character.isJavaIdentifierPart(c)) {
                buffer.append('-');
            } else {
                buffer.append(c);
            }
        }

        if (buffer.length() == 0) {
            return null;
        } else {
            return buffer.toString();
        }
    }

}
