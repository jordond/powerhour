package ca.hoogit.powerhour.DataLayer;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import ca.hoogit.powerhour.FinishActivity;
import ca.hoogit.powerhour.GameActivity;
import ca.powerhour.common.DataLayer.Consts;

public class GameListenerService extends WearableListenerService {

    private static final String TAG = GameListenerService.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private boolean mShouldLaunchFinish;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged: " + dataEvents);
        if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
            ConnectionResult result = mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);
            if (!result.isSuccess()) {
                Log.e(TAG, "onDataChanged: Failed to connect to GoogleApiClient, error: "
                        + result.getErrorCode());
                return;
            }
        }

        for (DataEvent event : dataEvents) {
            switch (event.getDataItem().getUri().getPath()) {
                case Consts.Paths.GAME_FINISH:
                    if (mShouldLaunchFinish) {
                        Intent finish = new Intent(this, FinishActivity.class);
                        DataMapItem item = DataMapItem.fromDataItem(event.getDataItem());
                        GameInformation info = GameInformation.fromDataMap(item.getDataMap());
                        finish.putExtra(Consts.Keys.GAME_DATA, info);
                        finish.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(finish);
                        mShouldLaunchFinish = false;
                        Log.d(TAG, "onDataChanged: launching Finish activity");
                    }
                    break;
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent event) {
        String path = event.getPath();
        Log.d(TAG, "onMessageReceived: Path " + path);

        switch (path) {
            case Consts.Paths.START_ACTIVITY:
                Intent start = new Intent(this, GameActivity.class);
                start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(start);
                mShouldLaunchFinish = true;
                break;
            case Consts.Paths.GAME_FINISH:
                mShouldLaunchFinish = true;
                break;
        }
        super.onMessageReceived(event);
    }
}
