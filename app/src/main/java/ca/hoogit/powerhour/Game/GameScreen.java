

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
    private static final String ARG_GAME = "game";

    public static final String INSTANCE_STATE_GAME_STARTED = "gameStarted";
    public static final String INSTANCE_STATE_GAME = "game";

    public static final String PAUSES_REMAINING_TEXT = " Pauses Remaining";

    private AppCompatActivity mActivity;

    private Game mGame;

    private boolean mIsActive = false;
    private boolean mIsGameStarted = false;
    private int mPrimaryColor;
    private int mAccentColor;

    // Game variables
    private int mPauses = -1;
    private int mRound = -1;

    @Bind(R.id.appBar) Toolbar mToolbar;
    @Bind(R.id.game_screen_layout) RelativeLayout mLayout;
    @Bind(R.id.game_screen_control) GameControlButtons mControl;

    @Bind(R.id.game_screen_title) TextView mTitle;
    @Bind(R.id.game_screen_remaining_pauses) TextView mPausesText;
    @Bind(R.id.game_screen_rounds_remaining) TextView mRoundsText;

    @Bind(R.id.game_screen_circle_progress_seconds) HoloCircularProgressBar mProgressSeconds;
    @Bind(R.id.game_screen_circle_progress_rounds) HoloCircularProgressBar mProgressRounds;

    /**
     * @param game Parameter 1.
     * @return A new instance of fragment GameScreen.
     */
    public static GameScreen newInstance(Game game) {
        GameScreen fragment = new GameScreen();
        Bundle args = new Bundle();
        args.putSerializable(ARG_GAME, game);
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
        if (getArguments() != null) {
            mGame = (Game) getArguments().getSerializable(ARG_GAME);
        }
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

        // Change colors to those set in options;
        mPrimaryColor = mGame.getOptions().getBackgroundColor();
        mAccentColor = mGame.getOptions().getAccentColor();

        mToolbar.setBackgroundColor(mPrimaryColor);
        mLayout.setBackgroundColor(mPrimaryColor);
        BusProvider.getInstance().post(new ChangeStatusColor(mActivity, mPrimaryColor));

        // Setup view items
        mTitle.setText(mGame.getOptions().getTitle());
        mProgressRounds.setProgressColor(mAccentColor);
        mProgressSeconds.setThumbColor(mAccentColor);
        mProgressSeconds.setProgressColor(mPrimaryColor);
        mProgressSeconds.setProgressBackgroundColor(mPrimaryColor);

        //TODO check for active game, or retrieve from saved instance
        updatePauses();
        updateRounds();

        setupControlButtons();

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(INSTANCE_STATE_GAME_STARTED, mIsGameStarted);
        outState.putSerializable(INSTANCE_STATE_GAME, mGame);

        // TODO save current round and pauses, so on rotate updatePauses/updateRounds will work correctly
    }

    private void setupControlButtons() {
        mControl.setMaxPauses(mGame.getOptions().getMaxPauses());
        mControl.setIsAcitve(mIsActive);
        mControl.setColor(mGame.getOptions().getAccentColor());

        GameControl buttonPressed = new GameControl() {
            @Override
            public void soundPressed() {
                Toast.makeText(getActivity(), "Sound button pressed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void controlPressed(boolean isActive, int numberOfPauses) {
                if (!mIsGameStarted) {
                    updateRounds();
                }
                if (isActive) {
                    Toast.makeText(getActivity(), "Game running", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Game has been paused", Toast.LENGTH_SHORT).show();
                    updatePauses();
                    Log.d(TAG, "Paused: " + numberOfPauses + " times");
                    Log.d(TAG, "Remaining pauses: " + (mGame.getOptions().getMaxPauses() - numberOfPauses) + " times");
                }
                mIsGameStarted = true;
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
                .content("Are you sure you want to stop this game of " + mGame.getOptions().getTitle() + "?")
                .positiveText("Quit!")
                .negativeText("Keep drinking!")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        mIsGameStarted = false;
                        Toast.makeText(getActivity(), "Game was stopped...", Toast.LENGTH_SHORT).show();
                        BusProvider.getInstance().post(new GameEvent(Action.STOP));
                    }
                }).show();

    }

    private void updatePauses() {
        mPauses++;
        if (mGame.getOptions().getMaxPauses() == -1) {
            mPausesText.setText("∞ pauses");
        } else if (mGame.getOptions().getMaxPauses() == mPauses) {
            mPausesText.setText("No more pausing");
        } else {
            int remaining = mGame.getOptions().getMaxPauses() - mPauses;
            mPausesText.setText(String.valueOf(remaining) + PAUSES_REMAINING_TEXT);
        }
    }

    private void updateRounds() {
        mRound++;
        if (mRound <= mGame.getOptions().getRounds()) {
            mRoundsText.setText(mRound + " of " + mGame.getOptions().getRounds());
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // TODO handle back button, sharedprefs?
    }
}
