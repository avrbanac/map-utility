package hr.avrbanac.utility;

import hr.avrbanac.utility.converters.*;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapDecorator {
    private static final Logger LOG = LoggerFactory.getLogger(MapDecorator.class);
    private static final Map<Class<?>, Converter<?>> converters = new HashMap<>();

    MapDecorator() {
        converters.put(Boolean.class, new BooleanConverter());
        converters.put(Character.class, new CharacterConverter());
        converters.put(Byte.class, new ByteConverter());
        converters.put(Short.class, new ShortConverter());
        converters.put(Integer.class, new IntegerConverter());
        converters.put(Long.class, new LongConverter());
        converters.put(Float.class, new FloatConverter());
        converters.put(Double.class, new DoubleConverter());
        converters.put(String.class, new StringConverter());
        converters.put(Map.class, new MapConverter());
        converters.put(List.class, new ListConverter());
    }

    /**
     * Add custom converter if needed. This method is not synchronized by design. Intended usage is in bootstrap phase, no need to interlock
     * this method with {@link #convert(Object, Class)}. This would impact performance.
     * @param clazz custom converter return class
     * @param converter custom converter should implement {@link Converter}
     */
    public void addConverter(
            final Class<?> clazz,
            final Converter<?> converter) {

        converters.put(clazz, converter);
    }

    public <T> T convert(
            final Object source,
            final Class<T> clazz,
            @NotNull final T defaultValue) {

        T result = convert(source, clazz);
        return result != null ? result : defaultValue;
    }

    @SuppressWarnings("unchecked")
    public <T> T convert(final Object source, final Class<T> clazz) {
        Converter<?> converter = converters.get(clazz);
        if (converter == null) {
            LOG.warn("No converter found for class {}, returning null", clazz);
            return null;
        }

        return (T) converter.convert(source);
    }

    public <T> T extract(
            final Map<String, Object> source,
            final String dotPath,
            final Class<T> clazz,
            @NotNull final T defaultValue) {

        T result = extract(source, dotPath, clazz);
        return result != null ? result : defaultValue;
    }

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

    public Builder extractBuilder() {
        return new Builder();
    }

    private Map<?, ?> getNodeFromList(
            final Map<?, ?> map,
            final String dotToken) {

        String[] keyWithIndex = dotToken.substring(0, dotToken.length() - 1).split("\\[");
        if (keyWithIndex.length == 2 && Utility.isWholeNumber(keyWithIndex[1])) {
            Object tmp = map.get(keyWithIndex[0]);
            if (tmp instanceof List<?> list) {
                try {
                    int index = Integer.parseInt(keyWithIndex[1]);
                    if (index < list.size() && index >= 0) {
                        return (Map<?, ?>) list.get(index);
                    } else if (index < 0 && index >= -list.size()) {
                        return (Map<?, ?>) list.get(list.size() + index);
                    }
                } catch (NumberFormatException e) {
                    LOG.warn("Index conversion failed or index out of bounds: [index: {}, size: {}].", keyWithIndex[1], list.size());
                }
            }
        }

        return null;
    }

    /**
     * This is not thread safe! Each thread should use its own builder.
     */
    public class Builder {
        private Map<String, Object> source;

        private Builder() {}

        public Builder from(final Map<String, Object> newSource) {
            source = newSource;
            return this;
        }

        @SuppressWarnings("unchecked")
        public Builder mapNode(final String pathToken) {
            if (source == null) {
                return null;
            }

            source = convert(source.get(pathToken), Map.class);
            return this;
        }

        public <T> T value(
                final String endPathToken,
                final Class<T> clazz) {

            if (source == null) {
                return null;
            }

            return convert(source.get(endPathToken), clazz);
        }
    }
}
