package ivs.games.accessories.slot.demo.config.combination;

import ivs.game.accessories.slot.mapping.combination.CombinationDescription;
import ivs.game.accessories.slot.mapping.combination.CombinationListMapper;
import ivs.game.accessories.slot.mapping.combination.impl.StandardCombinationListMapper;
import ivs.game.accessories.slot.matcher.Combination;
import ivs.game.accessories.slot.reel.impl.StandardReelItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts raw combination configuration into slot-core domain objects.
 *
 * <p>Linear and scatter descriptions are mapped separately. The parser also
 * associates configured prize identifiers with the resulting combinations.</p>
 */
public final class CombinationConfigParser {

    private final CombinationListMapper<StandardReelItem> combinationMapper =
            StandardCombinationListMapper.forNames();

    /**
     * Parses wild symbols, combinations, and their prize identifiers.
     *
     * @param rawConfig raw string-based combination configuration
     * @return parsed combination configuration
     * @throws IllegalStateException if the configuration cannot be converted
     */
    public CombinationConfig parse(RawCombinationConfig rawConfig) {
        try {
            Map<Combination<StandardReelItem>, List<String>> prizes = new LinkedHashMap<>();

            Map<Integer, Combination<StandardReelItem>> linear = parseGroups(rawConfig.linear(), prizes);
            Map<Integer, Combination<StandardReelItem>> scatter = parseGroups(rawConfig.scatter(), prizes);

            return new CombinationConfig(
                    parseSymbols(rawConfig.wildSymbols()),
                    linear,
                    scatter,
                    Collections.unmodifiableMap(prizes)
            );
        } catch (RuntimeException e) {
            throw new IllegalStateException("Could not parse combination config", e);
        }
    }

    /*
     * Creates descriptions in group declaration order, maps them, and links
     * the mapped combinations to their configured prizes.
     */
    private Map<Integer, Combination<StandardReelItem>> parseGroups(
            Map<String, List<RawCombination>> rawGroups,
            Map<Combination<StandardReelItem>, List<String>> prizes) {

        List<CombinationDescription> descriptions = new ArrayList<>();
        Map<Integer, List<String>> prizesById = new LinkedHashMap<>();

        for (Map.Entry<String, List<RawCombination>> group : rawGroups.entrySet()) {
            for (RawCombination rawCombination : group.getValue()) {
                descriptions.add(new CombinationDescription(
                        rawCombination.id(),
                        group.getKey(),
                        rawCombination.symbols()
                ));
                prizesById.put(rawCombination.id(), List.copyOf(rawCombination.prizes()));
            }
        }

        Map<Integer, Combination<StandardReelItem>> combinations =
                combinationMapper.map(descriptions);

        for (Combination<StandardReelItem> combination : combinations.values()) {
            prizes.put(combination, prizesById.get(combination.getId()));
        }

        return combinations;
    }

    /*
     * Converts configured wild symbol names to standard reel item values.
     */
    private List<StandardReelItem> parseSymbols(List<String> symbols) {
        return symbols.stream()
                .map(StandardReelItem::valueOf)
                .toList();
    }
}
