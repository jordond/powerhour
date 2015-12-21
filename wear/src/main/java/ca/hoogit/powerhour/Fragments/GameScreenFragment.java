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

import ca.hoogit.powerhour.R;

public class GameScreenFragment extends Fragment {

    private static final String TAG = GameScreenFragment.class.getSimpleName();
    private static final String ARG_COLOR_PRIMARY = "color_primary";
    private static final String ARG_COLOR_ACCENT = "color_accent";

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

    private int mPrimaryColor;
    private int mAccentColor;

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

    public void updateScreen(boolean isAmbient) {
        if (isAmbient) {
            mActiveLayout.setVisibility(View.GONE);
            mAmbientLayout.setVisibility(View.VISIBLE);
            mRoundsText.getPaint().setAntiAlias(false);
            mAmbientRemainingRounds.getPaint().setAntiAlias(false);
        } else {
            mActiveLayout.setVisibility(View.VISIBLE);
            mAmbientLayout.setVisibility(View.GONE);
            mProgress.setThumbColor(mPrimaryColor);
            mProgress.setProgressColor(mAccentColor);
            mProgress.setProgressBackgroundColor(mPrimaryColor);
        }
    }

}
