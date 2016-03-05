package ca.hoogit.powerhour.Game;

import ca.hoogit.powerhour.DataLayer.GameInformation;

/**
 * Created by jordon on 05/03/16.
 * Global singleton to hold game state
 */
public class GameState {
    private static GameState ourInstance = new GameState();

    public static GameState getInstance() {
        return ourInstance;
    }

    private GameState() {
    }

    private GameInformation mInformation = new GameInformation();
    private boolean mIsShotTime = false;

    public void update(GameInformation info) {
        this.mInformation = info;
    }

    public GameInformation get() {
        return this.mInformation;
    }

    public boolean isShotTime() {
        return mIsShotTime;
    }

    public void setIsShotTime(boolean shotTime) {
        this.mIsShotTime = shotTime;
    }

    public boolean hasGameInfo() {
        return this.mInformation.getRounds() != 0;
    }

    public void stop() {
        this.mInformation = new GameInformation();
        this.mIsShotTime = false;
    }
}
