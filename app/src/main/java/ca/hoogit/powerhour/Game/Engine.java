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
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.util.concurrent.TimeUnit;

import ca.hoogit.powerhour.BusProvider;

public class Engine extends Service {

    private static final String TAG = Engine.class.getSimpleName();

    public static State mState = State.NONE;
    public static boolean initialized = false;

    private Bus mBus;
    private static Game mGame;
    private CountDownTimer mTimer;

    private long mRoundCounter = 0;

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
            mGame.setState(State.INITIALIZED);
            mState = State.INITIALIZED;
            initialized = true;
            broadcast(Action.UPDATE);
        }
        if (mGame == null) {
            throw new NullPointerException("No game object was passed to the service.");
        } else {
            if (mGame.isAutoStart()) {
                start();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void broadcast(Action action) {
        mBus.post(new GameEvent(action, mGame));
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

    @Produce
    public GameEvent produceGameInformation() {
        return new GameEvent(Action.PRODUCE, mGame);
    }

    private void start() {
        if (!mGame.hasStarted()) {
            long milliseconds = roundsToMilliseconds(mGame.getTotalRounds());
            Log.i(TAG, "Starting the game with " + mGame.getTotalRounds() + " rounds");
            createTimer(milliseconds);
            mGame.setStarted(true);
            mGame.setState(State.ACTIVE);
            broadcast(Action.UPDATE);
        } else {
            Log.e(TAG, "Game already started, cannot start another.");
        }
    }

    private void pause() {
        if (mGame.is(State.ACTIVE)) {
            if (mGame.canPause()) {
                Log.i(TAG, "Pausing game on round " + mGame.currentRound());
                Log.i(TAG, "Remaining pauses: " + mGame.remainingPauses());
                logTimeLeft();
                mTimer.cancel();
                mGame.incrementPauses();
                mGame.setState(State.PAUSED);
                broadcast(Action.UPDATE);
            } else {
                Log.i(TAG, "No pauses remaining");
            }
        }
    }

    private void resume(long milliseconds) {
        if (mGame.is(State.PAUSED)) {
            Log.i(TAG, "Resuming game on round " + mGame.currentRound());
            logTimeLeft();
            createTimer(milliseconds);
            mGame.setState(State.ACTIVE);
            broadcast(Action.UPDATE);
        } else {
            Log.e(TAG, "Trying to resume an active game");
        }
    }

    private void stop() {
        if (mGame.hasStarted()) {
            Log.i(TAG, "Stopping game on round " + mGame.currentRound());
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
                    mRoundCounter += 100;
                    if (mRoundCounter == Game.ROUND_DURATION_MILLIS) {
                        action = Action.NEW_ROUND;
                        mGame.incrementRound();
                        mRoundCounter = 0;
                        Log.d(TAG, "A Round has been completed");
                    } else {
                        action = Action.UPDATE;
                    }
                    updateGameMilliseconds(millisUntilFinished);
                    mGame.setState(State.ACTIVE);
                    broadcast(action);
                    // TODO Pause the timer to allow for animations, maybe do so in fragment
                }

                @Override
                public void onFinish() {
                    mGame.setMillisRemainingGame(0);
                    mGame.setMillisRemainingRound(0);
                    mGame.setRound(mGame.getTotalRounds());
                    mGame.setState(State.FINISHED);
                    broadcast(Action.FINISH);
                    Log.d(TAG, "Game has completed");
                    finish();
                }
            };
            mTimer.start();
        }
    }

    private void updateGameMilliseconds(long millis) {
        long roundMillis = Game.ROUND_DURATION_MILLIS - mRoundCounter;
        mGame.setMillisRemainingGame(millis);
        mGame.setMillisRemainingRound(roundMillis);
    }

    private void finish() {
        mState = State.NONE;
        mGame = null;
        initialized = false;
        Log.d(TAG, "Goodnight friend, it was a pleasure");
        mBus.unregister(this);
        stopSelf();
    }

    public static boolean started() {
        return mState != State.NONE;
    }

    public static GameOptions options() {
        return mGame.options();
    }

    /**
     * Helpers
     */

    private long roundsToMilliseconds(int rounds) {
        int seconds = rounds * Game.ROUND_DURATION_SECONDS;
        return TimeUnit.SECONDS.toMillis(seconds);
    }

    private long millisecondsToMinutes(long milliseconds) {
        return TimeUnit.MILLISECONDS.toMinutes(milliseconds);
    }

    private void logTimeLeft() {
        Log.d(TAG, "Game Minutes Left : " + TimeUnit.MILLISECONDS.toMinutes(
                mGame.getMillisRemainingGame()));
        Log.d(TAG, "Round Seconds Left: " + TimeUnit.MILLISECONDS.toSeconds(
                mGame.getMillisRemainingRound()));
    }
}
