package hr.avrbanac.utility;

import hr.avrbanac.utility.converters.*;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapDecorator {
    private static final Logger LOG = LoggerFactory.getLogger(MapDecorator.class);
    private static final Map<Class<?>, Converter<?>> converters = new HashMap<>();

    MapDecorator() {
        converters.put(Boolean.class,   new BooleanConverter());
        converters.put(Character.class, new CharacterConverter());
        converters.put(Byte.class,      new ByteConverter());
        converters.put(Short.class,     new ShortConverter());
        converters.put(Integer.class,   new IntegerConverter());
        converters.put(Long.class,      new LongConverter());
        converters.put(Float.class,     new FloatConverter());
        converters.put(Double.class,    new DoubleConverter());
        converters.put(String.class,    new StringConverter());
        converters.put(Map.class,       new MapConverter());
        converters.put(List.class,      new ListConverter());
    }

    /**
     * Add custom converter if needed. This method is not synchronized by design. Intended CTOR usage is in bootstrap
     * phase, no need to interlock this method with {@link #convert(Object, Class)}. This would impact performance.
     * @param clazz custom converter return class
     * @param converter custom converter should implement {@link Converter}
     */
    public void addConverter(
            final Class<?> clazz,
            final Converter<?> converter) {

        converters.put(clazz, converter);
    }

    /**
     * Method converts source to clazz type. This is unchecked cast (performance reasons).
     * @param source {@link Object} to be converted into clazz type
     * @param clazz {@link Class} target type
     * @param defaultValue type T object that will be returned if underlying {@link #convert(Object, Class)} returns null
     * @return object value as target type
     * @param <T> type of the target
     */
    public <T> T convert(
            final Object source,
            final Class<T> clazz,
            @NotNull final T defaultValue) {

        T result = convert(source, clazz);
        return result != null ? result : defaultValue;
    }

    /**
     * Method converts source to clazz type. This is unchecked cast (performance reasons).
     * @param source {@link Object} to be converted into clazz type
     * @param clazz {@link Class} target type
     * @return object value as target type
     * @param <T> type of the target
     */
    @SuppressWarnings("unchecked")
    public <T> T convert(final Object source, final Class<T> clazz) {
        Converter<?> converter = converters.get(clazz);
        if (converter == null) {
            LOG.warn("No converter found for class {}, returning null", clazz);
            return null;
        }

        return (T) converter.convert(source);
    }

    /**
     * Extracts object from nested map with path defined in dot notation.
     * @param source {@link Map} multiple nested string-object map from which object needs to be extracted
     * @param dotPath {@link String} dot notated path, e.g. map1.map2.list[n].map3.key
     * @param clazz {@link Class} target type
     * @param defaultValue type T object that will be returned if underlying {@link #extract(Map, String, Class)} returns null
     * @return object value as target type
     * @param <T> type of the target
     */
    public <T> T extract(
            final Map<String, Object> source,
            final String dotPath,
            final Class<T> clazz,
            @NotNull final T defaultValue) {

        T result = extract(source, dotPath, clazz);
        return result != null ? result : defaultValue;
    }

    /**
     * Extracts object from nested map with path defined in dot notation.
     * @param source {@link Map} multiple nested string-object map from which object needs to be extracted
     * @param dotPath {@link String} dot notated path, e.g. map1.map2.list[n].map3.key
     * @param clazz {@link Class} target type
     * @return object value as target type
     * @param <T> type of the target
     */
    public <T> T extract(
            final Map<String, Object> source,
            final String dotPath,
            final Class<T> clazz) {

        String[] tokens = Utility.getValidTokens(dotPath);
        if (tokens == null || tokens.length == 0) {
            return null;
        }

        if (tokens.length > 1) {
            Object node = source;
            for (int i = 0; true; i++) {
                if (node == null) {
                    return null;
                } else if (i == tokens.length) {
                    return convert(node, clazz);
                } else if (node instanceof Map<?, ?> map) {
                    node = tokens[i].endsWith("]") ? getNodeFromList(map, tokens[i]) : map.get(tokens[i]);
                } else {
                    return null;
                }
            }
        } else {
            return convert(source.get(tokens[0]), clazz);
        }
    }

    /**
     * Returns builder instance. Separate instances should be used by each thread in multithreaded environment. No locking (performance).
     * @return {@link Builder} instance of map decorator builder
     */
    public Builder extractBuilder() {
        return new Builder();
    }

    private Object getNodeFromList(
            final Map<?, ?> map,
            final String dotToken) {

        String[] keyWithIndex = dotToken.substring(0, dotToken.length() - 1).split("\\[");
        if (keyWithIndex.length == 2 && Utility.isWholeNumber(keyWithIndex[1])) {
            Object tmp = map.get(keyWithIndex[0]);
            if (tmp instanceof List<?> list) {
                try {
                    int index = Integer.parseInt(keyWithIndex[1]);
                    if (index < list.size() && index >= 0) {
                        return list.get(index);
                    } else if (index < 0 && index >= -list.size()) {
                        return list.get(list.size() + index);
                    }
                } catch (NumberFormatException e) {
                    LOG.warn("Index conversion failed or index out of bounds: [index: {}, size: {}].",
                            keyWithIndex[1],
                            list.size());
                }
            }
        }

        return null;
    }

    /**
     * This is not thread safe! Each thread should use its own builder (performance).
     */
    public class Builder {
        private Map<String, Object> source;

        private Builder() {}

        /**
         * Entry point method for builder usage.
         * @param newSource {@link Map} source from which objects need to be extracted. For each extraction, call from method again.
         * @return {@link Builder} current builder instance
         */
        public Builder from(final Map<String, Object> newSource) {
            source = newSource;
            return this;
        }

        /**
         * Returns map node one path token lower. This can be chained as many times as needed. Unchecked conversion (performance reasons).
         * @param pathToken {@link String} single path token (one level in nested chain)
         * @return {@link Builder} current builder instance
         */
        @SuppressWarnings("unchecked")
        public Builder mapNode(final String pathToken) {
            if (source == null) {
                return null;
            }

            source = convert(source.get(pathToken), Map.class);
            return this;
        }

        /**
         * Exit point method for builder usage. This will end builder method chain.
         * @param endPathToken {@link String} last path token
         * @param clazz {@link Class} type of the return value
         * @return object of defined type by clazz
         * @param <T> target type of the return value
         */
        public <T> T value(
                final String endPathToken,
                final Class<T> clazz) {

            if (source == null) {
                return null;
            }

            return convert(source.get(endPathToken), clazz);
        }

        /**
         * Exit point method for builder usage. This will end builder method chain.
         * @param endPathToken {@link String} last path token
         * @param clazz {@link Class} type of the return value
         * @param defaultValue type T object that will be returned if underlying {@link #value(String, Class)} returns null
         * @return object of defined type by clazz
         * @param <T> target type of the return value
         */
        public <T> T value(
                final String endPathToken,
                final Class<T> clazz,
                final T defaultValue) {

            if (source == null) {
                return defaultValue;
            }

            return convert(source.get(endPathToken), clazz, defaultValue);
        }

        public boolean booleanValue(final String endPathToken) {
            Boolean result = value(endPathToken, Boolean.class);
            return result != null ? result : false;
        }
        public char charValue(final String endPathToken) {
            Character result = value(endPathToken, Character.class);
            return result != null ? result : '\0';
        }
        public byte byteValue(final String endPathToken) {
            Byte result = value(endPathToken, Byte.class);
            return result != null ? result : 0;
        }
        public short shortValue(final String endPathToken) {
            Short result = value(endPathToken, Short.class);
            return result != null ? result : 0;
        }
        public int intValue(final String endPathToken) {
            Integer result = value(endPathToken, Integer.class);
            return result != null ? result : 0;
        }
        public long longValue(final String endPathToken) {
            Long result = value(endPathToken, Long.class);
            return result != null ? result : 0;
        }
        public float floatValue(final String endPathToken) {
            Float result = value(endPathToken, Float.class);
            return result != null ? result : 0F;
        }
        public double doubleValue(final String endPathToken) {
            Double result = value(endPathToken, Double.class);
            return result != null ? result : 0D;
        }
        public String stringValue(final String endPathToken) {
            String result = value(endPathToken, String.class);
            return result != null ? result : "";
        }

        /**
         * Returns {@link Map} even if object does not exist (empty map as result).
         * Somewhat problematic method. It can return different K, V types from expected ones.
         * @param endPathToken {@link String} last path token
         * @return {@link Map} of K, V (maybe different from expected)
         * @param <K> key type
         * @param <V> value type
         */
        @SuppressWarnings("unchecked")
        public <K, V> Map<K, V> mapValue(final String endPathToken) {
            Map<K, V> result = value(endPathToken, Map.class);
            return result != null ? result : new HashMap<>();
        }

        /**
         * Returns {@link List} even if object does not exist (empty list as result).
         * Somewhat problematic method. It can return different T type from expected one.
         * @param endPathToken {@link String} last path token
         * @return {@link List} of T type (maybe different from expected)
         * @param <T> collection element type
         */
        @SuppressWarnings("unchecked")
        public <T> List<T> listValue(final String endPathToken) {
            List<T> result = value(endPathToken, List.class);
            return result != null ? result : new ArrayList<>();
        }
    }
}
