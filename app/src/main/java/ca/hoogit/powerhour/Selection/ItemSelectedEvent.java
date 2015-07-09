package ca.hoogit.powerhour.Selection;

import ca.hoogit.powerhour.Game.GameOptions;

/**
 * Created by jordon on 08/07/15.
 */
public class ItemSelectedEvent {
    public final GameOptions options;
    public boolean isConfiguring;
    public boolean itemIsAGame;

    public ItemSelectedEvent(GameOptions options, boolean isConfiguring) {
        this.options = options;
        this.isConfiguring = isConfiguring;
        this.itemIsAGame = true;
    }

    public ItemSelectedEvent(GameOptions options) {
        this.options = options;
        this.isConfiguring = false;
        this.itemIsAGame = options.getType() != GameOptions.Type.NONE;
    }
}
