package ivs.games.accessories.slot.demo.config;

import ivs.game.accessories.slot.mapping.line.FieldLineListMapper;
import ivs.game.accessories.slot.mapping.line.impl.DefaultFieldLineListMapper;
import ivs.game.accessories.slot.mapping.line.impl.DefaultFieldLineMapper;
import ivs.game.accessories.slot.mapping.reel.impl.StandardReelBankMapper;
import ivs.game.accessories.slot.matcher.FieldPosition;
import ivs.game.accessories.slot.matcher.linear.FieldLine;
import ivs.game.accessories.slot.reel.ReelBank;
import ivs.game.accessories.slot.reel.impl.StandardReelItem;
import ivs.games.accessories.slot.demo.config.combination.CombinationConfig;
import ivs.games.accessories.slot.demo.config.combination.CombinationConfigLoader;
import ivs.games.accessories.slot.demo.config.combination.CombinationConfigParser;
import ivs.games.accessories.slot.demo.config.field.FieldConfig;
import ivs.games.accessories.slot.demo.config.field.FieldConfigLoader;
import ivs.games.accessories.slot.demo.config.field.RawFieldConfig;
import ivs.games.accessories.slot.demo.config.line.LineConfigLoader;
import org.apache.commons.lang3.Validate;

import java.util.List;

/**
 * Loads and assembles the complete demo slot configuration.
 *
 * <p>Configuration resources are read from the {@code demoslot} classpath
 * directory. The field loader extracts reel descriptions, which are passed
 * to the standard reel bank mapper.</p>
 *
 * <pre>
 * demoslot/slotfield.yaml
 *            |
 *            v
 * FieldConfigLoader -> StandardReelBankMapper -> FieldConfig
 *
 * demoslot/lines.yaml
 *            |
 *            v
 * LineConfigLoader -> FieldLineListMapper -> List&lt;FieldLine&gt;
 *
 * demoslot/combinations.yaml
 *            |
 *            v
 * CombinationConfigLoader -> CombinationConfigParser -> CombinationConfig
 *
 *            FieldConfig
 *                 +
 *         List&lt;FieldLine&gt;
 *                 +
 *        CombinationConfig
 *                 |
 *                 v
 *            SlotConfig
 * </pre>
 *
 * <p>Specialized loaders extract and validate the YAML structure. Mappers
 * convert extracted values into slot-core domain objects. The assembled lines
 * are checked against the field dimensions.</p>
 */
public final class SlotConfigLoader {

    private static final String PATH = "demoslot/";
    private static final String FIELD_CONFIG = PATH + "slotfield.yaml";
    private static final String LINE_CONFIG = PATH + "lines.yaml";
    private static final String COMBINATION_CONFIG = PATH + "combinations.yaml";

    private final FieldConfigLoader fieldLoader;
    private final LineConfigLoader lineLoader;
    private final FieldLineListMapper lineMapper;
    private final CombinationConfigLoader combinationLoader;
    private final CombinationConfigParser combinationParser;

    /**
     * Creates a slot configuration loader using the default YAML reader factory.
     */
    public SlotConfigLoader() {
        YamlConfigReader yamlReader = new YamlConfigReader(YamlReaderFactory.INSTANCE);

        fieldLoader = new FieldConfigLoader(yamlReader);

        lineLoader = new LineConfigLoader(yamlReader);
        lineMapper = new DefaultFieldLineListMapper(new DefaultFieldLineMapper());

        combinationLoader = new CombinationConfigLoader(yamlReader);
        combinationParser = new CombinationConfigParser();
    }

    /**
     * Loads all demo slot configuration resources and combines their parsed
     * values into a single configuration.
     *
     * @return complete demo slot configuration
     * @throws IllegalStateException if a resource cannot be read, its structure
     *                               is invalid, or its values cannot be mapped
     */
    public SlotConfig load() {
        try {
            FieldConfig field = getFieldConfig();
            List<FieldLine> lines = lineMapper.map(lineLoader.load(LINE_CONFIG));
            validateLines(field, lines);

            CombinationConfig combinations = combinationParser.parse(combinationLoader.load(COMBINATION_CONFIG));

            return new SlotConfig(field, lines, combinations);

        } catch (RuntimeException e) {
            throw new IllegalStateException("Could not load slot configuration", e);
        }
    }

    private FieldConfig getFieldConfig() {
        RawFieldConfig rawField = fieldLoader.load(FIELD_CONFIG);
        ReelBank<StandardReelItem> reelBank = StandardReelBankMapper.forNames().map(
                rawField.reels(),
                rawField.fieldHeight()
        );
        return new FieldConfig(
                rawField.fieldHeight(),
                reelBank
        );
    }

    /*
     * Checks that every configured line addresses exactly the configured reels
     * and that its positions lie within the visible field.
     */
    static void validateLines(FieldConfig field, List<FieldLine> lines) {
        int reelCount = field.reelBank().size();
        int fieldHeight = field.fieldHeight();

        for (FieldLine line : lines) {
            List<FieldPosition> positions = line.positions();

            Validate.isTrue(
                    positions.size() == reelCount,
                    "Line %d must contain %d positions, but contains %d",
                    line.id(),
                    reelCount,
                    positions.size()
            );

            for (int column = 0; column < reelCount; column++) {
                FieldPosition position = positions.get(column);

                Validate.isTrue(
                        position.column() == column,
                        "Line %d, position %d must use column %d, but uses %d",
                        line.id(),
                        column,
                        column,
                        position.column()
                );

                Validate.isTrue(
                        position.row() >= 0 && position.row() < fieldHeight,
                        "Line %d, column %d: row %d is outside field height %d",
                        line.id(),
                        column,
                        position.row(),
                        fieldHeight
                );
            }
        }
    }
}
