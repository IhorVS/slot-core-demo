package ivs.games.accessories.slot.demo.engine;

import ivs.game.accessories.slot.matcher.linear.FieldLine;
import ivs.game.accessories.slot.reel.ReelBank;
import ivs.game.accessories.slot.reel.impl.StandardReelItem;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates physical reel positions for the hint command.
 *
 * <p>Each supplied symbol is assigned to the corresponding position of the selected
 * line from left to right. The resolver searches every physical position of the
 * corresponding reel and returns the first position that places the symbol on the
 * required field row.</p>
 *
 * <p>Hint calculation is independent of configured winning combinations.</p>
 */
final class HintResolver {

    private final ReelBank<StandardReelItem> reelBank;
    private final List<FieldLine> lines;

    /**
     * Creates a hint resolver.
     *
     * @param reelBank reel bank containing the configured physical reels
     * @param lines    configured field lines
     * @throws NullPointerException if an argument is {@code null}
     */
    HintResolver(
            @NonNull ReelBank<StandardReelItem> reelBank,
            @NonNull List<FieldLine> lines) {

        this.reelBank = reelBank;
        this.lines = List.copyOf(lines);
    }

    /**
     * Calculates reel positions for the supplied symbols and line.
     *
     * @param symbols symbols to place on the selected line
     * @param lineId  identifier of the selected line
     * @return calculated hint result
     * @throws NullPointerException     if {@code symbols} is {@code null}
     * @throws IllegalArgumentException if the line does not exist, no symbols are supplied,
     *                                  too many symbols are supplied, or a symbol is absent
     *                                  from the corresponding reel
     */
    HintResult resolve(
            @NonNull List<StandardReelItem> symbols,
            int lineId) {

        FieldLine line = getLine(lineId);
        validateSymbolsCount(symbols, line);

        List<Integer> reelPositions = new ArrayList<>(symbols.size());

        for (int symbolIndex = 0; symbolIndex < symbols.size(); symbolIndex++) {
            int reelIndex = line.positions().get(symbolIndex).column();
            int row = line.positions().get(symbolIndex).row();
            StandardReelItem symbol = symbols.get(symbolIndex);

            reelPositions.add(findReelPosition(reelIndex, row, symbol));
        }

        return new HintResult(symbols, lineId, reelPositions);
    }

    private FieldLine getLine(int lineId) {
        return lines.stream()
                .filter(line -> line.id() == lineId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Line %d does not exist.".formatted(lineId)
                ));
    }

    private void validateSymbolsCount(List<StandardReelItem> symbols, FieldLine line) {

        Validate.isTrue(!symbols.isEmpty(), "At least one symbol must be specified.");

        Validate.isTrue(symbols.size() <= line.positions().size(),
                "Expected no more than %d symbols, but received %d.",
                line.positions().size(),
                symbols.size()
        );
    }

    private int findReelPosition(
            int reelIndex,
            int row,
            StandardReelItem symbol) {

        int reelSize = reelBank.getReelSize(reelIndex);

        for (int position = 0; position < reelSize; position++) {
            List<StandardReelItem> visibleItems = reelBank.getItems(reelIndex, position);

            if (visibleItems.get(row) == symbol) {
                return position;
            }
        }

        throw new IllegalArgumentException("Symbol %s was not found on reel %d.".formatted(symbol, reelIndex)
        );
    }
}
