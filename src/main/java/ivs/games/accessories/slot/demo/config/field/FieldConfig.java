package ivs.games.accessories.slot.demo.config.field;

import ivs.game.accessories.slot.reel.ReelBank;
import ivs.game.accessories.slot.reel.impl.StandardReelItem;

/**
 * Contains the field height and the configured physical reels.
 *
 * @param fieldHeight number of visible rows in the generated field
 * @param reelBank    configured physical reels
 */
public record FieldConfig(
        int fieldHeight,
        ReelBank<StandardReelItem> reelBank
) {
}
