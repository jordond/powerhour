package ca.hoogit.powerhour_wear;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pascalwelsch.holocircularprogressbar.HoloCircularProgressBar;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends WearableActivity {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    /**
     * Containing layout
     */
    private LinearLayout mGameScreen;

    /**
     * Active layout components
     */
    private RelativeLayout mActiveLayout;
    private TextView mRemainingRounds;
    private TextView mRemainingSeconds;
    private HoloCircularProgressBar mProgress;

    /**
     * Ambient layout components
     */
    private LinearLayout mAmbientLayout;
    private TextView mRoundsText;
    private TextView mAmbientRemaningRounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        final WatchViewStub viewStub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        viewStub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mGameScreen = (LinearLayout) stub.findViewById(R.id.game_screen);

                mActiveLayout = (RelativeLayout) stub.findViewById(R.id.active);
                mRemainingRounds = (TextView) stub.findViewById(R.id.rounds_remaining);
                mRemainingSeconds = (TextView) stub.findViewById(R.id.progress_seconds_text);
                mProgress = (HoloCircularProgressBar) stub.findViewById(R.id.progress_seconds_circle);

                mAmbientLayout = (LinearLayout) stub.findViewById(R.id.ambient);
                mRoundsText = (TextView) stub.findViewById(R.id.rounds_text);
                mAmbientRemaningRounds = (TextView) stub.findViewById(R.id.ambient_rounds_remaining);
                updateDisplay();
            }
        });
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

    private void updateDisplay() {
        if (isAmbient()) {
            mGameScreen.setBackgroundColor(getResources().getColor(android.R.color.black));
            mActiveLayout.setVisibility(View.GONE);
            mAmbientLayout.setVisibility(View.VISIBLE);
            mRoundsText.getPaint().setAntiAlias(false);
            mAmbientRemaningRounds.getPaint().setAntiAlias(false);
        } else {
            mGameScreen.setBackgroundColor(getResources().getColor(R.color.primary));
            mActiveLayout.setVisibility(View.VISIBLE);
            mAmbientLayout.setVisibility(View.GONE);
        }
    }
}
