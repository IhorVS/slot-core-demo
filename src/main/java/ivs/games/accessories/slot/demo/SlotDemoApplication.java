package ivs.games.accessories.slot.demo;

import ivs.games.accessories.slot.demo.config.SlotConfig;
import ivs.games.accessories.slot.demo.config.SlotConfigLoader;
import ivs.games.accessories.slot.demo.console.CommandProcessor;
import ivs.games.accessories.slot.demo.console.ConsoleLoop;
import ivs.games.accessories.slot.demo.console.ConsoleRenderer;
import ivs.games.accessories.slot.demo.console.HintCommand;
import ivs.games.accessories.slot.demo.console.SpinCommand;
import ivs.games.accessories.slot.demo.engine.DemoSlot;
import ivs.games.accessories.slot.demo.engine.HintHandler;

import java.util.Scanner;

/**
 * Starts the Slot Core Demo console application.
 *
 * <p>The application loads the slot configuration, creates the demo slot and
 * hint handler, assembles the console components, and starts the input loop.</p>
 *
 * <pre>
 * SlotConfigLoader
 *        |
 *        v
 *    SlotConfig
 *        |
 *        +-------------------+
 *        |                   |
 *        v                   v
 *     DemoSlot          HintHandler
 *        |                   |
 *        v                   v
 *   SpinCommand         HintCommand
 *        |                   |
 *        +---------+---------+
 *                  |
 *                  v
 *          CommandProcessor
 *                  |
 *                  v
 *            ConsoleLoop
 * </pre>
 */
public final class SlotDemoApplication {

    /**
     * Creates and starts the console demo application.
     *
     * @param args command-line arguments; currently not used
     */
    public static void main(String[] args) {
        SlotConfig config = new SlotConfigLoader().load();
        DemoSlot slot = new DemoSlot(config);
        HintHandler hintHandler = new HintHandler(
                config.field().reelBank(),
                config.lines()
        );

        ConsoleRenderer renderer = new ConsoleRenderer(System.out);
        ConsoleLoop consoleLoop = getConsoleLoop(slot, hintHandler, renderer);
        consoleLoop.run();
    }

    private static ConsoleLoop getConsoleLoop(
            DemoSlot slot,
            HintHandler hintHandler,
            ConsoleRenderer renderer) {

        SpinCommand spinCommand = new SpinCommand(slot, renderer);
        HintCommand hintCommand = new HintCommand(hintHandler, renderer);

        CommandProcessor commandProcessor = new CommandProcessor(
                spinCommand,
                hintCommand,
                renderer
        );

        Scanner scanner = new Scanner(System.in);
        return new ConsoleLoop(scanner, System.out, commandProcessor);
    }
}
