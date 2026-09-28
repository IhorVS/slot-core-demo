package ivs.games.accessories.slot.demo.config.combination;

import ivs.games.accessories.slot.demo.config.YamlConfigReader;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Loads and validates raw combination configuration from a YAML resource.
 *
 * <p>The loader reads wild symbols and grouped linear and scatter combinations.
 * Each combination contains an identifier, a symbol list, and a prize identifier
 * list.</p>
 *
 * <p>Combination identifiers must form a continuous sequence starting with
 * {@code 0}. Linear combinations are validated first, followed by scatter
 * combinations.</p>
 *
 * <p>The resulting string-based configuration is converted to slot-core domain
 * objects by {@link CombinationConfigParser}.</p>
 */
@RequiredArgsConstructor
public final class CombinationConfigLoader {

    private final YamlConfigReader reader;

    /**
     * Loads the raw combination configuration from a classpath resource.
     *
     * @param fileName resource path, for example
     *                 "demoslot/combinations.yaml"
     * @return wild symbols and grouped combination descriptions
     * @throws NullPointerException     if {@code fileName} is {@code null}
     * @throws IllegalArgumentException if {@code fileName} is blank
     * @throws IllegalStateException    if the resource cannot be read or its
     *                                  combination structure is invalid
     */
    public RawCombinationConfig load(String fileName) {
        Validate.notBlank(fileName, "Resource name must not be null or blank");

        try {
            Map<String, Object> document = reader.read(fileName);

            List<String> wildSymbols = getWildSymbols(document);
            Map<String, Object> combinations = getCombinations(document);

            Map<String, List<RawCombination>> linear = getGroups(combinations, "linear");
            Map<String, List<RawCombination>> scatter = getGroups(combinations, "scatter");

            validateIds(linear, scatter);

            return new RawCombinationConfig(wildSymbols, linear, scatter);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Could not load config file: %s".formatted(fileName), e);
        }
    }

    /*
     * Reads the required non-empty list of wild symbol names.
     */
    private List<String> getWildSymbols(Map<String, Object> document) {
        Object value = document.get("wildSymbols");

        if (!(value instanceof List<?> symbols) || symbols.isEmpty()) {
            throw new IllegalArgumentException("'wildSymbols' must be a non-empty list");
        }
        return getStrings(symbols, "wildSymbols");
    }

    /*
     * Reads the object containing linear and scatter combination groups.
     */
    private Map<String, Object> getCombinations(Map<String, Object> document) {
        Object value = document.get("combinations");
        if (!(value instanceof Map<?, ?> configuredCombinations)) {
            throw new IllegalArgumentException("'combinations' must be an object");
        }

        return getStringKeyMap(configuredCombinations, "combinations");
    }

    /*
     * Reads all groups belonging to one combination type and preserves their
     * declaration order.
     */
    private Map<String, List<RawCombination>> getGroups(Map<String, Object> combinations, String combinationType) {
        Object value = combinations.get(combinationType);
        if (!(value instanceof Map<?, ?> configuredGroups) || configuredGroups.isEmpty()) {
            throw new IllegalArgumentException(
                    "'combinations.%s' must be a non-empty object".formatted(combinationType));
        }

        Map<String, Object> groups = getStringKeyMap(configuredGroups, "combinations." + combinationType);

        Map<String, List<RawCombination>> result = new LinkedHashMap<>();

        groups.forEach((groupId, configuredCombinations) ->
                result.put(groupId, getGroup(configuredCombinations, combinationType, groupId))
        );
        return Collections.unmodifiableMap(result);
    }

    /*
     * Reads one required non-empty list of combinations belonging to a group.
     */
    private List<RawCombination> getGroup(Object value, String combinationType, String groupId) {
        String path = "combinations.%s.%s".formatted(combinationType, groupId);

        if (!(value instanceof List<?> configuredCombinations) || configuredCombinations.isEmpty()) {
            throw new IllegalArgumentException("'%s' must be a non-empty list".formatted(path));
        }

        return IntStream.range(0, configuredCombinations.size())
                .mapToObj(index -> getCombination(
                        configuredCombinations.get(index),
                        path,
                        index
                ))
                .toList();
    }

    /*
     * Reads one combination object and extracts its identifier, symbols, and
     * prize identifiers.
     */
    private RawCombination getCombination(Object value, String groupPath, int combinationIndex) {
        String path = "%s[%d]".formatted(groupPath, combinationIndex);

        if (!(value instanceof Map<?, ?> configuredCombination)) {
            throw new IllegalArgumentException("'%s' must be an object".formatted(path));
        }

        Map<String, Object> combination = getStringKeyMap(configuredCombination, path);

        return new RawCombination(
                getRequiredId(combination, path),
                getRequiredStrings(combination, "symbols", path),
                getRequiredStrings(combination, "prizes", path)
        );
    }

    /*
     * Reads the required integer identifier of one combination.
     */
    private int getRequiredId(Map<String, Object> combination, String combinationPath) {
        String path = combinationPath + ".id";
        Object value = combination.get("id");

        if (!(value instanceof Integer id)) {
            throw new IllegalArgumentException("'%s' must be an integer".formatted(path));
        }

        return id;
    }

    /*
     * Reads a required non-empty list of strings from a combination property.
     */
    private List<String> getRequiredStrings(Map<String, Object> object, String propertyName, String objectPath) {
        String path = objectPath + "." + propertyName;
        Object value = object.get(propertyName);

        if (!(value instanceof List<?> values) || values.isEmpty()) {
            throw new IllegalArgumentException("'%s' must be a non-empty list".formatted(path));
        }
        return getStrings(values, path);
    }

    /*
     * Converts an untyped YAML sequence into a list of validated strings.
     */
    private List<String> getStrings(List<?> values, String path) {
        return IntStream.range(0, values.size())
                .mapToObj(index -> getString(values.get(index), path, index))
                .toList();
    }

    /*
     * Validates and returns one non-blank string from a YAML sequence.
     */
    private String getString(Object value, String path, int index) {
        if (!(value instanceof String string) || string.isBlank()) {
            throw new IllegalArgumentException("'%s[%d]' must be a non-blank string".formatted(path, index));
        }
        return string;
    }

    /*
     * Converts an untyped YAML mapping into an insertion-ordered map with
     * validated string keys.
     */
    private Map<String, Object> getStringKeyMap(Map<?, ?> configuredMap, String path) {
        return configuredMap.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> getStringKey(entry.getKey(), path),
                        Map.Entry::getValue,
                        (first, second) -> second,
                        LinkedHashMap::new
                ));
    }

    /*
     * Validates and returns one non-blank string mapping key.
     */
    private String getStringKey(Object key, String path) {
        if (!(key instanceof String stringKey) || stringKey.isBlank()) {
            throw new IllegalArgumentException("'%s' keys must be non-blank strings".formatted(path));
        }
        return stringKey;
    }

    /*
     * Validates continuous global IDs in declaration order across linear and
     * scatter groups. Combinations with different IDs must have different symbol
     * sequences; symbols are compared in order, regardless of combination type.
     */
    private void validateIds(
            Map<String, List<RawCombination>> linear,
            Map<String, List<RawCombination>> scatter
    ) {
        int expectedId = 0;
        Map<List<String>, Integer> idsBySymbols = new HashMap<>();

        for (Map<String, List<RawCombination>> groups : List.of(linear, scatter)) {
            for (List<RawCombination> combinations : groups.values()) {
                for (RawCombination combination : combinations) {
                    Validate.isTrue(
                            combination.id() == expectedId,
                            "Combination ID must be %d, but was %d",
                            expectedId,
                            combination.id()
                    );

                    Integer previousId = idsBySymbols.putIfAbsent(
                            combination.symbols(),
                            combination.id()
                    );
                    Validate.isTrue(
                            previousId == null,
                            "Combinations %d and %d have identical symbol sequences",
                            previousId,
                            combination.id()
                    );

                    expectedId++;
                }
            }
        }
    }
}
