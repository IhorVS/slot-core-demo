package ivs.games.accessories.slot.demo.console;

import ivs.games.accessories.slot.demo.engine.DemoSlot;
import ivs.games.accessories.slot.demo.engine.SpinResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests parsing, validation, execution, and result rendering of the
 * {@link SpinCommand}.
 *
 * <p>The mocked slot contains three reels. The tests verify only command-level
 * behavior; reel position validation and field generation are tested separately
 * by the demo slot engine tests.</p>
 */
@ExtendWith(MockitoExtension.class)
class SpinCommandTest {

    @Mock
    private DemoSlot slot;

    @Mock
    private ConsoleRenderer renderer;

    @Mock
    private SpinResult spinResult;

    private SpinCommand command;

    /**
     * Creates the command and configures the mocked slot with three reels.
     */
    @BeforeEach
    void setUp() {
        command = new SpinCommand(slot, renderer);
        when(slot.getReelCount()).thenReturn(3);
    }

    /**
     * Verifies a spin without explicitly specified reel positions.
     *
     * <pre>
     * spin
     * </pre>
     *
     * <p>An empty position list is passed to the slot. The slot engine is
     * responsible for generating all reel positions randomly.</p>
     */
    @Test
    void executesSpinWithoutPositions() {
        when(slot.spin(List.of())).thenReturn(spinResult);

        command.execute(new String[]{});

        verify(slot).spin(List.of());
        verify(renderer).printSpinResult(spinResult);
        verify(renderer, never()).printError(anyString());
    }

    /**
     * Verifies a spin with positions specified for only some reels.
     *
     * <pre>
     * spin 1 2
     * </pre>
     *
     * <p>The supplied positions are passed to the slot in their original order.
     * The slot engine is responsible for generating the missing third position.</p>
     */
    @Test
    void executesSpinWithSomePositions() {
        List<Integer> positions = List.of(1, 2);
        when(slot.spin(positions)).thenReturn(spinResult);

        command.execute(new String[]{"1", "2"});

        verify(slot).spin(positions);
        verify(renderer).printSpinResult(spinResult);
        verify(renderer, never()).printError(anyString());
    }

    /**
     * Verifies a spin with positions specified for every reel.
     *
     * <pre>
     * spin 1 2 3
     * </pre>
     *
     * <p>All parsed positions are passed to the slot without modification.</p>
     */
    @Test
    void executesSpinWithAllPositions() {
        List<Integer> positions = List.of(1, 2, 3);
        when(slot.spin(positions)).thenReturn(spinResult);

        command.execute(new String[]{"1", "2", "3"});

        verify(slot).spin(positions);
        verify(renderer).printSpinResult(spinResult);
        verify(renderer, never()).printError(anyString());
    }

    /**
     * Verifies that the command rejects more positions than the slot has reels.
     *
     * <pre>
     * spin 0 1 2 3
     * </pre>
     *
     * <p>The test slot has three reels, but the command contains four positions.
     * The slot must not be called.</p>
     */
    @Test
    void printsErrorWhenTooManyPositionsAreSpecified() {
        command.execute(new String[]{"0", "1", "2", "3"});

        verify(renderer).printError(
                "Expected no more than 3 reel positions, but received 4."
        );
        verify(slot, never()).spin(any());
        verify(renderer, never()).printSpinResult(any());
    }

    /**
     * Verifies that every supplied reel position must be an integer.
     *
     * <pre>
     * spin 1 invalid
     * </pre>
     *
     * <p>Argument conversion fails before the slot is called.</p>
     */
    @Test
    void printsErrorWhenPositionIsNotInteger() {
        command.execute(new String[]{"1", "invalid"});

        verify(renderer).printError(
                "Invalid reel position: 'invalid'. Position must be an integer."
        );
        verify(slot, never()).spin(any());
        verify(renderer, never()).printSpinResult(any());
    }

    /**
     * Verifies that a reel position validation error from the slot is displayed
     * by the console renderer.
     *
     * <pre>
     * spin 1 100
     * </pre>
     *
     * <p>The arguments are valid integers, but position {@code 100} is outside
     * the corresponding reel.</p>
     */
    @Test
    void printsErrorWhenSlotRejectsPosition() {
        List<Integer> positions = List.of(1, 100);
        IllegalArgumentException exception = new IllegalArgumentException(
                "Position 100 is outside reel 1. Valid positions: 0..3."
        );

        when(slot.spin(positions)).thenThrow(exception);

        command.execute(new String[]{"1", "100"});

        verify(slot).spin(positions);
        verify(renderer).printError(
                "Position 100 is outside reel 1. Valid positions: 0..3."
        );
        verify(renderer, never()).printSpinResult(any());
    }
}