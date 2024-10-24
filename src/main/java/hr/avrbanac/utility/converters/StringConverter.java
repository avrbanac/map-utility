package hr.avrbanac.utility.converters;

import hr.avrbanac.utility.Converter;

public class StringConverter implements Converter<String> {

    @Override
    public String convert(final Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof String string) {
            return string;
        }

        return source.toString();
    }
}
