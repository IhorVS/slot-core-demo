package ivs.games.accessories.slot.demo.engine;

import ivs.game.accessories.slot.reel.ReelBank;
import ivs.game.accessories.slot.reel.impl.StandardReelItem;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Resolves the complete set of physical reel positions for a spin.
 *
 * <p>The resolver validates explicitly supplied positions and appends random valid
 * positions for reels whose positions were not supplied.</p>
 *
 * <p>The returned list always contains exactly one position for every reel.</p>
 */
final class ReelPositionResolver {

    private final ReelBank<StandardReelItem> reelBank;
    private final RandomGenerator random;

    /**
     * Creates a resolver for the supplied reel bank.
     *
     * @param reelBank source of reel counts and physical reel sizes
     * @param random   random generator used for missing positions
     * @throws NullPointerException if an argument is {@code null}
     */
    ReelPositionResolver(
            @NonNull ReelBank<StandardReelItem> reelBank,
            @NonNull RandomGenerator random) {

        this.reelBank = reelBank;
        this.random = random;
    }

    /**
     * Validates supplied positions and completes the missing positions randomly.
     *
     * @param specifiedPositions physical positions supplied from left to right
     * @return an unmodifiable complete list of reel positions
     * @throws NullPointerException     if {@code specifiedPositions} is {@code null}
     * @throws IllegalArgumentException if too many positions are supplied, a position
     *                                  is {@code null}, or a position is outside its reel
     */
    List<Integer> resolve(@NonNull List<Integer> specifiedPositions) {
        int reelBankSize = reelBank.size();

        Validate.isTrue(
                specifiedPositions.size() <= reelBankSize,
                "Expected no more than %d reel positions, but received %d.",
                reelBankSize,
                specifiedPositions.size()
        );

        for (int reelIndex = 0; reelIndex < specifiedPositions.size(); reelIndex++) {
            validatePosition(reelIndex, specifiedPositions.get(reelIndex));
        }

        return Stream.concat(
                specifiedPositions.stream(),
                IntStream.range(specifiedPositions.size(), reelBankSize)
                        .mapToObj(this::randomPosition)
        ).toList();
    }

    private void validatePosition(int reelIndex, Integer position) {
        Validate.isTrue(position != null, "Position for reel %d must not be null.", reelIndex);

        int reelSize = reelBank.getReelSize(reelIndex);
        Validate.isTrue(
                position >= 0 && position < reelSize,
                "Position %d is outside reel %d. Valid positions: 0..%d.",
                position,
                reelIndex,
                reelSize - 1
        );
    }

    private int randomPosition(int reelIndex) {
        return random.nextInt(reelBank.getReelSize(reelIndex));
    }
}
