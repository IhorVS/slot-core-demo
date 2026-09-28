package ivs.games.accessories.slot.demo.config.combination;

import ivs.game.accessories.slot.matcher.Combination;
import ivs.game.accessories.slot.reel.impl.StandardReelItem;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static ivs.game.accessories.slot.reel.impl.StandardReelItem.A;
import static ivs.game.accessories.slot.reel.impl.StandardReelItem.MUL;
import static ivs.game.accessories.slot.reel.impl.StandardReelItem.SCT;
import static ivs.game.accessories.slot.reel.impl.StandardReelItem.WLD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CombinationConfigParserTest {

    private static final int ID_3A = 0;
    private static final int ID_4A = 1;
    private static final int ID_5A = 2;
    private static final int ID_3SCT = 3;
    private static final int ID_4SCT = 4;
    private static final int ID_3MUL = 5;

    private final CombinationConfigParser parser = new CombinationConfigParser();

    @Test
    void parsesCombinationConfig() {
        RawCombinationConfig rawConfig = new RawCombinationConfig(
                List.of("WLD"),
                Map.of(
                        "A",
                        List.of(
                                rawCombination(ID_3A, List.of("A", "A", "A"), List.of("C10")),
                                rawCombination(ID_4A, List.of("A", "A", "A", "A"), List.of("C25")),
                                rawCombination(ID_5A, List.of("A", "A", "A", "A", "A"), List.of("C100", "FS1"))
                        )
                ),
                Map.of(
                        "SCT",
                        List.of(
                                rawCombination(ID_3SCT, List.of("SCT", "SCT", "SCT"), List.of("C15", "FS3")),
                                rawCombination(ID_4SCT, List.of("SCT", "SCT", "SCT", "SCT"), List.of("C50"))
                        ),
                        "MUL",
                        List.of(
                                rawCombination(ID_3MUL, List.of("MUL", "MUL", "MUL"), List.of("x2"))
                        )
                )
        );

        CombinationConfig config = parser.parse(rawConfig);

        Combination<StandardReelItem> a3 = combination(ID_3A, "A", A, A, A);
        Combination<StandardReelItem> a4 = combination(ID_4A, "A", A, A, A, A);
        Combination<StandardReelItem> a5 = combination(ID_5A, "A", A, A, A, A, A);
        Combination<StandardReelItem> sct3 = combination(ID_3SCT, "SCT", SCT, SCT, SCT);
        Combination<StandardReelItem> sct4 = combination(ID_4SCT, "SCT", SCT, SCT, SCT, SCT);
        Combination<StandardReelItem> mul3 = combination(ID_3MUL, "MUL", MUL, MUL, MUL);

        assertEquals(List.of(WLD), config.wildSymbols());
        assertEquals(Map.of(ID_3A, a3, ID_4A, a4, ID_5A, a5), config.linear());
        assertEquals(Map.of(ID_3SCT, sct3, ID_4SCT, sct4, ID_3MUL, mul3), config.scatter());

        assertEquals(List.of("C10"), config.prizes().get(a3));
        assertEquals(List.of("C25"), config.prizes().get(a4));
        assertEquals(List.of("C100", "FS1"), config.prizes().get(a5));
        assertEquals(List.of("C15", "FS3"), config.prizes().get(sct3));
        assertEquals(List.of("C50"), config.prizes().get(sct4));
        assertEquals(List.of("x2"), config.prizes().get(mul3));
    }

    private RawCombination rawCombination(int id, List<String> symbols, List<String> prizes) {
        return new RawCombination(id, symbols, prizes);
    }

    private Combination<StandardReelItem> combination(int id, String groupId, StandardReelItem... items) {
        return new Combination<>(id, groupId, items);
    }

    @Test
    void rejectsUnknownCombinationItem() {
        RawCombinationConfig rawConfig = new RawCombinationConfig(
                List.of("WLD"),
                Map.of(
                        "A",
                        List.of(
                                rawCombination(ID_3A, List.of("A", "UNKNOWN", "A"), List.of("C10"))
                        )
                ),
                Map.of()
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> parser.parse(rawConfig));

        assertEquals("Could not parse combination config", exception.getMessage());
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }
}
