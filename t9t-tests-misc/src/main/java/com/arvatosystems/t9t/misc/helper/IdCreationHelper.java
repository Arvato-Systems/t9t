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
package com.arvatosystems.t9t.misc.helper;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

import org.eclipse.xtend2.lib.StringConcatenation;

public final class IdCreationHelper {

    private IdCreationHelper() {
        // empty, private to avoid instantiation
    }

    private static final Random RANDOM = new Random();

    public static String createRandomCustomerId() {
        int randomInt = RANDOM.nextInt();
        while (randomInt < 10000) {
            randomInt = RANDOM.nextInt();
        }
        return String.valueOf(randomInt);
    }

    public static String createRandomOrderId() {
        return UUID.randomUUID().toString().substring(0, 15);
    }

    public static String createRandomEMail() {
        final String uuid = UUID.randomUUID().toString();
        final StringConcatenation emailBuilder = new StringConcatenation();
        emailBuilder.append("random-email-");
        emailBuilder.append(uuid.substring(0, 20));
        emailBuilder.append("@domain-");
        emailBuilder.append(uuid.substring(20, 30));
        emailBuilder.append(".com");
        return emailBuilder.toString();
    }

    public static String createRandomPhoneNumber() {
        final String uuid = UUID.randomUUID().toString();
        final StringConcatenation phoneNoBuilder = new StringConcatenation();
        phoneNoBuilder.append(uuid.substring(0, 5));
        phoneNoBuilder.append("/");
        phoneNoBuilder.append(uuid.substring(5, 10));
        return phoneNoBuilder.toString();
    }

    /**
     * Creates a new 'timestamp' id.
     */
    public static String newId() {
        final String id = String.valueOf(Instant.now().toEpochMilli());
        return id.substring(id.length() - 8, id.length());
    }
}
