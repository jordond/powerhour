package ca.hoogit.powerhour_wear.Fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pascalwelsch.holocircularprogressbar.HoloCircularProgressBar;

import ca.hoogit.powerhour_wear.R;

public class GameScreenFragment extends Fragment {

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

    public GameScreenFragment() { }


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
        }
    }

}
