package ivs.games.accessories.slot.demo.config.field;

import java.util.List;

/**
 * Contains the raw field configuration loaded from a YAML resource.
 *
 * <p>Reel symbols remain strings at this stage. The reel bank mapper converts
 * them into the symbol objects used by the slot engine.</p>
 *
 * @param fieldHeight number of visible rows in the generated field
 * @param reels       ordered reel strips containing symbol names
 */
public record RawFieldConfig(
        int fieldHeight,
        List<List<String>> reels
) {
}
