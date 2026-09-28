package ivs.games.accessories.slot.demo.config.combination;

import ivs.games.accessories.slot.demo.config.YamlConfigReader;
import ivs.games.accessories.slot.demo.config.YamlReaderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CombinationConfigLoaderTest {

    private static final String PATH = "config/combination/";

    private final CombinationConfigLoader loader =
            new CombinationConfigLoader(new YamlConfigReader(YamlReaderFactory.INSTANCE));

    @Test
    void loadsCombinationConfig() {
        RawCombinationConfig config = loader.load(PATH.concat("combinations-ok.yaml"));

        assertEquals(List.of("WLD"), config.wildSymbols());
        assertEquals(List.of("A"), config.linear().keySet().stream().toList());
        assertEquals(List.of("SCT", "MUL"), config.scatter().keySet().stream().toList());

        assertEquals(List.of(
                combination(0, List.of("A", "A", "A"), List.of("C10")),
                combination(1, List.of("A", "A", "A", "A"), List.of("C25")),
                combination(2, List.of("A", "A", "A", "A", "A"), List.of("C100", "FS1"))
        ), config.linear().get("A"));

        assertEquals(List.of(
                combination(3, List.of("SCT", "SCT", "SCT"), List.of("C15", "FS3")),
                combination(4, List.of("SCT", "SCT", "SCT", "SCT"), List.of("C50")),
                combination(5, List.of("SCT", "SCT", "SCT", "SCT", "SCT"), List.of("C200"))
        ), config.scatter().get("SCT"));

        assertEquals(List.of(
                combination(6, List.of("MUL", "MUL", "MUL"), List.of("x2")),
                combination(7, List.of("MUL", "MUL", "MUL", "MUL"), List.of("x4")),
                combination(8, List.of("MUL", "MUL", "MUL", "MUL", "MUL"), List.of("x10"))
        ), config.scatter().get("MUL"));
    }

    @Test
    void allowsSameSymbolsInDifferentOrder() {
        RawCombinationConfig config = loader.load(PATH.concat("different-symbol-order.yaml"));

        assertEquals(List.of("SCT", "MUL"), config.linear().get("A").getFirst().symbols());
        assertEquals(List.of("MUL", "SCT"), config.scatter().get("SCT").getFirst().symbols());
    }

    private RawCombination combination(int id, List<String> symbols, List<String> prizes) {
        return new RawCombination(id, symbols, prizes);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidConfigs")
    void rejectsInvalidConfig(String fileName, String expectedMessage) {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> loader.load(PATH.concat(fileName))
        );

        IllegalArgumentException cause = assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertEquals(expectedMessage, cause.getMessage());
    }

    private static Stream<Arguments> invalidConfigs() {
        return Stream.of(
                Arguments.of(
                        "empty-wild-symbols.yaml",
                        "'wildSymbols' must be a non-empty list"
                ),
                Arguments.of(
                        "blank-wild-symbol.yaml",
                        "'wildSymbols[0]' must be a non-blank string"
                ),
                Arguments.of(
                        "missing-combinations.yaml",
                        "'combinations' must be an object"
                ),
                Arguments.of(
                        "missing-linear.yaml",
                        "'combinations.linear' must be a non-empty object"
                ),
                Arguments.of(
                        "missing-id.yaml",
                        "'combinations.linear.A[0].id' must be an integer"
                ),
                Arguments.of(
                        "empty-scatter.yaml",
                        "'combinations.scatter' must be a non-empty object"
                ),
                Arguments.of(
                        "blank-group-id.yaml",
                        "'combinations.linear' keys must be non-blank strings"
                ),
                Arguments.of(
                        "empty-group.yaml",
                        "'combinations.linear.A' must be a non-empty list"
                ),
                Arguments.of(
                        "invalid-combination.yaml",
                        "'combinations.linear.A[0]' must be an object"
                ),
                Arguments.of(
                        "blank-prize.yaml",
                        "'combinations.linear.A[0].prizes[0]' must be a non-blank string"
                ),
                Arguments.of(
                        "empty-symbols.yaml",
                        "'combinations.linear.A[0].symbols' must be a non-empty list"
                ),
                Arguments.of(
                        "blank-symbol.yaml",
                        "'combinations.linear.A[0].symbols[1]' must be a non-blank string"
                ),
                Arguments.of(
                        "empty-prizes.yaml",
                        "'combinations.linear.A[0].prizes' must be a non-empty list"
                ),
                Arguments.of(
                        "duplicate-linear-symbols.yaml",
                        "Combinations 0 and 1 have identical symbol sequences"
                ),
                Arguments.of(
                        "duplicate-symbols-across-types.yaml",
                        "Combinations 0 and 1 have identical symbol sequences"
                )
        );
    }
}
