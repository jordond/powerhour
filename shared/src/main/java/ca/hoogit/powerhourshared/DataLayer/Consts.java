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
package ca.hoogit.powerhourshared.DataLayer;

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
        public static final String WEAR_NOT_READY = "/not_ready";
        public static final String GAME_START = "/start";
        public static final String GAME_PAUSE = "/pause";
        public static final String GAME_STOP = "/stop";
        public static final String GAME_SHOT = "/shot";
        public static final String GAME_FINISH = "/finish";
        public static final String GAME_CLOSE = "/close";
    }

    public static class Keys {
        public static final String GAME_DATA = "data";
        public static final String COLOR_PRIMARY = "primary";
        public static final String COLOR_ACCENT = "accent";
        public static final String MAX_ROUNDS = "max_rounds";
        public static final String MAX_PAUSES = "max_pauses";
        public static final String CURRENT_ROUND = "current_round";
        public static final String CURRENT_PAUSES = "current_pauses";
        public static final String REMAINING_MILLIS = "millis";
        public static final String MUTED = "muted";
        public static final String STARTED = "started";
    }

    public static class Game {
        public static final int ROUND_DURATION_SECONDS = 60; // TODO change back to 60 (1 min)
        public static final long ROUND_DURATION_MILLIS = ROUND_DURATION_SECONDS * 1000;
        public static final long PROGRESS_WHEEL_ANIMATION_DURATION = 300;
        public static final long WEAR_UPDATE_INTERVAL_IN_MILLISECONDS = 1000; // One second
        public static final String FLAG_GAME_STOP = "stop";
        public static final long DEFAULT_SHOT_TIME_DELAY = 4000;
        public static final String FLAG_GAME_IS_SHOT_TIME = "shot_time_drink_up";
    }

    public static class Wear {
        public static final String NOTIFICATION_INTENT_ACTION = "ca.hoogit.powerhour.wear.gameactivity";
        public static final int NOTIFICATION_ID = 548853;
    }

}
