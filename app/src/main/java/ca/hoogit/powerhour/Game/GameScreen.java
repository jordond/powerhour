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
import com.squareup.otto.Bus;
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

    private static final String PAUSES_REMAINING_TEXT = " Pauses Remaining";
    private static final String PAUSES_UNLIMITED_TEXT = "∞ pauses";

    public static final String INSTANCE_STATE_GAME = "game";

    private Game mGame;
    private boolean canUpdate = true;

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
        BusProvider.getInstance().register(this); // TODO move to handle the produce? get rid of instance state
        if (getArguments() != null) {
            GameOptions options = (GameOptions) getArguments().getSerializable(ARG_OPTIONS);
            mGame = new Game(options);
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
        int mPrimaryColor = mGame.options().getBackgroundColor();
        int mAccentColor = mGame.options().getAccentColor();

        mToolbar.setBackgroundColor(mPrimaryColor);
        mLayout.setBackgroundColor(mPrimaryColor);
        BusProvider.getInstance().post(new ChangeStatusColor(mActivity, mPrimaryColor));

        // Setup titles and colors
        mTitle.setText(mGame.options().getTitle());
        mProgressRounds.setProgressColor(mAccentColor);
        mProgressSeconds.setThumbColor(mAccentColor);
        mProgressSeconds.setProgressColor(mAccentColor);
        mProgressSeconds.setProgressBackgroundColor(mPrimaryColor);

        ROUND_OF_MAX_TEXT = " of " + mGame.options().getRounds();

        // Set the text and progress wheels
        mRoundsText.setText(mGame.currentRound() + ROUND_OF_MAX_TEXT);
        if (mGame.getMaxPauses() == -1) {
            mPausesText.setText(PAUSES_UNLIMITED_TEXT);
        } else {
            mPausesText.setText(mGame.remainingPauses() + PAUSES_REMAINING_TEXT);
        }

        updateRoundsProgress(mGame.currentRound(), false);
        updateSecondsProgress(mGame.getMillisRemainingRound(), false);

        // Get status of game, if it hasn't started then initialize
        mGame.setStarted(Engine.started());

        if (!mGame.hasStarted()) {
            broadcast(Action.INITIALIZE, mGame);
            if (mGame.options().isAutoStart()) {
                mControl.toggleCenterButton();
            }
        }

        setupControlButtons();

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(INSTANCE_STATE_GAME, mGame);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mGame = (Game) savedInstanceState.getSerializable(INSTANCE_STATE_GAME);
        }
    }

    private void setupControlButtons() {
        mControl.setColor(mGame.options().getAccentColor());

        GameControl buttonPressed = new GameControl() {
            @Override
            public void soundPressed() {
                Toast.makeText(getActivity(), "Sound button pressed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void controlPressed() {
                if (mGame.is(State.INITIALIZED)) {
                    broadcast(Action.START, null);
                } else if (mGame.is(State.ACTIVE)) {
                    if (mGame.canPause()) {
                        broadcast(Action.PAUSE, null);
                    }
                } else if (mGame.is(State.PAUSED)) {
                    broadcast(Action.RESUME, null);
                }
            }

            @Override
            public void stopPressed() {
                canUpdate = false;
                stopGame();
            }
        };
        mControl.setOnButtonPressed(buttonPressed);
    }

    private void stopGame() {
        new MaterialDialog.Builder(getActivity())
                .title("Stop the game?")
                .content("Are you sure you want to stop this game of " + mGame.options().getTitle() + "?")
                .positiveText("Quit!")
                .negativeText("Keep drinking!")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        Toast.makeText(getActivity(), "Game was stopped...", Toast.LENGTH_SHORT).show(); //TODO remove
                        broadcast(Action.STOP, null);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        canUpdate = true;
                    }
                }).show();

    }

    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
        // TODO handle back button, sharedprefs?
    }

    private void broadcast(Action action, Game game) {
        BusProvider.getInstance().post(new GameEvent(action, game));
    }

    @Subscribe
    public void onGameEvent(GameEvent event) {
        switch (event.action) {
            case PRODUCE:
                if (event.game != null) {
                    mGame = event.game;
                }
                break;
            case UPDATE:
                if (!canUpdate) break;

                int oldPauses = mGame.getPauses();
                mGame = event.game;

                updateSecondsProgress(mGame.getMillisRemainingRound());
                if (oldPauses != mGame.getPauses()) {
                    updatePauses();
                }
                break;
            case NEW_ROUND:
                if (!canUpdate) break;

                mGame = event.game;
                updateRoundsProgress(mGame.currentRound());
                updateSecondsProgress(Game.ROUND_DURATION_MILLIS);
                break;
            case FINISH:
                mGame = event.game;
                updateSecondsProgress(Game.ROUND_DURATION_MILLIS);
                updateRoundsProgress(mGame.currentRound());
                mRoundsText.setText("finished");
                mCountdownText.setText("zero");
                mControl.hideCenter();
                break;
        }
    }

    private void updateSecondsProgress(long milliseconds) {
        updateSecondsProgress(milliseconds, true);
    }

    private void updateSecondsProgress(long milliseconds, boolean animate) {
        float secondsLeft = milliseconds / 1000.0f;
        float progress = (secondsLeft / (float) Game.ROUND_DURATION_SECONDS);

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
        float progress = (float) round / mGame.options().getRounds();

        mRoundsText.setText(String.valueOf(round) + ROUND_OF_MAX_TEXT);

        if (animate) {
            animateProgressWheel(mProgressRounds, progress);
        } else {
            mProgressRounds.setProgress(progress);
        }
    }

    private void updatePauses() {
        if (mGame.getMaxPauses() == -1) {
            mPausesText.setText(PAUSES_UNLIMITED_TEXT);
        } else if (mGame.canPause()) {
            mPausesText.setText(mGame.remainingPauses() + PAUSES_REMAINING_TEXT);
            Log.d(TAG, "Paused: " + mGame.getPauses() + " times");
            Log.d(TAG, "Remaining pauses: " + mGame.remainingPauses() + " times");
        } else {
            mPausesText.setText("zero pauses");
            if (mGame.is(State.ACTIVE)) {
                mControl.hideCenter();
            }
        }
    }

    private void animateProgressWheel(HoloCircularProgressBar view, float progress) {
        ObjectAnimator animation = ObjectAnimator.ofFloat(view, "progress", progress);
        animation.setDuration(500); // 0.5 second
        animation.setInterpolator(new LinearInterpolator());
        animation.start();
    }
}
