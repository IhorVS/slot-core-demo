package ivs.games.accessories.slot.demo.console;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintStream;
import java.util.Scanner;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the console input loop and its interaction with the command processor.
 */
@ExtendWith(MockitoExtension.class)
class ConsoleLoopTest {

    @Mock
    private PrintStream output;

    @Mock
    private CommandProcessor commandProcessor;

    /**
     * Verifies that the loop stops immediately when the input stream is empty.
     *
     * <pre>
     * &lt;end of input&gt;
     * </pre>
     */
    @Test
    void stopsWhenInputEnds() {
        when(commandProcessor.process("help")).thenReturn(CommandResult.CONTINUE);

        ConsoleLoop loop = new ConsoleLoop(
                new Scanner(""),
                output,
                commandProcessor
        );

        loop.run();

        verify(output).println("Slot Core Demo");
        verify(output).print("> ");
        verify(output).flush();
        verify(commandProcessor).process("help");
        verifyNoMoreInteractions(commandProcessor);
    }

    /**
     * Verifies that commands are processed in their input order until an exit
     * command is received.
     *
     * <pre>
     * spin 1 2
     * q
     * </pre>
     */
    @Test
    void processesCommandsUntilExit() {
        when(commandProcessor.process("help")).thenReturn(CommandResult.CONTINUE);
        when(commandProcessor.process("spin 1 2")).thenReturn(CommandResult.CONTINUE);
        when(commandProcessor.process("q")).thenReturn(CommandResult.EXIT);

        ConsoleLoop loop = new ConsoleLoop(
                new Scanner("spin 1 2%nq%n".formatted()),
                output,
                commandProcessor
        );

        loop.run();

        InOrder order = inOrder(commandProcessor);

        order.verify(commandProcessor).process("help");
        order.verify(commandProcessor).process("spin 1 2");
        order.verify(commandProcessor).process("q");

        verify(output).println("Slot Core Demo");
        verify(output, times(2)).print("> ");
        verify(output, times(2)).flush();
    }
}