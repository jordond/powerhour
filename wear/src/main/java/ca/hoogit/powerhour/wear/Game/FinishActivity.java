package ca.hoogit.powerhour.wear.Game;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.pascalwelsch.holocircularprogressbar.HoloCircularProgressBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import ca.hoogit.powerhour.wear.DataLayer.GameInformation;
import ca.hoogit.powerhour.wear.DataLayer.Message;
import ca.hoogit.powerhour.R;
import ca.hoogit.powerhour.wear.DataLayer.NotificationManager;
import ca.hoogit.powerhourshared.DataLayer.Consts;

public class FinishActivity extends WearableActivity implements GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener {

    private static final String TAG = FinishActivity.class.getSimpleName();

    @Bind(R.id.container) RelativeLayout mContainerView;
    @Bind(R.id.rounds_complete) TextView mCompletedRoundsTextView;
    @Bind(R.id.round_progress) HoloCircularProgressBar mRoundProgress;
    @Bind(R.id.total_rounds) TextView mTotalRoundsTextView;

    private float mProgress;
    private int mPrimaryColor;
    private GoogleApiClient mGoogleApiClient;

    private Runnable mAnimateProgress = new Runnable() {
        @Override
        public void run() {
            ObjectAnimator animation = ObjectAnimator.ofFloat(mRoundProgress, "progress", mProgress);
            animation.setDuration(2000);
            animation.setInterpolator(new LinearInterpolator());
            animation.start();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
        ButterKnife.bind(this);
        setAmbientEnabled();

        Intent intent = getIntent();
        GameInformation gameInfo = (GameInformation) intent.getSerializableExtra(Consts.Keys.GAME_DATA);

        mPrimaryColor = gameInfo.getColorPrimary();

        mContainerView.setBackgroundColor(mPrimaryColor);
        mCompletedRoundsTextView.setText(String.valueOf(gameInfo.getCurrentRound()));
        mTotalRoundsTextView.setText("of " + gameInfo.getRounds() + " rounds");

        mProgress = (float) gameInfo.getCurrentRound() / gameInfo.getRounds();
        mRoundProgress.setProgress(0);
        mRoundProgress.setProgressColor(gameInfo.getColorAccent());
        mRoundProgress.setThumbColor(gameInfo.getColorAccent());
        Handler handler = new Handler();
        handler.postDelayed(mAnimateProgress, 2000);

        updateDisplay(false); // Force ambient off
        NotificationManager.remove(this);

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

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected: Connected to Google Api");
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Message.sendReady(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended: Error code" + i);
    }

    @Override
    protected void onStop() {
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        super.onStop();
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay(isAmbient());
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay(isAmbient());
    }

    @Override
    public void onExitAmbient() {
        updateDisplay(isAmbient());
        super.onExitAmbient();
    }

    private void updateDisplay(boolean isAmbient) {
        if (isAmbient) {
            mContainerView.setBackgroundColor(Color.BLACK);
            mRoundProgress.setVisibility(View.INVISIBLE);
        } else {
            mContainerView.setBackgroundColor(mPrimaryColor);
            mRoundProgress.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent.getPath());
        switch (messageEvent.getPath()) {
            case Consts.Paths.START_ACTIVITY:
            case Consts.Paths.GAME_CLOSE:
                Log.d(TAG, "onMessageReceived: finishing activity");
                finish();
                break;
        }
    }
}
