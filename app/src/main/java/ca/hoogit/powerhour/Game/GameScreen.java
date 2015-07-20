package ca.hoogit.powerhour.Game;

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
import ca.hoogit.powerhour.Util.ProgressUpdater;
import ca.hoogit.powerhour.Views.GameControlButtons;
import ca.hoogit.powerhour.Views.GameControlButtons.GameControl;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GameScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameScreen extends Fragment {

    private static final String TAG = GameScreen.class.getSimpleName();

    private static final String ARG_DETAILS = "details";

    private final String PAUSES_REMAINING_TEXT = " pauses remaining";
    private final String PAUSES_UNLIMITED_TEXT = "∞ pauses";

    private AppCompatActivity mActivity;
    private Game mGame;
    private boolean canUpdate = true;

    private int mPauseCount = 0;

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

    private ProgressUpdater mSecondsUpdater;
    private ProgressUpdater mRoundsUpdater;

    /**
     * @return A new instance of fragment GameScreen.
     */
    public static GameScreen newInstance(Game game) {
        GameScreen fragment = new GameScreen();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DETAILS, game);
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
    @SuppressWarnings("ConstantConditions")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_screen, container, false);
        ButterKnife.bind(this, view);

        // Setup the toolbar
        mActivity = (AppCompatActivity) getActivity();
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_icon);
        mActivity.setSupportActionBar(mToolbar);

        try {
            mActivity.getSupportActionBar().setHomeButtonEnabled(true);
            mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (NullPointerException ex) {
            Log.e(TAG, ex.getMessage());
        }

        mRoundsUpdater = new ProgressUpdater(mProgressRounds);
        mSecondsUpdater = new ProgressUpdater(mProgressSeconds);

        BusProvider.getInstance().register(this);

        return view;
    }

    private void setup() {
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
        mPauseCount = mGame.getPauses();

        mRoundsUpdater.update(calculateRounds(mGame.getMillisRemainingGame()), false);
        mSecondsUpdater.update(calculateSeconds(mGame.getMillisRemainingRound()), false);

        // Get status of game, if it hasn't started then initialize
        if (mGame.is(State.ACTIVE)) {
            mControl.toggleCenterButton();
        }

        setupControlButtons();

        Log.d(TAG, "Setup complete");
    }

    private void setupControlButtons() {
        mControl.setColor(mGame.options().getAccentColor());
        mControl.setOnButtonPressed(new GameControl() {
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
                    if (!mGame.canPause()) {
                        mControl.hideCenter();
                    }
                }
            }

            @Override
            public void stopPressed() {
                canUpdate = false;
                stopGame();
            }
        });
        if (!mGame.canPause() && mGame.is(State.ACTIVE)) {
            updatePauses();
            mControl.hideCenter();
        }
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
                        BusProvider.getInstance().unregister(this);
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
    }

    /**
     * Otto Event methods
     */

    private void broadcast(Action action, Game game) {
        BusProvider.getInstance().post(new GameEvent(action, game));
    }

    @Subscribe
    public void onGameEvent(GameEvent event) {
        switch (event.action) {
            case PRODUCE:
                if (event.game != null) {
                    mGame = event.game;
                    Log.i(TAG, "Received game information from engine");
                } else {
                    Log.i(TAG, "Produced a null game, using stale data");
                    if (getArguments() != null) {
                        mGame = (Game) getArguments().getSerializable(ARG_DETAILS);
                    } else {
                        Log.e(TAG, "No game details could be loaded, closing.");
                        BusProvider.getInstance().post(new GameEvent(Action.STOP));
                        break;
                    }
                }
                setup();
                break;
            case UPDATE:
                if (!canUpdate) break;

                mGame = event.game;
                mControl.setIcon(mGame.is(State.PAUSED));
                mRoundsUpdater.update(calculateRounds(mGame.getMillisRemainingGame()));
                mSecondsUpdater.update(calculateSeconds(mGame.getMillisRemainingRound()));

                // Don't run this unneeded method 10 times a second.
                if (mPauseCount < mGame.getPauses()) {
                    updatePauses();
                    mPauseCount++;
                }
                break;
            case NEW_ROUND:
                if (!canUpdate) break;
                // TODO HANDLE NEW ROUND
                mGame = event.game;
                mRoundsUpdater.update(calculateRounds(mGame.getMillisRemainingGame()));
                mSecondsUpdater.update(calculateSeconds(Game.ROUND_DURATION_MILLIS));
                break;
            case FINISH:
                mGame = event.game;
                mRoundsUpdater.update(calculateRounds(Game.ROUND_DURATION_MILLIS), 500);
                mSecondsUpdater.update(calculateSeconds(mGame.gameMillis()), 500);
                mRoundsText.setText("finished");
                mCountdownText.setText("zero");
                mControl.hideCenter();
                break;
        }
    }

    private void updatePauses() {
        if (mGame.getMaxPauses() == -1) {
            mPausesText.setText(PAUSES_UNLIMITED_TEXT);
        } else if (mGame.canPause()) {
            mPausesText.setText(mGame.remainingPauses() + PAUSES_REMAINING_TEXT);
            Log.d(TAG, "Pause count: " + mGame.getPauses());
            Log.d(TAG, "Remaining  :" + mGame.remainingPauses());
        } else {
            mPausesText.setText("zero pauses");
            Log.d(TAG, "All pause breaks have been used");
        }
    }

    private float calculateSeconds(long milliseconds) {
        float secondsLeft = milliseconds / 1000.0f;
        mCountdownText.setText(String.format("%.1f", secondsLeft));
        return (float) milliseconds / Game.ROUND_DURATION_MILLIS;
    }

    private float calculateRounds(long milliseconds) {
        float elapsed = (float) mGame.gameMillis() - milliseconds;
        elapsed = elapsed == mGame.gameMillis() ? 0 : elapsed;
        mRoundsText.setText(String.valueOf(mGame.currentRound()) + ROUND_OF_MAX_TEXT);
        return elapsed / mGame.gameMillis();
    }

}
