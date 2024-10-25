package hr.avrbanac.utility.converters;

import hr.avrbanac.utility.Converter;

public class ShortConverter implements Converter<Short> {

    @Override
    public Short convert(final Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof Short shortValue) {
            return shortValue;
        }

        if (source instanceof String string) {
            try {
                return Short.parseShort(string);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }
}
