package ivs.games.accessories.slot.demo.config;

import ivs.game.accessories.slot.matcher.FieldPosition;
import ivs.game.accessories.slot.matcher.linear.FieldLine;
import ivs.game.accessories.slot.reel.ReelBank;
import ivs.game.accessories.slot.reel.impl.StandardReelItem;
import ivs.games.accessories.slot.demo.config.field.FieldConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests validation of configured lines against the assembled field.
 */
class SlotConfigLoaderValidationTest {

    /**
     * Verifies that a line covering all reels within the visible rows is accepted.
     */
    @Test
    void acceptsLineMatchingFieldDimensions() {
        FieldConfig field = field();
        FieldLine line = line(
                new FieldPosition(0, 0),
                new FieldPosition(1, 1),
                new FieldPosition(2, 0)
        );

        assertDoesNotThrow(() -> SlotConfigLoader.validateLines(field, List.of(line)));
    }

    /**
     * Verifies that a line must contain one position for every reel.
     */
    @Test
    void rejectsLineWithMissingReelPosition() {
        FieldConfig field = field();
        FieldLine line = line(
                new FieldPosition(0, 0),
                new FieldPosition(1, 1)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SlotConfigLoader.validateLines(field, List.of(line))
        );

        assertEquals("Line 7 must contain 3 positions, but contains 2", exception.getMessage());
    }

    /**
     * Verifies that a line cannot refer to a different set of reel columns.
     */
    @Test
    void rejectsLineWithShiftedColumns() {
        FieldConfig field = field();
        FieldLine line = line(
                new FieldPosition(1, 0),
                new FieldPosition(2, 1),
                new FieldPosition(3, 0)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SlotConfigLoader.validateLines(field, List.of(line))
        );

        assertEquals(
                "Line 7, position 0 must use column 0, but uses 1",
                exception.getMessage()
        );
    }

    /**
     * Verifies that a line cannot address a row below the visible field.
     */
    @Test
    void rejectsLineWithRowOutsideField() {
        FieldConfig field = field();
        FieldLine line = line(
                new FieldPosition(0, 0),
                new FieldPosition(1, 2),
                new FieldPosition(2, 1)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SlotConfigLoader.validateLines(field, List.of(line))
        );

        assertEquals(
                "Line 7, column 1: row 2 is outside field height 2",
                exception.getMessage()
        );
    }

    /**
     * Creates a three-reel field with two visible rows.
     */
    @SuppressWarnings("unchecked")
    private FieldConfig field() {
        ReelBank<StandardReelItem> reelBank = mock(ReelBank.class);
        when(reelBank.size()).thenReturn(3);

        return new FieldConfig(2, reelBank);
    }

    /**
     * Creates a line with a stable ID for error-message checks.
     */
    private FieldLine line(FieldPosition... positions) {
        return new FieldLine(7, List.of(positions));
    }
}
