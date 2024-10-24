package hr.avrbanac.utility.converters;

import hr.avrbanac.utility.Converter;

public class CharacterConverter implements Converter<Character> {

    @Override
    public Character convert(final Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof Character character) {
            return character;
        }

        if (source instanceof String string
                && string.length() == 1) {

            try {
                return (string).charAt(0);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }
}
