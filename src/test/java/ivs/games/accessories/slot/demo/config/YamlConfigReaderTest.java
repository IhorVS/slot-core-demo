package ivs.games.accessories.slot.demo.config;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YamlConfigReaderTest {

    private static final String PATH = "config/reader/";
    private final YamlConfigReader reader = new YamlConfigReader(YamlReaderFactory.INSTANCE);

    @Test
    void readsMappingFromClasspathResource() {
        // Input: src/test/resources/config/valid.yaml contains a name
        // and two configured lines.
        // ---------------------
        // name: demo
        // lines:
        //  - [0, 1]
        //  - [1, 0]
        Map<String, Object> result = reader.read(PATH + "valid.yaml");

        // Expected: the reader returns a map with the parsed values.
        assertEquals("demo", result.get("name"));
        assertEquals(
                List.of(List.of(0, 1), List.of(1, 0)),
                result.get("lines")
        );
    }

    @Test
    void rejectsBlankResourceName() {
        // Input: null, an empty string, or strings containing only whitespace.
        // Expected: null causes NullPointerException; blank strings cause
        // IllegalArgumentException. No resource is opened.
        assertThrows(NullPointerException.class, () -> reader.read(null));
        assertThrows(IllegalArgumentException.class, () -> reader.read("  "));
        assertThrows(IllegalArgumentException.class, () -> reader.read(""));
        assertThrows(IllegalArgumentException.class, () -> reader.read("\t"));
        assertThrows(IllegalArgumentException.class, () -> reader.read("\n"));
    }

    @Test
    void rejectsMissingResource() {
        // Input: a valid resource path that does not exist on the classpath.
        // Expected: IllegalArgumentException because the resource cannot be found.
        assertThrows(
                IllegalStateException.class,
                () -> reader.read(PATH + "missing.yaml")
        );
    }

    @Test
    void rejectsDocumentWithoutRootMapping() {
        // Input: valid YAML containing a list directly at the root,
        // without a top-level key such as "lines".
        // Expected: IllegalArgumentException because the reader
        // requires a root mapping of keys to values.
        assertThrows(
                IllegalStateException.class,
                () -> reader.read(PATH + "list-root.yaml")
        );
    }

    @Test
    void rejectsNonStringRootKey() {
        // Input: a root mapping with a numeric key, such as "1: value".
        // Expected: IllegalArgumentException because root keys must be strings.
        assertThrows(
                IllegalStateException.class,
                () -> reader.read(PATH + "numeric-key.yaml")
        );
    }

    @Test
    void rejectsDuplicateKeys() {
        // Input: a YAML mapping that defines the same key twice.
        // Expected: YAMLException because duplicate keys are disabled.
        assertThrows(
                IllegalStateException.class,
                () -> reader.read(PATH + "duplicate-keys.yaml")
        );
    }
}