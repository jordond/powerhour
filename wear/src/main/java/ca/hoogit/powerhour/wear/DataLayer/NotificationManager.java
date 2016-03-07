package ca.hoogit.powerhour.wear.DataLayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import ca.hoogit.powerhour.R;
import ca.hoogit.powerhour.wear.Game.GameActivity;
import ca.hoogit.powerhourshared.DataLayer.Consts;

/**
 * Created by jordon on 06/03/16.
 * Manager object for handling the notification
 */
public class NotificationManager {

    private Context mContext;
    private Notification mNotification;

    public NotificationManager(Context context, Notification notification) {
        this.mContext = context;
        this.mNotification = notification;
    }

    /**
     * Statically build a NotificationManager object for easily chaining.
     * Will build a notification with the intent to launch the {@link GameActivity}
     *
     * @param context Reference to calling activity
     * @return NotificationManger
     */
    public static NotificationManager build(Context context) {
        Intent intent = new Intent(context, GameActivity.class);
        intent.setAction(Consts.Wear.NOTIFICATION_INTENT_ACTION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(context.getString(R.string.wear_notification_title))
                        .setContentText(context.getString(R.string.wear_notification_text))
                        .setContentIntent(pendingIntent);

        return new NotificationManager(context, notificationBuilder.build());
    }

    /**
     * If the notification has been built then display it
     */
    public void show() {
        if (mNotification != null) {
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(mContext);
            notificationManager.notify(Consts.Wear.NOTIFICATION_ID, mNotification);
        }
    }

    /**
     * If the notification exists remove it from the device
     */
    public void remove() {
        remove(mContext);
    }

    /**
     * Remove the notification from the device
     *
     * @param context Calling activity reference
     */
    public static void remove(Context context) {
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);
        notificationManager.cancel(Consts.Wear.NOTIFICATION_ID);
    }
}
