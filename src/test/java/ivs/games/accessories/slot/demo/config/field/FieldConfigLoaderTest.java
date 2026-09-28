package ivs.games.accessories.slot.demo.config.field;

import ivs.games.accessories.slot.demo.config.YamlConfigReader;
import ivs.games.accessories.slot.demo.config.YamlReaderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests loading and validating raw slot-field configurations from YAML resources.
 */
class FieldConfigLoaderTest {

    private static final String PATH = "config/field/";

    private final FieldConfigLoader loader = new FieldConfigLoader(new YamlConfigReader(YamlReaderFactory.INSTANCE));

    /**
     * Verifies that the field height and all configured reel strips are loaded
     * in their original order.
     *
     * <pre>
     * fieldHeight: 3
     *
     * reels:
     *   - [A, K, Q, WILD]
     *   - [K, Q, A, SCT]
     *   - [Q, A, K, MUL]
     * </pre>
     */
    @Test
    void loadsFieldConfig() {
        RawFieldConfig result = loader.load(PATH + "field-ok.yaml");

        assertEquals(
                new RawFieldConfig(
                        3,
                        List.of(
                                List.of("A", "K", "Q", "WILD"),
                                List.of("K", "Q", "A", "SCT"),
                                List.of("Q", "A", "K", "MUL")
                        )
                ),
                result
        );
    }

    /**
     * Verifies that loading a nonexistent classpath resource fails and preserves
     * the underlying resource-reading error as the cause.
     */
    @Test
    void rejectsMissingResource() {
        String fileName = PATH + "missing-resource.yaml";

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> loader.load(fileName)
        );

        assertEquals(
                "Could not load config file: %s".formatted(fileName),
                exception.getMessage()
        );
        assertEquals(
                "Cannot read configuration resource: %s".formatted(fileName),
                exception.getCause().getMessage()
        );
    }

    /**
     * Verifies that {@code fieldHeight} is required and must be a positive integer.
     *
     * @param resourceName YAML resource with a missing, zero, negative or noninteger field height
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "missing-field-height.yaml",
            "zero-field-height.yaml",
            "negative-field-height.yaml",
            "string-field-height.yaml"
    })
    void rejectsInvalidFieldHeight(String resourceName) {
        assertLoadFailure(
                resourceName,
                "'fieldHeight' must be a positive integer"
        );
    }

    /**
     * Verifies that {@code reels} is required and must contain a non-empty list.
     *
     * @param resourceName YAML resource with a missing or invalid reels property
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "missing-reel-items.yaml",
            "reel-items-not-list.yaml",
            "empty-reel-items.yaml"
    })
    void rejectsInvalidReelItemsProperty(String resourceName) {
        assertLoadFailure(
                resourceName,
                "'reels' must be a non-empty list"
        );
    }

    /**
     * Verifies that every reel is represented by a list containing enough symbols
     * to fill the configured field height.
     *
     * @param resourceName YAML resource containing an invalid reel
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "reel-not-list.yaml",
            "reel-too-short.yaml"
    })
    void rejectsInvalidReel(String resourceName) {
        assertLoadFailure(
                resourceName,
                "Reel #1 must contain at least 3 symbols"
        );
    }

    /**
     * Verifies that every reel position contains a non-blank string symbol.
     *
     * @param resourceName YAML resource containing a non-string or blank symbol
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "non-string-symbol.yaml",
            "blank-symbol.yaml"
    })
    void rejectsInvalidSymbol(String resourceName) {
        assertLoadFailure(
                resourceName,
                "Reel 0, position 1: symbol must be a non-blank string"
        );
    }

    /**
     * Verifies that the classpath resource name must not be {@code null}, empty or blank.
     *
     * @param fileName invalid resource name
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\r", "\n"})
    void rejectsNullOrBlankResourceName(String fileName) {
        assertThrows(RuntimeException.class, () -> loader.load(fileName));
    }

    /*
     * Loads an invalid configuration and verifies the loader error together
     * with the validation error preserved as its cause.
     */
    private void assertLoadFailure(
            String resourceName,
            String causeMessage
    ) {
        String fileName = PATH + resourceName;

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> loader.load(fileName)
        );

        assertEquals(
                "Could not load config file: %s".formatted(fileName),
                exception.getMessage()
        );
        assertInstanceOf(
                IllegalArgumentException.class,
                exception.getCause()
        );
        assertEquals(
                causeMessage,
                exception.getCause().getMessage()
        );
    }
}