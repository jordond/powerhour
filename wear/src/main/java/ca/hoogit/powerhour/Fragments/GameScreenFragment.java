package ca.hoogit.powerhour.Fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pascalwelsch.holocircularprogressbar.HoloCircularProgressBar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ca.hoogit.powerhour.DataLayer.GameInformation;
import ca.hoogit.powerhour.R;

public class GameScreenFragment extends Fragment {

    private static final String TAG = GameScreenFragment.class.getSimpleName();
    private static final String ARG_COLOR_PRIMARY = "color_primary";
    private static final String ARG_COLOR_ACCENT = "color_accent";

    private static final int FONT_SIZE_AMBIENT = 12;
    private static final int FONT_SIZE_ACTIVE = 18;
    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("h:mm a", Locale.US);

    /**
     * Waiting components
     */
    private LinearLayout mWaitingLayout;
    private TextView mWaitingText;
    private TextView mWaitingMoreText;
    private TextView mWaitingClock;

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
    private TextView mAmbientRemainingRounds;

    private boolean mReceivedGameInfo;
    private int mPrimaryColor;
    private int mAccentColor;
    private int mTotalRounds;
    private int mTotalPauses;

    public static GameScreenFragment newInstance(int primaryColor, int accentColor) {
        GameScreenFragment fragment = new GameScreenFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLOR_PRIMARY, primaryColor);
        args.putInt(ARG_COLOR_ACCENT, accentColor);
        fragment.setArguments(args);
        return fragment;
    }

    public GameScreenFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPrimaryColor = getArguments().getInt(ARG_COLOR_PRIMARY);
            mAccentColor = getArguments().getInt(ARG_COLOR_ACCENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_screen, container, false);

        // Waiting for game components
        mWaitingLayout = (LinearLayout) view.findViewById(R.id.waiting);
        mWaitingText = (TextView) view.findViewById(R.id.waiting_text);
        mWaitingMoreText = (TextView) view.findViewById(R.id.waiting_text_more);
        mWaitingClock = (TextView) view.findViewById(R.id.waiting_clock);

        // Active components
        mActiveLayout = (RelativeLayout) view.findViewById(R.id.active);
        mRemainingRounds = (TextView) view.findViewById(R.id.rounds_remaining);
        mRemainingSeconds = (TextView) view.findViewById(R.id.progress_seconds_text);
        mProgress = (HoloCircularProgressBar) view.findViewById(R.id.progress_seconds_circle);

        // Ambient components
        mAmbientLayout = (LinearLayout) view.findViewById(R.id.ambient);
        mRoundsText = (TextView) view.findViewById(R.id.rounds_text);
        mAmbientRemainingRounds = (TextView) view.findViewById(R.id.ambient_rounds_remaining);

        return view;
    }

    public void updateInfo(GameInformation info) {
        mReceivedGameInfo = true;
        mPrimaryColor = info.getColorPrimary();
        mAccentColor = info.getColorAccent();
        mTotalRounds = info.getRounds();
        mTotalPauses = info.getPauses();

        mRemainingRounds.setText("0 of " + mTotalRounds);
    }

    public void stop() {
        mReceivedGameInfo = false;
    }

    public void updateColors() {
        mProgress.setThumbColor(mAccentColor);
        mProgress.setProgressColor(mAccentColor);
        mProgress.setProgressBackgroundColor(mPrimaryColor);
    }

    public void updateScreen(boolean isAmbient) {
        if (isAmbient) {
            if (!mReceivedGameInfo) {
                mWaitingLayout.setVisibility(View.VISIBLE);
                mWaitingText.setTextSize(FONT_SIZE_AMBIENT);
                mWaitingMoreText.setVisibility(View.GONE);
                mWaitingClock.setVisibility(View.VISIBLE);
                mWaitingClock.setText(AMBIENT_DATE_FORMAT.format(new Date()));
            } else {
                mWaitingLayout.setVisibility(View.GONE);
                mActiveLayout.setVisibility(View.GONE);
                mAmbientLayout.setVisibility(View.VISIBLE);
                mRoundsText.getPaint().setAntiAlias(false);
                mAmbientRemainingRounds.getPaint().setAntiAlias(false);
            }
        } else {
            if (!mReceivedGameInfo) {
                mWaitingLayout.setVisibility(View.VISIBLE);
                mWaitingText.setTextSize(FONT_SIZE_ACTIVE);
                mWaitingMoreText.setVisibility(View.VISIBLE);
                mWaitingClock.setVisibility(View.GONE);
            } else {
                mWaitingLayout.setVisibility(View.GONE);
                mActiveLayout.setVisibility(View.VISIBLE);
                mAmbientLayout.setVisibility(View.GONE);
                updateColors();
            }
        }
    }

}
