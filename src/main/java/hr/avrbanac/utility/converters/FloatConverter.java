package hr.avrbanac.utility.converters;

import hr.avrbanac.utility.Converter;
import hr.avrbanac.utility.Utility;

public class FloatConverter implements Converter<Float> {

    @Override
    public Float convert(final Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof Float floatValue) {
            return floatValue;
        }

        if (source instanceof String string
                && Utility.isDecimalNumber(string)) {

            try {
                return Float.parseFloat(string);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }
}
