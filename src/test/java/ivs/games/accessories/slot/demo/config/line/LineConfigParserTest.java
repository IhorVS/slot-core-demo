package ivs.games.accessories.slot.demo.config.line;

import ivs.game.accessories.slot.matcher.FieldPosition;
import ivs.game.accessories.slot.matcher.linear.FieldLine;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LineConfigParserTest {

    private final LineConfigParser parser = new LineConfigParser();

    @Test
    void parsesLines() {
        /*
         * Input:
         *
         * Line 0: [0, 1, 2]
         *
         *   X . .
         *   . X .
         *   . . X
         *
         * Line 1: [2, 1, 0]
         *
         *   . . X
         *   . X .
         *   X . .
         */
        List<List<Integer>> configuredLines = List.of(
                List.of(0, 1, 2),
                List.of(2, 1, 0)
        );

        List<FieldLine> result = parser.parse(configuredLines);

        assertEquals(
                List.of(
                        new FieldLine(0, List.of(
                                new FieldPosition(0, 0),
                                new FieldPosition(1, 1),
                                new FieldPosition(2, 2)
                        )),
                        new FieldLine(1, List.of(
                                new FieldPosition(0, 2),
                                new FieldPosition(1, 1),
                                new FieldPosition(2, 0)
                        ))
                ),
                result
        );
    }
}