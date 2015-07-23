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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.util.concurrent.TimeUnit;

import ca.hoogit.powerhour.Util.BusProvider;
import ca.hoogit.powerhour.Selection.MainActivity;
import ca.hoogit.powerhour.R;

public class Engine extends Service {

    private static final String TAG = Engine.class.getSimpleName();

    public static final String ACTION_MAIN = "ca.hoogit.powerhour.game.engine.main";
    public static final String ACTION_INITIALIZE_GAME = "ca.hoogit.powerhour.game.engine.init";
    public static final String ACTION_PAUSE_GAME = "ca.hoogit.powerhour.game.engine.pause";
    public static final String ACTION_RESUME_GAME = "ca.hoogit.powerhour.game.engine.resume";
    public static final int FOREGROUND_NOTIFICATION_ID = 5673;

    public static State mState = State.NONE;
    public static boolean initialized = false;
    public static boolean started = false;

    private Bus mBus;
    private static GameModel mGame;
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
            handleIntent(intent);
        }
        return START_NOT_STICKY;
    }

    private void handleIntent(Intent intent) {
        switch (intent.getAction()) {
            case ACTION_INITIALIZE_GAME:
                mGame = (GameModel) intent.getSerializableExtra("game");
                if (mGame != null) {
                    mGame.setState(State.INITIALIZED);
                    mState = State.INITIALIZED;
                    initialized = true;
                    if (mGame.isAutoStart()) {
                        start();
                    }
                    Log.i(TAG, "GameModel has been initialized");
                } else {
                    stopSelf();
                    Log.e(TAG, "No game was passed to engine");
                }
                break;
            case ACTION_PAUSE_GAME:
                Log.d(TAG, "Pause intent has been received");
                pause();
                break;
            case ACTION_RESUME_GAME:
                Log.d(TAG, "Resume intent has been received");
                resume();
                break;
        }
    }

    private Notification buildNotification() {
        // Intent for the notification
        Intent intentMain = new Intent(this, MainActivity.class);
        intentMain.setAction(ACTION_MAIN);
        intentMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingMain = PendingIntent.getActivity(this, 0, intentMain, 0);

        // Pending intent action to pause the game
        Intent intentPause = new Intent(this, Engine.class);
        intentPause.setAction(ACTION_PAUSE_GAME);
        PendingIntent pendingPause = PendingIntent.getService(this, 0, intentPause, 0);

        // Pending intent action to pause the game
        Intent intentResume = new Intent(this, Engine.class);
        intentResume.setAction(ACTION_RESUME_GAME);
        PendingIntent pendingResume = PendingIntent.getService(this, 0, intentResume, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        String contentText = mGame.minutesRemaining();
        NotificationCompat.Action action = new NotificationCompat.Action(
                R.drawable.ic_stat_av_pause, "Pause", pendingPause);
        if (mGame.is(State.ACTIVE)) {
            contentText = "Active with " + contentText;
        } else if (mGame.is(State.PAUSED)) {
            contentText = "Paused with " + contentText;
            action = new NotificationCompat.Action(R.drawable.ic_stat_av_play_arrow,
                    "Resume", pendingResume);
        }

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(mGame.options().getTitle() + " - Round " + mGame.currentRound())
                .setTicker(mGame.options().getTitle() + " - Round " + mGame.currentRound())
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_stat_beer)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingMain)
                .setOngoing(true)
                .addAction(action).build();

        if (mGame.is(State.ACTIVE) && !mGame.canPause()) {
            notification.actions[0] = new Notification.Action(
                    R.drawable.ic_stat_action_flip_to_front, "Open", pendingMain);
        }
        return notification;
    }

    private void updateNotification() {
        Notification notification = buildNotification();

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification);
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
                    case INITIALIZED:
                        start();
                        break;
                    case ACTIVE:
                        pause();
                        break;
                    case PAUSED:
                        resume();
                        break;
                }
                break;
            case START:
                start();
                break;
            case PAUSE:
                pause();
                break;
            case RESUME:
                resume();
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
            mState = State.STARTED;
            started = true;
            broadcast(Action.UPDATE);
            startForeground(FOREGROUND_NOTIFICATION_ID, buildNotification());
        } else {
            Log.e(TAG, "GameModel already started, cannot start another.");
        }
    }

    private boolean pause() {
        if (mGame.is(State.ACTIVE)) {
            if (mGame.canPause()) {
                Log.i(TAG, "Pausing game on round " + mGame.currentRound());
                Log.i(TAG, "Remaining pauses: " + mGame.remainingPauses());
                logTimeLeft();
                mTimer.cancel();
                mGame.incrementPauses();
                mGame.setState(State.PAUSED);
                broadcast(Action.UPDATE);
                updateNotification();
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
        if (mGame.is(State.PAUSED)) {
            Log.i(TAG, "Resuming game on round " + mGame.currentRound());
            logTimeLeft();
            createTimer(mGame.getMillisRemainingGame());
            mGame.setState(State.ACTIVE);
            broadcast(Action.UPDATE);
            updateNotification();
            return true;
        } else {
            Log.e(TAG, "Trying to resume an active game");
        }
        return false;
    }

    private void stop() {
        if (mGame.hasStarted()) {
            Log.i(TAG, "Stopping game on round " + mGame.currentRound());
            if (mGame.is(State.ACTIVE)) {
                mTimer.cancel();
            }
            started = false;
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
                    if (mRoundCounter == GameModel.ROUND_DURATION_MILLIS) {
                        action = Action.NEW_ROUND;
                        mGame.incrementRound();
                        mRoundCounter = 0;
                        updateNotification();
                        Log.d(TAG, "Starting round: " + mGame.currentRound() + " of " + mGame.getTotalRounds());
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
                    Log.d(TAG, "GameModel has completed");
                    finish();
                }
            };
            mTimer.start();
        }
    }

    private void updateGameMilliseconds(long millis) {
        long roundMillis = GameModel.ROUND_DURATION_MILLIS - mRoundCounter;
        mGame.setMillisRemainingGame(millis);
        mGame.setMillisRemainingRound(roundMillis);
    }

    private void finish() {
        mState = State.NONE;
        mGame = null;
        initialized = false;
        Log.i(TAG, "Cleaning up the engine");
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

    public static GameModel details() {
        return mGame;
    }

    /**
     * Helpers
     */

    private long roundsToMilliseconds(int rounds) {
        int seconds = rounds * GameModel.ROUND_DURATION_SECONDS;
        return TimeUnit.SECONDS.toMillis(seconds);
    }

    private void logTimeLeft() {
        Log.d(TAG, "GameModel Minutes Left : " + TimeUnit.MILLISECONDS.toMinutes(
                mGame.getMillisRemainingGame()));
        Log.d(TAG, "Round Seconds Left: " + TimeUnit.MILLISECONDS.toSeconds(
                mGame.getMillisRemainingRound()));
    }
}
