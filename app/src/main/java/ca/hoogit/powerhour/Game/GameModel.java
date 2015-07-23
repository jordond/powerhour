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

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import ca.hoogit.powerhour.Configure.GameOptions;

/**
 * @author jordon
 *         <p/>
 *         Date    18/07/15
 *         Description
 */
public class GameModel implements Serializable {

    public static final int ROUND_DURATION_SECONDS = 5; // TODO change back to 60 (1 min)
    public static final long ROUND_DURATION_MILLIS = ROUND_DURATION_SECONDS * 1000;

    private boolean started = false;
    private State state;
    private int round = 0;
    private int totalRounds;
    private int pauses = 0;
    private int maxPauses;
    private long millisRemainingGame;
    private long millisRemainingRound = ROUND_DURATION_MILLIS;
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
        this.millisRemainingGame = this.totalRounds * ROUND_DURATION_MILLIS;
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
        return this.totalRounds * ROUND_DURATION_MILLIS;
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
