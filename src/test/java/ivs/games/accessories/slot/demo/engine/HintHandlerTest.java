package ivs.games.accessories.slot.demo.engine;

import ivs.game.accessories.slot.matcher.FieldPosition;
import ivs.game.accessories.slot.matcher.linear.FieldLine;
import ivs.game.accessories.slot.reel.Reel;
import ivs.game.accessories.slot.reel.impl.StandardReel;
import ivs.game.accessories.slot.reel.impl.StandardReelBank;
import ivs.game.accessories.slot.reel.impl.StandardReelItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static ivs.game.accessories.slot.reel.impl.StandardReelItem.A;
import static ivs.game.accessories.slot.reel.impl.StandardReelItem.K;
import static ivs.game.accessories.slot.reel.impl.StandardReelItem.MUL;
import static ivs.game.accessories.slot.reel.impl.StandardReelItem.Q;
import static ivs.game.accessories.slot.reel.impl.StandardReelItem.SCT;
import static ivs.game.accessories.slot.reel.impl.StandardReelItem.WLD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests hint calculation without creating a demo slot.
 */
class HintHandlerTest {

    private HintHandler handler;

    @BeforeEach
    void setUp() {
        List<List<StandardReelItem>> strips = List.of(
                List.of(A, K, SCT, WLD),
                List.of(A, Q, SCT, MUL),
                List.of(A, K, SCT, Q)
        );
        List<Reel<StandardReelItem>> reels = new ArrayList<>();

        for (List<StandardReelItem> strip : strips) {
            reels.add(new StandardReel<>(strip, 1));
        }

        FieldLine line = new FieldLine(
                0,
                List.of(
                        new FieldPosition(0, 0),
                        new FieldPosition(1, 0),
                        new FieldPosition(2, 0)
                )
        );

        handler = new HintHandler(new StandardReelBank<>(reels), List.of(line));
    }

    /**
     * Verifies positions for symbols whose names contain one character.
     */
    @Test
    void findsHintForSingleCharacterSymbols() {
        HintResult result = handler.hint(List.of(A, Q, Q), 0);

        assertEquals(List.of(A, Q, Q), result.symbols());
        assertEquals(0, result.lineId());
        assertEquals(List.of(0, 1, 3), result.reelPositions());
    }

    /**
     * Verifies positions for symbols whose names contain multiple characters.
     */
    @Test
    void findsHintForMultiCharacterSymbols() {
        HintResult result = handler.hint(List.of(WLD, MUL, SCT), 0);

        assertEquals(List.of(WLD, MUL, SCT), result.symbols());
        assertEquals(0, result.lineId());
        assertEquals(List.of(3, 3, 2), result.reelPositions());
    }

    /**
     * Verifies that a symbol absent from its reel is rejected.
     */
    @Test
    void rejectsSymbolMissingOnReel() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> handler.hint(List.of(MUL), 0)
        );

        assertEquals("Symbol MUL was not found on reel 0.", exception.getMessage());
    }

    /**
     * Verifies that the selected line must exist.
     */
    @Test
    void rejectsUnknownLine() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> handler.hint(List.of(A), 1)
        );

        assertEquals("Line 1 does not exist.", exception.getMessage());
    }

    /**
     * Verifies that at least one symbol is required.
     */
    @Test
    void rejectsEmptySymbols() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> handler.hint(List.of(), 0)
        );

        assertEquals("At least one symbol must be specified.", exception.getMessage());
    }

    /**
     * Verifies that the request cannot be longer than the selected line.
     */
    @Test
    void rejectsTooManySymbols() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> handler.hint(List.of(A, K, Q, SCT), 0)
        );

        assertEquals("Expected no more than 3 symbols, but received 4.", exception.getMessage());
    }
}
