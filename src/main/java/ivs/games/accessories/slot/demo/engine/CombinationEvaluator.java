package ivs.games.accessories.slot.demo.engine;

import ivs.game.accessories.slot.field.SlotField;
import ivs.game.accessories.slot.matcher.Combination;
import ivs.game.accessories.slot.matcher.CombinationMatch;
import ivs.game.accessories.slot.matcher.CombinationMatcher;
import ivs.game.accessories.slot.matcher.linear.FieldLine;
import ivs.game.accessories.slot.matcher.linear.LinearCombinationMatcher;
import ivs.game.accessories.slot.matcher.orchestration.SequentialCombinationMatcherOrchestrator;
import ivs.game.accessories.slot.matcher.policy.LongestCombinationMatchPolicy;
import ivs.game.accessories.slot.matcher.scatter.ScatterCombinationMatcher;
import ivs.game.accessories.slot.reel.impl.StandardReelItem;
import ivs.games.accessories.slot.demo.config.combination.CombinationConfig;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Evaluates winning combinations on a generated slot field.
 *
 * <p>Linear combinations are evaluated independently on every configured line.
 * Scatter combinations are evaluated across the entire field. The longest match
 * policy is applied within each combination group.</p>
 *
 * <p>Every accepted match is associated with the prize identifiers loaded from
 * the demo configuration.</p>
 */
final class CombinationEvaluator {

    private final List<FieldLine> lines;
    private final CombinationConfig combinations;
    private final SequentialCombinationMatcherOrchestrator<StandardReelItem> matcherOrchestrator;

    /**
     * Creates an evaluator from configured lines, combinations, and prizes.
     *
     * @param lines        lines used for linear combination matching
     * @param combinations linear and scatter combinations with their prizes
     * @throws NullPointerException if an argument is {@code null}
     */
    CombinationEvaluator(
            @NonNull List<FieldLine> lines,
            @NonNull CombinationConfig combinations) {

        this.lines = List.copyOf(lines);
        this.combinations = combinations;
        matcherOrchestrator = new SequentialCombinationMatcherOrchestrator<>();
    }

    /**
     * Finds all accepted combination matches and resolves their configured prizes.
     *
     * @param field generated slot field
     * @return detected wins in matcher execution order
     * @throws NullPointerException  if {@code field} is {@code null}
     * @throws IllegalStateException if a matched combination has no configured prizes
     */
    List<CombinationWin> evaluate(@NonNull SlotField<StandardReelItem> field) {
        List<CombinationMatch<StandardReelItem>> matches = findMatches(field);

        return matches.stream()
                .map(match -> new CombinationWin(match, getPrizes(match)))
                .toList();
    }

    private List<CombinationMatch<StandardReelItem>> findMatches(
            SlotField<StandardReelItem> field) {

        List<CombinationMatcher<
                StandardReelItem,
                ? extends CombinationMatch<StandardReelItem>>> matchers = new ArrayList<>();

        List<Combination<StandardReelItem>> linearCombinations =
                List.copyOf(combinations.linear().values());

        Set<StandardReelItem> wildSymbols = Set.copyOf(combinations.wildSymbols());

        for (FieldLine line : lines) {
            matchers.add(new LinearCombinationMatcher<>(
                    field,
                    List.of(line),
                    linearCombinations,
                    wildSymbols,
                    new LongestCombinationMatchPolicy<>()
            ));
        }

        matchers.add(new ScatterCombinationMatcher<>(
                field,
                List.copyOf(combinations.scatter().values()),
                new LongestCombinationMatchPolicy<>()
        ));

        return matcherOrchestrator.match(matchers);
    }

    private List<String> getPrizes(CombinationMatch<StandardReelItem> match) {
        List<String> prizes = combinations.prizes().get(match.combination());
        Validate.validState(prizes != null,
                "Prizes are not configured for combination %d",
                match.combination().getId());
        return prizes;
    }
}
