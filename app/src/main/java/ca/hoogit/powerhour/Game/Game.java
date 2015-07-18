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
 *
 * Date    18/07/15
 * Description 
 *
 */
public class Game implements Serializable {

    private GameOptions options;
    private boolean started;
    private State state;
    private int round;
    private int pauses;
    private long millisRemainingGame;
    private long millisRemainingRound;
    private boolean isMuted;

    /**
     * Constructors
     */

    public Game() {
    }

    public Game(GameOptions options) {
        this.options = options;
    }

    public Game(GameOptions options, boolean started) {
        this.options = options;
        this.started = started;
    }

    public void incrementRound() {
        this.round++;
    }

    public void incrementPauses() {
        this.pauses++;
    }

    public boolean canPause() {
        return this.pauses <= options.getMaxPauses();
    }

    public boolean is(State state) {
        return this.state == state;
    }

    /**
     * Accessors
     */

    public GameOptions getOptions() {
        return options;
    }

    public void setOptions(GameOptions options) {
        this.options = options;
    }

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

    public boolean isMuted() {
        return isMuted;
    }

    public void setIsMuted(boolean isMuted) {
        this.isMuted = isMuted;
    }
}
