package ca.hoogit.powerhour_wear.Fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.hoogit.powerhour_wear.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ControlsFragment extends Fragment {


    public ControlsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_controls, container, false);
    }

}
