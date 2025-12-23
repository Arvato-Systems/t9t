package com.arvatosystems.t9t.zkui.converters.grid;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.BiConsumer;

import org.zkoss.util.resource.Labels;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.Constants;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class NumberConverter<T extends Number> implements IItemConverter<T> {
    /** The format to use for consistent formatting with fixed decimal places */
    private final String format;
    private final BiConsumer<DecimalFormat, T> minFractionSetter;

    /**
     * Creates a new NumberConverter with the specified decimal format and a lambda to optionally configure minimum decimal places of the DecimalFormat.
     */
    public NumberConverter(@Nonnull final String format, @Nullable final BiConsumer<DecimalFormat, T> minFractionSetter) {
        this.format = format;
        this.minFractionSetter = minFractionSetter;
    }

    public NumberConverter(@Nonnull final FieldDefinition meta, @Nonnull final String defaultFormat, @Nullable final BiConsumer<DecimalFormat, T> defaultMinFractionSetter) {
        format = Labels.getLabel("com.decimal.format", defaultFormat);
        final String decimalsStr = T9tUtil.getFieldProperty(meta, Constants.UiFieldProperties.DECIMALS);
        final Integer decimals = decimalsStr != null && decimalsStr.length() == 1 && Character.isDigit(decimalsStr.charAt(0))
            ? Character.getNumericValue(decimalsStr.charAt(0))
            : null;
        minFractionSetter = decimals != null
                ? (df, value) -> df.setMinimumFractionDigits(decimals)
                : defaultMinFractionSetter;
    }

    /**
     * Formats a numeric value using the predetermined decimal format.
     *
     * @param value the numeric value to format
     * @param wholeDataObject the complete data object containing the field (may be null)
     * @param fieldName the name of the field being formatted
     * @param meta the metadata definition for the field
     * @return the formatted string representation of the numeric value
     */
    @Override
    public String getFormattedLabel(final T value, final BonaPortable wholeDataObject, final String fieldName, final FieldDefinition meta) {
        final Locale userLocale = ApplicationSession.get().getUserLocale();
        final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(userLocale);
        df.applyPattern(format);
        if (minFractionSetter != null) {
            minFractionSetter.accept(df, value);
        }
        return df.format(value.doubleValue());
    }

    /**
     * Indicates that numeric values should be right-aligned in the UI.
     *
     * @return {@code true} to indicate right alignment is preferred for numeric display
     */
    @Override
    public boolean isRightAligned() {
        return true;
    }
}
