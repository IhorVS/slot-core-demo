package ivs.games.accessories.slot.demo.console;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * Runs the interactive console input loop.
 *
 * <p>The loop prints the application title and help, reads commands line by line,
 * and passes them to the {@link CommandProcessor}.</p>
 *
 * <p>The interaction flow is:</p>
 *
 * <pre>
 * start
 *   |
 *   v
 * print title and help
 *   |
 *   v
 * print prompt
 *   |
 *   v
 * read input line
 *   |
 *   v
 * CommandProcessor
 *   |
 *   +---- CONTINUE ----> print next prompt
 *   |
 *   +---- EXIT --------> stop
 * </pre>
 *
 * <p>The loop also stops when the input stream reaches the end of input.</p>
 */
@RequiredArgsConstructor
public final class ConsoleLoop {

    @NonNull
    private final Scanner scanner;

    @NonNull
    private final PrintStream output;

    @NonNull
    private final CommandProcessor commandProcessor;

    /**
     * Starts the blocking console input loop.
     *
     * <p>The method returns when the command processor produces
     * {@link CommandResult#EXIT} or when the input stream reaches its end.</p>
     */
    public void run() {
        printWelcome();

        while (true) {
            output.print("> ");
            output.flush();

            if (!scanner.hasNextLine()) {
                output.println();
                return;
            }

            String input = scanner.nextLine();

            if (commandProcessor.process(input) == CommandResult.EXIT) {
                return;
            }
        }
    }

    /*
     * Prints the application title and delegates help rendering to the
     * command processor.
     */
    private void printWelcome() {
        output.println("Slot Core Demo");
        output.println();

        commandProcessor.process("help");
    }
}