package ca.hoogit.powerhour.wear.DataLayer;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.concurrent.TimeUnit;

import ca.hoogit.powerhour.wear.Game.FinishActivity;
import ca.hoogit.powerhour.wear.Game.GameActivity;
import ca.hoogit.powerhour.wear.Game.GameState;
import ca.hoogit.powerhourshared.DataLayer.Consts;

public class GameListenerService extends WearableListenerService {

    private static final String TAG = GameListenerService.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;

    private static final long[] VIBRATE_PATTERN = {0, 500, 50, 300, 50, 300, 300, 500, 200};
    private Vibrator mVibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
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
                    DataMapItem item = DataMapItem.fromDataItem(event.getDataItem());
                    GameInformation info = GameInformation.fromDataMap(item.getDataMap());
                    GameState.getInstance().update(info);

                    Intent finish = new Intent(this, FinishActivity.class);
                    finish.putExtra(Consts.Keys.GAME_DATA, info);
                    finish.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    GameState.getInstance().stop();
                    NotificationManager.remove(this);
                    mVibrator.cancel();

                    Log.d(TAG, "onDataChanged: launching Finish activity");
                    startActivity(finish);
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
                startActivity(createActivityIntent(false));
                break;
            case Consts.Paths.GAME_SHOT:
                GameState.getInstance().setIsShotTime(true);
                mVibrator.vibrate(VIBRATE_PATTERN, -1);

                startActivity(createActivityIntent(true));
                break;
            case Consts.Paths.GAME_INFORMATION:
                GameState.getInstance().setIsShotTime(false);
                mVibrator.cancel();
                break;
        }
        super.onMessageReceived(event);
    }

    private Intent createActivityIntent(boolean isShotTime) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Consts.Game.FLAG_GAME_IS_SHOT_TIME, isShotTime);
        return intent;
    }
}
