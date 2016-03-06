package ca.hoogit.powerhour.wear.Game;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import ca.hoogit.powerhour.wear.DataLayer.GameInformation;
import ca.hoogit.powerhour.R;

/**
 * Created by jordon on 05/03/16.
 * Global singleton to hold game state
 */
public class GameState {
    private static GameState ourInstance = new GameState();

    private GameInformation mInformation = new GameInformation();
    private boolean mIsShotTime = false;

    private int mDefaultPrimaryColor;
    private int mDefaultAccentColor;

    public static GameState getInstance() {
        return ourInstance;
    }

    public void init(Context context) {
        this.mDefaultPrimaryColor = ContextCompat.getColor(context, R.color.primary);
        this.mDefaultAccentColor = ContextCompat.getColor(context, R.color.accent);
    }

    private GameState() {
    }

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

    public int getPrimary() {
        if (mInformation.getColorPrimary() != 0) {
            return mInformation.getColorPrimary();
        }
        return mDefaultPrimaryColor;
    }

    public int getAccent() {
        if (mInformation.getColorAccent() != 0) {
            return mInformation.getColorAccent();
        }
        return mDefaultAccentColor;
    }

    public boolean isActive() {
        return mInformation.isStarted();
    }
}
