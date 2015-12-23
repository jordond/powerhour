/**
 * Copyright (C) 2015, Jordon de Hoog
 * <p/>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package ca.powerhour.common.DataLayer;

/**
 * @author jordon
 *         <p/>
 *         Date    22/12/15
 *         Description
 */
public class GameUpdate {

    private int rounds;
    private int pauses;
    private long millisRemainingRound;

    public GameUpdate(int rounds, int pauses, long millisRemainingRound) {
        this.rounds = rounds;
        this.pauses = pauses;
        this.millisRemainingRound = millisRemainingRound;
    }

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public int getPauses() {
        return pauses;
    }

    public void setPauses(int pauses) {
        this.pauses = pauses;
    }

    public long getMillisRemainingRound() {
        return millisRemainingRound;
    }

    public void setMillisRemainingRound(long millisRemainingRound) {
        this.millisRemainingRound = millisRemainingRound;
    }
}
