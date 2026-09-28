package ivs.games.accessories.slot.demo.config.combination;

import ivs.game.accessories.slot.matcher.Combination;
import ivs.game.accessories.slot.reel.impl.StandardReelItem;

import java.util.List;
import java.util.Map;

/**
 * Contains mapped combinations, wild symbols, and prize identifiers.
 *
 * <p>Linear and scatter combinations are stored separately and indexed by
 * their combination IDs. Each combination retains its group identifier for
 * match policy processing.</p>
 *
 * @param wildSymbols symbols that may substitute regular symbols in linear matches
 * @param linear      linear combinations indexed by combination ID
 * @param scatter     scatter combinations indexed by combination ID
 * @param prizes      prize identifiers associated with each configured combination
 */
public record CombinationConfig(
        List<StandardReelItem> wildSymbols,
        Map<Integer, Combination<StandardReelItem>> linear,
        Map<Integer, Combination<StandardReelItem>> scatter,
        Map<Combination<StandardReelItem>, List<String>> prizes
) {
}
