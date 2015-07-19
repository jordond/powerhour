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

package ca.hoogit.powerhour.Game;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.concurrent.TimeUnit;

import ca.hoogit.powerhour.BusProvider;

public class Engine extends Service {

    private static final String TAG = Engine.class.getSimpleName();

    private final int ROUND_COUNTER_MAX = 60 * 1000;

    public static State mState = State.NONE;

    private Bus mBus;

    private static Game mGame;

    private long mRoundCounter = 0;

    private CountDownTimer mTimer;

    public Engine() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBus = BusProvider.getInstance();
        mBus.register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mGame = (Game) intent.getSerializableExtra("game");
            mState = State.INITIALIZED;
        }
        if (mGame == null) {
            throw new NullPointerException("No game object was passed to the service.");
        } else {
            if (mGame.isAutostart()) {
                start();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Subscribe
    public void onGameEvent(GameEvent event) {
        switch (event.action) {
            case START:
                start();
                break;
            case PAUSE:
                pause();
                break;
            case RESUME:
                resume(mGame.getMillisRemainingGame());
                break;
            case STOP:
                stop();
                break;
        }
    }

    private void start() {
        if (!mGame.hasStarted()) {
            long milliseconds = roundsToMilliseconds(mGame.getTotalRounds());
            Log.i(TAG, "Starting the game with " + mGame.getTotalRounds() + " rounds");
            createTimer(milliseconds);
            mGame.setStarted(true);
            mGame.setState(State.ACTIVE);
        } else {
            Log.e(TAG, "Game already started, cannot start another.");
        }
    }

    private void pause() {
        if (mGame.is(State.ACTIVE)) {
            if (mGame.canPause()) {
                Log.i(TAG, "Pausing game on round " + mGame.getRound());
                logTimeLeft();
                mTimer.cancel();
                mGame.incrementPauses();
                mGame.setState(State.PAUSED);
            } else {
                Log.i(TAG, "No pauses remaining");
            }
        }
    }

    private void resume(long milliseconds) {
        if (mGame.is(State.ACTIVE)) {
            mTimer.cancel();
        }
        Log.i(TAG, "Resuming game on round " + mGame.getRound());
        logTimeLeft();
        createTimer(milliseconds);
        mGame.setState(State.ACTIVE);
    }

    private void stop() {
        if (mGame.hasStarted()) {
            Log.i(TAG, "Stopping game on round " + mGame.getRound());
            if (mGame.is(State.ACTIVE)) {
                mTimer.cancel();
            }
        }
        finish();
    }

    private void createTimer(long milliseconds) {
        if (!mGame.is(State.ACTIVE)) {
            mTimer = new CountDownTimer(milliseconds, 100) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Action action;
                    Game game;
                    mRoundCounter += 100;
                    if (mRoundCounter == ROUND_COUNTER_MAX) {
                        action = Action.NEW_ROUND;
                        mGame.incrementRound();
                        mRoundCounter = 0;
                        Log.d(TAG, "A Round has been completed");
                    } else {
                        action = Action.UPDATE;
                    }
                    updateGameMilliseconds(millisUntilFinished);
                    game = mGame;
                    game.setOptions(null);
                    mBus.post(new GameEvent(action, game));
                    mGame.setState(State.ACTIVE);
                    // TODO Pause the timer to allow for animations, maybe do so in fragment
                }

                @Override
                public void onFinish() {
                    mGame.setMillisRemainingGame(0);
                    mGame.setMillisRemainingRound(0);
                    mGame.setRound(mGame.getTotalRounds());
                    mGame.setState(State.FINISHED);
                    mBus.post(new GameEvent(Action.FINISH, mGame));
                    finish();
                }
            };
            mTimer.start();
        }
    }

    private void updateGameMilliseconds(long millis) {
        long roundMillis = ROUND_COUNTER_MAX - mRoundCounter;
        mGame.setMillisRemainingGame(millis);
        mGame.setMillisRemainingRound(roundMillis);
    }

    private void finish() {
        mState = State.NONE;
        mBus.unregister(this);
        stopSelf();
    }

    public static boolean started() {
        return mState != State.NONE;
    }

    public static GameOptions options() {
        return mGame.getOptions();
    }

    /**
     * Helpers
     */

    private long roundsToMilliseconds(int rounds) {
        int seconds = rounds * 60;
        return TimeUnit.SECONDS.toMillis(seconds);
    }

    private long millisecondsToMinutes(long milliseconds) {
        return TimeUnit.MILLISECONDS.toMinutes(milliseconds);
    }

    private void logTimeLeft() {
        Log.i(TAG, "Game Minutes Left : " + TimeUnit.MILLISECONDS.toMinutes(
                mGame.getMillisRemainingGame()));
        Log.i(TAG, "Round Seconds Left: " + TimeUnit.MILLISECONDS.toSeconds(
                mGame.getMillisRemainingRound()));
    }
}
