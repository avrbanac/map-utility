package hr.avrbanac.utility.converters;

import hr.avrbanac.utility.Converter;
import hr.avrbanac.utility.Utility;

public class DoubleConverter implements Converter<Double> {

    @Override
    public Double convert(final Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof Double doubleValue) {
            return doubleValue;
        }

        if (source instanceof String string
                && Utility.isDecimalNumber(string)) {

            try {
                return Double.parseDouble(string);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }
}
