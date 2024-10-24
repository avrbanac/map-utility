package hr.avrbanac.utility.converters;

import hr.avrbanac.utility.Converter;

import java.util.Map;

public class MapConverter implements Converter<Map<?, ?>> {

    @Override
    public Map<?, ?> convert(final Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof Map<?, ?> map) {
           return map;
        }

        return null;
    }
}
