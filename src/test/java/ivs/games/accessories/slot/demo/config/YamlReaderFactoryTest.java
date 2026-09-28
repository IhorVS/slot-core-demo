package ivs.games.accessories.slot.demo.config;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests YAML reader creation and parser restrictions configured by
 * {@link YamlReaderFactory}.
 */
class YamlReaderFactoryTest {

    /**
     * Verifies that every factory call creates an independent YAML reader.
     */
    @Test
    void createsNewYamlReaderForEveryCall() {
        Yaml first = YamlReaderFactory.INSTANCE.create();
        Yaml second = YamlReaderFactory.INSTANCE.create();

        assertNotSame(first, second);
    }

    /**
     * Verifies that a created reader parses a regular YAML mapping.
     *
     * <pre>
     * key: value
     * </pre>
     */
    @Test
    void createsReaderThatParsesMapping() {
        Yaml yaml = YamlReaderFactory.INSTANCE.create();

        Map<?, ?> result = yaml.load("key: value");

        assertEquals("value", result.get("key"));
    }

    /**
     * Verifies that a created reader rejects duplicate mapping keys.
     *
     * <pre>
     * key: first
     * key: second
     * </pre>
     */
    @Test
    void createsReaderThatRejectsDuplicateKeys() {
        Yaml yaml = YamlReaderFactory.INSTANCE.create();

        String document = """
                key: first
                key: second
                """;

        assertThrows(
                YAMLException.class,
                () -> yaml.load(document)
        );
    }
}