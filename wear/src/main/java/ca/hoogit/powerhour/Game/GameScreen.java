package ca.hoogit.powerhour.Game;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import ca.hoogit.powerhour.DataLayer.GameInformation;
import ca.hoogit.powerhour.R;
import ca.hoogit.powerhour.Utils.Colors;
import ca.hoogit.powerhourshared.DataLayer.Consts;

/**
 * Handle all the views related to the game
 */
public class GameScreen extends LinearLayout {

    private static final String TAG = GameScreen.class.getSimpleName();

    private static final int FONT_SIZE_AMBIENT = 12;
    private static final int FONT_SIZE_ACTIVE = 18;
    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("h:mm a", Locale.US);

    /**
     * Waiting components
     */
    @Bind(R.id.waiting) LinearLayout mWaitingLayout;
    @Bind(R.id.waiting_text) TextView mWaitingText;
    @Bind(R.id.waiting_text_more) TextView mWaitingMoreText;
    @Bind(R.id.waiting_clock) TextView mWaitingClock;

    /**
     * Active layout components
     */
    @Bind(R.id.active) RelativeLayout mActiveLayout;
    @Bind(R.id.rounds_remaining) TextView mRemainingRounds;
    @Bind(R.id.progress_seconds_text) TextView mRemainingSeconds;
    @Bind(R.id.progress_seconds_circle) HoloCircularProgressBar mProgress;

    /**
     * Ambient layout components
     */
    @Bind(R.id.ambient) LinearLayout mAmbientLayout;
    @Bind(R.id.rounds_text) TextView mRoundsText;
    @Bind(R.id.ambient_rounds_remaining) TextView mAmbientRemainingRounds;
    @Bind(R.id.ambient_clock) TextView mAmbientClock;

    private boolean mShotAnimatorIsRunning;

    private Colors mColors = Colors.getInstance();

    public GameScreen(Context context) {
        super(context);
        init(context, null, 0);
    }

    public GameScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public GameScreen(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        inflate(getContext(), R.layout.view_game_screen, this);
        ButterKnife.bind(this);
    }

    public void updateColors() {
        mProgress.setThumbColor(mColors.getAccent());
        mProgress.setProgressColor(mColors.getAccent());
        mProgress.setProgressBackgroundColor(mColors.getPrimary());
    }

    public void updateProgress() {
        long remainingMillis = GameState.getInstance().get().getRemainingMillis();
        int seconds = (int) (remainingMillis / 1000.0);
        mRemainingSeconds.setText(String.valueOf(seconds));
        Log.d(TAG, "updateProgress: remaining: " + remainingMillis);
        animateProgress((float) remainingMillis / (float) Consts.Game.ROUND_DURATION_MILLIS,
                Consts.Game.WEAR_UPDATE_INTERVAL_IN_MILLISECONDS);
    }

    public void updateScreen(boolean isAmbient) {
        if (mShotAnimatorIsRunning) {
            if (!isAmbient) {
                mProgress.setProgressBackgroundColor(Colors.getInstance().getPrimary());
            }
            return;
        }
        GameInformation info = GameState.getInstance().get();
        if (isAmbient) {
            if (!GameState.getInstance().hasGameInfo()) {
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
                mAmbientRemainingRounds.setText(info.getCurrentRound() + " of " + info.getRounds());
                mAmbientRemainingRounds.getPaint().setAntiAlias(false);
                mAmbientClock.setVisibility(View.VISIBLE);
                mAmbientClock.setText(AMBIENT_DATE_FORMAT.format(new Date()));
            }
        } else {
            if (!GameState.getInstance().hasGameInfo()) {
                mWaitingLayout.setVisibility(View.VISIBLE);
                mWaitingText.setTextSize(FONT_SIZE_ACTIVE);
                mWaitingMoreText.setVisibility(View.VISIBLE);
                mWaitingClock.setVisibility(View.GONE);
            } else {
                mWaitingLayout.setVisibility(View.GONE);
                mActiveLayout.setVisibility(View.VISIBLE);
                mAmbientLayout.setVisibility(View.GONE);
                mRemainingRounds.setText(info.getCurrentRound() + " of " + info.getRounds());
                updateColors();
                updateProgress();
            }
        }
    }

    public void showShotMessage(boolean isAmbient) {
        updateScreen(false);
        if (isAmbient) {
            mProgress.setProgressBackgroundColor(Color.BLACK);
        }
        mProgress.setProgress(0);
        animateProgress(1f, Consts.Game.DEFAULT_SHOT_TIME_DELAY, new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mShotAnimatorIsRunning = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mShotAnimatorIsRunning = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mShotAnimatorIsRunning = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        mRemainingSeconds.setText(R.string.end_of_round_message);
        YoYo.with(Techniques.Flash)
                .duration(Consts.Game.DEFAULT_SHOT_TIME_DELAY)
                .playOn(mRemainingSeconds);
    }

    private void animateProgress(float progress, long duration) {
        animateProgress(progress, duration, null);
    }

    private void animateProgress(float progress, long duration, Animator.AnimatorListener listener) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(mProgress, "progress", progress);
        anim.setDuration(duration);
        anim.setInterpolator(new LinearInterpolator());
        if (listener != null) {
            anim.addListener(listener);
        }
        anim.start();
    }
}
