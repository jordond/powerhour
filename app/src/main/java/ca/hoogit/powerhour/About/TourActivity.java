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
package ca.hoogit.powerhour.About;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

import ca.hoogit.powerhour.R;
import ca.hoogit.powerhour.Selection.MainActivity;
import ca.hoogit.powerhour.Util.SharedPrefs;

/**
 * @author jordon
 *         <p/>
 *         Date    29/07/15
 *         Description
 *         <p/>
 *         First time user tour of application
 */
public class TourActivity extends AppIntro2 {
    @Override
    public void init(Bundle bundle) {
        int primary = getResources().getColor(R.color.primary);

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.tour_slide_1_title),
                getString(R.string.tour_slide_1_desc),
                R.drawable.tour_start,
                primary));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.tour_slide_2_title),
                getString(R.string.tour_slide_2_desc),
                R.drawable.tour_configure,
                ContextCompat.getColor(this, R.color.md_teal_500)));


        addSlide(AppIntroFragment.newInstance(getString(R.string.tour_slide_3_title),
                getString(R.string.tour_slide_3_desc),
                R.drawable.tour_progress,
                ContextCompat.getColor(this, R.color.md_red_500)));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.tour_slide_4_title),
                getString(R.string.tour_slide_4_desc),
                R.drawable.tour_drink,
                ContextCompat.getColor(this, R.color.md_amber_500)));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.tour_slide_5_title),
                getString(R.string.tour_slide_5_desc),
                R.drawable.tour_game_over,
                ContextCompat.getColor(this, R.color.md_deep_purple_500)));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.tour_slide_wear_title),
                getString(R.string.tour_slide_wear_text),
                R.drawable.tour_wear_shot,
                ContextCompat.getColor(this, R.color.md_pink_500)));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.tour_slide_6_title),
                getString(R.string.tour_slide_6_desc),
                R.drawable.tour_start, primary));

        showStatusBar(false);
        setProgressButtonEnabled(true);

        Answers.getInstance().logCustom(new CustomEvent("Viewed Tour")
                .putCustomAttribute(
                        "IsFirstRun",
                        SharedPrefs.get(this).isFirstRun() ? "Yes" : "No"));
    }

    private void launchMainActivity() {
        Intent main = new Intent(this, MainActivity.class);
        startActivity(main);
        finish();
    }

    public void onSkipPressed() {
        launchMainActivity();
    }

    @Override
    public void onDonePressed() {
        launchMainActivity();
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onSlideChanged() {

    }
}
