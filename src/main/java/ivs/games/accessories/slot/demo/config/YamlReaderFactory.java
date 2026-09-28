package ivs.games.accessories.slot.demo.config;

import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * Creates configured SnakeYAML readers for demo configuration resources.
 *
 * <p>Every call to {@link #create()} returns a new {@link Yaml} instance.
 * Individual YAML read operations therefore do not share parser state.</p>
 *
 * <p>The factory configures readers with the following restrictions:</p>
 *
 * <ul>
 *     <li>{@link SafeConstructor} is used instead of arbitrary Java object construction;</li>
 *     <li>duplicate mapping keys are rejected.</li>
 * </ul>
 *
 * <p>The shared {@link #INSTANCE} can be used because the factory itself does not
 * contain mutable parser state.</p>
 */
@NoArgsConstructor
public class YamlReaderFactory {

    /**
     * Shared stateless factory instance used by the demo configuration loader.
     */
    public static final YamlReaderFactory INSTANCE = new YamlReaderFactory();

    /**
     * Creates a new configured YAML reader.
     *
     * @return a new YAML reader using safe construction and rejecting duplicate keys
     */
    public Yaml create() {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);

        return new Yaml(new SafeConstructor(options));
    }
}