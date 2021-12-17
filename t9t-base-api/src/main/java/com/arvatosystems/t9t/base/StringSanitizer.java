package com.arvatosystems.t9t.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.converter.DataConverterAbstract;
import de.jpaw.bonaparte.core.DataConverter;
import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;

public final class StringSanitizer extends DataConverterAbstract<String, AlphanumericElementaryDataItem>
  implements DataConverter<String, AlphanumericElementaryDataItem> {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringSanitizer.class);

    /** An internal lookup field, which allows to perform the check of a single character in O(1) by array lookup. */
    private final boolean[] isForbidden = new boolean[128];
    private final Character replacementCharacter;

    public StringSanitizer(final String forbiddenCharacters, final Character replacementCharacter) {
        this.replacementCharacter = replacementCharacter;
        for (int i = 0; i < 128; ++i) {
            isForbidden[i] = false;
        }
        for (int i = 0; i < forbiddenCharacters.length(); ++i) {
            final char c = forbiddenCharacters.charAt(i);
            if (c >= 0 && c < 128)
                isForbidden[(int)c] = true;
        }
    }

    @Override
    public String convert(final String oldValue, final AlphanumericElementaryDataItem meta) {
        if (oldValue != null && !meta.getAllowControlCharacters() && meta.getRegexp() == null) {
            // a non-null string, which has neither control characters allowed, nor a specific regular expression
            for (int i = 0; i < oldValue.length(); ++i) {
                final char c = oldValue.charAt(i);
                if (c >= 0 && c < 128 && isForbidden[(int)c]) {
                    LOGGER.warn("Forbidden character {} found in field {}", (int)c, meta.getName());
                    if (replacementCharacter != null) {
                        // character will be silently replaced
                        return convertString(oldValue, i);
                    } else {
                        // reject the request
                        throw new T9tException(T9tException.ILLEGAL_CHARACTER, meta.getName());
                    }
                }
            }
        }
        // unchanged string returned
        return oldValue;
    }

    /** Converts a string with forbidden characters. */
    private String convertString(final String oldValue, int conversionStart) {
        final StringBuilder buff = new StringBuilder(oldValue.length());
        // initial characters have been found to be OK, use a fast loop here
        for (int i = 0; i < conversionStart; ++i) {
            buff.append(oldValue.charAt(i));
        }
        for (int i = conversionStart; i < oldValue.length(); ++i) {
            final char c = oldValue.charAt(i);
            if (c >= 0 && c < 128 && isForbidden[(int)c]) {
                buff.append(replacementCharacter);
            } else {
                buff.append(c);
            }
        }
        return buff.toString();
    }
}
