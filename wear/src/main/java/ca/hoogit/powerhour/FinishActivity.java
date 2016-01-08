package ca.hoogit.powerhour;

import android.animation.ObjectAnimator;
import android.content.Intent;
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

import ca.hoogit.powerhour.DataLayer.GameInformation;
import ca.hoogit.powerhour.DataLayer.Message;
import ca.hoogit.powerhour.Utils.Colors;
import ca.powerhour.common.DataLayer.Consts;

public class FinishActivity extends WearableActivity implements GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener {

    private static final String TAG = FinishActivity.class.getSimpleName();

    private RelativeLayout mContainerView;
    private TextView mCompletedRoundsTextView;
    private HoloCircularProgressBar mRoundProgress;
    private TextView mTotalRoundsTextView;

    private Colors mColors = Colors.getInstance();
    private float mProgress;
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
        setAmbientEnabled();

        Intent intent = getIntent();
        GameInformation gameInfo = (GameInformation) intent.getSerializableExtra(Consts.Keys.GAME_DATA);
        mColors.update(gameInfo);

        mContainerView = (RelativeLayout) findViewById(R.id.container);
        mCompletedRoundsTextView = (TextView) findViewById(R.id.rounds_complete);
        mRoundProgress = (HoloCircularProgressBar) findViewById(R.id.round_progress);
        mTotalRoundsTextView = (TextView) findViewById(R.id.total_rounds);

        mContainerView.setBackgroundColor(mColors.getPrimary());
        mCompletedRoundsTextView.setText(String.valueOf(gameInfo.getCurrentRound()));
        mTotalRoundsTextView.setText("of " + gameInfo.getRounds() + " rounds");

        mProgress = (float) gameInfo.getCurrentRound() / gameInfo.getRounds();
        mRoundProgress.setProgress(0);
        mRoundProgress.setProgressColor(mColors.getAccent());
        mRoundProgress.setThumbColor(mColors.getAccent());
        Handler handler = new Handler();
        handler.postDelayed(mAnimateProgress, 2000);

        updateDisplay(false); // Force ambient off

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
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            mRoundProgress.setVisibility(View.INVISIBLE);
        } else {
            mContainerView.setBackgroundColor(mColors.getPrimary());
            mRoundProgress.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent.getPath());
        switch (messageEvent.getPath()) {
            case Consts.Paths.START_ACTIVITY:
                Log.d(TAG, "onMessageReceived: New activity starting, finishing current");
                finish();
                break;
        }
    }
}
