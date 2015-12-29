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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import ca.hoogit.powerhour.Notifications.Constants;
import ca.hoogit.powerhour.Notifications.Foreground;
import ca.hoogit.powerhour.Selection.MainActivity;
import ca.hoogit.powerhour.Util.BusProvider;
import ca.powerhour.common.DataLayer.Consts;

public class Engine extends Service {

    private static final String TAG = Engine.class.getSimpleName();
    private static final long[] VIBRATE_PATTERN = {200, 400, 200, 500, 200, 300};

    private final int TICK_LENGTH = 100;

    public static boolean initialized = false;
    public static boolean started = false;

    private Bus mBus;
    private GameModel mGame;
    private CountDownTimer mTimer;
    private Celebrator mCelebrator;

    private Vibrator mVibrator;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;

    private long mRoundCounter = 0;

    public Engine() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBus = BusProvider.getInstance();
        mBus.register(this);
        mPowerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        mVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        getWakelock(false);
    }

    private void getWakelock(boolean wakeScreen) {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        if (!wakeScreen) {
            mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        } else {
            mWakeLock = mPowerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            handleIntent(intent);
        }
        return START_NOT_STICKY;
    }

    private void handleIntent(Intent intent) {
        switch (intent.getAction()) {
            case Constants.ACTION.INITIALIZE_GAME:
                mGame = (GameModel) intent.getSerializableExtra("game");
                if (mGame != null) {
                    mGame.setState(State.INITIALIZED);
                    initialized = true;
                    if (mGame.isAutoStart()) {
                        start();
                    }
                    Log.i(TAG, "Game has been initialized");
                } else {
                    stopSelf();
                    Log.e(TAG, "No game was passed to engine");
                }
                break;
            case Constants.ACTION.PAUSE_GAME:
                Log.d(TAG, "Pause intent has been received");
                pause();
                break;
            case Constants.ACTION.RESUME_GAME:
                Log.d(TAG, "Resume intent has been received");
                resume();
                break;
        }
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
            case TOGGLE:
                switch (mGame.getState()) {
                    case INITIALIZED: start(); break;
                    case ACTIVE: pause(); break;
                    case PAUSED: resume(); break;
                    case NEW_ROUND:
                        Log.i(TAG, "Resuming game early after new round.");
                        resume();
                        break;
                }
                break;
            case START: start(); break;
            case PAUSE: pause(); break;
            case RESUME: resume(); break;
            case STOP: stop(); break;
            case UPDATE_SETTINGS:
                if (event.game != null) {
                    mGame.setOptions(event.game.options());
                }
                break;
        }
    }

    @Produce
    public GameEvent produceGameInformation() {
        return new GameEvent(Action.PRODUCE, mGame);
    }

    private void start() {
        if (!mGame.hasStarted()) {
            long milliseconds = mGame.totalRoundsLeftToMilliseconds();
            Log.i(TAG, "Starting the game with " + mGame.getTotalRounds() + " rounds");
            createTimer(milliseconds);
            mGame.setStarted(true);
            mGame.setState(State.ACTIVE);
            started = true;
            broadcast(Action.UPDATE);
            mCelebrator = new Celebrator(getApplicationContext(), mGame.options());
            mWakeLock.acquire(mGame.getMillisRemainingGame() - TICK_LENGTH);
            Log.d(TAG, "Status of wakelock: " + mWakeLock.isHeld());
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_ID, Foreground.build(this, mGame));
        } else {
            Log.e(TAG, "Game already started, cannot start another.");
        }
    }

    private boolean pause() {
        if (mGame.is(State.ACTIVE)) {
            if (mGame.canPause()) {
                Log.i(TAG, "Pausing game on round " + mGame.currentRound());
                Log.i(TAG, "Remaining pauses: " + mGame.remainingPauses());
                mGame.logTimeLeft(TAG);
                mTimer.cancel();
                mGame.incrementPauses();
                mGame.setState(State.PAUSED);
                broadcast(Action.UPDATE);
                Foreground.update(this, mGame);
                return true;
            } else {
                Log.i(TAG, "No pauses remaining");
            }
        } else {
            Log.e(TAG, "Trying to pause a game that is not active");
        }
        return false;
    }

    private boolean resume() {
        if (mGame.is(State.PAUSED) || mGame.is(State.NEW_ROUND)) {
            String message = String.valueOf(mGame.currentRound());
            message = mGame.is(State.PAUSED) ? "Resuming game on round: " + message
                    : "Starting new round: " + message;
            Log.i(TAG, message);
            mGame.logTimeLeft(TAG);

            mVibrator.cancel();
            createTimer(mGame.getMillisRemainingGame());
            mGame.setState(State.ACTIVE);
            broadcast(Action.UPDATE);
            Foreground.update(this, mGame);
            return true;
        } else {
            Log.e(TAG, "Trying to resume an active game");
        }
        return false;
    }

    private void stop() {
        if (mGame.hasStarted()) {
            started = false;
            if (mGame.is(State.ACTIVE)) {
                mTimer.cancel();
                mVibrator.cancel();
            }
            Log.i(TAG, "Stopping game on round " + mGame.currentRound());
        }
        finish();
    }

    private void createTimer(long milliseconds) {
        if (!mGame.is(State.ACTIVE)) {
            mTimer = new CountDownTimer(milliseconds, TICK_LENGTH) {
                @Override
                public void onTick(long millisUntilFinished) {
                    mRoundCounter += TICK_LENGTH;
                    mGame.updateGameMilliseconds(millisUntilFinished, mRoundCounter);
                    if (mRoundCounter != Consts.Game.ROUND_DURATION_MILLIS) {
                        mGame.setState(State.ACTIVE);
                    } else {
                        onNewRound();
                    }
                    broadcast(Action.UPDATE);
                }

                @Override
                public void onFinish() {
                    mGame.setMillisRemainingGame(0);
                    mGame.setMillisRemainingRound(0);
                    mGame.setRound(mGame.getTotalRounds());
                    mGame.setState(State.FINISHED);
                    mCelebrator.celebrate();
                    broadcast(Action.FINISH);
                    Log.d(TAG, "Game has completed");
                    finish();
                }
            };
            mTimer.start();
        }
    }

    private void onNewRound() {
        // Pause the game, set state as waiting
        mTimer.cancel();
        mGame.setState(State.NEW_ROUND);
        mGame.incrementRound();
        Foreground.update(this, mGame);
        mRoundCounter = 0;
        Log.i(TAG, "Pausing game for new round: " + mGame.currentRound() + " of " + mGame.getTotalRounds());

        // Make sure app is open
        Intent i = new Intent(this.getApplicationContext(), MainActivity.class);
        i.setAction(Intent.ACTION_MAIN);
        i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);

        getWakelock(true);
        mWakeLock.acquire(mGame.getMillisRemainingGame());
        if (!mGame.options().isMuted()) {
            mVibrator.vibrate(VIBRATE_PATTERN, 1);
        }
    }

    private void finish() {
        mGame = null;
        initialized = false;
        Log.i(TAG, "Cleaning up the engine");
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
            Log.d(TAG, "Releasing wakelock");
        }
        Log.d(TAG, "Goodnight friend, it was a pleasure");
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        mBus.unregister(this);
        super.onDestroy();
    }

    public static boolean started() {
        return started;
    }

}
