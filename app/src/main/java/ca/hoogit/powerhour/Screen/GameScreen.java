package ca.hoogit.powerhour.Screen;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
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

import ca.hoogit.powerhour.Game.Action;
import ca.hoogit.powerhour.Game.Celebrator;
import ca.hoogit.powerhour.Game.GameEvent;
import ca.hoogit.powerhour.Game.GameModel;
import ca.hoogit.powerhour.Game.State;
import ca.hoogit.powerhour.R;
import ca.hoogit.powerhour.Util.BusProvider;
import ca.hoogit.powerhour.Views.GameControlButtons.GameControl;
import ca.powerhour.common.DataLayer.Consts;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GameScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameScreen extends Fragment implements GameControl {

    private static final String TAG = GameScreen.class.getSimpleName();

    private static final String ARG_DETAILS = "details";

    private ScreenView mScreenView;
    private GameModel mGame;

    private int mPauseCount = 0;

    private ProgressUpdater mSecondsUpdater;
    private ProgressUpdater mRoundsUpdater;

    /**
     * @return A new instance of fragment GameScreen.
     */
    public static GameScreen newInstance(GameModel gameModel) {
        GameScreen fragment = new GameScreen();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DETAILS, gameModel);
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

        mScreenView.setControlListener(this);

        if (mGame.is(State.NEW_ROUND)) {
            //handleNewRound();
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
                        broadcast(Action.STOP, mGame);
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

    private void broadcast(Action action, GameModel gameModel) {
        BusProvider.getInstance().post(new GameEvent(action, gameModel));
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
                        mGame = (GameModel) getArguments().getSerializable(ARG_DETAILS);
                    } else {
                        Log.e(TAG, "No game details could be loaded, closing.");
                        broadcast(Action.STOP, null);
                        break;
                    }
                }
                setup();
                break;
            case UPDATE:
                mGame = event.game;
                mScreenView.setState(mGame.getState());

                mScreenView.setRemainingMinutes(mGame.getMillisRemainingGame());
                mRoundsUpdater.set(calculateRounds(mGame.getMillisRemainingGame()));
                mSecondsUpdater.set(calculateSeconds(mGame.getMillisRemainingRound()));

                // Don't run this unneeded method 10 times a second.
                if (mPauseCount <= mGame.getPauses()) {
                    mScreenView.setPauseText(mGame.getPauses());
                    mPauseCount++;
                }
                break;
            case FINISH:
                mGame = event.game;

                mRoundsUpdater.set(calculateRounds(mGame.gameMillis()));
                mSecondsUpdater.set(calculateSeconds(Consts.Game.ROUND_DURATION_MILLIS));
                break;
        }
    }

    @Subscribe
    public void onCelebrateEvent(Celebrator.CelebrationEvent event) {
        switch (event.action) {
            case Celebrator.CelebrationEvent.ACTION_START:
                mScreenView.shotAmate();
                break;
            case Celebrator.CelebrationEvent.ACTION_TICK:
                mRoundsUpdater.set(event.progress / 100.0f);
                break;
            case Celebrator.CelebrationEvent.ACTION_FINISH:
                mRoundsUpdater.set(calculateRounds(mGame.gameMillis()));
                break;
        }
    }

    private float calculateSeconds(long milliseconds) {
        float secondsLeft = milliseconds / 1000.0f;
        mScreenView.setCountdownText(String.format("%.1f", secondsLeft));
        return (float) milliseconds / Consts.Game.ROUND_DURATION_MILLIS;
    }

    private float calculateRounds(long milliseconds) {
        float elapsed = (float) mGame.gameMillis() - milliseconds;
        elapsed = elapsed == mGame.gameMillis() ? 0 : elapsed;
        mScreenView.setRoundsText(String.valueOf(mGame.currentRound()));
        return elapsed / mGame.gameMillis();
    }

    @Override
    public void soundPressed() {
        boolean muted = mGame.options().isMuted();
        mScreenView.getControl().setMuteIcon(!muted);
        mGame.options().setIsMuted(!muted);
        broadcast(Action.UPDATE_SETTINGS, mGame);
        Log.i(TAG, "Mute button was pressed, status: " + muted);

        if (!muted) {
            Snackbar
                .make(getView(), getActivity().getString(R.string.muted_game),
                        Snackbar.LENGTH_LONG)
                    .setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            soundPressed();
                        }
                }).show();
        }
    }

    @Override
    public void soundLongPressed() {
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
}
