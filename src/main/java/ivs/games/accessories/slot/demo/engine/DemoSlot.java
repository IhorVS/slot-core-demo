package ivs.games.accessories.slot.demo.engine;

import ivs.game.accessories.slot.field.SlotField;
import ivs.game.accessories.slot.field.impl.ReelBankFieldBuilder;
import ivs.game.accessories.slot.reel.ReelBank;
import ivs.game.accessories.slot.reel.impl.StandardReelItem;
import ivs.games.accessories.slot.demo.config.SlotConfig;
import lombok.NonNull;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Provides the main entry point to the demo slot engine.
 *
 * <p>The slot is created from a {@link SlotConfig}. It owns the configured reel bank
 * and coordinates field generation, combination evaluation, and prize resolution.</p>
 *
 * <p>The internal components interact as follows:</p>
 *
 * <pre>
 *              DemoSlot
 *                  |
 *                  v
 *       ReelPositionResolver
 *                  |
 *                  v
 *       ReelBankFieldBuilder
 *                  |
 *                  v
 *       CombinationEvaluator
 *                  |
 *                  v
 *              SpinResult
 * </pre>
 *
 * <p>The class acts as a facade. Details such as position validation, random position
 * generation, combination matching, and prize lookup are delegated to specialized
 * components.</p>
 */
public final class DemoSlot {

    private final ReelBank<StandardReelItem> reelBank;
    private final ReelBankFieldBuilder<StandardReelItem> fieldBuilder;
    private final ReelPositionResolver reelPositionResolver;
    private final CombinationEvaluator combinationEvaluator;

    /**
     * Creates a demo slot from the supplied configuration.
     *
     * @param config field, line, combination, and prize configuration
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public DemoSlot(@NonNull SlotConfig config) {
        reelBank = config.field().reelBank();
        fieldBuilder = new ReelBankFieldBuilder<>(reelBank);

        reelPositionResolver = new ReelPositionResolver(
                reelBank,
                RandomGenerator.getDefault()
        );
        combinationEvaluator = new CombinationEvaluator(
                config.lines(),
                config.combinations()
        );
    }

    /**
     * Returns the number of reels in the slot.
     *
     * @return the reel count
     */
    public int getReelCount() {
        return reelBank.size();
    }

    /**
     * Performs a spin using the supplied physical reel positions.
     *
     * <p>Positions are assigned to reels from left to right. If fewer positions than
     * reels are supplied, the remaining positions are generated randomly.</p>
     *
     * @param specifiedPositions physical positions supplied from left to right
     * @return generated field, actual reel positions, and detected wins
     * @throws NullPointerException     if {@code specifiedPositions} is {@code null}
     * @throws IllegalArgumentException if too many positions are supplied or a position
     *                                  is outside the corresponding reel
     */
    public SpinResult spin(@NonNull List<Integer> specifiedPositions) {
        List<Integer> actualPositions = reelPositionResolver.resolve(specifiedPositions);
        SlotField<StandardReelItem> field = generateField(actualPositions);
        List<CombinationWin> wins = combinationEvaluator.evaluate(field);

        return new SpinResult(actualPositions, field, wins);
    }

    private SlotField<StandardReelItem> generateField(List<Integer> reelPositions) {
        int[] positions = reelPositions.stream()
                .mapToInt(Integer::intValue)
                .toArray();

        return fieldBuilder.build(positions);
    }
}
