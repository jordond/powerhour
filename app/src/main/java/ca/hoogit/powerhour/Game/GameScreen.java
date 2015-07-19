

package ca.hoogit.powerhour.Game;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pascalwelsch.holocircularprogressbar.HoloCircularProgressBar;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import ca.hoogit.powerhour.BusProvider;
import ca.hoogit.powerhour.R;
import ca.hoogit.powerhour.Util.ChangeStatusColor;
import ca.hoogit.powerhour.Views.GameControlButtons;
import ca.hoogit.powerhour.Views.GameControlButtons.GameControl;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GameScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameScreen extends Fragment {

    private static final String TAG = GameScreen.class.getSimpleName();
    private static final String ARG_OPTIONS = "options";

    public static final String INSTANCE_STATE_OPTIONS = "options";
    public static final String INSTANCE_STATE_GAME_CURRENT_ROUND = "currentRound";
    public static final String INSTANCE_STATE_GAME_PAUSED_COUNT = "pauseCount";
    public static final String INSTANCE_STATE_GAME_ROUND_MILLIS = "roundMillis";

    public static final String PAUSES_REMAINING_TEXT = " Pauses Remaining";

    private final long DEFAULT_ROUND_MILLISECONDS = Engine.MINUTE_DURATION * 1000;

    private Game mGame;

    private boolean mGameHasStarted;

    private GameOptions mOptions;
    private int mCurrentRound = 0;
    private int mPausedCount = 0;
    private long mRemainingRoundMillis = DEFAULT_ROUND_MILLISECONDS;

    private String ROUND_OF_MAX_TEXT;

    @Bind(R.id.appBar) Toolbar mToolbar;
    @Bind(R.id.game_screen_layout) RelativeLayout mLayout;
    @Bind(R.id.game_screen_control) GameControlButtons mControl;

    @Bind(R.id.game_screen_title) TextView mTitle;
    @Bind(R.id.game_screen_countdown_text) TextView mCountdownText;
    @Bind(R.id.game_screen_remaining_pauses) TextView mPausesText;
    @Bind(R.id.game_screen_rounds_remaining) TextView mRoundsText;

    @Bind(R.id.game_screen_circle_progress_seconds) HoloCircularProgressBar mProgressSeconds;
    @Bind(R.id.game_screen_circle_progress_rounds) HoloCircularProgressBar mProgressRounds;

    /**
     * @param options Parameter 1.
     * @return A new instance of fragment GameScreen.
     */
    public static GameScreen newInstance(GameOptions options) {
        GameScreen fragment = new GameScreen();
        Bundle args = new Bundle();
        args.putSerializable(ARG_OPTIONS, options);
        fragment.setArguments(args);
        return fragment;
    }

    public GameScreen() {
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (!enter) {
            return AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_right);
        }
        return super.onCreateAnimation(transit, true, nextAnim);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BusProvider.getInstance().register(this);
        if (getArguments() != null) {
            mOptions = (GameOptions) getArguments().getSerializable(ARG_OPTIONS);
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_screen, container, false);
        ButterKnife.bind(this, view);

        // Setup the toolbar
        AppCompatActivity mActivity = (AppCompatActivity) getActivity();
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_icon);
        mActivity.setSupportActionBar(mToolbar);

        try {
            mActivity.getSupportActionBar().setHomeButtonEnabled(true);
            mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (NullPointerException ex) {
            Log.e(TAG, ex.getMessage());
        }

        // Change colors to those set in options;
        int mPrimaryColor = mOptions.getBackgroundColor();
        int mAccentColor = mOptions.getAccentColor();

        mToolbar.setBackgroundColor(mPrimaryColor);
        mLayout.setBackgroundColor(mPrimaryColor);
        BusProvider.getInstance().post(new ChangeStatusColor(mActivity, mPrimaryColor));

        // Setup titles and colors
        mTitle.setText(mOptions.getTitle());
        mProgressRounds.setProgressColor(mAccentColor);
        mProgressSeconds.setThumbColor(mAccentColor);
        mProgressSeconds.setProgressColor(mAccentColor);
        mProgressSeconds.setProgressBackgroundColor(mPrimaryColor);

        ROUND_OF_MAX_TEXT = " of " + mOptions.getRounds();

        // Set the text and progress wheels
        mRoundsText.setText(mCurrentRound + ROUND_OF_MAX_TEXT);
        updatePauses();

        updateRoundsProgress(mCurrentRound, false);
        updateSecondsProgress(mRemainingRoundMillis, false);

        // Get status of game, if it hasn't started and auto-start is enabled, go.
        mGameHasStarted = Engine.started();

        if (!mGameHasStarted) {
            Game game = new Game(mOptions, mOptions.isAutoStart());
            BusProvider.getInstance().post(new GameEvent(Action.INITIALIZE, game));
            mControl.setIsActive(!mOptions.isAutoStart());
        }

        setupControlButtons();

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(INSTANCE_STATE_OPTIONS, mOptions);
        outState.putInt(INSTANCE_STATE_GAME_CURRENT_ROUND, mCurrentRound);
        outState.putInt(INSTANCE_STATE_GAME_PAUSED_COUNT, mPausedCount);
        outState.putLong(INSTANCE_STATE_GAME_ROUND_MILLIS, mRemainingRoundMillis);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mOptions = (GameOptions) savedInstanceState.getSerializable(INSTANCE_STATE_OPTIONS);
            mCurrentRound = savedInstanceState.getInt(INSTANCE_STATE_GAME_CURRENT_ROUND);
            mPausedCount = savedInstanceState.getInt(INSTANCE_STATE_GAME_PAUSED_COUNT);
            mRemainingRoundMillis = savedInstanceState.getLong(INSTANCE_STATE_GAME_ROUND_MILLIS);
            // TODO update views
        }
    }

    private void setupControlButtons() {
        mControl.setPauseCount(mPausedCount);
        mControl.setMaxPauses(mOptions.getMaxPauses());
        mControl.setColor(mOptions.getAccentColor());

        GameControl buttonPressed = new GameControl() {
            @Override
            public void soundPressed() {
                Toast.makeText(getActivity(), "Sound button pressed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void controlPressed(boolean isActive, int numberOfPauses) {
                if (mGameHasStarted) {
                    if (isActive) {
                        BusProvider.getInstance().post(new GameEvent(Action.RESUME));
                    } else {
                        if (numberOfPauses < mOptions.getMaxPauses() || mOptions.unlimitedPauses()) {
                            updatePauses();
                            mPausedCount++;
                            Log.d(TAG, "Paused: " + numberOfPauses + " times");
                            Log.d(TAG, "Remaining pauses: " + (mOptions.getMaxPauses() - numberOfPauses) + " times");
                            BusProvider.getInstance().post(new GameEvent(Action.PAUSE));
                        }
                    }
                } else {
                    BusProvider.getInstance().post(new GameEvent(Action.START));
                    mGameHasStarted = true;
                }
            }

            @Override
            public void stopPressed() {
                stopGame();
            }
        };
        mControl.setOnButtonPressed(buttonPressed);
    }

    private void stopGame() {
        new MaterialDialog.Builder(getActivity())
                .title("Stop the game?")
                .content("Are you sure you want to stop this game of " + mOptions.getTitle() + "?")
                .positiveText("Quit!")
                .negativeText("Keep drinking!")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        Toast.makeText(getActivity(), "Game was stopped...", Toast.LENGTH_SHORT).show(); //TODO remove
                        BusProvider.getInstance().post(new GameEvent(Action.STOP));
                    }
                }).show();

    }

    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
        // TODO handle back button, sharedprefs?
    }

    @Subscribe
    public void onGameEvent(GameEvent event) {
        switch (event.action) {
            case UPDATE:
                mGameHasStarted = true;
                mRemainingRoundMillis = event.game.getMillisRemainingRound();

                updateSecondsProgress(mRemainingRoundMillis);
                break;
            case NEW_ROUND:
                mCurrentRound = event.game.getRound();
                mRemainingRoundMillis = DEFAULT_ROUND_MILLISECONDS;

                updateRoundsProgress(mCurrentRound);
                updateSecondsProgress(mRemainingRoundMillis);
                break;
            case FINISH:
                mCurrentRound = mOptions.getRounds();
                mRemainingRoundMillis = DEFAULT_ROUND_MILLISECONDS;

                updateSecondsProgress(DEFAULT_ROUND_MILLISECONDS);
                updateRoundsProgress(mCurrentRound);
                mRoundsText.setText("finished");
                mCountdownText.setText("zero");
                mControl.updateControlIcon(GameControlButtons.GameStates.HIDE);
                break;
        }
    }

    private void updateSecondsProgress(long milliseconds) {
        updateSecondsProgress(milliseconds, true);
    }

    private void updateSecondsProgress(long milliseconds, boolean animate) {
        float secondsLeft = milliseconds / 1000.0f;
        float progress = (secondsLeft / (float) Engine.MINUTE_DURATION);

        mCountdownText.setText(String.format("%.1f", secondsLeft));

        if (animate) {
            animateProgressWheel(mProgressSeconds, progress);
        } else {
            mProgressSeconds.setProgress(progress);
        }
    }

    private void updateRoundsProgress(int rounds) {
        updateRoundsProgress(rounds, true);
    }

    private void updateRoundsProgress(int round, boolean animate) {
        float progress = (float) round / mOptions.getRounds();

        mRoundsText.setText(String.valueOf(round) + ROUND_OF_MAX_TEXT);

        if (animate) {
            animateProgressWheel(mProgressRounds, progress);
        } else {
            mProgressRounds.setProgress(progress);
        }
    }

    private void updatePauses() {
        if (mOptions.getMaxPauses() == -1) {
            mPausesText.setText("∞ pauses");
        } else if (mOptions.getMaxPauses() == mPausedCount) {
            mPausesText.setText("No more pausing");
        } else {
            int remaining = mOptions.getMaxPauses() - mPausedCount;
            mPausesText.setText(String.valueOf(remaining) + PAUSES_REMAINING_TEXT);
        }
        mControl.setPauseCount(mPausedCount);
    }

    private void animateProgressWheel(HoloCircularProgressBar view, float progress) {
        ObjectAnimator animation = ObjectAnimator.ofFloat(view, "progress", progress);
        animation.setDuration(500); // 0.5 second
        animation.setInterpolator(new LinearInterpolator());
        animation.start();
    }
}
