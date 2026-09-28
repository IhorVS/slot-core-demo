package ivs.games.accessories.slot.demo.engine;

import ivs.game.accessories.slot.matcher.linear.FieldLine;
import ivs.game.accessories.slot.reel.ReelBank;
import ivs.game.accessories.slot.reel.impl.StandardReelItem;
import lombok.NonNull;

import java.util.List;

/**
 * Provides hint calculation independently of the demo slot.
 *
 * <p>The handler uses configured field lines and physical reels to find
 * positions that place the requested symbols on a selected line.</p>
 */
public final class HintHandler {

    private final HintResolver resolver;

    /**
     * Creates a hint handler from the components needed for position search.
     *
     * @param reelBank configured physical reels
     * @param lines    configured field lines
     * @throws NullPointerException if an argument is {@code null}
     */
    public HintHandler(
            @NonNull ReelBank<StandardReelItem> reelBank,
            @NonNull List<FieldLine> lines) {

        resolver = new HintResolver(reelBank, lines);
    }

    /**
     * Finds physical reel positions for the supplied symbols and line.
     *
     * @param symbols symbols to place from left to right
     * @param lineId  identifier of the selected line
     * @return calculated hint result
     * @throws NullPointerException     if {@code symbols} is {@code null}
     * @throws IllegalArgumentException if the line or requested symbols are invalid
     */
    public HintResult hint(@NonNull List<StandardReelItem> symbols, int lineId) {
        return resolver.resolve(symbols, lineId);
    }
}
