package ivs.games.accessories.slot.demo.console;

/**
 * Describes whether the console loop should continue processing commands or stop.
 *
 * <p>The result is returned by {@link CommandProcessor} after processing one
 * console input line and is interpreted by {@link ConsoleLoop}.</p>
 *
 * <pre>
 * CommandProcessor
 *        |
 *        v
 * CommandResult
 *        |
 *        +-- CONTINUE -> read the next command
 *        |
 *        +-- EXIT ----> stop the console loop
 * </pre>
 */
public enum CommandResult {

    /**
     * Indicates that the console loop should continue reading commands.
     */
    CONTINUE,

    /**
     * Indicates that the console loop should terminate.
     */
    EXIT
}