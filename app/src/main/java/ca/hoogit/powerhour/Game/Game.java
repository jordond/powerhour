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

/**
 * @author jordon
 *         <p/>
 *         Date    18/07/15
 *         Description
 */
public class Game implements Serializable {

    private boolean started;
    private State state;
    private int round;
    private int totalRounds;
    private int pauses;
    private int maxPauses;
    private long millisRemainingGame;
    private long millisRemainingRound;
    private boolean muted;
    private boolean autostart;

    private GameOptions options;

    /**
     * Constructors
     */

    public Game() {
    }

    public Game(GameOptions options) {
        this.totalRounds = options.getRounds();
        this.maxPauses = options.getMaxPauses();
        this.options = options;
    }

    public Game(GameOptions options, boolean autostart) {
        this.totalRounds = options.getRounds();
        this.maxPauses = options.getMaxPauses();
        this.autostart = autostart;
        this.options = options;
    }

    public void incrementRound() {
        this.round++;
    }

    public void incrementPauses() {
        this.pauses++;
    }

    public boolean canPause() {
        return this.pauses <= maxPauses || maxPauses == -1;
    }

    public boolean is(State state) {
        return this.state == state;
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

    public int getRound() {
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

    public GameOptions getOptions() {
        return options;
    }

    public void setOptions(GameOptions options) {
        this.options = options;
    }

    public boolean isAutostart() {
        return autostart;
    }
}
