package ivs.games.accessories.slot.demo.console;

import ivs.games.accessories.slot.demo.engine.DemoSlot;
import ivs.games.accessories.slot.demo.engine.SpinResult;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses and executes the console {@code spin} command.
 *
 * <p>The command accepts zero or more physical reel positions:</p>
 *
 * <pre>
 * spin
 * spin 0 1 2
 * spin 0 1 2 3 4
 * </pre>
 *
 * <p>If no positions are supplied, the slot selects all positions randomly.
 * If only some positions are supplied, they are assigned to reels from left
 * to right and the remaining positions are selected randomly.</p>
 *
 * <p>The command validates console arguments and delegates the spin operation
 * to {@link DemoSlot}. The resulting field and wins are passed to the
 * {@link ConsoleRenderer}.</p>
 */
@RequiredArgsConstructor
public final class SpinCommand {

    @NonNull
    private final DemoSlot slot;

    @NonNull
    private final ConsoleRenderer renderer;

    /**
     * Parses the supplied reel positions and performs a spin.
     *
     * <p>Validation errors are printed through the console renderer and do not
     * propagate to the console loop.</p>
     *
     * @param arguments physical reel position arguments without the command name
     * @return {@code true} if the spin succeeds; {@code false} if validation fails
     */
    public boolean execute(String[] arguments) {
        if (arguments.length > slot.getReelCount()) {
            renderer.printError(
                    "Expected no more than %d reel positions, but received %d.".formatted(
                            slot.getReelCount(),
                            arguments.length
                    )
            );
            return false;
        }

        List<Integer> reelPositions = getReelPositions(arguments);

        if (reelPositions == null) {
            return false;
        }

        try {
            SpinResult result = slot.spin(reelPositions);
            renderer.printSpinResult(result);
            return true;
        } catch (IllegalArgumentException e) {
            renderer.printError(e.getMessage());
            return false;
        }
    }

    /*
     * Converts position arguments to integers. If an argument cannot be converted,
     * the method prints an error and returns null to stop command execution.
     */
    private List<Integer> getReelPositions(String[] arguments) {
        List<Integer> reelPositions = new ArrayList<>(arguments.length);

        for (String argument : arguments) {
            try {
                reelPositions.add(Integer.parseInt(argument));
            } catch (NumberFormatException e) {
                renderer.printError(
                        "Invalid reel position: '%s'. Position must be an integer.".formatted(argument)
                );
                return null;
            }
        }

        return reelPositions;
    }
}
