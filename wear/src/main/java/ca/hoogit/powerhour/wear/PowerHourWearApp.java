package ca.hoogit.powerhour.wear;

import android.app.Application;

import ca.hoogit.powerhour.wear.Game.GameState;

/**
 * Created by jordon on 05/03/16.
 * Entry point
 */
public class PowerHourWearApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        GameState.getInstance().init(this);
    }
}
