package com.arvatosystems.t9t.base;

/**
 * Workaround to throw unchecked exceptions and avoid eclipse' dead code issue.
 */
public final class T9tSneakyException {

    /**
     * Private, do not in
     */
    private T9tSneakyException() {
        // empty to avoid instantiation
    }

    /**
     * Throw exception and return a value that will never be returned. Just to avoid dead code warnings.
     *
     * @param exc the {@link Throwable} to throw sneakily.
     * @return always null, or not? ;-)
     */
    public static RuntimeException throwIt(final Throwable exc) {
        T9tSneakyException.<RuntimeException>throwUnchecked(exc);
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwUnchecked(final Throwable exc) throws T {
        throw (T) exc;
    }

}
