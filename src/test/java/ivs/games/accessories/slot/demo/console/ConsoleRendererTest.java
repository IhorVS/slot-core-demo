package ivs.games.accessories.slot.demo.console;

import ivs.game.accessories.slot.field.SlotField;
import ivs.game.accessories.slot.matcher.Combination;
import ivs.game.accessories.slot.matcher.FieldPosition;
import ivs.game.accessories.slot.matcher.linear.FieldLine;
import ivs.game.accessories.slot.matcher.linear.LinearCombinationMatch;
import ivs.game.accessories.slot.matcher.scatter.ScatterCombinationMatch;
import ivs.game.accessories.slot.reel.impl.StandardReelItem;
import ivs.games.accessories.slot.demo.engine.CombinationWin;
import ivs.games.accessories.slot.demo.engine.HintResult;
import ivs.games.accessories.slot.demo.engine.SpinResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static ivs.game.accessories.slot.reel.impl.StandardReelItem.A;
import static ivs.game.accessories.slot.reel.impl.StandardReelItem.K;
import static ivs.game.accessories.slot.reel.impl.StandardReelItem.MUL;
import static ivs.game.accessories.slot.reel.impl.StandardReelItem.Q;
import static ivs.game.accessories.slot.reel.impl.StandardReelItem.SCT;
import static ivs.game.accessories.slot.reel.impl.StandardReelItem.WLD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Tests text formatting performed by {@link ConsoleRenderer}.
 */
@ExtendWith(MockitoExtension.class)
class ConsoleRendererTest {

    @Mock
    private SlotField<StandardReelItem> field;

    private ByteArrayOutputStream output;
    private ConsoleRenderer renderer;

    /**
     * Creates a renderer that writes to an in-memory output stream.
     */
    @BeforeEach
    void setUp() {
        output = new ByteArrayOutputStream();
        renderer = new ConsoleRenderer(new PrintStream(output));
    }

    /**
     * Verifies that help contains the supported commands and hint examples.
     *
     * <pre>
     * help
     * </pre>
     */
    @Test
    void printsHelp() {
        renderer.printHelp();

        String text = outputText();

        assertTrue(text.contains("spin"));
        assertTrue(text.contains("hint"));
        assertTrue(text.contains("repeat"));
        assertTrue(text.contains("help"));
        assertTrue(text.contains("exit"));
        assertTrue(text.contains("Alias"));
    }

    /**
     * Verifies hint formatting and generation of a ready-to-use spin command.
     *
     * <pre>
     * hint WLD, A, SCT line 0
     * </pre>
     */
    @Test
    void printsHintResult() {
        HintResult result = new HintResult(
                List.of(WLD, A, SCT),
                0,
                List.of(3, 1, 2)
        );

        renderer.printHintResult(result);

        assertEquals(
                """
                        WLD,A,SCT positions for line 0:
                        3 1 2
                        
                        Use:
                          spin 3 1 2
                        """.replace("\n", System.lineSeparator()),
                outputText()
        );
    }

    /**
     * Verifies formatting of error, unknown-command, and application stop messages.
     */
    @Test
    void printsApplicationMessages() {
        renderer.printError("Invalid input.");
        renderer.printUnknownCommand("start");
        renderer.printStopped();

        assertEquals(
                """
                        Invalid input.
                        Unknown command: 'start'.
                        Type 'help' to see available commands.
                        Slot Core Demo stopped.
                        """.replace("\n", System.lineSeparator()),
                outputText()
        );
    }

    /**
     * Verifies field formatting and the message printed when a spin has no wins.
     *
     * <pre>
     * spin 1 1 3
     * </pre>
     */
    @Test
    void printsSpinResultWithoutWins() {
        configureSingleRowField(K, Q, MUL);

        SpinResult result = new SpinResult(
                List.of(1, 1, 3),
                field,
                List.of()
        );

        renderer.printSpinResult(result);

        String text = outputText();

        assertTrue(text.contains("Actual reel positions:"));
        assertTrue(text.contains("[1, 1, 3]"));
        assertTrue(text.contains("Game field:"));
        assertTrue(text.contains("R0    R1    R2"));
        assertTrue(text.contains("Row 0 |   K     Q    MUL"));
        assertTrue(text.contains("Wins:"));
        assertTrue(text.contains("No winning combinations."));
    }

    /**
     * Verifies formatting of linear and scatter wins with configured prizes.
     *
     * <pre>
     * spin 0 0 0
     * </pre>
     */
    @SuppressWarnings("ExtractMethodRecommender")
    @Test
    void printsLinearAndScatterWins() {
        configureSingleRowField(A, A, A);

        List<FieldPosition> positions = List.of(
                new FieldPosition(0, 0),
                new FieldPosition(1, 0),
                new FieldPosition(2, 0)
        );
        FieldLine line = new FieldLine(0, positions);

        Combination<StandardReelItem> linearCombination = new Combination<>(
                0,
                "A",
                List.of(A, A, A)
        );
        Combination<StandardReelItem> scatterCombination = new Combination<>(
                1,
                "SCT",
                List.of(SCT, SCT, SCT)
        );

        CombinationWin linearWin = new CombinationWin(
                new LinearCombinationMatch<>(
                        linearCombination,
                        positions,
                        line
                ),
                List.of("C10")
        );
        CombinationWin scatterWin = new CombinationWin(
                new ScatterCombinationMatch<>(
                        scatterCombination,
                        positions
                ),
                List.of("C15", "FS3")
        );

        SpinResult result = new SpinResult(
                List.of(0, 0, 0),
                field,
                List.of(linearWin, scatterWin)
        );

        renderer.printSpinResult(result);

        String text = outputText();

        assertTrue(text.contains("Linear:"));
        assertTrue(text.contains("Combination ID: 0"));
        assertTrue(text.contains("Group: A"));
        assertTrue(text.contains("Line ID: 0"));
        assertTrue(text.contains("Symbols: [A, A, A]"));
        assertTrue(text.contains("Prizes: [C10]"));

        assertTrue(text.contains("Scatter:"));
        assertTrue(text.contains("Combination ID: 1"));
        assertTrue(text.contains("Group: SCT"));
        assertTrue(text.contains("Symbols: [SCT, SCT, SCT]"));
        assertTrue(text.contains("Prizes: [C15, FS3]"));
    }

    /*
     * Configures a rectangular field containing one visible row.
     */
    private void configureSingleRowField(
            StandardReelItem first,
            StandardReelItem second,
            StandardReelItem third) {

        when(field.getColumnCount()).thenReturn(3);
        when(field.getColumnSize(0)).thenReturn(1);
        when(field.getItem(0, 0)).thenReturn(first);
        when(field.getItem(1, 0)).thenReturn(second);
        when(field.getItem(2, 0)).thenReturn(third);
    }

    /*
     * Returns all text written by the renderer.
     */
    private String outputText() {
        return output.toString();
    }
}
