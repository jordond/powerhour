package ca.hoogit.powerhour.Fragments;


import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.pascalwelsch.holocircularprogressbar.HoloCircularProgressBar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ca.hoogit.powerhour.DataLayer.GameInformation;
import ca.hoogit.powerhour.R;
import ca.hoogit.powerhour.Utils.Colors;
import ca.hoogit.powerhourshared.DataLayer.Consts;

public class GameScreenFragment extends Fragment {

    private static final String TAG = GameScreenFragment.class.getSimpleName();
    private static final String ARG_COLOR_PRIMARY = "color_primary";
    private static final String ARG_COLOR_ACCENT = "color_accent";

    private static final int FONT_SIZE_AMBIENT = 12;
    private static final int FONT_SIZE_ACTIVE = 18;
    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("h:mm a", Locale.US);

    /**
     * Waiting components
     */
    private LinearLayout mWaitingLayout;
    private TextView mWaitingText;
    private TextView mWaitingMoreText;
    private TextView mWaitingClock;

    /**
     * Active layout components
     */
    private RelativeLayout mActiveLayout;
    private TextView mRemainingRounds;
    private TextView mRemainingSeconds;
    private HoloCircularProgressBar mProgress;

    /**
     * Ambient layout components
     */
    private LinearLayout mAmbientLayout;
    private TextView mRoundsText;
    private TextView mAmbientRemainingRounds;

    private boolean mReceivedGameInfo;
    private int mTotalRounds;
    private int mCurrentRound = 0;
    private int mTotalPauses;
    private int mCurrentPauses = 0;
    private long mRemainingMillis = 60 * 1000;

    private Colors mColors = Colors.getInstance();

    public static GameScreenFragment newInstance() {
        return new GameScreenFragment();
    }

    public GameScreenFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_screen, container, false);

        // Waiting for game components
        mWaitingLayout = (LinearLayout) view.findViewById(R.id.waiting);
        mWaitingText = (TextView) view.findViewById(R.id.waiting_text);
        mWaitingMoreText = (TextView) view.findViewById(R.id.waiting_text_more);
        mWaitingClock = (TextView) view.findViewById(R.id.waiting_clock);

        // Active components
        mActiveLayout = (RelativeLayout) view.findViewById(R.id.active);
        mRemainingRounds = (TextView) view.findViewById(R.id.rounds_remaining);
        mRemainingSeconds = (TextView) view.findViewById(R.id.progress_seconds_text);
        mProgress = (HoloCircularProgressBar) view.findViewById(R.id.progress_seconds_circle);

        // Ambient components
        mAmbientLayout = (LinearLayout) view.findViewById(R.id.ambient);
        mRoundsText = (TextView) view.findViewById(R.id.rounds_text);
        mAmbientRemainingRounds = (TextView) view.findViewById(R.id.ambient_rounds_remaining);

        return view;
    }

    public void updateInfo(GameInformation info) {
        mReceivedGameInfo = true;
        mTotalRounds = info.getRounds();
        mTotalPauses = info.getPauses();
        mCurrentRound = info.getCurrentRound();
        mCurrentPauses = info.getCurrentPauses();
        mRemainingMillis = info.getRemainingMillis();

        mRemainingRounds.setText(mCurrentRound + " of " + mTotalRounds);
    }

    public void stop() {
        mReceivedGameInfo = false;
    }

    public void updateColors() {
        mProgress.setThumbColor(mColors.getAccent());
        mProgress.setProgressColor(mColors.getAccent());
        mProgress.setProgressBackgroundColor(mColors.getPrimary());
    }

    public void updateProgress() {
        int seconds = (int) (mRemainingMillis / 1000.0);
        mRemainingSeconds.setText(String.valueOf(seconds));
        Log.d(TAG, "updateProgress: remaining: " + mRemainingMillis);
        animateProgress((float) mRemainingMillis / (float) Consts.Game.ROUND_DURATION_MILLIS,
                Consts.Game.WEAR_UPDATE_INTERVAL_IN_MILLISECONDS);
    }

    public void updateScreen(boolean isAmbient) {
        if (isAmbient) {
            if (!mReceivedGameInfo) {
                mWaitingLayout.setVisibility(View.VISIBLE);
                mWaitingText.setTextSize(FONT_SIZE_AMBIENT);
                mWaitingMoreText.setVisibility(View.GONE);
                mWaitingClock.setVisibility(View.VISIBLE);
                mWaitingClock.setText(AMBIENT_DATE_FORMAT.format(new Date()));
            } else {
                mWaitingLayout.setVisibility(View.GONE);
                mActiveLayout.setVisibility(View.GONE);
                mAmbientLayout.setVisibility(View.VISIBLE);
                mRoundsText.getPaint().setAntiAlias(false);
                mAmbientRemainingRounds.setText(mCurrentRound + " of " + mTotalRounds);
                mAmbientRemainingRounds.getPaint().setAntiAlias(false);
            }
        } else {
            if (!mReceivedGameInfo) {
                mWaitingLayout.setVisibility(View.VISIBLE);
                mWaitingText.setTextSize(FONT_SIZE_ACTIVE);
                mWaitingMoreText.setVisibility(View.VISIBLE);
                mWaitingClock.setVisibility(View.GONE);
            } else {
                mWaitingLayout.setVisibility(View.GONE);
                mActiveLayout.setVisibility(View.VISIBLE);
                mAmbientLayout.setVisibility(View.GONE);
                updateColors();
                updateProgress();
            }
        }
    }

    public void showShotMessage() {
        updateScreen(false);
        mProgress.setProgress(0);
        animateProgress(1f, Consts.Game.DEFAULT_SHOT_TIME_DELAY);
        mRemainingSeconds.setText(R.string.end_of_round_message);
        YoYo.with(Techniques.Flash)
                .duration(Consts.Game.DEFAULT_SHOT_TIME_DELAY)
                .playOn(mRemainingSeconds);
    }

    private void animateProgress(float progress, long duration) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(mProgress, "progress", progress);
        anim.setDuration(duration);
        anim.setInterpolator(new LinearInterpolator());
        anim.start();
    }

}
