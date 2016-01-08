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
package ca.hoogit.powerhour.DataLayer;

import com.google.android.gms.wearable.DataMap;

import java.io.Serializable;

import ca.hoogit.powerhourshared.DataLayer.Consts;

/**
 * @author jordon
 *
 * Date    22/12/15
 * Description
 *
 */
public class GameInformation implements Serializable {

    private int colorPrimary;
    private int colorAccent;
    private int rounds;
    private int pauses;
    private int currentRound;
    private int currentPauses;
    private long remainingMillis;
    private boolean muted;
    private boolean started;

    public static GameInformation fromDataMap(DataMap data) {
        GameInformation info = new GameInformation();
        info.setColorPrimary(data.getInt(Consts.Keys.COLOR_PRIMARY));
        info.setColorAccent(data.getInt(Consts.Keys.COLOR_ACCENT));
        info.setRounds(data.getInt(Consts.Keys.MAX_ROUNDS));
        info.setPauses(data.getInt(Consts.Keys.MAX_PAUSES));
        info.setCurrentRound(data.getInt(Consts.Keys.CURRENT_ROUND));
        info.setCurrentPauses(data.getInt(Consts.Keys.CURRENT_PAUSES));
        info.setRemainingMillis(data.getLong(Consts.Keys.REMAINING_MILLIS));
        info.setMuted(data.getBoolean(Consts.Keys.MUTED));
        info.setStarted(data.getBoolean(Consts.Keys.STARTED));
        return info;
    }

    public GameInformation() {
    }

    public GameInformation(int colorPrimary, int colorAccent, int rounds, int pauses,
                           boolean muted, boolean started) {
        this.colorPrimary = colorPrimary;
        this.colorAccent = colorAccent;
        this.rounds = rounds;
        this.pauses = pauses;
        this.muted = muted;
        this.started = started;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public int getCurrentPauses() {
        return currentPauses;
    }

    public void setCurrentPauses(int currentPauses) {
        this.currentPauses = currentPauses;
    }

    public long getRemainingMillis() {
        return remainingMillis;
    }

    public void setRemainingMillis(long remainingMillis) {
        this.remainingMillis = remainingMillis;
    }

    public int getColorPrimary() {
        return colorPrimary;
    }

    public void setColorPrimary(int colorPrimary) {
        this.colorPrimary = colorPrimary;
    }

    public int getColorAccent() {
        return colorAccent;
    }

    public void setColorAccent(int colorAccent) {
        this.colorAccent = colorAccent;
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

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }
}
