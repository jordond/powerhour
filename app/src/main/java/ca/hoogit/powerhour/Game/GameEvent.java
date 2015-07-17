package ca.hoogit.powerhour.Game;

/**
 * Created by jordon on 16/07/15.
 * Handle game events with otto
 */
public class GameEvent {
    public enum GameStatus {
        STARTED, ACTIVE, PAUSED, STOPPED, FINISHED
    }

    public GameStatus status;
    public GameOptions options;

    public GameEvent(GameOptions options) {
        this.options = options;
    }

    public GameEvent(GameStatus status) {

        this.status = status;
    }

    public GameEvent(GameStatus status, GameOptions options) {

        this.status = status;
        this.options = options;
    }
}
