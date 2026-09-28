package ivs.games.accessories.slot.demo.config.combination;

import java.util.List;
import java.util.Map;

/**
 * Contains the complete string-based combination configuration loaded from YAML.
 *
 * <p>The record is an intermediate representation between the untyped YAML
 * document and the parsed {@link CombinationConfig} containing slot-core domain
 * objects.</p>
 *
 * <pre>
 * RawCombinationConfig
 * |
 * +-- wildSymbols
 * |       |
 * |       +-- [WLD]
 * |
 * +-- linear
 * |       |
 * |       +-- group ID -> List&lt;RawCombination&gt;
 * |
 * +-- scatter
 *         |
 *         +-- group ID -> List&lt;RawCombination&gt;
 * </pre>
 *
 * <p>Group declaration order is preserved so that globally sequential
 * combination identifiers can be validated consistently.</p>
 *
 * @param wildSymbols names of symbols used as wild substitutes
 * @param linear      raw linear combinations grouped by group identifier
 * @param scatter     raw scatter combinations grouped by group identifier
 */
public record RawCombinationConfig(
        List<String> wildSymbols,
        Map<String, List<RawCombination>> linear,
        Map<String, List<RawCombination>> scatter
) {
}