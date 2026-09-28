package ivs.games.accessories.slot.demo.config;

import ivs.game.accessories.slot.matcher.FieldPosition;
import ivs.game.accessories.slot.matcher.linear.FieldLine;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static ivs.game.accessories.slot.reel.impl.StandardReelItem.WLD;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SlotConfigLoaderTest {

    private final SlotConfigLoader loader = new SlotConfigLoader();

    @Test
    void loadsSlotConfig() {
        SlotConfig config = loader.load();

        assertAll(
                () -> assertEquals(3, config.field().fieldHeight()),
                () -> assertEquals(5, config.field().reelBank().size()),

                () -> assertEquals(
                        List.of(
                                List.of(
                                        new FieldPosition(0, 0),
                                        new FieldPosition(1, 0),
                                        new FieldPosition(2, 0),
                                        new FieldPosition(3, 0),
                                        new FieldPosition(4, 0)
                                ),
                                List.of(
                                        new FieldPosition(0, 1),
                                        new FieldPosition(1, 1),
                                        new FieldPosition(2, 1),
                                        new FieldPosition(3, 1),
                                        new FieldPosition(4, 1)
                                ),
                                List.of(
                                        new FieldPosition(0, 2),
                                        new FieldPosition(1, 2),
                                        new FieldPosition(2, 2),
                                        new FieldPosition(3, 2),
                                        new FieldPosition(4, 2)
                                )
                        ),
                        config.lines().stream()
                                .map(FieldLine::positions)
                                .toList()
                ),

                () -> assertEquals(
                        List.of(WLD),
                        config.combinations().wildSymbols()
                ),
                () -> assertEquals(
                        Set.of(0, 1, 2),
                        config.combinations().linear().keySet()
                ),
                () -> assertEquals(
                        Set.of(3, 4, 5, 6, 7, 8),
                        config.combinations().scatter().keySet()
                ),
                () -> assertEquals(
                        "A",
                        config.combinations().linear().get(0).getGroupId()
                ),
                () -> assertEquals(
                        "SCT",
                        config.combinations().scatter().get(3).getGroupId()
                ),
                () -> assertEquals(
                        "MUL",
                        config.combinations().scatter().get(6).getGroupId()
                ),
                () -> assertEquals(
                        9,
                        config.combinations().prizes().size()
                )
        );
    }
}
