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
package ca.hoogit.powerhour.Screen;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pascalwelsch.holocircularprogressbar.HoloCircularProgressBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import ca.hoogit.powerhour.Game.GameModel;
import ca.hoogit.powerhour.Game.State;
import ca.hoogit.powerhour.R;
import ca.hoogit.powerhour.Util.StatusBarUtil;
import ca.hoogit.powerhour.Views.GameControlButtons;

/**
 * @author jordon
 *         <p/>
 *         Date    21/07/15
 *         Description
 */
public class ScreenView {

    private static final String TAG = ScreenView.class.getSimpleName();

    public final String PAUSES_REMAINING_TEXT = " pauses remaining";
    public final String PAUSES_UNLIMITED_TEXT = "∞ pauses";

    private AppCompatActivity mActivity;
    private View mView;

    private State mGameState;
    private int mMaxPause;
    private boolean mKeepOn;
    private boolean mCanPause = true;

    private String ROUND_OF_MAX_TEXT;

    @Bind(R.id.game_screen_layout) RelativeLayout mLayout;
    @Bind(R.id.game_screen_control) GameControlButtons mControl;

    @Bind(R.id.game_screen_title) TextView mTitle;
    @Bind(R.id.game_screen_countdown_text) TextView mCountdownText;
    @Bind(R.id.game_screen_remaining_pauses) TextView mPausesText;
    @Bind(R.id.game_screen_rounds_remaining) TextView mRoundsText;

    @Bind(R.id.game_screen_circle_progress_seconds) HoloCircularProgressBar mProgressSeconds;
    @Bind(R.id.game_screen_circle_progress_rounds) HoloCircularProgressBar mProgressRounds;

    public ScreenView(Activity activity, View view) {
        this.mActivity = (AppCompatActivity) activity;
        this.mView = view;
        ButterKnife.bind(this, view);
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }

    public void setup(GameModel gameModel) {
        int primary = gameModel.options().getBackgroundColor();
        int secondary = gameModel.options().getAccentColor();

        changeColors(primary, secondary);

        mTitle.setText(gameModel.options().getTitle());

        // Setup text views
        ROUND_OF_MAX_TEXT = " of " + gameModel.options().getRounds();
        mRoundsText.setText(gameModel.currentRound() + ROUND_OF_MAX_TEXT);

        mMaxPause = gameModel.getMaxPauses();
        mKeepOn = gameModel.options().isKeepScreenOn();

        mControl.toggleScreenLock(mKeepOn);
        toggleKeepOnFlags(mKeepOn);

        if (gameModel.getMaxPauses() == -1) {
            mPausesText.setText(PAUSES_UNLIMITED_TEXT);
        } else {
            mPausesText.setText(gameModel.remainingPauses() + PAUSES_REMAINING_TEXT);
        }

        // Setup control button
        if (gameModel.is(State.ACTIVE)) {
            mControl.toggleCenterButton();
            if (!gameModel.canPause()) {
                mControl.hideCenter();
            }
            setPauseText(gameModel.getPauses());
        }

        mGameState = gameModel.getState();
        Log.d(TAG, "Setup complete");
    }

    public void changeColors(int primary, int secondary) {
        mLayout.setBackgroundColor(primary);

        StatusBarUtil.getInstance().set(mActivity, primary);

        mProgressRounds.setProgressColor(secondary);
        mProgressSeconds.setThumbColor(secondary);
        mProgressSeconds.setProgressColor(secondary);
        mProgressSeconds.setProgressBackgroundColor(primary);

        mControl.setColor(secondary);
    }

    public void toggleKeepOnButton() {
        mKeepOn = !mKeepOn;
        toggleKeepOnButton(mKeepOn);
    }

    public void toggleKeepOnButton(boolean keepOn) {
        mControl.toggleScreenLock(keepOn);
        toggleKeepOnFlags(keepOn);

        if (keepOn) {
            Snackbar
                .make(mView, mActivity.getString(R.string.screen_on_warning),
                        Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleKeepOnFlags(false);
                        mControl.toggleScreenLock(false);
                    }
                }).show();
        }
    }

    public void toggleKeepOnFlags(boolean keepOn) {
        if (keepOn) {
            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            Log.d(TAG, "Adding flag 'KEEP_SCREEN_ON'");
        } else {
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            Log.d(TAG, "Removing flag 'KEEP_SCREEN_ON'");
        }
    }

    /**
     * View getters
     */

    public ProgressUpdater getUpdater(int id) {
        if (id == R.id.game_screen_circle_progress_rounds) {
            return new ProgressUpdater(mProgressRounds);
        } else if (id == R.id.game_screen_circle_progress_seconds) {
            return new ProgressUpdater(mProgressSeconds);
        }
        return new ProgressUpdater(mActivity, id);
    }

    public GameControlButtons getControl() {
        return mControl;
    }

    /**
     * View setters
     */

    public void setControlListener(GameControlButtons.GameControl listener) {
        mControl.setOnButtonPressed(listener);
    }

    public void setState(State state) {
        this.mGameState = state;

        // Handle new State
        mControl.setIcon(state == State.PAUSED || state == State.NEW_ROUND);

        switch (mGameState) {
            case FINISHED:
                mRoundsText.setText("finished");
                mCountdownText.setText("zero");
                mPausesText.setVisibility(View.INVISIBLE);
                mControl.hideCenter();
                break;
            case ACTIVE:
                if (!mCanPause) {
                    mControl.hideCenter();
                }
                break;
        }
    }

    public void setPauseText(int count) {
        if (mMaxPause == -1) {
            mPausesText.setText(PAUSES_UNLIMITED_TEXT);
        } else if (count == mMaxPause - 1) {
            mPausesText.setText("one pause break left");
        } else if (count < mMaxPause) {
            mPausesText.setText((mMaxPause - count) + PAUSES_REMAINING_TEXT);
            Log.d(TAG, "Pause count: " + count);
            Log.d(TAG, "Remaining  :" + (mMaxPause - count));
        } else {
            mPausesText.setText("zero pauses");
            mCanPause = false;
            Log.d(TAG, "All pause breaks have been used");
        }
    }

    public void setCountdownText(String text) {
        mCountdownText.setText(text);
    }

    public void setRoundsText(String text) {
        mRoundsText.setText(text + ROUND_OF_MAX_TEXT);
    }
}
