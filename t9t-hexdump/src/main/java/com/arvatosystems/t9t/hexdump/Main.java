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
package com.arvatosystems.t9t.hexdump;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class Main {

    private static final int BYTES_PER_LINE = 16;

    private Main() { }

    public static void main(final String[] args) {
        try (InputStream input = getInputStream(args)) {
            hexdump(input);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static InputStream getInputStream(final String[] args) throws IOException {
        if (args.length > 0) {
            return new FileInputStream(args[0]);
        }
        return System.in;
    }

    private static void hexdump(final InputStream input) throws IOException {
        final byte[] buffer = new byte[BYTES_PER_LINE];
        int offset = 0;
        int bytesRead;

        while ((bytesRead = input.read(buffer)) != -1) {
            printLine(buffer, bytesRead, offset);
            offset += bytesRead;
        }
    }

    private static void printLine(final byte[] buffer, final int length, final int offset) {
        // Print offset (6 hex digits)
        System.out.printf("%06x ", offset);

        // Print first 8 bytes as hex pairs
        for (int i = 0; i < 8; i++) {
            if (i < length) {
                System.out.printf("%02x ", buffer[i] & 0xFF);
            } else {
                System.out.print("   ");
            }
        }

        // Extra space between two groups
        System.out.print(" ");

        // Print next 8 bytes as hex pairs
        for (int i = 8; i < BYTES_PER_LINE; i++) {
            if (i < length) {
                System.out.printf("%02x ", buffer[i] & 0xFF);
            } else {
                System.out.print("   ");
            }
        }

        // Print ASCII representation
        for (int i = 0; i < length; i++) {
            byte b = buffer[i];
            if (b >= 32 && b < 127) {
                System.out.print((char) b);
            } else {
                System.out.print('.');
            }
        }

        System.out.println();
    }
}
