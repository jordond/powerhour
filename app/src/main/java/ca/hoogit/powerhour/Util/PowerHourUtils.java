/*
 * Copyright (C) 2015, Jordon de Hoog
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package ca.hoogit.powerhour.Util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.format.DateUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import ca.hoogit.powerhour.R;

/**
 * @author jordon
 *         Date 17/07/15
 *         Description
 *         Some Handy utils
 */
public class PowerHourUtils {

    public static String[] mSoundFileNames = {
            "alarm",
            "argon",
            "ariel",
            "atria",
            "dione",
            "krypton",
            "lmfao",
            "oxygen",
            "pager",
            "sedna",
            "timer",
            "umbriel"
    };

    public static int randInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    public static boolean isLollopopUp() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static ArrayList<SoundFile> getSounds() {
        Field[] fields = R.raw.class.getDeclaredFields();
        ArrayList<SoundFile> sounds = new ArrayList<>();
        try {
            for (Field field : fields) {
                SoundFile s = new SoundFile(field.getName());
                if (Arrays.asList(mSoundFileNames).contains(s.name)) {
                    sounds.add(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sounds;
    }

    public static int[] soundArrayListToIdArray(Context context, ArrayList<SoundFile> list) {
        Resources res = context.getResources();
        int[] ids = new int[list.size()];
        int count = 0;
        for (SoundFile s : list) {
            ids[count] = res.getIdentifier(s.name, "raw", context.getPackageName());
            count++;
        }
        return ids;
    }

    public static class SoundFile {
        String name;

        public SoundFile(String name) {
            this.name = name;
        }
    }

    public static void rateApp(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=ca.hoogit.powerhour")));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
    }

    public static void delay(long duration, final OnDelay listener) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listener.run();
            }
        }, duration);
    }

    public interface OnDelay {
        void run();
    }

    /**
     * Create a relative from now time string
     *
     * @param timestamp Time since epoch
     * @return Formatted string ie. "2 Minutes from now"
     * @see <a href="https://github.com/jordond/garagepi-android/blob/master/app/src/main/java/ca/hoogit/garagepi/Utils/Helpers.java#L89">Source</a>
     */
    public static String epochToFromNow(long timestamp) {
        return DateUtils
                .getRelativeTimeSpanString(
                        timestamp,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS)
                .toString();
    }

}
