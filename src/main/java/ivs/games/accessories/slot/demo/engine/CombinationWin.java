package ivs.games.accessories.slot.demo.engine;

import ivs.game.accessories.slot.matcher.CombinationMatch;
import ivs.game.accessories.slot.reel.impl.StandardReelItem;

import java.util.List;
import java.util.Objects;

/**
 * Associates a detected combination match with its configured prize identifiers.
 *
 * <p>The class does not interpret or apply prizes. Prize identifiers are preserved
 * exactly as they are defined in the demo configuration.</p>
 *
 * @param match  accepted combination match
 * @param prizes configured prize identifiers
 */
public record CombinationWin(
        CombinationMatch<StandardReelItem> match,
        List<String> prizes
) {

    /**
     * Creates an immutable combination win.
     *
     * @throws NullPointerException if {@code match}, {@code prizes}, or a prize element
     *                              is {@code null}
     */
    public CombinationWin {
        Objects.requireNonNull(match, "Combination match must not be null");
        prizes = List.copyOf(prizes);
    }
}