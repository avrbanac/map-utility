package hr.avrbanac.utility.converters;

import hr.avrbanac.utility.Converter;

import java.util.Arrays;

public class BooleanConverter implements Converter<Boolean> {
    private static final String[] TRUE_VALUES = {"1", "t", "y", "yes", "true", "ok"};
    private static final String[] FALSE_VALUES = {"0", "f", "n", "no", "false", "nok"};

    @Override
    public Boolean convert(final Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof Boolean booleanValue) {
            return booleanValue;
        }

        if (source instanceof String string
                && string.length() <= 5) {

            if (Arrays.stream(TRUE_VALUES).anyMatch(string::equalsIgnoreCase)) {
                return true;
            } else if (Arrays.stream(FALSE_VALUES).anyMatch(string::equalsIgnoreCase)) {
                return false;
            }
        }

        return null;
    }
}
