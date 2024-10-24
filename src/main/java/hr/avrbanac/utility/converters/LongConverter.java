package hr.avrbanac.utility.converters;

import hr.avrbanac.utility.Converter;
import hr.avrbanac.utility.Utility;

public class LongConverter implements Converter<Long> {

    @Override
    public Long convert(final Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof Long longValue) {
            return longValue;
        }

        if (source instanceof String string
                && string.length() <= 21
                && Utility.isWholeNumber(string)) {

            try {
                return Long.parseLong(string);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;

    }
}
