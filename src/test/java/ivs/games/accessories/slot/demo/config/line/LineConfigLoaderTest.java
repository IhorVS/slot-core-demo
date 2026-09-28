package ivs.games.accessories.slot.demo.config.line;

import ivs.games.accessories.slot.demo.config.YamlConfigReader;
import ivs.games.accessories.slot.demo.config.YamlReaderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests loading and validating raw winning-line configurations from YAML resources.
 */
class LineConfigLoaderTest {

    private static final String PATH = "config/line/";

    private final LineConfigLoader lineConfigLoader =
            new LineConfigLoader(new YamlConfigReader(YamlReaderFactory.INSTANCE));

    /**
     * Verifies that all configured lines are loaded in their original order.
     *
     * <pre>
     * lines:
     *   - [0, 0, 0, 0, 0]
     *   - [1, 1, 1, 1, 1]
     *   - [2, 2, 2, 2, 2]
     * </pre>
     */
    @Test
    void loadsConfiguredLines() {
        // lines-ok.yaml contains three horizontal lines for a 5x3 field.
        List<List<Integer>> lines =
                lineConfigLoader.load(PATH + "lines-ok.yaml");

        assertEquals(
                List.of(
                        List.of(0, 0, 0, 0, 0),
                        List.of(1, 1, 1, 1, 1),
                        List.of(2, 2, 2, 2, 2)
                ),
                lines
        );
    }

    /**
     * Verifies that loading a nonexistent classpath resource fails.
     */
    @Test
    void rejectsMissingResource() {
        assertThrows(IllegalStateException.class, () -> lineConfigLoader.load(PATH + "missing-resource.yaml"));
    }

    /**
     * Verifies that the root {@code lines} property is required.
     */
    @Test
    void rejectsMissingLinesProperty() {
        String fileName = PATH + "missing-lines.yaml";

        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> lineConfigLoader.load(PATH + "missing-lines.yaml")
        );

        assertEquals(
                "'lines' must be a list in %s".formatted(fileName),
                exception.getMessage()
        );
    }

    /**
     * Verifies that the root {@code lines} property must contain a YAML list.
     */
    @Test
    void rejectsLinesPropertyThatIsNotAList() {
        String fileName = PATH + "lines-not-list.yaml";

        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> lineConfigLoader.load(fileName)
        );

        assertEquals(
                "'lines' must be a list in %s".formatted(fileName),
                exception.getMessage()
        );
    }

    /**
     * Verifies that each configured line must be represented by a non-empty list.
     *
     * @param fileName YAML resource containing an invalid line structure
     */
    @ParameterizedTest
    @ValueSource(strings = {"line-not-list.yaml", "empty-line.yaml"})
    void rejectsInvalidLineStructure(String fileName) {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> lineConfigLoader.load(PATH + fileName)
        );

        assertEquals(
                "Line #1 must be a non-empty list",
                exception.getMessage()
        );
    }

    /**
     * Verifies that every column entry must be a non-negative integer representing an index in that column.
     *
     * @param fileName YAML resource containing a string, fractional or negative column index
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "string-row.yaml",
            "fractional-row.yaml",
            "negative-row.yaml"
    })
    void rejectsInvalidIndexInColumn(String fileName) {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> lineConfigLoader.load(PATH + fileName)
        );

        assertEquals(
                "Line 0, column 1: indexInColumn must be a non-negative integer",
                exception.getMessage()
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
        Class<? extends RuntimeException> expectedType =
                fileName == null
                        ? NullPointerException.class
                        : IllegalArgumentException.class;

        RuntimeException exception = assertThrows(
                expectedType,
                () -> lineConfigLoader.load(fileName)
        );

        assertEquals(
                "Resource name must not be null or blank",
                exception.getMessage()
        );
    }
}