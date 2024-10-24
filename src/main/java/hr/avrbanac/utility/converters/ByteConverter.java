package hr.avrbanac.utility.converters;

import hr.avrbanac.utility.Converter;

public class ByteConverter implements Converter<Byte> {

    @Override
    public Byte convert(final Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof Byte byteValue) {
            return byteValue;
        }

        if (source instanceof String string) {
            try {
                return Byte.parseByte(string);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }
}
