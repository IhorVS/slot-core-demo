package ivs.games.accessories.slot.demo.engine;

import ivs.game.accessories.slot.field.SlotField;
import ivs.game.accessories.slot.reel.impl.StandardReelItem;

import java.util.List;
import java.util.Objects;

/**
 * Contains the complete result of a demo slot spin.
 *
 * @param reelPositions actual physical positions used for all reels
 * @param field         generated visible slot field
 * @param wins          detected combinations and their configured prizes
 */
public record SpinResult(
        List<Integer> reelPositions,
        SlotField<StandardReelItem> field,
        List<CombinationWin> wins
) {

    /**
     * Creates an immutable spin result.
     *
     * @throws NullPointerException if an argument or one of the list elements is
     *                              {@code null}
     */
    public SpinResult {
        reelPositions = List.copyOf(reelPositions);
        Objects.requireNonNull(field, "Field must not be null");
        wins = List.copyOf(wins);
    }
}