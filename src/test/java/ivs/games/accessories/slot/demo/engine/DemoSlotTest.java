package ivs.games.accessories.slot.demo.engine;

import ivs.game.accessories.slot.field.SlotField;
import ivs.game.accessories.slot.matcher.Combination;
import ivs.game.accessories.slot.matcher.FieldPosition;
import ivs.game.accessories.slot.matcher.linear.FieldLine;
import ivs.game.accessories.slot.matcher.linear.LinearCombinationMatch;
import ivs.game.accessories.slot.matcher.scatter.ScatterCombinationMatch;
import ivs.game.accessories.slot.reel.Reel;
import ivs.game.accessories.slot.reel.impl.StandardReel;
import ivs.game.accessories.slot.reel.impl.StandardReelBank;
import ivs.game.accessories.slot.reel.impl.StandardReelItem;
import ivs.games.accessories.slot.demo.config.SlotConfig;
import ivs.games.accessories.slot.demo.config.combination.CombinationConfig;
import ivs.games.accessories.slot.demo.config.field.FieldConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ivs.game.accessories.slot.reel.impl.StandardReelItem.A;
import static ivs.game.accessories.slot.reel.impl.StandardReelItem.K;
import static ivs.game.accessories.slot.reel.impl.StandardReelItem.MUL;
import static ivs.game.accessories.slot.reel.impl.StandardReelItem.Q;
import static ivs.game.accessories.slot.reel.impl.StandardReelItem.SCT;
import static ivs.game.accessories.slot.reel.impl.StandardReelItem.WLD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the demo slot engine using a small deterministic configuration.
 *
 * <p>The test slot contains three reels with four symbols on each reel,
 * one horizontal line, one linear combination, and one scatter combination.</p>
 */
class DemoSlotTest {

    /*
     * Physical reel positions:
     *
     *             0    1    2    3
     * Reel 0:     A    K   SCT  WLD
     * Reel 1:     A    Q   SCT  MUL
     * Reel 2:     A    K   SCT   Q
     */
    private static final List<List<StandardReelItem>> REELS = List.of(
            List.of(A, K, SCT, WLD),
            List.of(A, Q, SCT, MUL),
            List.of(A, K, SCT, Q)
    );

    private static final Combination<StandardReelItem> LINEAR_A = new Combination<>(
            0,
            "A",
            List.of(A, A, A)
    );

    private static final Combination<StandardReelItem> SCATTER_SCT = new Combination<>(
            1,
            "SCT",
            List.of(SCT, SCT, SCT)
    );

    private DemoSlot slot;

    @BeforeEach
    void setUp() {
        slot = new DemoSlot(createConfig());
    }

    /**
     * Verifies that explicitly supplied positions are preserved and missing positions
     * are generated within the valid ranges of the corresponding reels.
     */
    @Test
    void completesMissingPositionsRandomly() {
        SpinResult result = slot.spin(List.of(1));

        assertEquals(3, result.reelPositions().size());
        assertEquals(1, result.reelPositions().getFirst());

        for (int reelIndex = 1; reelIndex < REELS.size(); reelIndex++) {
            int position = result.reelPositions().get(reelIndex);

            assertTrue(position >= 0);
            assertTrue(position < REELS.get(reelIndex).size());
        }
    }

    /**
     * Verifies that the field contains the symbols selected by the supplied physical
     * reel positions.
     */
    @Test
    void buildsFieldFromSpecifiedPositions() {
        SpinResult result = slot.spin(List.of(1, 1, 3));
        SlotField<StandardReelItem> field = result.field();

        assertEquals(3, field.getColumnCount());
        assertEquals(1, field.getColumnSize(0));
        assertEquals(K, field.getItem(0, 0));
        assertEquals(Q, field.getItem(1, 0));
        assertEquals(Q, field.getItem(2, 0));
    }

    /**
     * Verifies that three A symbols on the configured line produce the expected
     * linear combination match.
     */
    @Test
    void detectsLinearWin() {
        SpinResult result = slot.spin(List.of(0, 0, 0));
        CombinationWin win = getOnlyWin(result);

        assertInstanceOf(LinearCombinationMatch.class, win.match());
        assertEquals(LINEAR_A, win.match().combination());
    }

    /**
     * Verifies that three SCT symbols anywhere on the field produce the expected
     * scatter combination match.
     */
    @Test
    void detectsScatterWin() {
        SpinResult result = slot.spin(List.of(2, 2, 2));
        CombinationWin win = getOnlyWin(result);

        assertInstanceOf(ScatterCombinationMatch.class, win.match());
        assertEquals(SCATTER_SCT, win.match().combination());
    }

    /**
     * Verifies that a field without configured linear or scatter combinations
     * produces no wins.
     */
    @Test
    void returnsNoWinsWhenFieldHasNoCombinations() {
        SpinResult result = slot.spin(List.of(1, 1, 3));

        assertTrue(result.wins().isEmpty());
    }

    /**
     * Verifies that a detected combination is associated with its configured prizes.
     */
    @Test
    void returnsConfiguredPrizes() {
        SpinResult result = slot.spin(List.of(0, 0, 0));
        CombinationWin win = getOnlyWin(result);

        assertEquals(List.of("C10"), win.prizes());
    }

    /**
     * Returns the only detected win and fails the test when the number of wins differs.
     */
    private CombinationWin getOnlyWin(SpinResult result) {
        assertEquals(1, result.wins().size());
        return result.wins().getFirst();
    }

    /**
     * Creates the deterministic slot configuration shared by all tests.
     */
    private SlotConfig createConfig() {
        List<Reel<StandardReelItem>> reels = new ArrayList<>();

        for (List<StandardReelItem> items : REELS) {
            reels.add(new StandardReel<>(items, 1));
        }

        FieldConfig field = new FieldConfig(1, new StandardReelBank<>(reels));
        FieldLine line = new FieldLine(
                0,
                List.of(
                        new FieldPosition(0, 0),
                        new FieldPosition(1, 0),
                        new FieldPosition(2, 0)
                )
        );
        CombinationConfig combinations = new CombinationConfig(
                List.of(WLD),
                Map.of(0, LINEAR_A),
                Map.of(1, SCATTER_SCT),
                Map.of(
                        LINEAR_A, List.of("C10"),
                        SCATTER_SCT, List.of("C15", "FS3")
                )
        );

        return new SlotConfig(field, List.of(line), combinations);
    }
}
