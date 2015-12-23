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
import com.google.android.gms.wearable.PutDataMapRequest;

import java.io.Serializable;

import ca.powerhour.common.DataLayer.Consts;

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
    private boolean muted;
    private boolean started;

    public static PutDataMapRequest toDataMap(int primary, int accent, int rounds, int pauses, boolean muted, boolean started) {
        PutDataMapRequest data = PutDataMapRequest.create(Consts.Paths.GAME_INFORMATION);

        data.getDataMap().putInt(Consts.Keys.COLOR_PRIMARY, primary);
        data.getDataMap().putInt(Consts.Keys.COLOR_ACCENT, accent);
        data.getDataMap().putInt(Consts.Keys.MAX_ROUNDS, rounds);
        data.getDataMap().putInt(Consts.Keys.MAX_PAUSES, pauses);
        data.getDataMap().putBoolean(Consts.Keys.MUTED, muted);
        data.getDataMap().putBoolean(Consts.Keys.STARTED, started);
        return data;
    }

    public static GameInformation fromDataMap(DataMap data) {
        GameInformation info = new GameInformation();
        info.setColorPrimary(data.getInt(Consts.Keys.COLOR_PRIMARY));
        info.setColorAccent(data.getInt(Consts.Keys.COLOR_ACCENT));
        info.setRounds(data.getInt(Consts.Keys.MAX_ROUNDS));
        info.setPauses(data.getInt(Consts.Keys.MAX_PAUSES));
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
