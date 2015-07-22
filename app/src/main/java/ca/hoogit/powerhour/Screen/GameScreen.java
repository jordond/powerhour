package ca.hoogit.powerhour.Screen;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.otto.Subscribe;

import ca.hoogit.powerhour.BusProvider;
import ca.hoogit.powerhour.Game.Action;
import ca.hoogit.powerhour.Game.Game;
import ca.hoogit.powerhour.Game.GameEvent;
import ca.hoogit.powerhour.Game.State;
import ca.hoogit.powerhour.R;
import ca.hoogit.powerhour.Views.GameControlButtons.GameControl;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GameScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameScreen extends Fragment {

    private static final String TAG = GameScreen.class.getSimpleName();

    private static final String ARG_DETAILS = "details";

    private ScreenView mScreenView;
    private Game mGame;

    private int mPauseCount = 0;

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

        mScreenView = new ScreenView(getActivity(), view);

        mRoundsUpdater = mScreenView.getUpdater(R.id.game_screen_circle_progress_rounds);
        mSecondsUpdater = mScreenView.getUpdater(R.id.game_screen_circle_progress_seconds);

        BusProvider.getInstance().register(this);

        return view;
    }

    private void setup() {
        mScreenView.setup(mGame);

        mPauseCount = mGame.getPauses();

        mRoundsUpdater.set(calculateRounds(mGame.getMillisRemainingGame()), false);
        mSecondsUpdater.set(calculateSeconds(mGame.getMillisRemainingRound()), false);

        mScreenView.setControlListener(new GameControl() {
            @Override
            public void soundPressed() {
                Toast.makeText(getActivity(), "Sound button pressed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void soundLongPressed() {
                Toast.makeText(getActivity(), "Long press", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void screenLockPressed() {
                mScreenView.toggleKeepOnButton();
            }

            @Override
            public void controlPressed() {
                broadcast(Action.TOGGLE, null);
            }

            @Override
            public void stopPressed() {
                stopGame();
            }
        });

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
                mGame = event.game;
                mScreenView.setState(mGame.getState(), mGame.getPauses());

                mRoundsUpdater.set(calculateRounds(mGame.getMillisRemainingGame()));
                mSecondsUpdater.set(calculateSeconds(mGame.getMillisRemainingRound()));

                // Don't run this unneeded method 10 times a second.
                if (mPauseCount <= mGame.getPauses()) {
                    mScreenView.setPauseText(mGame.getPauses());
                    mPauseCount++;
                }
                break;
            case NEW_ROUND:
                mGame = event.game;
                mRoundsUpdater.set(calculateRounds(mGame.getMillisRemainingGame()));
                mSecondsUpdater.set(calculateSeconds(Game.ROUND_DURATION_MILLIS));
                break;
            case FINISH:
                mGame = event.game;

                mRoundsUpdater.set(calculateRounds(Game.ROUND_DURATION_MILLIS));
                mSecondsUpdater.set(calculateSeconds(mGame.gameMillis()));
                mScreenView.setState(State.FINISHED, 0);
                break;
        }
    }

    private float calculateSeconds(long milliseconds) {
        float secondsLeft = milliseconds / 1000.0f;
        mScreenView.setCountdownText(String.format("%.1f", secondsLeft));
        return (float) milliseconds / Game.ROUND_DURATION_MILLIS;
    }

    private float calculateRounds(long milliseconds) {
        float elapsed = (float) mGame.gameMillis() - milliseconds;
        elapsed = elapsed == mGame.gameMillis() ? 0 : elapsed;
        mScreenView.setRoundsText(String.valueOf(mGame.currentRound()));
        return elapsed / mGame.gameMillis();
    }

}
