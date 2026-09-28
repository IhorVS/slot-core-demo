package ivs.games.accessories.slot.demo.console;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests command recognition, routing, and repetition performed by
 * {@link CommandProcessor}.
 *
 * <p>Command-specific parsing is tested separately by {@link SpinCommandTest}
 * and {@link HintCommandTest}.</p>
 */
@ExtendWith(MockitoExtension.class)
class CommandProcessorTest {

    @Mock
    private SpinCommand spinCommand;

    @Mock
    private HintCommand hintCommand;

    @Mock
    private ConsoleRenderer renderer;

    private CommandProcessor processor;

    /**
     * Creates a command processor with mocked command handlers and renderer.
     */
    @BeforeEach
    void setUp() {
        processor = new CommandProcessor(spinCommand, hintCommand, renderer);
    }

    /**
     * Verifies that an empty input line performs no action.
     */
    @Test
    void ignoresEmptyInput() {
        CommandResult result = processor.process("   ");

        assertEquals(CommandResult.CONTINUE, result);
        verifyNoInteractions(spinCommand, hintCommand, renderer);
    }

    /**
     * Verifies routing of the help command and its alias.
     */
    @ParameterizedTest
    @ValueSource(strings = {"help", "?"})
    void processesHelpCommand(String command) {
        CommandResult result = processor.process(command);

        assertEquals(CommandResult.CONTINUE, result);
        verify(renderer).printHelp();
        verifyNoInteractions(spinCommand, hintCommand);
    }

    /**
     * Verifies routing of the spin command and its alias with position arguments.
     *
     * <p>Repeated and surrounding whitespace is ignored.</p>
     */
    @SuppressWarnings("DataFlowIssue")
    @ParameterizedTest
    @ValueSource(strings = {"spin", "s"})
    void processesSpinCommand(String command) {
        CommandResult result = processor.process("  " + command + "   1   2  ");

        assertEquals(CommandResult.CONTINUE, result);
        verify(spinCommand).execute(aryEq(new String[]{"1", "2"}));
        verifyNoInteractions(hintCommand, renderer);
    }

    /**
     * Verifies routing of a spin without position arguments.
     */
    @SuppressWarnings("DataFlowIssue")
    @ParameterizedTest
    @ValueSource(strings = {"spin", "s"})
    void processesSpinCommandWithoutArguments(String command) {
        CommandResult result = processor.process(command);

        assertEquals(CommandResult.CONTINUE, result);
        verify(spinCommand).execute(aryEq(new String[]{}));
        verifyNoInteractions(hintCommand, renderer);
    }

    /**
     * Verifies routing of the hint command and its alias.
     */
    @ParameterizedTest
    @ValueSource(strings = {"hint", "h"})
    void processesHintCommand(String command) {
        CommandResult result = processor.process(command + " A,A,A line 0");

        assertEquals(CommandResult.CONTINUE, result);
        verify(hintCommand).execute(aryEq(new String[]{"A,A,A", "line", "0"}));
        verifyNoInteractions(spinCommand, renderer);
    }

    /**
     * Verifies that spaces around hint symbol separators are preserved for
     * further processing by {@link HintCommand}.
     */
    @Test
    void processesHintCommandWithSpacesBetweenSymbols() {
        CommandResult result = processor.process("hint WLD, A, SCT line 0");

        assertEquals(CommandResult.CONTINUE, result);
        verify(hintCommand).execute(
                aryEq(new String[]{"WLD,", "A,", "SCT", "line", "0"})
        );
        verifyNoInteractions(spinCommand, renderer);
    }

    /**
     * Verifies that repetition requires a previously successful spin or hint.
     */
    @ParameterizedTest
    @ValueSource(strings = {"repeat", "r"})
    void rejectsRepeatWithoutPreviousCommand(String command) {
        CommandResult result = processor.process(command);

        assertEquals(CommandResult.CONTINUE, result);
        verify(renderer).printError("No spin or hint command to repeat.");
        verifyNoInteractions(spinCommand, hintCommand);
    }

    /**
     * Verifies that a repeated spin displays and executes the saved command.
     */
    @SuppressWarnings("DataFlowIssue")
    @Test
    void repeatsLastSuccessfulSpin() {
        when(spinCommand.execute(aryEq(new String[]{"1", "2"}))).thenReturn(true);

        processor.process("s 1 2");
        CommandResult result = processor.process("r");

        assertEquals(CommandResult.CONTINUE, result);
        verify(renderer).printRepeatedCommand("spin 1 2");
        verify(spinCommand, times(2)).execute(aryEq(new String[]{"1", "2"}));
        verifyNoInteractions(hintCommand);
    }

    /**
     * Verifies that a repeated hint displays and executes the saved command.
     */
    @Test
    void repeatsLastSuccessfulHint() {
        when(hintCommand.execute(aryEq(new String[]{"A,A,A", "line", "0"}))).thenReturn(true);

        processor.process("h A,A,A line 0");
        CommandResult result = processor.process("repeat");

        assertEquals(CommandResult.CONTINUE, result);
        verify(renderer).printRepeatedCommand("hint A,A,A line 0");
        verify(hintCommand, times(2)).execute(aryEq(new String[]{"A,A,A", "line", "0"}));
        verifyNoInteractions(spinCommand);
    }

    /**
     * Verifies that a failed hint does not replace the last successful spin.
     */
    @SuppressWarnings("DataFlowIssue")
    @Test
    void keepsLastSuccessfulCommandAfterFailure() {
        when(spinCommand.execute(aryEq(new String[]{"1"}))).thenReturn(true);

        processor.process("s 1");
        processor.process("h INVALID line 0");
        processor.process("r");

        verify(renderer).printRepeatedCommand("spin 1");
        verify(spinCommand, times(2)).execute(aryEq(new String[]{"1"}));
        verify(hintCommand).execute(aryEq(new String[]{"INVALID", "line", "0"}));
    }

    /**
     * Verifies that help does not replace the command available for repetition.
     */
    @SuppressWarnings("DataFlowIssue")
    @Test
    void keepsLastSuccessfulCommandAfterHelp() {
        when(spinCommand.execute(aryEq(new String[]{}))).thenReturn(true);

        processor.process("s");
        processor.process("?");
        processor.process("r");

        verify(renderer).printHelp();
        verify(renderer).printRepeatedCommand("spin");
        verify(spinCommand, times(2)).execute(aryEq(new String[]{}));
    }

    /**
     * Verifies that arguments are rejected by repeat without executing history.
     */
    @Test
    void rejectsRepeatWithArguments() {
        CommandResult result = processor.process("r 1");

        assertEquals(CommandResult.CONTINUE, result);
        verify(renderer).printError("Usage: repeat");
        verifyNoInteractions(spinCommand, hintCommand);
    }

    /**
     * Verifies all supported commands for terminating the application.
     */
    @ParameterizedTest
    @ValueSource(strings = {"exit", "quit", "q"})
    void processesExitCommand(String command) {
        CommandResult result = processor.process(command);

        assertEquals(CommandResult.EXIT, result);
        verify(renderer).printStopped();
        verifyNoInteractions(spinCommand, hintCommand);
    }

    /**
     * Verifies that an unsupported command is passed to the renderer.
     */
    @Test
    void processesUnknownCommand() {
        CommandResult result = processor.process("start");

        assertEquals(CommandResult.CONTINUE, result);
        verify(renderer).printUnknownCommand("start");
        verifyNoInteractions(spinCommand, hintCommand);
        verify(renderer, never()).printStopped();
    }
}
