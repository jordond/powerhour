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

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

import ca.powerhour.common.DataLayer.Consts;

/**
 * @author jordon
 *
 * Date    22/12/15
 * Description
 *
 */
public class Message implements Runnable {
    private static final String TAG = Message.class.getSimpleName();
    private byte[] mObjectArray;
    private Context mContext;
    private String mPath;

    public static void sendReady(Context context) {
        new Message(Consts.Paths.WEAR_READY, "", context);
    }

    public static void sendNotReady(Context context) {
        new Message(Consts.Paths.WEAR_NOT_READY, "", context);
    }

    public Message(String path, String textToSend, Context context) {
        mContext = context;
        mPath = path;
        if(textToSend != null){
            mObjectArray = textToSend.getBytes();
        } else {
            mObjectArray = "".getBytes();
        }
        new Thread(this).start();
    }

    public void run() {
        if ((mObjectArray.length / 1024) > 100) {
            throw new RuntimeException("Object is too big to push it via Google Play Services");
        }
        GoogleApiClient googleApiClient = GoogleApiManager.getInstance(mContext);
        if (googleApiClient != null) {
            googleApiClient.blockingConnect(200, TimeUnit.MILLISECONDS);
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
            for (Node node : nodes.getNodes()) {
                MessageApi.SendMessageResult result;
                result = Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), mPath, mObjectArray).await();
                if (!result.getStatus().isSuccess()) {
                    Log.v(TAG, "ERROR: failed to send Message via Google Play Services");
                } else {
                    Log.d(TAG, "run: Sent message to " + mPath);
                }
            }
        } else {
            Log.e(TAG, "run: GoogleApiClient was null");
        }
    }
}
