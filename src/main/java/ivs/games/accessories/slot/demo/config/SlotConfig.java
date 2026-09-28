package ivs.games.accessories.slot.demo.config;

import ivs.game.accessories.slot.matcher.linear.FieldLine;
import ivs.games.accessories.slot.demo.config.combination.CombinationConfig;
import ivs.games.accessories.slot.demo.config.field.FieldConfig;

import java.util.List;

/**
 * Contains the complete configuration required to create the demo slot.
 *
 * <p>The configuration combines the independently loaded field, line, and
 * combination configurations into one immutable aggregate.</p>
 *
 * <pre>
 *                     SlotConfig
 *                         |
 *          +--------------+--------------+
 *          |              |              |
 *          v              v              v
 *     FieldConfig   List&lt;FieldLine&gt;  CombinationConfig
 *          |              |              |
 *          v              v              v
 *     field size      winning lines   combinations,
 *     and reels                       wild symbols,
 *                                     and prizes
 * </pre>
 *
 * @param field        slot field dimensions and physical reel contents
 * @param lines        configured lines used for linear combination matching
 * @param combinations linear and scatter combinations, wild symbols, and prizes
 */
public record SlotConfig(
        FieldConfig field,
        List<FieldLine> lines,
        CombinationConfig combinations
) {
}