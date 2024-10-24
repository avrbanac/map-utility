package hr.avrbanac.utility.converters;

import hr.avrbanac.utility.Converter;

import java.util.List;

public class ListConverter implements Converter<List<?>> {

    @Override
    public List<?> convert(final Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof List<?> list) {
            return list;
        }

        return null;
    }
}
