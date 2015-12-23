package ca.hoogit.powerhour.DataLayer;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.concurrent.TimeUnit;

import ca.hoogit.powerhour.GameActivity;
import ca.powerhour.common.DataLayer.Consts;

public class GameListenerService extends WearableListenerService {

    private static final String TAG = GameListenerService.class.getSimpleName();

    GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

//    @Override
//    public void onDataChanged(DataEventBuffer dataEvents) {
//        Log.d(TAG, "onDataChanged: " + dataEvents);
//        if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
//            ConnectionResult result = mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);
//            if (!result.isSuccess()) {
//                Log.e(TAG, "onDataChanged: Failed to connect to GoogleApiClient, error: "
//                        + result.getErrorCode());
//            }
//        }
//    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        Log.d(TAG, "onMessageReceived: Path " + path);
        Log.d(TAG, "onMessageReceived: " + messageEvent);

        switch (path) {
            case Consts.Paths.START_ACTIVITY:
                Intent start = new Intent(this, GameActivity.class);
                start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(start);
                break;
        }
        super.onMessageReceived(messageEvent);
    }
}
