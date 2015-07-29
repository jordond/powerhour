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
import android.net.Uri;
import android.os.Build;
import android.os.Handler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import ca.hoogit.powerhour.Game.Action;
import ca.hoogit.powerhour.Game.GameEvent;
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
        Field[] fields = R.raw.class.getFields();
        ArrayList<SoundFile> sounds = new ArrayList<>();
        try {
            for (Field field : fields) {
                SoundFile s = new SoundFile(field.getName(), field.getInt(field));
                if (Arrays.asList(mSoundFileNames).contains(s.name)) {
                    sounds.add(s);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return  sounds;
    }

    public static int[] soundArrayListToIdArray(ArrayList<SoundFile> list) {
        int[] ids = new int[list.size()];
        int count = 0;
        for (SoundFile s : list) {
            ids[count] = s.id;
            count++;
        }
        return ids;
    }

    public static String soundIdToName(ArrayList<SoundFile> list, int id) {
        for (SoundFile sound : list) {
            if (sound.id == id) {
                return sound.name;
            }
        }
        return "custom";
    }

    public static class SoundFile {
        String name;
        int id;

        public SoundFile(String name, int id) {
            this.name = name;
            this.id = id;
        }
    }

    public static void rateApp(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" +
                            context.getPackageName())));
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

}
