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
package com.arvatosystems.t9t.ai;

import java.util.ArrayList;
import java.util.List;

import de.jpaw.bonaparte.pojos.meta.ClassDefinition;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.Multiplicity;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public final class T9tAiTools {
    private T9tAiTools() { }

    /** Returns a description of a class from meta data. */
    public static String getToolDescription(@Nonnull final ClassDefinition cd) {
        if (cd.getRegularComment() != null) {
            // if there is a regular comment, use it
            return cd.getRegularComment();
        }
        // return the stripped javadoc
        return stripJavadoc(cd.getJavaDoc());
    }

    public static String stripJavadoc(@Nullable final String javadoc) {
        if (javadoc == null) {
            return "";
        }
        final int len = javadoc.length();
        final StringBuilder sb = new StringBuilder(len);
        int i = skipSpacesAndStars(javadoc, 3, len, true);
        // loop. End if the previous was a '*' and the current is a '/'
        while (i < len) {
            final char c = javadoc.charAt(i);
            if (c == '/' && javadoc.charAt(i - 1) == '*') {
                break;  // we're done!
            }
            // transfer until new line, then again skip initial spaces and stars
            sb.append(c);
            if (c == '\n') {
                i = skipSpacesAndStars(javadoc, i + 1, len, false);
            } else {
                ++i;
            }
        }
        return sb.toString();
    }

    private static int skipSpacesAndStars(final String javadoc, int pos, final int len, final boolean alsoNewline) {
        while (pos < len && (javadoc.charAt(pos) == ' ' || javadoc.charAt(pos) == '*'
                || (alsoNewline && (javadoc.charAt(pos) == '\n' || javadoc.charAt(pos) == '\r')))) {
            ++pos;
        }
        return pos;
    }

    /** Returns the list of required parameters. */
    public static List<String> buildRequiredFromFields(final List<FieldDefinition> fields) {
        final List<String> required = new ArrayList<>(fields.size());
        for (final FieldDefinition field : fields) {
            if (field.getMultiplicity() == Multiplicity.SCALAR ? field.getIsRequired() : field.getIsAggregateRequired()) {
                required.add(field.getName());
            }
        }
        return required;
    }
}
