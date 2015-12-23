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
 *
 * Date    22/12/15
 * Description
 *
 */
public class Consts {

    public static class Paths {
        public static final String START_ACTIVITY = "/start-activity";
        public static final String GAME_INFORMATION = "/game-info";
        public static final String UPDATE_PROGRESS = "/update";
        public static final String WEAR_READY = "/ready";
        public static final String GAME_START = "/start";
        public static final String GAME_PAUSE = "/pause";
        public static final String GAME_STOP = "/stop";
    }

    public static class Keys {
        public static final String GAME_DATA = "data";
        public static final String COLOR_PRIMARY = "primary";
        public static final String COLOR_ACCENT = "accent";
        public static final String MAX_ROUNDS = "max_rounds";
        public static final String MAX_PAUSES = "max_pauses";
        public static final String MUTED = "muted";
        public static final String STARTED = "started";
    }

}
