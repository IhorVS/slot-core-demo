package ivs.games.accessories.slot.demo.console;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Parses console input and routes commands to their corresponding handlers.
 *
 * <p>The processor recognizes the following commands and aliases:</p>
 *
 * <pre>
 * help, &#63;
 * spin [position ...], s [position ...]
 * hint &lt;symbol,...&gt; line &lt;line-id&gt;, h &lt;symbol,...&gt; line &lt;line-id&gt;
 * repeat, r
 * exit, quit, q
 * </pre>
 *
 * <p>A successfully executed spin or hint can be repeated with its original
 * arguments. Other commands do not replace the saved command.</p>
 */
@RequiredArgsConstructor
public final class CommandProcessor {

    @NonNull
    private final SpinCommand spinCommand;

    @NonNull
    private final HintCommand hintCommand;

    @NonNull
    private final ConsoleRenderer renderer;

    private String lastCommand;
    private String[] lastArguments;

    /**
     * Processes one line of console input.
     *
     * <p>Leading and trailing whitespace is ignored. An empty input line performs
     * no action and allows the console loop to continue.</p>
     *
     * @param input complete console input line
     * @return {@link CommandResult#EXIT} when an exit command is received;
     * otherwise {@link CommandResult#CONTINUE}
     * @throws NullPointerException if {@code input} is {@code null}
     */
    public CommandResult process(@NonNull String input) {
        String normalizedInput = input.trim();

        if (normalizedInput.isEmpty()) {
            return CommandResult.CONTINUE;
        }

        String[] tokens = normalizedInput.split("\\s+");
        String command = tokens[0];
        String[] arguments = Arrays.copyOfRange(tokens, 1, tokens.length);

        return switch (command) {
            case "help", "?" -> executeHelp();
            case "spin", "s" -> executeSpin(arguments);
            case "hint", "h" -> executeHint(arguments);
            case "repeat", "r" -> {
                executeRepeat(arguments);
                yield CommandResult.CONTINUE;
            }
            case "exit", "quit", "q" -> executeExit();
            default -> executeUnknown(command);
        };
    }

    /*
     * Prints the available commands and continues the console loop.
     */
    private CommandResult executeHelp() {
        renderer.printHelp();
        return CommandResult.CONTINUE;
    }

    /*
     * Executes a spin and remembers its arguments only when it succeeds.
     */
    private CommandResult executeSpin(String[] arguments) {
        if (spinCommand.execute(arguments)) {
            remember("spin", arguments);
        }

        return CommandResult.CONTINUE;
    }

    /*
     * Executes a hint and remembers its arguments only when it succeeds.
     */
    private CommandResult executeHint(String[] arguments) {
        if (hintCommand.execute(arguments)) {
            remember("hint", arguments);
        }

        return CommandResult.CONTINUE;
    }

    /*
     * Repeats the last successful spin or hint without replacing the history entry.
     */
    private void executeRepeat(String[] arguments) {
        if (arguments.length != 0) {
            renderer.printError("Usage: repeat");
            return;
        }

        if (lastCommand == null) {
            renderer.printError("No spin or hint command to repeat.");
            return;
        }

        String command = lastCommand;

        if (lastArguments.length != 0) {
            command += " " + String.join(" ", lastArguments);
        }

        renderer.printRepeatedCommand(command);

        if ("spin".equals(lastCommand)) {
            spinCommand.execute(lastArguments.clone());
        } else {
            hintCommand.execute(lastArguments.clone());
        }
    }

    /*
     * Saves a copy because command arguments must not change between executions.
     */
    private void remember(String command, String[] arguments) {
        lastCommand = command;
        lastArguments = arguments.clone();
    }

    /*
     * Prints the application stop message and requests termination of the
     * console loop.
     */
    private CommandResult executeExit() {
        renderer.printStopped();
        return CommandResult.EXIT;
    }

    /*
     * Prints an error for an unsupported command and continues the console loop.
     */
    private CommandResult executeUnknown(String command) {
        renderer.printUnknownCommand(command);
        return CommandResult.CONTINUE;
    }
}
