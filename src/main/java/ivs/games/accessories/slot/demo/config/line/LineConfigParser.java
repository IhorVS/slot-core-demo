package ivs.games.accessories.slot.demo.config.line;

import ivs.game.accessories.slot.matcher.FieldPosition;
import ivs.game.accessories.slot.matcher.linear.FieldLine;
import lombok.NonNull;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Converts raw line configuration into slot-core {@link FieldLine} objects.
 *
 * <p>Each raw line contains one row index for every field column. The position
 * of a value in the list becomes the column index, while the value itself
 * becomes the row index.</p>
 *
 * <pre>
 * Raw line:
 *
 * [0, 1, 2, 1, 0]
 *  |  |  |  |  |
 *  0  1  2  3  4  <- column index
 *
 * Parsed positions:
 *
 * (column=0, row=0)
 * (column=1, row=1)
 * (column=2, row=2)
 * (column=3, row=1)
 * (column=4, row=0)
 * </pre>
 *
 * <p>The zero-based index of a line in the configuration becomes its
 * {@link FieldLine#id()}.</p>
 */
public final class LineConfigParser {

    /**
     * Parses all configured lines in their original order.
     *
     * @param configuredLines row indices for every configured line
     * @return parsed field lines with zero-based identifiers
     * @throws NullPointerException     if {@code configuredLines} is {@code null}
     *                                  or contains invalid null values
     * @throws IllegalArgumentException if a configured line cannot be converted
     *                                  to a valid {@link FieldLine}
     */
    public List<FieldLine> parse(@NonNull List<List<Integer>> configuredLines) {

        return IntStream.range(0, configuredLines.size())
                .mapToObj(lineId -> parseLine(lineId, configuredLines.get(lineId)))
                .toList();
    }

    /*
     * Converts one list of row indices into ordered field positions. The list
     * index becomes the column index, and the stored value becomes the row index.
     */
    private FieldLine parseLine(int lineId, List<Integer> indicesInColumns) {
        List<FieldPosition> positions = IntStream.range(0, indicesInColumns.size())
                .mapToObj(columnIndex ->
                        new FieldPosition(columnIndex, indicesInColumns.get(columnIndex)))
                .toList();
        return new FieldLine(lineId, positions);
    }
}