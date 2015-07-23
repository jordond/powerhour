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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import ca.hoogit.powerhour.Game.Engine;
import ca.hoogit.powerhour.Game.GameModel;
import ca.hoogit.powerhour.Game.State;
import ca.hoogit.powerhour.R;
import ca.hoogit.powerhour.Selection.MainActivity;

/**
 * @author jordon
 *
 * Date    23/07/15
 * Description 
 *
 */
public class Foreground {

    private Context mContext;
    private GameModel mGameModel;

    public Foreground(Context context, GameModel gameModel) {
        this.mContext = context;
        this.mGameModel = gameModel;
    }

    public Notification build() {
        if (mContext != null && mGameModel != null) {
            return build(mContext, mGameModel);
        }
        return null;
    }

    public static Notification build(Context context, GameModel game) {
        // Intent for the notification
        Intent intentMain = new Intent(context, MainActivity.class);
        intentMain.setAction(Constants.ACTION.MAIN);
        intentMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingMain = PendingIntent.getActivity(context, 0, intentMain, 0);

        // Pending intent action to pause the game
        Intent intentPause = new Intent(context, Engine.class);
        intentPause.setAction(Constants.ACTION.PAUSE_GAME);
        PendingIntent pendingPause = PendingIntent.getService(context, 0, intentPause, 0);

        // Pending intent action to pause the game
        Intent intentResume = new Intent(context, Engine.class);
        intentResume.setAction(Constants.ACTION.RESUME_GAME);
        PendingIntent pendingResume = PendingIntent.getService(context, 0, intentResume, 0);

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        String contentText = game.minutesRemaining();
        NotificationCompat.Action action = new NotificationCompat.Action(
                R.drawable.ic_stat_av_pause, "Pause", pendingPause);
        if (game.is(State.ACTIVE)) {
            contentText = "Active with " + contentText;
        } else if (game.is(State.PAUSED)) {
            contentText = "Paused with " + contentText;
            action = new NotificationCompat.Action(R.drawable.ic_stat_av_play_arrow,
                    "Resume", pendingResume);
        }

        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(game.options().getTitle() + " - Round " + game.currentRound())
                .setTicker(game.options().getTitle() + " - Round " + game.currentRound())
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_stat_beer)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingMain)
                .setOngoing(true)
                .addAction(action).build();

        if (game.is(State.ACTIVE) && !game.canPause()) {
            notification.actions[0] = new Notification.Action(
                    R.drawable.ic_stat_action_flip_to_front, "Open", pendingMain);
        }
        return notification;
    }

    public void update() {
        if (mContext != null && mGameModel != null) {
            update(mContext, mGameModel);
        }
    }

    public static void update(Context context, GameModel gameModel) {
        Notification notif = build(context, gameModel);

        NotificationManager manager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);

        manager.notify(Constants.NOTIFICATION_ID.FOREGROUND_ID, notif);
    }
}
