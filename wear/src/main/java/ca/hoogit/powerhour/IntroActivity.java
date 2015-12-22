package ca.hoogit.powerhour;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class IntroActivity extends WearableActivity {

    private static final String TAG = IntroActivity.class.getSimpleName();
    private static final int FONT_SIZE_AMBIENT = 12;
    private static final int FONT_SIZE_ACTIVE = 18;
    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("h:mm a", Locale.US);

    private LinearLayout mContainerView;
    private TextView mIntroText;
    private TextView mMoreIntroText;
    private TextView mClockView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        setAmbientEnabled();

        mContainerView = (LinearLayout) findViewById(R.id.container);
        mIntroText = (TextView) findViewById(R.id.intro_text);
        mMoreIntroText = (TextView) findViewById(R.id.intro_text_more);
        mClockView = (TextView) findViewById(R.id.clock);
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
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            mIntroText.setTextSize(FONT_SIZE_AMBIENT);
            mMoreIntroText.setVisibility(View.GONE);
            mClockView.setVisibility(View.VISIBLE);
            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackgroundColor(ContextCompat.getColor(getApplication(), R.color.primary));
            mIntroText.setTextSize(FONT_SIZE_ACTIVE);
            mMoreIntroText.setVisibility(View.VISIBLE);
            mClockView.setVisibility(View.GONE);
        }
        mIntroText.getPaint().setAntiAlias(!isAmbient());
    }
}
