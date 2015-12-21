package ca.hoogit.powerhour.Fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.support.wearable.view.CircularButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import ca.hoogit.powerhour.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ControlsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = ControlsFragment.class.getSimpleName();
    private static final String ARG_COLOR_PRIMARY = "color_primary";
    private static final String ARG_COLOR_ACCENT = "color_accent";

    private RelativeLayout mContainer;
    private CircularButton mMute;
    private CircularButton mPlayPause;
    private CircularButton mStop;

    private int mPrimaryColor;
    private int mAccentColor;

    public static ControlsFragment newInstance(int primaryColor, int accentColor) {
        ControlsFragment fragment = new ControlsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLOR_PRIMARY, primaryColor);
        args.putInt(ARG_COLOR_ACCENT, accentColor);
        fragment.setArguments(args);
        return fragment;
    }

    public ControlsFragment() { }

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
        View view = inflater.inflate(R.layout.fragment_controls, container, false);

        mContainer = (RelativeLayout) view.findViewById(R.id.controls);
        mMute = (CircularButton) view.findViewById(R.id.mute);
        mPlayPause = (CircularButton) view.findViewById(R.id.pause);
        mStop = (CircularButton) view.findViewById(R.id.stop);

        mMute.setOnClickListener(this);
        mPlayPause.setOnClickListener(this);
        mStop.setOnClickListener(this);

        mContainer.setBackgroundColor(mPrimaryColor);
        mMute.setBackgroundColor(mPrimaryColor);
        mPlayPause.setBackgroundColor(mAccentColor);
        mStop.setBackgroundColor(mPrimaryColor);

        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        // TODO handle the icon changes
        // TODO Either handle separate state or get it from phone
        switch (id) {
            case R.id.mute:
                Log.d(TAG, "onClick: Mute Pressed");
                break;
            case R.id.pause:
                Log.d(TAG, "onClick: Pause Pressed");
                break;
            case R.id.stop:
                Log.d(TAG, "onClick: Stop pressed");
                break;
        }
    }
}
