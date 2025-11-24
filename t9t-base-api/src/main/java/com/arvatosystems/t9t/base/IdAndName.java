package com.arvatosystems.t9t.base;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public record IdAndName(@Nonnull String id, @Nullable String name) {
    /** Returns an IdAndName parsed from a string of the form "id:name". If no colon is present, name is null. */
    public static IdAndName of(@Nullable String pair) {
        if (pair == null) {
            return null;
        }
        final int colonPos = pair.indexOf(':');
        if (colonPos >= 0) {
            return new IdAndName(pair.substring(0, colonPos), pair.substring(colonPos + 1));
        } else {
            return new IdAndName(pair, null);
        }
    }
}
