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

package ca.hoogit.powerhour.GameOver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pascalwelsch.holocircularprogressbar.HoloCircularProgressBar;

import butterknife.Bind;
import ca.hoogit.powerhour.About.AboutActivity;
import ca.hoogit.powerhour.BaseActivity;
import ca.hoogit.powerhour.Game.GameModel;
import ca.hoogit.powerhour.R;
import ca.hoogit.powerhour.Screen.ProgressUpdater;
import ca.hoogit.powerhour.Selection.MainActivity;
import ca.hoogit.powerhour.Util.PowerHourUtils;
import ca.hoogit.powerhour.Util.StatusBarUtil;

public class GameOver extends BaseActivity implements View.OnClickListener {

    private static final double BEER_DIVISOR = 12.0;

    @Bind(R.id.container) RelativeLayout mContainer;

    @Bind(R.id.title) TextView mTitle;
    @Bind(R.id.round_progress) HoloCircularProgressBar mProgress;
    @Bind(R.id.rounds_complete) TextView mRoundsComplete;
    @Bind(R.id.total_rounds) TextView mTotalRounds;
    @Bind(R.id.beer_count) TextView mBeerCount;
    @Bind(R.id.pause_count) TextView mPauseCount;

    @Bind(R.id.rate) LinearLayout mRateApp;
    @Bind(R.id.game_over_okay) LinearLayout mOkayLayout;
    @Bind(R.id.okay) Button mOkay;

    private GameModel mGame;

    @Override
    protected int getToolbarColor() {
        return mGame.options().getBackgroundColor();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_game_over;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.menu_game_over;
    }

    @Override
    protected boolean getDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    protected boolean getEventBusEnabled() {
        return false;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(Bundle savedInstanceState) {
        mGame = (GameModel) getIntent().getSerializableExtra("game");
        if (mGame == null) {
            finish();
        }
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        changeColor();

        mRateApp.setOnClickListener(this);
        mOkay.setOnClickListener(this);

        mTitle.setText(mGame.options().getTitle());
        mRoundsComplete.setText(String.valueOf(mGame.currentRound()));
        mTotalRounds.setText("of " + mGame.getTotalRounds() + " rounds");

        setPauseCount(mGame.getPauses());
        setBeerCount(mGame.currentRound());

        mProgress.setProgress(0);
        PowerHourUtils.delay(2000, new PowerHourUtils.OnDelay() {
            @Override
            public void run() {
                ProgressUpdater p = new ProgressUpdater(mProgress);
                float progress = (float) mGame.currentRound() / mGame.getTotalRounds();
                p.set(progress, 2500);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case android.R.id.home:
                launchHome();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        launchHome();
    }

    private void changeColor() {
        StatusBarUtil.getInstance().set(this, mGame.options().getBackgroundColor());
        mContainer.setBackgroundColor(mGame.options().getBackgroundColor());

        mProgress.setProgressColor(mGame.options().getAccentColor());
        mOkayLayout.setBackgroundColor(mGame.options().getAccentColor());
    }

    private void setPauseCount(int count) {
        if (count == 0) {
            mPauseCount.setText("you finished without pausing and");
        } else if (count == 1) {
            mPauseCount.setText("you paused once and");
        } else {
            mPauseCount.setText("you paused " + count + " times and");
        }
    }

    private void setBeerCount(int rounds) {
        double beers = (double) rounds / BEER_DIVISOR;
        if (beers < 1) {
            mBeerCount.setText("you barely even drank one beer");
        } else {
            mBeerCount.setText("you drank about " + String.format("%.2f", beers) + " beers");
        }
    }

    private void launchHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rate:
                PowerHourUtils.rateApp(getApplication());
            case R.id.okay:
                launchHome();
                break;
        }
    }
}
