package ivs.games.accessories.slot.demo.engine;

import ivs.game.accessories.slot.reel.impl.StandardReelItem;

import java.util.List;

/**
 * Contains reel positions calculated for a hint request.
 *
 * @param symbols       symbols placed on the selected line from left to right
 * @param lineId        identifier of the selected line
 * @param reelPositions calculated physical positions for the corresponding reels
 */
public record HintResult(
        List<StandardReelItem> symbols,
        int lineId,
        List<Integer> reelPositions
) {

    /**
     * Creates an immutable hint result.
     *
     * @throws NullPointerException if a list or one of its elements is {@code null}
     */
    public HintResult {
        symbols = List.copyOf(symbols);
        reelPositions = List.copyOf(reelPositions);
    }
}