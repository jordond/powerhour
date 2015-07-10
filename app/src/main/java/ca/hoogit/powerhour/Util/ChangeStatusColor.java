package ca.hoogit.powerhour.Util;

import android.app.Activity;

/**
 * Created by jordon on 09/07/15.
 */
public class ChangeStatusColor {
    private final int color;
    private Activity activity;

    public ChangeStatusColor(Activity activity, int color) {
        this.activity = activity;
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
