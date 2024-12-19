package com.arvatosystems.t9t.zkui.filters;

import de.jpaw.bonaparte.core.BonaPortable;
import jakarta.annotation.Nonnull;

import java.util.function.Predicate;

public interface IResultTextFilter<DWT extends BonaPortable> {

    /**
     * Returns a filter for the given filterText.
     *
     * @param filterText
     * @return {@link Predicate<DWT>}
     */
    Predicate<DWT> getFilter(@Nonnull String filterText);
}
