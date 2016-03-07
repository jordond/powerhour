package ca.hoogit.powerhour.wear.Game;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import ca.hoogit.powerhour.wear.DataLayer.GameInformation;
import ca.hoogit.powerhour.wear.DataLayer.Message;
import ca.hoogit.powerhour.R;
import ca.hoogit.powerhour.wear.DataLayer.NotificationManager;
import ca.hoogit.powerhourshared.DataLayer.Consts;

public class GameActivity extends WearableActivity implements
        GoogleApiClient.ConnectionCallbacks,
        DataApi.DataListener, MessageApi.MessageListener {

    private static final String TAG = GameActivity.class.getSimpleName();

    /**
     * Containing layout
     */
    private RelativeLayout mContainer;

    private GameScreen mGameScreen;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        setAmbientEnabled();

        final WatchViewStub viewStub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        viewStub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mContainer = (RelativeLayout) stub.findViewById(R.id.container_game_screen);
                mGameScreen = (GameScreen) stub.findViewById(R.id.game_screen_view);

                updateDisplay();
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult result) {
                        Log.e(TAG, "onConnectionFailed " + result.toString());
                    }
                })
                .build();
    }

    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected(): Successfully connected to Google API client");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Message.sendReady(getApplicationContext());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.e(TAG, "onConnectionSuspended: Google API client was suspended " + cause);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (GameState.getInstance().isActive()) {
            NotificationManager.build(this).show();
        }

        Log.d(TAG, "onPause: disconnecting and removing listeners");
        // TODO need to investigate how to handle the listener removal properly, maybe onStop()?
        //Wearable.DataApi.removeListener(mGoogleApiClient, this);
        //Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    @SuppressWarnings("ResourceAsColor")
    private void updateDisplay() {
        Log.d(TAG, "updateDisplay: Ambient mode " + isAmbient());
        int color = isAmbient() ? ContextCompat.getColor(getApplicationContext(),
                android.R.color.black) : GameState.getInstance().getPrimary();
        mContainer.setBackgroundColor(color);
        mGameScreen.updateScreen(isAmbient());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Message.sendNotReady(this);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged: " + dataEvents);

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                Log.d(TAG, "onDataChanged: Path" + path);
                switch (path) {
                    case Consts.Paths.GAME_INFORMATION:
                        DataMapItem item = DataMapItem.fromDataItem(event.getDataItem());
                        GameInformation info = GameInformation.fromDataMap(item.getDataMap());
                        GameState.getInstance().update(info);
                        updateDisplay();
                        break;
                }
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent event) {
        Log.d(TAG, "onMessageReceived: " + event);
        switch (event.getPath()) {
            case Consts.Paths.GAME_SHOT:
                Log.d(TAG, "onMessageReceived: SHOT TIME");
                GameState.getInstance().setIsShotTime(true);
                mGameScreen.updateScreen(isAmbient());
                break;
            case Consts.Paths.GAME_STOP:
                Log.d(TAG, "onMessageReceived: Game has stopped");
                GameState.getInstance().stop();
                updateDisplay();
                Message.sendReady(this);
                break;
            case Consts.Paths.GAME_FINISH:
                Log.i(TAG, "onMessageReceived: Game has finished");
                Wearable.DataApi.removeListener(mGoogleApiClient, this);
                Wearable.MessageApi.removeListener(mGoogleApiClient, this);
                finish();
                break;
        }
    }
}
