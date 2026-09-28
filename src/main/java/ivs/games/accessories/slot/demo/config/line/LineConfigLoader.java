package ivs.games.accessories.slot.demo.config.line;

import ivs.games.accessories.slot.demo.config.YamlConfigReader;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Loads raw field line definitions from a YAML resource.
 *
 * <p>Each configured line is represented by a list of zero-based row indices.
 * The position of an index in the list identifies the field column:</p>
 *
 * <pre>
 * lines:
 *   - [0, 0, 0, 0, 0]
 *   - [1, 1, 1, 1, 1]
 *   - [0, 1, 2, 1, 0]
 *
 * Raw representation:
 *
 * columns:   0  1  2  3  4
 *  ------------------------
 * line 0 -> [0, 0, 0, 0, 0]
 * line 1 -> [1, 1, 1, 1, 1]
 * line 2 -> [0, 1, 2, 1, 0]
 * </pre>
 *
 * <p>The loader validates the YAML structure and returns raw row indices.
 * A field line mapper converts them to {@code FieldLine} objects.</p>
 */
@RequiredArgsConstructor
public final class LineConfigLoader {

    private final YamlConfigReader reader;

    /**
     * Loads raw line definitions from a classpath resource.
     *
     * @param fileName resource path, for example "demoslot/lines.yaml"
     * @return row indices for each line, in their original order
     * @throws NullPointerException     if {@code fileName} is {@code null}
     * @throws IllegalArgumentException if {@code fileName} is blank or the line
     *                                  structure contains invalid values
     * @throws IllegalStateException    if the resource cannot be read
     */
    public List<List<Integer>> load(String fileName) {
        Validate.notBlank(fileName, "Resource name must not be null or blank");

        List<?> configuredLines = getConfiguredLines(fileName);

        return IntStream.range(0, configuredLines.size())
                .mapToObj(lineIndex -> getIndicesInColumns(configuredLines, lineIndex))
                .toList();
    }

    /*
     * Reads the required top-level list stored under the "lines" key.
     */
    private List<?> getConfiguredLines(String fileName) {
        Map<String, Object> document = reader.read(fileName);
        Object value = document.get("lines");

        if (!(value instanceof List<?> configuredLines)) {
            throw new IllegalArgumentException(
                    "'lines' must be a list in %s".formatted(fileName)
            );
        }
        return configuredLines;
    }

    /*
     * Reads and validates row indices for one configured line. Every index must
     * be a non-negative integer, and the line must contain at least one column.
     */
    private List<Integer> getIndicesInColumns(List<?> configuredLines, int lineIndex) {
        Object lineValue = configuredLines.get(lineIndex);
        if (!(lineValue instanceof List<?> indicesInColumns) || indicesInColumns.isEmpty()) {
            throw new IllegalArgumentException("Line #%d must be a non-empty list".formatted(lineIndex));
        }

        return IntStream.range(0, indicesInColumns.size())
                .mapToObj(column -> {
                    Object value = indicesInColumns.get(column);
                    if (!(value instanceof Integer indexInColumn) || indexInColumn < 0) {
                        throw new IllegalArgumentException(
                                "Line %d, column %d: indexInColumn must be a non-negative integer"
                                        .formatted(lineIndex, column)
                        );
                    }
                    return indexInColumn;
                })
                .toList();
    }
}
