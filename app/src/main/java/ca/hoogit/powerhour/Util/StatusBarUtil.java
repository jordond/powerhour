package ca.hoogit.powerhour.Util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;

import ca.hoogit.powerhourshared.DataLayer.ColorUtil;

/**
 * Created by jordon on 09/07/15.
 * A Simple utility for changing the color of the status bar
 */
public class StatusBarUtil {
    private static StatusBarUtil instance = new StatusBarUtil();
    private int originalColor;

    public static StatusBarUtil getInstance() {
        return instance;
    }

    private StatusBarUtil() {
    }

    public int getOriginal() {
        return originalColor;
    }

    public void setColor(int originalColor) {
        this.originalColor = originalColor;
    }

    public void init(Activity activity) {
        if (Build.VERSION.SDK_INT >= 21) {
            originalColor = activity.getWindow().getStatusBarColor();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void resetColor(Activity activity) {
        if (Build.VERSION.SDK_INT >= 21) {
            activity.getWindow().setStatusBarColor(originalColor);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void set(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            int darker = ColorUtil.darken(color);
            activity.getWindow().setStatusBarColor(darker);
        }
    }
}
