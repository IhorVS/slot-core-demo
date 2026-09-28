package ivs.games.accessories.slot.demo.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads a YAML configuration document from a classpath resource.
 *
 * <p>The reader loads the YAML document and converts its top-level mapping into
 * a map with string keys. The original key order is preserved.</p>
 *
 * <p>The reading process is:</p>
 *
 * <pre>
 * classpath resource
 *         |
 *         v
 *    InputStream
 *         |
 *         v
 *   YamlReaderFactory
 *         |
 *         v
 *    YAML document
 *         |
 *         v
 * Map&lt;String, Object&gt;
 * </pre>
 *
 * <p>A new SnakeYAML reader is created for every read operation. This prevents
 * parser state from being shared between independently loaded configuration
 * resources.</p>
 *
 * <p>The reader validates only the common top-level YAML structure. Validation
 * of configuration-specific keys and nested values is performed by specialized
 * configuration loaders.</p>
 */
@RequiredArgsConstructor
public class YamlConfigReader {

    @NonNull
    private final YamlReaderFactory yamlReaderFactory;

    /**
     * Reads a YAML document from the specified classpath resource.
     *
     * <p>The document must contain a top-level mapping with non-null values and
     * string keys. A {@link LinkedHashMap} is used to preserve their declaration
     * order.</p>
     *
     * @param fileName classpath resource path
     * @return top-level YAML mapping with string keys
     * @throws NullPointerException     if {@code fileName} is {@code null}
     * @throws IllegalArgumentException if {@code fileName} is blank
     * @throws IllegalStateException    if the resource cannot be found or read,
     *                                  the YAML cannot be parsed, or the document
     *                                  has an invalid top-level structure
     */
    public Map<String, Object> read(String fileName) {
        Validate.notBlank(fileName, "Resource name must not be null or blank");

        ClassLoader classLoader = getClass().getClassLoader();

        try (InputStream input = classLoader.getResourceAsStream(fileName)) {
            /*
             * A new YAML reader is used for every operation so that parser state
             * is not shared between configuration resources.
             */
            Validate.isTrue(input != null, "File not found");

            Map<String, Object> result = new LinkedHashMap<>();
            Map<?, ?> content = getContent(input);
            for (Map.Entry<?, ?> entry : content.entrySet()) {
                String key = validateEntryAndGetKey(entry);
                result.put(key, entry.getValue());
            }

            return result;

        } catch (Exception exception) {
            throw new IllegalStateException("Cannot read configuration resource: %s".formatted(fileName), exception);
        }
    }

    private static String validateEntryAndGetKey(Map.Entry<?, ?> entry) {
        if (!(entry.getKey() instanceof String key)) {
            throw new IllegalArgumentException("Configuration must use string keys");
        }

        Validate.notNull(
                entry.getValue(),
                "No value found for key '%s'. Check the YAML structure and indentation.",
                key
        );

        return key;
    }

    private Map<?, ?> getContent(InputStream input) {
        Object document = yamlReaderFactory.create().load(input);

        if (!(document instanceof Map<?, ?> content)) {
            throw new IllegalArgumentException("Configuration must contain a mapping");
        }
        return content;
    }
}
