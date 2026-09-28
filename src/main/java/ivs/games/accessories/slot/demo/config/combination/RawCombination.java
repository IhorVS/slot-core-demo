package ivs.games.accessories.slot.demo.config.combination;

import java.util.List;

/**
 * Contains one string-based combination loaded from the YAML configuration.
 *
 * <p>The record represents an intermediate configuration form. Symbol names are
 * converted to reel item objects later by {@link CombinationConfigParser}.</p>
 *
 * <pre>
 * RawCombination
 * |
 * +-- id
 * |
 * +-- symbols
 * |       |
 * |       +-- [A, A, A]
 * |
 * +-- prizes
 *         |
 *         +-- [C10, FS1]
 * </pre>
 *
 * @param id      globally unique sequential combination identifier
 * @param symbols symbol names forming the combination
 * @param prizes  prize identifiers associated with the combination
 */
record RawCombination(
        int id,
        List<String> symbols,
        List<String> prizes
) {
}