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
package ca.hoogit.powerhour.Game;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.HashSet;

import ca.powerhour.common.DataLayer.Consts;

/**
 * @author jordon
 *         <p/>
 *         Date    22/12/15
 *         Description
 */
public class WearData implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, MessageApi.MessageListener {

    private static final String TAG = WearData.class.getSimpleName();

    private static final int REQUEST_RESOLVE_ERROR = 1000;

    private Activity mActivity;
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;

    private boolean mWearIsReady = false;

    public void setCurrentGame(GameModel currentGame) {
        if (currentGame != null) {
            this.mCurrentGame = currentGame;
        }
    }

    private GameModel mCurrentGame;

    public WearData(Activity activity) {
        this.mActivity = activity;
        this.mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void connect() {
        if (this.mGoogleApiClient != null && !mResolvingError) {
            this.mGoogleApiClient.connect();
        }
    }

    public void disconnect() {
        if (this.mGoogleApiClient != null) {
            this.mGoogleApiClient.disconnect();
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        }
    }

    public void sendStartActivity() {
        this.sendMessage(Consts.Paths.START_ACTIVITY, "");
    }

    public void sendMessage(String messagePath, String messageText) {
        Message message = new Message(messagePath, messageText);
        new Thread(message).start();
    }

    public void sendGameInformation(GameModel game) {
        mCurrentGame = game;
        if (!mWearIsReady) {
            Log.i(TAG, "sendGameInformation: Wear is not yet ready for info");
            return;
        }
        PutDataMapRequest dataMap = game.toDataMap();
        PutDataRequest request = dataMap.asPutDataRequest();
        sendMessage(Consts.Paths.GAME_INFORMATION, "");
        if (mGoogleApiClient.isConnected()) {
            Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                            if (!dataItemResult.getStatus().isSuccess()) {
                                Log.e(TAG, "ERROR: failed to putDataItem, status code: "
                                        + dataItemResult.getStatus().getStatusCode());
                            } else {
                                Log.d(TAG, "onResult: Successfully sent game info");
                            }
                        }
                    });
        }
    }

    public void sendFinish(GameModel game) {
        if (game == null) {
            Log.e(TAG, "sendFinish: Game model is null, aborting sending message");
            return;
        }
        PutDataMapRequest dataMap = game.toDataMap(Consts.Paths.GAME_FINISH);
        PutDataRequest req = dataMap.asPutDataRequest();
        sendMessage(Consts.Paths.GAME_FINISH, "");
        if (mGoogleApiClient.isConnected()) {
            Wearable.DataApi.putDataItem(mGoogleApiClient, req)
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                            if (dataItemResult.getStatus().isSuccess()) {
                                Log.d(TAG, "onResult: Successfully sent finish info");
                            }
                        }
                    });
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected(): Successfully connected to Google API client");
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.e(TAG, "onConnectionSuspended: Google API client was suspended " + cause);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (mResolvingError) {
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(mActivity, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            Log.e(TAG, "Connection to Google API client has failed");
            mResolvingError = false;
        }
    }

    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }

        return results;
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: Path " + messageEvent.getPath());
        switch (messageEvent.getPath()) {
            case Consts.Paths.WEAR_READY:
                mWearIsReady = true;
                if (mCurrentGame != null) {
                    sendGameInformation(mCurrentGame);
                }
                break;
            case Consts.Paths.WEAR_NOT_READY:
                mWearIsReady = false;
                break;
        }
    }

    public class Message implements Runnable {

        private final String TAG = Message.class.getSimpleName();
        private byte[] mObjectArray;
        private String mPath;

        public Message(String path, String textToSend) {
            mPath = path;
            if (textToSend != null) {
                mObjectArray = textToSend.getBytes();
            } else {
                mObjectArray = "".getBytes();
            }
        }

        @Override
        public void run() {
            if ((mObjectArray.length / 1024) > 100) {
                throw new RuntimeException("Object is too big to push it via Google Play Services");
            }
            if (mGoogleApiClient != null) {
                NodeApi.GetConnectedNodesResult nodes =
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result;
                    result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), mPath, mObjectArray).await();
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
}
