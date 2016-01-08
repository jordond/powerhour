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

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

import ca.hoogit.powerhour.R;
import ca.hoogit.powerhour.Selection.MainActivity;

/**
 * @author jordon
 *
 * Date    29/07/15
 * Description 
 *
 *  First time user tour of application
 *
 */
public class TourActivity extends AppIntro2 {
    @Override
    public void init(Bundle bundle) {
        int primary = getResources().getColor(R.color.primary);

        addSlide(AppIntroFragment.newInstance(getString(R.string.tour_slide_1_title),
                getString(R.string.tour_slide_1_desc),
                R.drawable.tour_start, primary));

        addSlide(AppIntroFragment.newInstance(getString(R.string.tour_slide_2_title),
                getString(R.string.tour_slide_2_desc),
                R.drawable.tour_configure, getResources().getColor(R.color.md_teal_500)));


        addSlide(AppIntroFragment.newInstance(getString(R.string.tour_slide_3_title),
                getString(R.string.tour_slide_3_desc),
                R.drawable.tour_progress, getResources().getColor(R.color.md_red_500)));

        addSlide(AppIntroFragment.newInstance(getString(R.string.tour_slide_4_title),
                getString(R.string.tour_slide_4_desc),
                R.drawable.tour_drink, getResources().getColor(R.color.md_amber_500)));

        addSlide(AppIntroFragment.newInstance(getString(R.string.tour_slide_5_title),
                getString(R.string.tour_slide_5_desc),
                R.drawable.tour_game_over, getResources().getColor(R.color.md_deep_purple_500)));

        addSlide(AppIntroFragment.newInstance(getString(R.string.tour_slide_6_title),
                getString(R.string.tour_slide_6_desc),
                R.drawable.tour_start, primary));

//        setBarColor(primary);
//        setSeparatorColor(primary);
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
}
