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

/**
 * Created by jordon on 16/07/15.
 * Handle game events with otto
 */
public class GameEvent {

    public Action action;
    public GameModel game;

    public GameEvent() {
    }

    public GameEvent(GameModel game) {
        this.action = Action.UPDATE;
        this.game = game;
    }

    public GameEvent(Action action) {
        this.action = action;
    }

    public GameEvent(Action action, GameModel game) {
        this.action = action;
        this.game = game;
    }
}
