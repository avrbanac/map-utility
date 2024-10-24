package hr.avrbanac.utility.converters;

import hr.avrbanac.utility.Converter;
import hr.avrbanac.utility.Utility;

public class IntegerConverter implements Converter<Integer> {

    @Override
    public Integer convert(final Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof Integer integer) {
            return integer;
        }

        if (source instanceof String string
                && string.length() <= 11
                && Utility.isWholeNumber(string)) {

            try {
                return Integer.parseInt(string);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }
 }
