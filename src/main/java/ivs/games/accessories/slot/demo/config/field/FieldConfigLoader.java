package ivs.games.accessories.slot.demo.config.field;

import ivs.games.accessories.slot.demo.config.YamlConfigReader;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Loads and validates the raw slot-field configuration from a YAML resource.
 *
 * <p>The loader verifies that the field height is positive, at least one reel
 * is configured, every reel can fill the visible field height and every reel
 * position contains a non-blank symbol name.</p>
 */
@RequiredArgsConstructor
public final class FieldConfigLoader {

    private final YamlConfigReader reader;

    /**
     * Loads the raw field configuration from a classpath resource.
     *
     * @param fileName resource path, for example "demoslot/slotfield.yaml"
     * @return field height and textual reel item descriptions
     */
    public RawFieldConfig load(String fileName) {
        Validate.notBlank(fileName, "Resource name must not be null or blank");

        try {
            Map<String, Object> document = reader.read(fileName);
            int fieldHeight = getFieldHeight(document);
            List<List<String>> reels = getReels(document, fieldHeight);

            return new RawFieldConfig(fieldHeight, reels);

        } catch (RuntimeException e) {
            throw new IllegalStateException("Could not load config file: %s".formatted(fileName), e);
        }
    }

    /*
     * Reads the visible field height and verifies that it is a positive integer.
     */
    private int getFieldHeight(Map<String, Object> document) {
        Object value = document.get("fieldHeight");
        if (!(value instanceof Integer height) || height <= 0) {
            throw new IllegalArgumentException("'fieldHeight' must be a positive integer");
        }
        return height;
    }

    /*
     * Reads the configured reels and preserves their order from the YAML document.
     */
    private List<List<String>> getReels(Map<String, Object> document, int fieldHeight) {
        Object value = document.get("reels");

        if (!(value instanceof List<?> configuredReels) || configuredReels.isEmpty()) {
            throw new IllegalArgumentException("'reels' must be a non-empty list");
        }

        return IntStream.range(0, configuredReels.size())
                .mapToObj(reelIndex ->
                        getReel(
                                configuredReels,
                                reelIndex,
                                fieldHeight
                        )
                )
                .toList();
    }

    /*
     * Reads one reel and verifies that it contains enough symbols to fill the field height.
     */
    private List<String> getReel(
            List<?> configuredReels,
            int reelIndex,
            int fieldHeight
    ) {
        Object value = configuredReels.get(reelIndex);

        if (!(value instanceof List<?> symbols) || symbols.size() < fieldHeight) {
            throw new IllegalArgumentException("Reel #%d must contain at least %d symbols".formatted(reelIndex, fieldHeight));
        }
        return IntStream.range(0, symbols.size())
                .mapToObj(position -> getSymbol(symbols, reelIndex, position))
                .toList();
    }

    /*
     * Reads one reel position and verifies that it contains a non-blank symbol name.
     */
    private String getSymbol(
            List<?> symbols,
            int reelIndex,
            int position
    ) {
        Object value = symbols.get(position);

        if (!(value instanceof String symbol) || symbol.isBlank()) {
            throw new IllegalArgumentException(
                    "Reel %d, position %d: symbol must be a non-blank string".formatted(reelIndex, position));
        }
        return symbol;
    }
}
