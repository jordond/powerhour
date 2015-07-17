package ca.hoogit.powerhour.Util;

import android.graphics.Color;

/**
 * Created by jordon on 09/07/15.
 */
public class ColorUtil {
    public static int lighten(int color, float fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        red = lightenColor(red, fraction);
        green = lightenColor(green, fraction);
        blue = lightenColor(blue, fraction);
        int alpha = Color.alpha(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static int darken(int color, float fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        red = darkenColor(red, fraction);
        green = darkenColor(green, fraction);
        blue = darkenColor(blue, fraction);
        int alpha = Color.alpha(color);

        return Color.argb(alpha, red, green, blue);
    }

    public static int darken(int color) {
        return darken(color, 0.3f);
    }

    private static int darkenColor(int color, float fraction) {
        return (int)Math.max(color - (color * fraction), 0);
    }

    private static int lightenColor(int color, float fraction) {
        return (int) Math.min(color + (color * fraction), 255);
    }
}
