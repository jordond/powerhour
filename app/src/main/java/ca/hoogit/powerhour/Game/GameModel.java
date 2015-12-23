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

import android.util.Log;

import com.google.android.gms.wearable.PutDataMapRequest;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import ca.hoogit.powerhour.Configure.GameOptions;
import ca.powerhour.common.DataLayer.Consts;

/**
 * @author jordon
 *         <p/>
 *         Date    18/07/15
 *         Description
 */
public class GameModel implements Serializable {

    private boolean started = false;
    private State state;
    private int round = 0;
    private int totalRounds;
    private int pauses = 0;
    private int maxPauses;
    private long millisRemainingGame;
    private long millisRemainingRound = Consts.Game.ROUND_DURATION_MILLIS;
    private boolean muted;
    private boolean autoStart;

    private GameOptions options;

    /**
     * Constructors
     */

    public GameModel() {
    }

    public GameModel(GameOptions options) {
        this.totalRounds = options.getRounds();
        this.maxPauses = options.getMaxPauses();
        this.autoStart = options.isAutoStart();
        this.options = options;
        this.millisRemainingGame = this.totalRounds * Consts.Game.ROUND_DURATION_MILLIS;
    }

    public PutDataMapRequest toDataMap() {
        PutDataMapRequest data = PutDataMapRequest.create(Consts.Paths.GAME_INFORMATION);
        data.getDataMap().putInt(Consts.Keys.COLOR_PRIMARY, this.options.getBackgroundColor());
        data.getDataMap().putInt(Consts.Keys.COLOR_ACCENT, this.options.getAccentColor());
        data.getDataMap().putInt(Consts.Keys.MAX_ROUNDS, this.totalRounds);
        data.getDataMap().putInt(Consts.Keys.MAX_PAUSES, this.maxPauses);
        data.getDataMap().putInt(Consts.Keys.CURRENT_ROUND, this.round);
        data.getDataMap().putInt(Consts.Keys.CURRENT_PAUSES, this.pauses);
        data.getDataMap().putLong(Consts.Keys.REMAINING_MILLIS, this.millisRemainingRound);
        data.getDataMap().putBoolean(Consts.Keys.MUTED, this.muted);
        data.getDataMap().putBoolean(Consts.Keys.STARTED, this.started);
        return data;
    }

    public void incrementRound() {
        this.round++;
    }

    public void incrementPauses() {
        this.pauses++;
    }

    public boolean canPause() {
        return this.pauses < maxPauses || maxPauses == -1;
    }

    public int remainingPauses() {
        return this.maxPauses - this.pauses;
    }

    public boolean is(State state) {
        return this.state == state;
    }

    public long gameMillis() {
        return this.totalRounds * Consts.Game.ROUND_DURATION_MILLIS;
    }

    public long gameMillisToMinutes() {
        return TimeUnit.MILLISECONDS.toMinutes(this.millisRemainingGame);
    }

    public String minutesRemaining() {
        long remaining = TimeUnit.MILLISECONDS.toMinutes(this.millisRemainingGame);
        if (remaining == 0) {
            return "Less than a minute remaining...";
        }
        return remaining + " minutes remaining.";
    }

    /**
     * Engine Helpers
     */

    public void updateGameMilliseconds(long millis, long roundCount) {
        long roundMillis = Consts.Game.ROUND_DURATION_MILLIS - roundCount;
        this.millisRemainingGame = millis;
        this.millisRemainingRound = roundCount == this.totalRounds
                ? Consts.Game.ROUND_DURATION_MILLIS : roundMillis;
    }

    public long totalRoundsLeftToMilliseconds() {
        return roundsToMilliseconds(this.totalRounds);
    }

    public long roundsToMilliseconds(int rounds) {
        int seconds = rounds * Consts.Game.ROUND_DURATION_SECONDS;
        return TimeUnit.SECONDS.toMillis(seconds);
    }

    public void logTimeLeft(String TAG) {
        Log.d(TAG, "GameModel Minutes Left : " + TimeUnit.MILLISECONDS.toMinutes(
                this.millisRemainingGame));
        Log.d(TAG, "Round Seconds Left: " + TimeUnit.MILLISECONDS.toSeconds(
                this.millisRemainingRound));
    }

    /**
     * Accessors
     */

    public boolean hasStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int currentRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public int getPauses() {
        return pauses;
    }

    public void setPauses(int pauses) {
        this.pauses = pauses;
    }

    public long getMillisRemainingGame() {
        return millisRemainingGame;
    }

    public void setMillisRemainingGame(long millisRemainingGame) {
        this.millisRemainingGame = millisRemainingGame;
    }

    public long getMillisRemainingRound() {
        return millisRemainingRound;
    }

    public void setMillisRemainingRound(long millisRemainingRound) {
        this.millisRemainingRound = millisRemainingRound;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(int totalRounds) {
        this.totalRounds = totalRounds;
    }

    public int getMaxPauses() {
        return maxPauses;
    }

    public void setMaxPauses(int maxPauses) {
        this.maxPauses = maxPauses;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public boolean isMuted() {
        return muted;
    }

    public GameOptions options() {
        return options;
    }

    public void setOptions(GameOptions options) {
        this.options = options;
    }

    public boolean isAutoStart() {
        return autoStart;
    }
}
