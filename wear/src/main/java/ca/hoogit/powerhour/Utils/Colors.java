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
package ca.hoogit.powerhour.Utils;

/**
 * @author jordon
 *
 * Date    23/12/15
 * Description
 *
 */
public class Colors {
    private static Colors ourInstance = new Colors();

    public static Colors getInstance() {
        return ourInstance;
    }

    private int mPrimary;
    private int mAccent;

    private Colors() {
    }

    public int getPrimary() {
        return mPrimary;
    }

    public void setPrimary(int primary) {
        this.mPrimary = primary;
    }

    public int getAccent() {
        return mAccent;
    }

    public void setAccent(int accent) {
        this.mAccent = accent;
    }
}
