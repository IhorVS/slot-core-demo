package ivs.games.accessories.slot.demo.console;

import ivs.game.accessories.slot.field.SlotField;
import ivs.game.accessories.slot.matcher.linear.LinearCombinationMatch;
import ivs.game.accessories.slot.matcher.scatter.ScatterCombinationMatch;
import ivs.game.accessories.slot.reel.impl.StandardReelItem;
import ivs.games.accessories.slot.demo.engine.CombinationWin;
import ivs.games.accessories.slot.demo.engine.HintResult;
import ivs.games.accessories.slot.demo.engine.SpinResult;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Formats and prints all console output produced by the demo application.
 *
 * <p>The renderer is responsible only for presentation. It does not parse commands,
 * generate fields, calculate hints, or evaluate winning combinations.</p>
 *
 * <p>The following results are supported:</p>
 *
 * <pre>
 * ConsoleRenderer
 *       |
 *       +-- help
 *       |
 *       +-- spin result
 *       |     |
 *       |     +-- reel positions
 *       |     +-- game field
 *       |     +-- linear wins
 *       |     +-- scatter wins
 *       |
 *       +-- hint result
 *       |
 *       +-- errors and application messages
 * </pre>
 */
@RequiredArgsConstructor
public final class ConsoleRenderer {

    private static final String HELP = """
            Available commands:
            
              Command                       Alias  Description
              ----------------------------  -----  ----------------------------------------
              spin                          s      Spin using random reel positions
              spin <p1> ... <pN>            s      Spin using the specified positions;
                                                   missing positions are selected randomly
              hint <symbol,...> line <id>   h      Find reel positions for symbols on a line;
                                                   separate symbols with commas
                                                   Example: h WLD,A,SCT line 0
              repeat                        r      Repeat the last successful spin or hint
              help                          ?      Show available commands
              exit                          q      Exit the application
            
            """;

    private static final int FIELD_CELL_WIDTH = 6;

    @NonNull
    private final PrintStream output;

    /**
     * Prints the list of supported console commands.
     */
    public void printHelp() {
        output.print(HELP);
    }

    /**
     * Prints the complete result of a slot spin.
     *
     * <p>The output contains actual reel positions, the generated field, and all
     * detected linear and scatter wins.</p>
     *
     * @param result completed spin result
     */
    public void printSpinResult(SpinResult result) {
        output.println();
        output.println("Actual reel positions:");
        output.printf("  %s%n", result.reelPositions());
        output.println();

        printField(result.field());
        printWins(result.wins());
    }

    /*
     * Prints detected wins or a message indicating that no winning combinations
     * were found.
     */
    private void printWins(List<CombinationWin> wins) {
        output.println("Wins:");

        if (wins.isEmpty()) {
            output.println("  No winning combinations.");
            return;
        }

        output.println();
        printLinearWins(wins);
        output.println();
        printScatterWins(wins);
    }

    /*
     * Prints all linear wins and their line identifiers, matched positions,
     * symbols, and configured prizes.
     */
    private void printLinearWins(List<CombinationWin> wins) {
        output.println("  Linear:");

        boolean found = false;

        for (CombinationWin win : wins) {
            if (!(win.match() instanceof LinearCombinationMatch<?>)) {
                continue;
            }

            found = true;

            LinearCombinationMatch<StandardReelItem> match =
                    (LinearCombinationMatch<StandardReelItem>) win.match();

            output.println();
            output.printf("    Combination ID: %d%n", match.combination().getId());
            output.printf("    Group: %s%n", match.combination().getGroupId());
            output.printf("    Line ID: %d%n", match.line().id());
            output.printf("    Symbols: %s%n", match.combination().getItems());
            output.printf("    Positions: %s%n", match.positions());
            output.printf("    Prizes: %s%n", win.prizes());
        }

        if (!found) {
            output.println("    None");
        }
    }

    /*
     * Prints all scatter wins and their matched positions, symbols, and
     * configured prizes.
     */
    private void printScatterWins(List<CombinationWin> wins) {
        output.println("  Scatter:");

        boolean found = false;

        for (CombinationWin win : wins) {
            if (!(win.match() instanceof ScatterCombinationMatch<?>)) {
                continue;
            }

            found = true;

            ScatterCombinationMatch<StandardReelItem> match =
                    (ScatterCombinationMatch<StandardReelItem>) win.match();

            output.println();
            output.printf("    Combination ID: %d%n", match.combination().getId());
            output.printf("    Group: %s%n", match.combination().getGroupId());
            output.printf("    Symbols: %s%n", match.combination().getItems());
            output.printf("    Positions: %s%n", match.positions());
            output.printf("    Prizes: %s%n", win.prizes());
        }

        if (!found) {
            output.println("    None");
        }
    }

    /**
     * Prints calculated reel positions for a hint request.
     *
     * <p>The output also contains a ready-to-use {@code spin} command.</p>
     *
     * @param result calculated hint result
     */
    public void printHintResult(HintResult result) {
        String symbols = result.symbols().stream()
                .map(StandardReelItem::name)
                .collect(Collectors.joining(","));

        String positions = result.reelPositions().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" "));

        output.printf(
                "%s positions for line %d:%n",
                symbols,
                result.lineId()
        );
        output.println(positions);
        output.println();
        output.println("Use:");
        output.println("  spin " + positions);
    }

    /**
     * Prints an error message.
     *
     * @param message error description
     */
    public void printError(String message) {
        output.println(message);
    }

    /**
     * Prints the application stop message.
     */
    public void printStopped() {
        output.println("Slot Core Demo stopped.");
    }

    /**
     * Prints an error for an unsupported command.
     *
     * @param command unsupported command name
     */
    public void printUnknownCommand(String command) {
        output.printf("Unknown command: '%s'.%n", command);
        output.println("Type 'help' to see available commands.");
    }

    /*
     * Prints the visible slot field as a table with one column per reel.
     */
    private void printField(SlotField<StandardReelItem> field) {
        output.println("Game field:");
        output.println();

        String header = createFieldHeader(field.getColumnCount());

        output.println(header);
        output.println("-".repeat(header.length()));

        int rowCount = field.getColumnSize(0);

        for (int row = 0; row < rowCount; row++) {
            output.println(createFieldRow(field, row));
        }

        output.println();
    }

    /*
     * Creates the field table header containing zero-based reel identifiers.
     */
    private String createFieldHeader(int columnCount) {
        StringBuilder header = new StringBuilder("       ");

        for (int column = 0; column < columnCount; column++) {
            header.append(formatFieldCell("R" + column));
        }

        return header.toString().stripTrailing();
    }

    /*
     * Creates one field table row and preserves the left-to-right reel order.
     */
    private String createFieldRow(SlotField<StandardReelItem> field, int row) {
        StringBuilder fieldRow = new StringBuilder("Row %d |".formatted(row));

        for (int column = 0; column < field.getColumnCount(); column++) {
            fieldRow.append(formatFieldCell(field.getItem(column, row).name()));
        }

        return fieldRow.toString().stripTrailing();
    }

    /*
     * Centers a field value inside a fixed-width cell. This keeps one-character
     * symbols aligned with symbols such as WLD, SCT, and MUL.
     */
    private String formatFieldCell(String value) {
        int padding = FIELD_CELL_WIDTH - value.length();

        if (padding <= 0) {
            return value;
        }

        int leftPadding = (padding + 1) / 2;
        int rightPadding = padding - leftPadding;

        return " ".repeat(leftPadding)
                + value
                + " ".repeat(rightPadding);
    }

    /**
     * Prints the command selected for repetition.
     *
     * @param command command name and arguments
     */
    public void printRepeatedCommand(String command) {
        output.println("Repeating: " + command);
    }
}
