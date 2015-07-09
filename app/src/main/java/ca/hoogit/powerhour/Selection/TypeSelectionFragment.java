package ca.hoogit.powerhour.Selection;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ca.hoogit.powerhour.BusProvider;
import ca.hoogit.powerhour.Game.GameOptions;
import ca.hoogit.powerhour.R;

/**
 */
public class TypeSelectionFragment extends Fragment {

    public final String TAG = TypeSelectionFragment.class.getSimpleName();

    @Bind(R.id.appBar) Toolbar mToolbar;
    @Bind(R.id.type_statistics) GameTypeItem mStatistics;
    @Bind({R.id.type_power_hour, R.id.type_century_club, R.id.type_spartan, R.id.type_custom})
    List<GameTypeItem> mGameTypes;

    public static TypeSelectionFragment newInstance() {
        return new TypeSelectionFragment();
    }

    public TypeSelectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_type_selection, container, false);
        ButterKnife.bind(this, view);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        mToolbar.setTitle("Choose an Option");
        activity.setSupportActionBar(mToolbar);

        setupListeners();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public void setupListeners() {
        for (GameTypeItem item : mGameTypes) {
            final GameOptions options = item.getOptions();

            if (item.hasButton()) {
                item.setConfigureOnClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "Configuring game options for " + options.getTitle());
                        ItemSelectedEvent event = new ItemSelectedEvent(options, true);
                        BusProvider.getInstance().post(event);
                    }
                });
            }
            item.setItemOnClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, options.getTitle() + " mode was selected");
                    ItemSelectedEvent event = new ItemSelectedEvent(options);
                    BusProvider.getInstance().post(event);
                }
            });
        }
        // TODO implement
        mStatistics.setItemOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TypeSelectionFragment", "Stats was pressed");
            }
        });
    }
}