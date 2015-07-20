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

import android.animation.ObjectAnimator;
import android.view.animation.LinearInterpolator;

import com.pascalwelsch.holocircularprogressbar.HoloCircularProgressBar;

/**
 * @author jordon
 *         <p/>
 *         Date    19/07/15
 *         Description
 */
public class ProgressUpdater {
    private final long DEFAULT_DURATION = 300;
    private HoloCircularProgressBar mWheel;

    public ProgressUpdater(HoloCircularProgressBar wheel) {
        this.mWheel = wheel;
    }

    public void update(float progress) {
        update(progress, true, DEFAULT_DURATION);
    }

    public void update(float progress, boolean animate) {
        update(progress, animate, DEFAULT_DURATION);
    }

    public void update(float progress, long duration) {
        update(progress, true, duration);
    }

    public void update(float progress, boolean animate, long duration) {
        if (animate) {
            animateProgressWheel(progress, duration);
        } else {
            if (mWheel != null) {
                mWheel.setProgress(progress);
            }
        }
    }

    private void animateProgressWheel(float progress, long duration) {
        if (mWheel != null) {
            ObjectAnimator animation = ObjectAnimator.ofFloat(mWheel, "progress", progress);
            animation.setDuration(duration);
            animation.setInterpolator(new LinearInterpolator());
            animation.start();
        }
    }

}
