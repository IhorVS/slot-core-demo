package ivs.games.accessories.slot.demo.console;

import ivs.game.accessories.slot.reel.impl.StandardReelItem;
import ivs.games.accessories.slot.demo.engine.HintHandler;
import ivs.games.accessories.slot.demo.engine.HintResult;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Parses and executes the console {@code hint} command.
 *
 * <p>The command calculates physical reel positions that place the requested
 * symbols on a configured field line.</p>
 *
 * <p>The supported syntax is:</p>
 *
 * <pre>
 * hint &lt;symbol,...&gt; line &lt;line-id&gt;
 * </pre>
 *
 * <p>Symbols must be separated by commas. Spaces around symbols are optional:</p>
 *
 * <pre>
 * hint A,A,A line 0
 * hint A, A, A line 0
 * hint WLD, A, SCT line 0
 * </pre>
 *
 * <p>Spaces without comma separators are not accepted:</p>
 *
 * <pre>
 * hint A A A line 0
 * </pre>
 *
 * <p>The command parses and validates console arguments. Reel position
 * calculation is delegated to {@link HintHandler}.</p>
 */
@RequiredArgsConstructor
public final class HintCommand {

    @NonNull
    private final HintHandler hintHandler;

    @NonNull
    private final ConsoleRenderer renderer;

    /**
     * Parses and executes the hint command arguments.
     *
     * <p>Validation errors are printed through the console renderer and do not
     * propagate to the console loop.</p>
     *
     * @param arguments command arguments without the {@code hint} command name
     * @return {@code true} if the hint succeeds; {@code false} if validation fails
     */
    public boolean execute(String[] arguments) {
        if (!hasValidStructure(arguments)) {
            renderer.printError("Usage: hint <symbol,...> line <line-id>");
            return false;
        }

        /*
         * CommandProcessor splits input by whitespace. Join every argument before
         * the final "line <line-id>" fragment to restore the complete symbol list.
         */
        String symbolsArgument =
                String.join(" ", Arrays.copyOfRange(arguments, 0, arguments.length - 2));

        List<StandardReelItem> symbols = getSymbols(symbolsArgument);
        if (symbols == null) {
            return false;
        }

        Integer lineId = getLineId(arguments[arguments.length - 1]);

        if (lineId == null) {
            return false;
        }

        try {
            HintResult result = hintHandler.hint(symbols, lineId);
            renderer.printHintResult(result);
            return true;
        } catch (IllegalArgumentException e) {
            renderer.printError(e.getMessage());
            return false;
        }
    }

    /*
     * Checks that the command contains symbols followed by {@code line} and a line ID.
     *
     * @param arguments command arguments
     * @return {@code true} when the command has the required structure
     */
    private boolean hasValidStructure(String[] arguments) {
        if (arguments.length < 3) {
            return false;
        }

        int lineArgumentIndex = arguments.length - 2;
        return "line".equals(arguments[lineArgumentIndex]);
    }

    /*
     * Parses the comma-separated symbol list.
     *
     * <p>Whitespace around individual symbols is removed. Empty elements and
     * whitespace-separated symbols without commas are rejected.</p>
     *
     * @param argument complete comma-separated symbol argument
     * @return parsed symbols, or {@code null} when validation fails
     */
    private List<StandardReelItem> getSymbols(String argument) {
        String[] symbolNames = argument.split(",", -1);
        List<StandardReelItem> symbols = new ArrayList<>(symbolNames.length);

        for (String symbolName : symbolNames) {
            String normalizedSymbolName = symbolName.trim();

            if (normalizedSymbolName.isEmpty()) {
                renderer.printError("Symbols must be separated by single commas.");
                return null;
            }

            if (normalizedSymbolName.chars().anyMatch(Character::isWhitespace)) {
                renderer.printError("Usage: hint <symbol,...> line <line-id>");
                return null;
            }

            try {
                symbols.add(StandardReelItem.valueOf(normalizedSymbolName));
            } catch (IllegalArgumentException e) {
                renderer.printError("Unknown symbol: '%s'.".formatted(normalizedSymbolName));
                return null;
            }
        }

        return List.copyOf(symbols);
    }

    /*
     * Converts the line identifier argument to an integer.
     *
     * @param argument line identifier argument
     * @return parsed line identifier, or {@code null} when conversion fails
     */
    private Integer getLineId(String argument) {
        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            renderer.printError(
                    "Invalid line ID: '%s'. Line ID must be an integer.".formatted(argument)
            );
            return null;
        }
    }
}
