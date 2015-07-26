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
package ca.hoogit.powerhour.Notifications;

/**
 * @author jordon
 *
 * Date    23/07/15
 * Description
 * Holds all constants used for the notifcation
 *
 */
public class Constants {
    public interface ACTION {
        String MAIN = "ca.hoogit.powerhour.game.engine.main";
        String INITIALIZE_GAME = "ca.hoogit.powerhour.game.engine.init";
        String PAUSE_GAME = "ca.hoogit.powerhour.game.engine.pause";
        String RESUME_GAME = "ca.hoogit.powerhour.game.engine.resume";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_ID = 5673;
    }
}
